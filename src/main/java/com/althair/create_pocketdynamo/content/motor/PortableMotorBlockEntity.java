package com.althair.create_pocketdynamo.content.motor;

import java.util.List;

import com.althair.create_pocketdynamo.PocketDynamo;
import com.althair.create_pocketdynamo.foundation.PDLang;
import com.althair.create_pocketdynamo.foundation.energy.PDEnergyStorage;
import com.althair.create_pocketdynamo.registry.PDBlockEntityTypes;
import com.althair.create_pocketdynamo.registry.PDDataComponents;
import com.althair.create_pocketdynamo.registry.PDSoundEvents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Battery-powered kinetic generator.
 * <p>
 * Server-authoritative: the battery drains every tick while running, in proportion to the stress its share of the
 * network is carrying. When the battery empties, {@link #getGeneratedSpeed()} drops to 0 and Create's own network
 * logic ({@link #updateGeneratedRotation()}) stops the rotation. Charge and RPM survive being picked up through
 * {@link PDDataComponents}.
 */
public class PortableMotorBlockEntity extends GeneratingKineticBlockEntity {
	public static final int DEFAULT_SPEED = 16;

	public ScrollValueBehaviour generatedSpeed;

	private final PDEnergyStorage battery = new PDEnergyStorage(
		() -> tier().batteryCapacity(),
		() -> tier().maxReceivePerTick(),
		() -> 0); // not extractable: cables must never pull energy back out of the motor

	/** True while the motor is producing rotation. Synced to clients (goggles and visuals read it). */
	private boolean running;
	private int lastSyncedEnergy = -1;
	private int lastDrainPerTick;

	public PortableMotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		battery.setOnChange(this::setChanged);
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, PDBlockEntityTypes.PORTABLE_MOTOR.get(),
			(be, side) -> be.battery);
	}

	public IEnergyStorage getBattery() {
		return battery;
	}

	/** The tier is a property of the block, so it can never disagree with the registered block. */
	public MotorTier tier() {
		return getBlockState().getBlock() instanceof PortableMotorBlock motor ? motor.getTier() : MotorTier.BRASS;
	}

	// --- Behaviours ---

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		int max = tier().maxRpm();
		generatedSpeed = new MotorSpeedBehaviour(
			PDLang.translateDirect("kinetics.portable_motor.rotation_speed"), this, new MotorValueBox());
		generatedSpeed.between(-max, max);
		generatedSpeed.value = Math.min(DEFAULT_SPEED, max);
		generatedSpeed.withCallback(i -> updateGeneratedRotation());
		behaviours.add(generatedSpeed);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (!hasSource() || getGeneratedSpeed() > getTheoreticalSpeed())
			updateGeneratedRotation();
	}

	// --- Kinetics ---

	@Override
	public float getGeneratedSpeed() {
		if (!running || !(getBlockState().getBlock() instanceof PortableMotorBlock))
			return 0;
		// Clamp again at use time: the config can be lowered after the motor was placed or its value saved.
		int max = tier().maxRpm();
		int rpm = Mth.clamp(generatedSpeed.getValue(), -max, max);
		return convertToDirection(rpm, getBlockState().getValue(PortableMotorBlock.FACING));
	}

	@Override
	public void tick() {
		super.tick();
		if (level == null || level.isClientSide)
			return;

		if (running) {
			lastDrainPerTick = computeDrainPerTick();
			battery.drain(lastDrainPerTick);
		} else {
			lastDrainPerTick = 0;
		}

		boolean shouldRun = battery.getEnergyStored() > 0;
		if (shouldRun != running) {
			running = shouldRun;
			updateGeneratedRotation();
			sendData();
			if (running)
				playStartEffects();
		}
	}

	/**
	 * Sound plus a burst of sparks when a motor that had run dry gets energy and spins up again.
	 * Server-side: the sound is broadcast to nearby players and the particles are sent to their clients.
	 */
	private void playStartEffects() {
		if (!(level instanceof ServerLevel serverLevel))
			return;
		serverLevel.playSound(null, worldPosition, PDSoundEvents.MOTOR_START.get(), SoundSource.BLOCKS, 0.8f, 1.0f);
		serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
			worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
			14, 0.3, 0.3, 0.3, 0.15);
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (level == null || level.isClientSide)
			return;
		if (battery.getEnergyStored() != lastSyncedEnergy) {
			lastSyncedEnergy = battery.getEnergyStored();
			sendData();
		}
	}

	/**
	 * FE per tick = a speed cost + a load cost.
	 * <ul>
	 * <li>Speed cost: proportional to the selected RPM, whether or not anything is connected. Spinning faster costs
	 * more even when idle.</li>
	 * <li>Load cost: proportional to the stress this motor's share of the network carries. Stress already scales
	 * with RPM in Create, so a given machine also costs more at higher speed.</li>
	 * </ul>
	 * An overstressed network does no useful work, so it only pays the speed cost.
	 */
	private int computeDrainPerTick() {
		double rpm = Math.abs(getGeneratedSpeed());
		int speedCost = Mth.ceil(rpm * tier().idleFePerRpm());
		if (isOverStressed())
			return speedCost;

		double ownCapacity = rpm * BlockStressValues.getCapacity(getStressConfigKey());
		float share = capacity <= 0 ? 1 : Mth.clamp((float) (ownCapacity / capacity), 0, 1);
		double load = stress * share;
		return speedCost + Mth.ceil(load * tier().fePerStressUnit());
	}

	// --- Persistence ---

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		tag.putInt("Energy", battery.getEnergyStored());
		tag.putBoolean("Running", running);
		tag.putInt("Drain", lastDrainPerTick);
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		battery.setEnergy(tag.getInt("Energy"));
		running = tag.getBoolean("Running");
		lastDrainPerTick = tag.getInt("Drain");
	}

	/** Item -> block: restore charge and RPM when placed. */
	@Override
	protected void applyImplicitComponents(DataComponentInput input) {
		super.applyImplicitComponents(input);
		battery.setEnergy(input.getOrDefault(PDDataComponents.STORED_ENERGY, 0));
		int max = tier().maxRpm();
		generatedSpeed.value = Mth.clamp(input.getOrDefault(PDDataComponents.TARGET_SPEED, Math.min(DEFAULT_SPEED, max)),
			-max, max);
		running = battery.getEnergyStored() > 0;
	}

	/** Block -> item: save charge and RPM (used by the loot table's copy_components on wrench pickup or break). */
	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		super.collectImplicitComponents(components);
		components.set(PDDataComponents.STORED_ENERGY, battery.getEnergyStored());
		components.set(PDDataComponents.TARGET_SPEED, generatedSpeed.getValue());
	}

	// --- Goggles ---

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		super.addToGoggleTooltip(tooltip, isPlayerSneaking);

		PDLang.translate("gui.goggles.battery").style(ChatFormatting.GRAY).forGoggles(tooltip);
		PDLang.number(battery.getEnergyStored())
			.text(" / ")
			.add(PDLang.number(battery.getMaxEnergyStored()))
			.space()
			.translate("generic.unit.fe")
			.style(ChatFormatting.GOLD)
			.forGoggles(tooltip, 1);

		if (running && lastDrainPerTick > 0) {
			PDLang.number(lastDrainPerTick)
				.space()
				.translate("generic.unit.fe_per_tick")
				.style(ChatFormatting.RED)
				.space()
				.add(PDLang.translate("gui.goggles.consumption").style(ChatFormatting.DARK_GRAY))
				.forGoggles(tooltip, 1);
		} else if (!running) {
			PDLang.translate("gui.goggles.empty").style(ChatFormatting.DARK_RED).forGoggles(tooltip, 1);
		}
		return true;
	}

	// --- Scroll value box on the motor's top ---

	static class MotorValueBox extends ValueBoxTransform.Sided {
		@Override
		protected Vec3 getSouthLocation() {
			return VecHelper.voxelSpace(8, 8, 12.5);
		}

		@Override
		public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
			Direction facing = state.getValue(PortableMotorBlock.FACING);
			return super.getLocalOffset(level, pos, state)
				.add(Vec3.atLowerCornerOf(facing.getNormal()).scale(-1 / 16f));
		}

		@Override
		public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
			super.rotate(level, pos, state, ms);
			Direction facing = state.getValue(PortableMotorBlock.FACING);
			if (facing.getAxis() == Axis.Y)
				return;
			if (getSide() != Direction.UP)
				return;
			TransformStack.of(ms).rotateZDegrees(-AngleHelper.horizontalAngle(facing) + 180);
		}

		@Override
		protected boolean isSideActive(BlockState state, Direction direction) {
			Direction facing = state.getValue(PortableMotorBlock.FACING);
			if (facing.getAxis() != Axis.Y && direction == Direction.DOWN)
				return false;
			return direction.getAxis() != facing.getAxis();
		}
	}
}
