package com.althair.create_pocketdynamo.ponder;

import java.util.ArrayList;
import java.util.List;

import com.althair.create_pocketdynamo.PocketDynamo;
import com.althair.create_pocketdynamo.content.motor.MotorTier;
import com.althair.create_pocketdynamo.registry.PDBlocks;
import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import com.tterrag.registrate.util.entry.RegistryEntry;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

/** Registers this addon's Ponder tag and scenes with Ponder. Client-only; added from the client setup event. */
public class PDPonderPlugin implements PonderPlugin {
	public static final ResourceLocation POCKET_POWER = PocketDynamo.asResource("pocket_power");

	@Override
	public String getModId() {
		return PocketDynamo.MOD_ID;
	}

	private static List<RegistryEntry<?, ?>> motors() {
		return new ArrayList<>(PDBlocks.PORTABLE_MOTORS.values());
	}

	private static List<RegistryEntry<?, ?>> panels() {
		return new ArrayList<>(PDBlocks.SOLAR_PANELS.values());
	}

	@Override
	public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
		helper.registerTag(POCKET_POWER)
			.addToIndex()
			.item(PDBlocks.PORTABLE_MOTORS.get(MotorTier.BRASS).get(), true, false)
			.title("Pocket Dynamo")
			.description("Portable, battery-powered motors, solar panels and cables")
			.register();

		PonderTagRegistrationHelper<RegistryEntry<?, ?>> entries = helper.withKeyFunction(RegistryEntry::getId);

		for (RegistryEntry<?, ?> motor : motors()) {
			entries.addToTag(POCKET_POWER).add(motor);
			entries.addToTag(AllCreatePonderTags.KINETIC_SOURCES).add(motor);
		}
		for (RegistryEntry<?, ?> panel : panels())
			entries.addToTag(POCKET_POWER).add(panel);
		entries.addToTag(POCKET_POWER).add(PDBlocks.ENERGY_CABLE);
	}

	@Override
	public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
		PonderSceneRegistrationHelper<RegistryEntry<?, ?>> entries = helper.withKeyFunction(RegistryEntry::getId);

		entries.forComponents(motors())
			.addStoryBoard("portable_motor/basics", PDPonderScenes::motorBasics, POCKET_POWER,
				AllCreatePonderTags.KINETIC_SOURCES);

		// The charging scene explains the whole energy chain, so every part of it links here.
		List<RegistryEntry<?, ?>> chain = new ArrayList<>(motors());
		chain.addAll(panels());
		entries.forComponents(chain)
			.addStoryBoard("portable_motor/charging", PDPonderScenes::motorCharging, POCKET_POWER);

		List<RegistryEntry<?, ?>> solar = new ArrayList<>(panels());
		solar.add(PDBlocks.ENERGY_CABLE);
		entries.forComponents(solar)
			.addStoryBoard("solar_panel/sunlight", PDPonderScenes::solarSunlight, POCKET_POWER);
	}
}
