package com.althair.create_pocketdynamo.content.motor;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Non-instanced fallback renderer. Must stay consistent with the Flywheel visual registered in
 * {@code PDBlockEntityTypes} (both draw Create's half shaft on the output side).
 */
public class PortableMotorRenderer extends KineticBlockEntityRenderer<PortableMotorBlockEntity> {

	public PortableMotorRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(PortableMotorBlockEntity be, BlockState state) {
		return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state);
	}
}
