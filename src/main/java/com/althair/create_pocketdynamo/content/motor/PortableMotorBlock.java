package com.althair.create_pocketdynamo.content.motor;

import com.althair.create_pocketdynamo.registry.PDBlockEntityTypes;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The portable motor. Shift + wrench picks it up as an item that keeps its battery charge and selected RPM
 * (see {@link PortableMotorBlockEntity} and the block's loot table).
 */
public class PortableMotorBlock extends DirectionalKineticBlock implements IBE<PortableMotorBlockEntity> {

	private final MotorTier tier;

	public PortableMotorBlock(Properties properties, MotorTier tier) {
		super(properties);
		this.tier = tier;
	}

	public MotorTier getTier() {
		return tier;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		// Same footprint as Create's own motor, so it fits the same spaces.
		return AllShapes.MOTOR_BLOCK.get(state.getValue(FACING));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction preferred = getPreferredFacing(context);
		if ((context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) || preferred == null)
			return super.getStateForPlacement(context);
		return defaultBlockState().setValue(FACING, preferred);
	}

	// IRotate

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return face == state.getValue(FACING);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(FACING).getAxis();
	}

	@Override
	public boolean hideStressImpact() {
		return true;
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		return false;
	}

	// IBE

	@Override
	public Class<PortableMotorBlockEntity> getBlockEntityClass() {
		return PortableMotorBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends PortableMotorBlockEntity> getBlockEntityType() {
		return PDBlockEntityTypes.PORTABLE_MOTOR.get();
	}
}
