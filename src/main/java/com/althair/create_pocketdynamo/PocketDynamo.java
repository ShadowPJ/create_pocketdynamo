package com.althair.create_pocketdynamo;

import org.slf4j.Logger;

import com.althair.create_pocketdynamo.config.PDConfig;
import com.althair.create_pocketdynamo.content.cable.EnergyCableBlockEntity;
import com.althair.create_pocketdynamo.content.motor.PortableMotorBlockEntity;
import com.althair.create_pocketdynamo.content.solar.SolarPanelBlockEntity;
import com.althair.create_pocketdynamo.data.PDLangEntries;
import com.althair.create_pocketdynamo.registry.PDBlockEntityTypes;
import com.althair.create_pocketdynamo.registry.PDBlocks;
import com.althair.create_pocketdynamo.registry.PDCreativeTabs;
import com.althair.create_pocketdynamo.registry.PDDataComponents;
import com.althair.create_pocketdynamo.registry.PDSoundEvents;
import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(PocketDynamo.MOD_ID)
public class PocketDynamo {
	public static final String MOD_ID = "create_pocketdynamo";
	public static final String NAME = "Create: Pocket Dynamo";
	public static final Logger LOGGER = LogUtils.getLogger();

	/**
	 * One registrate per addon; Create documents its own instance as off-limits for other mods.
	 * <p>
	 * Registrate's default creative tab is {@code CreativeModeTabs.SEARCH}: it adds every registered item there on
	 * its own. Our own tab ({@link PDCreativeTabs}) lists the same items (plus a charged motor variant), and the
	 * search tab aggregates all tabs, so leaving the default would add items twice and crash with "already exists in
	 * the tab's list". Create disables it the same way.
	 */
	private static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID)
		.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);

	public PocketDynamo(IEventBus modEventBus, ModContainer modContainer) {
		REGISTRATE.registerEventListeners(modEventBus);

		PDDataComponents.register(modEventBus);
		PDCreativeTabs.register(modEventBus);
		PDSoundEvents.register(modEventBus);
		PDBlocks.register();
		PDBlockEntityTypes.register();
		PDLangEntries.register();

		modEventBus.addListener(PortableMotorBlockEntity::registerCapabilities);
		modEventBus.addListener(SolarPanelBlockEntity::registerCapabilities);
		modEventBus.addListener(EnergyCableBlockEntity::registerCapabilities);
		modContainer.registerConfig(ModConfig.Type.SERVER, PDConfig.SERVER_SPEC);

		LOGGER.info("{} initializing", NAME);
	}

	public static ResourceLocation asResource(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	public static CreateRegistrate registrate() {
		return REGISTRATE;
	}
}
