package com.althair.create_pocketdynamo.content.cable;

import java.util.List;

import com.althair.create_pocketdynamo.config.PDConfig;
import com.althair.create_pocketdynamo.foundation.energy.EnergyNeighbors;
import com.althair.create_pocketdynamo.foundation.energy.PDEnergyStorage;
import com.althair.create_pocketdynamo.registry.PDBlockEntityTypes;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Each tick, a cable does two things with the energy in its buffer:
 * <ol>
 * <li>pushes it into adjacent non-cable blocks that accept FE (consumers first, round-robin so none starves);</li>
 * <li>levels it out with adjacent cables: it only sends to a neighbour holding less, and only half the difference,
 * so energy flows downhill without bouncing back and forth.</li>
 * </ol>
 * Cables never pull from other blocks: producers (the solar panel) push into the cable themselves.
 * The cable's own storage cannot be extracted, so another mod's cable cannot drain it backwards.
 */
public class EnergyCableBlockEntity extends SmartBlockEntity {
	private final PDEnergyStorage buffer = new PDEnergyStorage(
		() -> 2 * transferPerTick(),
		this::transferPerTick,
		() -> 0);
	private final EnergyNeighbors neighbours = new EnergyNeighbors(this);

	public EnergyCableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		buffer.setOnChange(this::setChanged);
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, PDBlockEntityTypes.ENERGY_CABLE.get(),
			(be, side) -> be.buffer);
	}

	private int transferPerTick() {
		return PDConfig.orDefault(() -> PDConfig.SERVER.cableTransferPerTick.get(), 2_000);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
	}

	@Override
	public void tick() {
		super.tick();
		if (!(level instanceof ServerLevel serverLevel) || buffer.getEnergyStored() <= 0)
			return;

		Direction[] dirs = Direction.values();
		int start = (int) (serverLevel.getGameTime() % dirs.length);
		int budget = Math.min(buffer.getEnergyStored(), transferPerTick());

		// 1. Consumers.
		for (int i = 0; i < dirs.length && budget > 0; i++) {
			Direction dir = dirs[(start + i) % dirs.length];
			if (isCable(serverLevel, dir))
				continue;
			IEnergyStorage target = neighbours.get(serverLevel, worldPosition, dir);
			if (target == null || !target.canReceive())
				continue;
			int accepted = target.receiveEnergy(budget, false);
			budget -= buffer.drain(accepted);
		}

		// 2. Level out with other cables, downhill only.
		for (int i = 0; i < dirs.length && budget > 0; i++) {
			Direction dir = dirs[(start + i) % dirs.length];
			if (!isCable(serverLevel, dir))
				continue;
			IEnergyStorage target = neighbours.get(serverLevel, worldPosition, dir);
			if (target == null)
				continue;
			int difference = buffer.getEnergyStored() - target.getEnergyStored();
			if (difference <= 1)
				continue;
			int accepted = target.receiveEnergy(Math.min(budget, difference / 2), false);
			budget -= buffer.drain(accepted);
		}
	}

	private boolean isCable(ServerLevel level, Direction dir) {
		return level.getBlockState(worldPosition.relative(dir)).getBlock() instanceof EnergyCableBlock;
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		tag.putInt("Energy", buffer.getEnergyStored());
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		buffer.setEnergy(tag.getInt("Energy"));
	}
}
