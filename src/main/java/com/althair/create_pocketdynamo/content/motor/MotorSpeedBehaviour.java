package com.althair.create_pocketdynamo.content.motor;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.content.kinetics.motor.KineticScrollValueBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Create's {@link KineticScrollValueBehaviour} hardcodes a 256 maximum on its value board, so a motor limited to a
 * lower RPM would let the player drag the slider past the real limit and then silently clamp it. This variant sizes
 * the board from the behaviour's own maximum ({@code between(min, max)}), i.e. the motor tier's RPM limit.
 */
public class MotorSpeedBehaviour extends KineticScrollValueBehaviour {

	public MotorSpeedBehaviour(Component label, SmartBlockEntity be, ValueBoxTransform slot) {
		super(label, be, slot);
	}

	@Override
	public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
		ImmutableList<Component> rows = ImmutableList.of(
			Component.literal("⟳").withStyle(ChatFormatting.BOLD),
			Component.literal("⟲").withStyle(ChatFormatting.BOLD));
		ValueSettingsFormatter formatter = new ValueSettingsFormatter(this::formatSettings);
		int milestone = Math.max(1, max / 8);
		return new ValueSettingsBoard(label, max, milestone, rows, formatter);
	}
}
