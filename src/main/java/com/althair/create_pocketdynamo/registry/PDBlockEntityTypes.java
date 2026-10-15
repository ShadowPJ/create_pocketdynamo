package com.althair.create_pocketdynamo.registry;

import com.althair.create_pocketdynamo.PocketDynamo;
import com.althair.create_pocketdynamo.content.cable.EnergyCableBlockEntity;
import com.althair.create_pocketdynamo.content.motor.MotorTier;
import com.althair.create_pocketdynamo.content.motor.PortableMotorBlockEntity;
import com.althair.create_pocketdynamo.content.solar.SolarPanelBlockEntity;
import com.althair.create_pocketdynamo.content.solar.SolarTier;
import com.althair.create_pocketdynamo.content.motor.PortableMotorRenderer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

public class PDBlockEntityTypes {
	private static final CreateRegistrate REGISTRATE = PocketDynamo.registrate();

	public static final BlockEntityEntry<PortableMotorBlockEntity> PORTABLE_MOTOR = REGISTRATE
		.blockEntity("portable_motor", PortableMotorBlockEntity::new)
		.visual(() -> OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF), false)
		.validBlocks(
			PDBlocks.PORTABLE_MOTORS.get(MotorTier.COPPER),
			PDBlocks.PORTABLE_MOTORS.get(MotorTier.BRASS),
			PDBlocks.PORTABLE_MOTORS.get(MotorTier.DIAMOND))
		.renderer(() -> PortableMotorRenderer::new)
		.register();

	public static final BlockEntityEntry<SolarPanelBlockEntity> SOLAR_PANEL = REGISTRATE
		.blockEntity("solar_panel", SolarPanelBlockEntity::new)
		.validBlocks(
			PDBlocks.SOLAR_PANELS.get(SolarTier.COPPER),
			PDBlocks.SOLAR_PANELS.get(SolarTier.BRASS),
			PDBlocks.SOLAR_PANELS.get(SolarTier.DIAMOND))
		.register();

	public static final BlockEntityEntry<EnergyCableBlockEntity> ENERGY_CABLE = REGISTRATE
		.blockEntity("energy_cable", EnergyCableBlockEntity::new)
		.validBlocks(PDBlocks.ENERGY_CABLE)
		.register();

	public static void register() {
	}
}
