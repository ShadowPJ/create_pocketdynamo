package com.althair.create_pocketdynamo.foundation.energy;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Lazily-created, invalidation-aware capability caches for the six neighbours of a block entity.
 * Avoids a full capability lookup per side per tick.
 */
public class EnergyNeighbors {
	private final BlockEntity owner;
	private final BlockCapabilityCache<IEnergyStorage, Direction>[] caches;

	@SuppressWarnings("unchecked")
	public EnergyNeighbors(BlockEntity owner) {
		this.owner = owner;
		this.caches = new BlockCapabilityCache[Direction.values().length];
	}

	/** The neighbour's energy storage as seen from {@code side} of the owner, or null if it has none. */
	@Nullable
	public IEnergyStorage get(ServerLevel level, BlockPos pos, Direction side) {
		BlockCapabilityCache<IEnergyStorage, Direction> cache = caches[side.ordinal()];
		if (cache == null) {
			cache = BlockCapabilityCache.create(Capabilities.EnergyStorage.BLOCK, level, pos.relative(side),
				side.getOpposite(), () -> !owner.isRemoved(), () -> {
				});
			caches[side.ordinal()] = cache;
		}
		return cache.getCapability();
	}
}
