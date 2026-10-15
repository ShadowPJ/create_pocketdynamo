package com.althair.create_pocketdynamo.gametest;

import static com.althair.create_pocketdynamo.gametest.PDGameTests.ARENA;
import static com.althair.create_pocketdynamo.gametest.PDGameTests.placeMotor;

import com.althair.create_pocketdynamo.PocketDynamo;
import com.althair.create_pocketdynamo.content.motor.MotorTier;
import com.althair.create_pocketdynamo.content.motor.PortableMotorBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Charging and discharging the portable motor's battery. */
@GameTestHolder(PocketDynamo.MOD_ID)
@PrefixGameTestTemplate(false)
public class MotorBatteryTests {
	private static final BlockPos MOTOR = new BlockPos(1, 2, 1);
	private static final BlockPos OTHER_MOTOR = new BlockPos(5, 2, 1);

	/** Charging works in steps of the tier's receive limit, never past capacity, and cannot be undone from outside. */
	@GameTest(template = ARENA)
	public static void chargesUpToItsLimits(GameTestHelper helper) {
		MotorTier tier = MotorTier.BRASS;
		IEnergyStorage battery = placeMotor(helper, MOTOR, tier).getBattery();
		helper.assertValueEqual(battery.getMaxEnergyStored(), tier.batteryCapacity(), "battery capacity");
		helper.assertTrue(battery.canReceive(), "the motor must accept energy");
		helper.assertFalse(battery.canExtract(), "cables must not be able to pull energy out of the motor");

		// A simulated charge reports what would be accepted without changing anything.
		int perCall = tier.maxReceivePerTick();
		helper.assertValueEqual(battery.receiveEnergy(Integer.MAX_VALUE, true), perCall, "simulated charge");
		helper.assertValueEqual(battery.getEnergyStored(), 0, "stored after a simulated charge");

		helper.assertValueEqual(battery.receiveEnergy(Integer.MAX_VALUE, false), perCall, "charge limited per call");
		helper.assertValueEqual(battery.getEnergyStored(), perCall, "stored after one charge");
		helper.assertValueEqual(battery.extractEnergy(100, false), 0, "extraction");
		helper.assertValueEqual(battery.getEnergyStored(), perCall, "stored after a refused extraction");

		// Fill it up: it stops exactly at capacity and then accepts nothing more.
		int guard = 0;
		while (battery.receiveEnergy(perCall, false) > 0 && guard++ < 100_000)
			;
		helper.assertValueEqual(battery.getEnergyStored(), tier.batteryCapacity(), "stored when full");
		helper.assertValueEqual(battery.receiveEnergy(perCall, false), 0, "charge of a full battery");
		helper.succeed();
	}

	/** Spins while there is charge, then drains to exactly zero and stops (it never goes negative). */
	@GameTest(template = ARENA, timeoutTicks = 100)
	public static void drainsAndStopsWhenEmpty(GameTestHelper helper) {
		PortableMotorBlockEntity motor = placeMotor(helper, MOTOR, MotorTier.BRASS);
		IEnergyStorage battery = motor.getBattery();
		helper.assertValueEqual(motor.getGeneratedSpeed(), 0f, "speed of an empty motor");

		int charge = 20;
		helper.assertValueEqual(battery.receiveEnergy(charge, false), charge, "charge accepted");

		int[] previous = { charge };
		helper.onEachTick(() -> {
			int stored = battery.getEnergyStored();
			helper.assertTrue(stored >= 0 && stored <= previous[0], "the battery only drains: " + stored);
			previous[0] = stored;
		});

		helper.startSequence()
			.thenWaitUntil(() -> helper.assertTrue(motor.getGeneratedSpeed() != 0, "the motor starts with energy"))
			.thenWaitUntil(() -> {
				helper.assertValueEqual(battery.getEnergyStored(), 0, "stored when drained");
				helper.assertValueEqual(motor.getGeneratedSpeed(), 0f, "speed of a drained motor");
			})
			.thenIdle(5)
			.thenExecute(() -> helper.assertValueEqual(battery.getEnergyStored(), 0, "stored after staying empty"))
			.thenSucceed();
	}

	/** An empty motor that receives energy starts again by itself. */
	@GameTest(template = ARENA, timeoutTicks = 100)
	public static void restartsWhenRecharged(GameTestHelper helper) {
		PortableMotorBlockEntity motor = placeMotor(helper, MOTOR, MotorTier.BRASS);
		helper.startSequence()
			.thenIdle(3)
			.thenExecute(() -> {
				helper.assertValueEqual(motor.getGeneratedSpeed(), 0f, "speed before charging");
				motor.getBattery().receiveEnergy(500, false);
			})
			.thenWaitUntil(() -> helper.assertTrue(motor.getGeneratedSpeed() != 0, "the motor starts when charged"))
			.thenSucceed();
	}

	/** A faster setting costs more energy, with nothing connected. */
	@GameTest(template = ARENA, timeoutTicks = 100)
	public static void drainGrowsWithSelectedRpm(GameTestHelper helper) {
		PortableMotorBlockEntity slow = placeMotor(helper, MOTOR, MotorTier.BRASS);
		PortableMotorBlockEntity fast = placeMotor(helper, OTHER_MOTOR, MotorTier.BRASS);
		slow.generatedSpeed.setValue(16);
		fast.generatedSpeed.setValue(48);
		int charge = 1_000;
		slow.getBattery().receiveEnergy(charge, false);
		fast.getBattery().receiveEnergy(charge, false);

		helper.startSequence()
			.thenIdle(10)
			.thenExecute(() -> {
				int slowLoss = charge - slow.getBattery().getEnergyStored();
				int fastLoss = charge - fast.getBattery().getEnergyStored();
				helper.assertTrue(slowLoss > 0, "the slow motor drains (lost " + slowLoss + ")");
				helper.assertTrue(fastLoss > slowLoss,
					"48 RPM drains more than 16 RPM (lost " + fastLoss + " vs " + slowLoss + ")");
			})
			.thenSucceed();
	}
}
