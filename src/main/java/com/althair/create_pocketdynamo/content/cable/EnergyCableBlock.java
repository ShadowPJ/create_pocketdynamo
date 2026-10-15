package com.althair.create_pocketdynamo.content.cable;

import com.althair.create_pocketdynamo.registry.PDBlockEntityTypes;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;

/**
 * Simple FE cable. Connects on its own to any neighbouring cable or block exposing an FE capability.
 * Energy moves by local equalisation (see {@link EnergyCableBlockEntity}); there is no global network graph,
 * so nothing has to be rebuilt on chunk load or unload.
 */
public class EnergyCableBlock extends Block implements IBE<EnergyCableBlockEntity> {
	private static final VoxelShape CORE = Block.box(5, 5, 5, 11, 11, 11);
	/** Index = Direction#ordinal(). */
	private static final VoxelShape[] ARMS = {
		Block.box(5, 0, 5, 11, 5, 11), // DOWN
		Block.box(5, 11, 5, 11, 16, 11), // UP
		Block.box(5, 5, 0, 11, 11, 5), // NORTH
		Block.box(5, 5, 11, 11, 11, 16), // SOUTH
		Block.box(0, 5, 5, 5, 11, 11), // WEST
		Block.box(11, 5, 5, 16, 11, 11) // EAST
	};
	/** Index = bitmask of connected directions (bit = Direction#ordinal()). */
	private static final VoxelShape[] SHAPES = new VoxelShape[64];

	static {
		for (int mask = 0; mask < SHAPES.length; mask++) {
			VoxelShape shape = CORE;
			for (Direction dir : Direction.values())
				if ((mask & (1 << dir.ordinal())) != 0)
					shape = Shapes.or(shape, ARMS[dir.ordinal()]);
			SHAPES[mask] = shape.optimize();
		}
	}

	public EnergyCableBlock(Properties properties) {
		super(properties);
		BlockState state = stateDefinition.any();
		for (BooleanProperty property : PipeBlock.PROPERTY_BY_DIRECTION.values())
			state = state.setValue(property, false);
		registerDefaultState(state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		PipeBlock.PROPERTY_BY_DIRECTION.values().forEach(builder::add);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return withConnections(defaultBlockState(), context.getLevel(), context.getClickedPos());
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
		boolean isMoving) {
		super.neighborChanged(state, level, pos, block, fromPos, isMoving);
		if (level.isClientSide)
			return;
		BlockState updated = withConnections(state, level, pos);
		if (updated != state)
			level.setBlock(pos, updated, Block.UPDATE_ALL);
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, level, pos, oldState, isMoving);
		if (level.isClientSide)
			return;
		BlockState updated = withConnections(state, level, pos);
		if (updated != state)
			level.setBlock(pos, updated, Block.UPDATE_ALL);
	}

	private BlockState withConnections(BlockState state, Level level, BlockPos pos) {
		for (Direction dir : Direction.values())
			state = state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(dir), connectsTo(level, pos, dir));
		return state;
	}

	private boolean connectsTo(Level level, BlockPos pos, Direction dir) {
		BlockPos neighbour = pos.relative(dir);
		if (level.getBlockState(neighbour).getBlock() instanceof EnergyCableBlock)
			return true;
		return level.getCapability(Capabilities.EnergyStorage.BLOCK, neighbour, dir.getOpposite()) != null;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		int mask = 0;
		for (Direction dir : Direction.values())
			if (state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(dir)))
				mask |= 1 << dir.ordinal();
		return SHAPES[mask];
	}

	@Override
	public Class<EnergyCableBlockEntity> getBlockEntityClass() {
		return EnergyCableBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends EnergyCableBlockEntity> getBlockEntityType() {
		return PDBlockEntityTypes.ENERGY_CABLE.get();
	}
}
