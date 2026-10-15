package com.althair.create_pocketdynamo.content.solar;

import java.util.List;

import com.althair.create_pocketdynamo.foundation.PDLang;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

/** The solar panel as an item. Its tooltip shows the tier's maximum output, so tiers can be compared at a glance. */
public class SolarPanelItem extends BlockItem {

	public SolarPanelItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		if (!(getBlock() instanceof SolarPanelBlock panel))
			return;

		tooltip.add(PDLang.translate("tooltip.max_output")
			.style(ChatFormatting.GRAY)
			.space()
			.add(PDLang.number(panel.getTier().peakFePerTick())
				.space()
				.translate("generic.unit.fe_per_tick")
				.style(ChatFormatting.GREEN))
			.component());
	}
}
