package com.althair.create_pocketdynamo.content.solar;

import java.util.List;

import com.althair.create_pocketdynamo.config.PDConfig;
import com.althair.create_pocketdynamo.foundation.PDLang;
import com.althair.create_pocketdynamo.foundation.energy.EnergyNeighbors;
import com.althair.create_pocketdynamo.foundation.energy.PDEnergyStorage;
import com.althair.create_pocketdynamo.registry.PDBlockEntityTypes;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Output = peak x sunlight factor, only with open sky in a dimension that has skylight, reduced while it rains.
 * Sunlight comes from the sky light level minus the sky darkening, so it ramps smoothly at dawn and dusk.
 */
public class SolarPanelBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
	private static final int OUTPUT_RECHECK_TICKS = 20;

	private final PDEnergyStorage buffer = new PDEnergyStorage(
		() -> tier().bufferCapacity(),
		() -> 0, // nothing can be pushed into a panel
		() -> tier().bufferCapacity());
	private final EnergyNeighbors neighbours = new EnergyNeighbors(this);

	private int currentOutput;
	private int recheckCountdown;

	public SolarPanelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		buffer.setOnChange(this::setChanged);
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, PDBlockEntityTypes.SOLAR_PANEL.get(),
			(be, side) -> be.buffer);
	}

	/** The tier is a property of the block, so it can never disagree with the registered block. */
	public SolarTier tier() {
		return getBlockState().getBlock() instanceof SolarPanelBlock panel ? panel.getTier() : SolarTier.COPPER;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
	}

	@Override
	public void tick() {
		super.tick();
		if (level != null && level.isClientSide) {
			spawnSunlightParticles();
			return;
		}
		if (!(level instanceof ServerLevel serverLevel))
			return;

		if (recheckCountdown-- <= 0) {
			recheckCountdown = OUTPUT_RECHECK_TICKS;
			int output = computeOutput(serverLevel);
			if (output != currentOutput) {
				currentOutput = output;
				sendData();
			}
		}

		buffer.generate(currentOutput);
		pushEnergy(serverLevel);
	}

	/** Chance per client tick of a glint at the weakest producing light, and the extra gained at full output. */
	private static final float GLINT_BASE_CHANCE = 0.004f;
	private static final float GLINT_FULL_SUN_BONUS = 0.012f;

	/**
	 * Client-side glint on the panel surface while it produces: an occasional single point of light that stays put,
	 * like sun reflecting off the glass. Roughly one every 3 seconds in full sun and one every 12 or so in weak light;
	 * nothing when it produces nothing. Uses the output synced by sendData().
	 */
	private void spawnSunlightParticles() {
		if (currentOutput <= 0)
			return;
		float fraction = Mth.clamp(currentOutput / (float) Math.max(1, tier().peakFePerTick()), 0f, 1f);
		RandomSource random = level.random;
		if (random.nextFloat() > GLINT_BASE_CHANCE + GLINT_FULL_SUN_BONUS * fraction)
			return;
		double x = worldPosition.getX() + 0.1 + random.nextDouble() * 0.8; // keep off the very edges
		double z = worldPosition.getZ() + 0.1 + random.nextDouble() * 0.8;
		double y = worldPosition.getY() + 4 / 16.0 + 0.01; // on the surface of the 4px-high panel
		level.addParticle(ParticleTypes.END_ROD, x, y, z, 0, 0, 0);
	}

	private void pushEnergy(ServerLevel serverLevel) {
		if (buffer.getEnergyStored() <= 0)
			return;
		Direction[] dirs = Direction.values();
		int start = (int) (serverLevel.getGameTime() % dirs.length);
		for (int i = 0; i < dirs.length && buffer.getEnergyStored() > 0; i++) {
			IEnergyStorage target = neighbours.get(serverLevel, worldPosition, dirs[(start + i) % dirs.length]);
			if (target == null || !target.canReceive())
				continue;
			buffer.drain(target.receiveEnergy(buffer.getEnergyStored(), false));
		}
	}

	private int computeOutput(ServerLevel serverLevel) {
		if (!serverLevel.dimensionType().hasSkyLight())
			return 0;
		BlockPos above = worldPosition.above();
		if (!serverLevel.canSeeSky(above))
			return 0;
		int light = serverLevel.getBrightness(LightLayer.SKY, above) - serverLevel.getSkyDarken();
		if (light <= 0)
			return 0;
		float factor = Mth.clamp(light / 15f, 0f, 1f);
		if (serverLevel.isRainingAt(above))
			factor *= PDConfig.SERVER.solarRainMultiplier.get().floatValue();
		return Math.round(tier().peakFePerTick() * factor);
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		tag.putInt("Energy", buffer.getEnergyStored());
		tag.putInt("Output", currentOutput);
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		buffer.setEnergy(tag.getInt("Energy"));
		currentOutput = tag.getInt("Output");
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		PDLang.translate("gui.goggles.solar_panel").forGoggles(tooltip);
		PDLang.number(currentOutput)
			.space()
			.translate("generic.unit.fe_per_tick")
			.style(currentOutput > 0 ? ChatFormatting.GREEN : ChatFormatting.DARK_RED)
			.space()
			.add(PDLang.translate("gui.goggles.generating").style(ChatFormatting.DARK_GRAY))
			.forGoggles(tooltip, 1);
		return true;
	}
}
