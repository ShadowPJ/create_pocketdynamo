package com.althair.create_pocketdynamo.content.motor;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.althair.create_pocketdynamo.foundation.PDLang;
import com.althair.create_pocketdynamo.registry.PDDataComponents;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

/**
 * The motor as an item. Shows the stored battery charge as a durability-style bar and lists charge and selected RPM
 * in the tooltip, so a carried motor can be judged without placing it.
 * <p>
 * The charge lives in {@link PDDataComponents#STORED_ENERGY}, written by the block entity when the motor is picked up.
 */
public class PortableMotorItem extends BlockItem {

	public PortableMotorItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Nullable
	private MotorTier tier() {
		return getBlock() instanceof PortableMotorBlock motor ? motor.getTier() : null;
	}

	private static int storedEnergy(ItemStack stack) {
		return stack.getOrDefault(PDDataComponents.STORED_ENERGY, 0);
	}

	private float chargeFraction(ItemStack stack) {
		MotorTier tier = tier();
		if (tier == null)
			return 0;
		return Mth.clamp(storedEnergy(stack) / (float) tier.batteryCapacity(), 0f, 1f);
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return storedEnergy(stack) > 0;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return Math.round(13 * chargeFraction(stack));
	}

	@Override
	public int getBarColor(ItemStack stack) {
		// Red when nearly empty through green when full, like a durability bar.
		return Mth.hsvToRgb(chargeFraction(stack) / 3f, 1f, 1f);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		MotorTier tier = tier();
		if (tier == null)
			return;

		int stored = storedEnergy(stack);
		int capacity = tier.batteryCapacity();
		tooltip.add(PDLang.translate("tooltip.battery")
			.style(ChatFormatting.GRAY)
			.space()
			.add(PDLang.number(stored)
				.text(" / ")
				.add(PDLang.number(capacity))
				.space()
				.translate("generic.unit.fe")
				.style(stored > 0 ? ChatFormatting.GOLD : ChatFormatting.DARK_RED))
			.component());

		if (stack.has(PDDataComponents.TARGET_SPEED)) {
			int rpm = stack.getOrDefault(PDDataComponents.TARGET_SPEED, 0);
			tooltip.add(PDLang.translate("tooltip.speed")
				.style(ChatFormatting.GRAY)
				.space()
				.add(PDLang.number(Math.abs(rpm))
					.text(rpm < 0 ? " ⟳" : " ⟲")
					.space()
					.translate("generic.unit.rpm")
					.style(ChatFormatting.AQUA))
				.component());
		}

		tooltip.add(PDLang.translate("tooltip.max_speed", tier.maxRpm())
			.style(ChatFormatting.DARK_GRAY)
			.component());
	}
}
