package com.althair.create_pocketdynamo.content.solar;

import com.althair.create_pocketdynamo.registry.PDBlockEntityTypes;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A flat solar panel. Generates FE from sunlight and pushes it into adjacent cables, motors and batteries. */
public class SolarPanelBlock extends Block implements IBE<SolarPanelBlockEntity> {
	private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 4, 16);

	private final SolarTier tier;

	public SolarPanelBlock(Properties properties, SolarTier tier) {
		super(properties);
		this.tier = tier;
	}

	public SolarTier getTier() {
		return tier;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public Class<SolarPanelBlockEntity> getBlockEntityClass() {
		return SolarPanelBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends SolarPanelBlockEntity> getBlockEntityType() {
		return PDBlockEntityTypes.SOLAR_PANEL.get();
	}
}
