package com.althair.create_pocketdynamo.compat;

import java.util.ArrayList;
import java.util.List;

import com.althair.create_pocketdynamo.PocketDynamo;
import com.althair.create_pocketdynamo.config.PDConfig;
import com.althair.create_pocketdynamo.content.motor.MotorTier;
import com.althair.create_pocketdynamo.content.solar.SolarTier;
import com.althair.create_pocketdynamo.registry.PDBlocks;

import net.createmod.catnip.lang.LangNumberFormat;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

/**
 * Description pages shown by recipe viewers (JEI and EMI). Viewer-agnostic: this class has no viewer imports, so
 * both plugins share the same texts and numbers. Numbers come from the live config, so a pack that rebalances the
 * addon sees its own values.
 */
public class PDRecipeInfo {

	/** One description page: the item it belongs to and its paragraphs. */
	public record Page(ItemLike item, String id, List<Component> text) {
	}

	public static List<Page> pages() {
		List<Page> pages = new ArrayList<>();

		for (MotorTier tier : MotorTier.values()) {
			var motor = PDBlocks.PORTABLE_MOTORS.get(tier);
			pages.add(new Page(motor.get(), motor.getId().getPath(), List.of(
				translate("recipe_info.motor.description"),
				translate("recipe_info.motor.stats",
					num(tier.maxRpm()),
					num(tier.stressCapacityPerRpm() * tier.maxRpm()),
					num(tier.batteryCapacity())),
				translate("recipe_info.motor.drain", num(tier.idleFePerRpm())))));
		}

		for (SolarTier tier : SolarTier.values()) {
			var panel = PDBlocks.SOLAR_PANELS.get(tier);
			pages.add(new Page(panel.get(), panel.getId().getPath(), List.of(
				translate("recipe_info.solar_panel.description"),
				translate("recipe_info.solar_panel.stats", num(tier.peakFePerTick()), num(tier.bufferCapacity())))));
		}

		pages.add(new Page(PDBlocks.ENERGY_CABLE.get(), PDBlocks.ENERGY_CABLE.getId().getPath(), List.of(
			translate("recipe_info.energy_cable.description"),
			translate("recipe_info.energy_cable.stats",
				num(PDConfig.orDefault(() -> PDConfig.SERVER.cableTransferPerTick.get(), 2_000))))));

		return pages;
	}

	private static String num(double value) {
		return LangNumberFormat.format(value);
	}

	private static Component translate(String key, Object... args) {
		return Component.translatable(PocketDynamo.MOD_ID + "." + key, args);
	}

	private PDRecipeInfo() {
	}
}
