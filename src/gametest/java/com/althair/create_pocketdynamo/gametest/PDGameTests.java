package com.althair.create_pocketdynamo.gametest;

import com.althair.create_pocketdynamo.content.cable.EnergyCableBlockEntity;
import com.althair.create_pocketdynamo.content.motor.MotorTier;
import com.althair.create_pocketdynamo.content.motor.PortableMotorBlock;
import com.althair.create_pocketdynamo.content.motor.PortableMotorBlockEntity;
import com.althair.create_pocketdynamo.content.solar.SolarTier;
import com.althair.create_pocketdynamo.registry.PDBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Shared setup for the GameTests. Every position is relative to the test: y=0 is the layer under the arena, y=1 is
 * its smooth stone floor and the tests build from y=2 upwards (see tools/GameTestStructureGen.java).
 */
final class PDGameTests {
	/** Template of the arena: a smooth stone floor at y=0 and open air above it. */
	static final String ARENA = "arena";

	private PDGameTests() {
	}

	static PortableMotorBlockEntity placeMotor(GameTestHelper helper, BlockPos pos, MotorTier tier) {
		helper.setBlock(pos, PDBlocks.PORTABLE_MOTORS.get(tier).get().defaultBlockState()
			.setValue(PortableMotorBlock.FACING, Direction.NORTH));
		return helper.getBlockEntity(pos);
	}

	static void placeSolarPanel(GameTestHelper helper, BlockPos pos, SolarTier tier) {
		helper.setBlock(pos, PDBlocks.SOLAR_PANELS.get(tier).get().defaultBlockState());
	}

	static EnergyCableBlockEntity placeCable(GameTestHelper helper, BlockPos pos) {
		helper.setBlock(pos, PDBlocks.ENERGY_CABLE.get().defaultBlockState());
		return helper.getBlockEntity(pos);
	}

	/** The block's FE storage as another mod's cable would see it (any side: ours do not care). */
	static IEnergyStorage energyAt(GameTestHelper helper, BlockPos pos) {
		IEnergyStorage storage = helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK,
			helper.absolutePos(pos), null);
		if (storage == null)
			helper.fail("No energy capability", pos);
		return storage;
	}
}
