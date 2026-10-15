package com.althair.create_pocketdynamo.ponder;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.althair.create_pocketdynamo.PocketDynamo;
import com.althair.create_pocketdynamo.content.motor.MotorTier;
import com.althair.create_pocketdynamo.content.solar.SolarTier;
import com.althair.create_pocketdynamo.registry.PDBlocks;
import com.simibubi.create.AllBlocks;

import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.registration.PonderSceneRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/** Datagen helper: validates the scene schematics, then compiles the scenes into the addon's lang file. */
public class PDPonderLang {

	public static void provide(BiConsumer<String, String> consumer) {
		verifySchematics();
		// FMLClientSetupEvent does not run during datagen, so the plugin has to be added by hand here.
		PonderIndex.addPlugin(new PDPonderPlugin());
		PonderIndex.getLangAccess().provideLang(PocketDynamo.MOD_ID, consumer);
	}

	/**
	 * Scene compilation during datagen runs without a level and never opens the schematics, so a broken or outdated
	 * .nbt would only show up in-game as an empty scene. Load each one with Ponder's own reader and check that the
	 * blocks the storyboard relies on are really there (unknown block names load as air).
	 */
	private static void verifySchematics() {
		Block motor = PDBlocks.PORTABLE_MOTORS.get(MotorTier.BRASS).get();
		Block shaft = AllBlocks.SHAFT.get();
		Block copper = PDBlocks.SOLAR_PANELS.get(SolarTier.COPPER).get();
		Block brass = PDBlocks.SOLAR_PANELS.get(SolarTier.BRASS).get();
		Block diamond = PDBlocks.SOLAR_PANELS.get(SolarTier.DIAMOND).get();
		Block cable = PDBlocks.ENERGY_CABLE.get();

		Map<String, Map<Block, Integer>> expected = new LinkedHashMap<>();
		expected.put("portable_motor/basics", Map.of(motor, 1, shaft, 2));
		expected.put("portable_motor/charging", Map.of(motor, 1, brass, 1, shaft, 2));
		expected.put("solar_panel/sunlight",
			Map.of(motor, 1, shaft, 2, copper, 1, brass, 1, diamond, 1, cable, 3));

		expected.forEach((path, blocks) -> {
			String resource = "/assets/" + PocketDynamo.MOD_ID + "/ponder/" + path + ".nbt";
			try (InputStream in = PDPonderLang.class.getResourceAsStream(resource)) {
				if (in == null)
					throw new IllegalStateException("Ponder schematic missing: " + resource
						+ " (run: java tools/PonderStructureGen.java src/main/resources/assets/" + PocketDynamo.MOD_ID
						+ "/ponder)");
				StructureTemplate template = PonderSceneRegistry.loadSchematic(in);
				blocks.forEach((block, count) -> {
					int found = template.filterBlocks(BlockPos.ZERO, new StructurePlaceSettings(), block).size();
					if (found < count)
						throw new IllegalStateException("Ponder schematic " + path + ": expected at least " + count
							+ " of " + block + " but found " + found);
				});
				PocketDynamo.LOGGER.info("Ponder schematic {} OK (size {})", path, template.getSize());
			} catch (IOException e) {
				throw new IllegalStateException("Could not read Ponder schematic " + resource, e);
			}
		});
	}

	private PDPonderLang() {
	}
}
