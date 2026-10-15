package com.althair.create_pocketdynamo.content.motor;

import com.althair.create_pocketdynamo.PocketDynamo;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.generators.ModelFile;

/**
 * Blockstate generator. Model JSONs are hand-written, one pair per tier:
 * {@code block/portable_motor/<tier>.json} (shaft SOUTH) and {@code <tier>_vertical.json} (shaft UP).
 * Tier models only override the body texture of the shared base models.
 */
public class PortableMotorGenerator extends SpecialBlockStateGen {
	private final MotorTier tier;

	public PortableMotorGenerator(MotorTier tier) {
		this.tier = tier;
	}

	@Override
	protected int getXRotation(BlockState state) {
		return state.getValue(PortableMotorBlock.FACING) == Direction.DOWN ? 180 : 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		Direction facing = state.getValue(PortableMotorBlock.FACING);
		return facing.getAxis().isVertical() ? 0 : horizontalAngle(facing);
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		String suffix = state.getValue(PortableMotorBlock.FACING).getAxis().isVertical() ? "_vertical" : "";
		return prov.models().getExistingFile(PocketDynamo.asResource("block/portable_motor/" + tier.id() + suffix));
	}
}
