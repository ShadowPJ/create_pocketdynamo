package com.althair.create_pocketdynamo.gametest;

import static com.althair.create_pocketdynamo.gametest.PDGameTests.ARENA;
import static com.althair.create_pocketdynamo.gametest.PDGameTests.energyAt;
import static com.althair.create_pocketdynamo.gametest.PDGameTests.placeCable;
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

/** How the Energy Cable hands energy to consumers and to other cables. */
@GameTestHolder(PocketDynamo.MOD_ID)
@PrefixGameTestTemplate(false)
public class EnergyCableTests {
	private static final BlockPos LEFT = new BlockPos(1, 2, 2);
	private static final BlockPos MIDDLE = new BlockPos(2, 2, 2);
	private static final BlockPos RIGHT = new BlockPos(3, 2, 2);

	/** A cable's own storage cannot be pulled from outside, so another mod's cable cannot drain it backwards. */
	@GameTest(template = ARENA)
	public static void cableStorageCannotBeExtracted(GameTestHelper helper) {
		placeCable(helper, MIDDLE);
		IEnergyStorage cable = energyAt(helper, MIDDLE);
		helper.assertTrue(cable.canReceive(), "a cable must accept energy");
		helper.assertValueEqual(cable.receiveEnergy(500, false), 500, "energy accepted");
		helper.assertFalse(cable.canExtract(), "a cable must not allow extraction");
		helper.assertValueEqual(cable.extractEnergy(500, false), 0, "energy extracted");
		helper.assertValueEqual(cable.getEnergyStored(), 500, "stored energy");
		helper.succeed();
	}

	/** Energy placed at one end of a line of cables spreads out downhill and none of it is lost on the way. */
	@GameTest(template = ARENA, timeoutTicks = 200)
	public static void cablesLevelOutWithoutLosingEnergy(GameTestHelper helper) {
		placeCable(helper, LEFT);
		placeCable(helper, MIDDLE);
		placeCable(helper, RIGHT);
		IEnergyStorage left = energyAt(helper, LEFT);
		IEnergyStorage middle = energyAt(helper, MIDDLE);
		IEnergyStorage right = energyAt(helper, RIGHT);

		int injected = left.receiveEnergy(2_000, false);
		helper.assertTrue(injected > 0, "the first cable accepted no energy");

		helper.onEachTick(() -> helper.assertValueEqual(
			left.getEnergyStored() + middle.getEnergyStored() + right.getEnergyStored(), injected,
			"total energy in the line"));
		helper.succeedWhen(() -> {
			helper.assertTrue(right.getEnergyStored() > 0, "the far end has received energy");
			helper.assertTrue(Math.abs(left.getEnergyStored() - middle.getEnergyStored()) <= 1,
				"left and middle are level: " + left.getEnergyStored() + " / " + middle.getEnergyStored());
			helper.assertTrue(Math.abs(middle.getEnergyStored() - right.getEnergyStored()) <= 1,
				"middle and right are level: " + middle.getEnergyStored() + " / " + right.getEnergyStored());
		});
	}

	/** Two cables holding the same amount do not push energy back and forth. */
	@GameTest(template = ARENA)
	public static void levelCablesStayPut(GameTestHelper helper) {
		placeCable(helper, LEFT);
		placeCable(helper, MIDDLE);
		IEnergyStorage left = energyAt(helper, LEFT);
		IEnergyStorage middle = energyAt(helper, MIDDLE);
		left.receiveEnergy(500, false);
		middle.receiveEnergy(500, false);

		helper.startSequence()
			.thenIdle(20)
			.thenExecute(() -> {
				helper.assertValueEqual(left.getEnergyStored(), 500, "left cable");
				helper.assertValueEqual(middle.getEnergyStored(), 500, "middle cable");
			})
			.thenSucceed();
	}

	/** A cable delivers what it holds to a consumer next to it. */
	@GameTest(template = ARENA, timeoutTicks = 100)
	public static void cableFeedsAConsumer(GameTestHelper helper) {
		PortableMotorBlockEntity motor = idleMotor(helper, LEFT);
		placeCable(helper, MIDDLE);
		IEnergyStorage cable = energyAt(helper, MIDDLE);
		cable.receiveEnergy(1_500, false);

		helper.succeedWhen(() -> {
			helper.assertValueEqual(cable.getEnergyStored(), 0, "energy left in the cable");
			helper.assertValueEqual(motor.getBattery().getEnergyStored(), 1_500, "energy in the motor");
		});
	}

	/** A cable never pulls energy out of a neighbour: only producers push into it. */
	@GameTest(template = ARENA)
	public static void cableDoesNotPullFromNeighbours(GameTestHelper helper) {
		PortableMotorBlockEntity motor = idleMotor(helper, LEFT);
		placeCable(helper, MIDDLE);
		IEnergyStorage cable = energyAt(helper, MIDDLE);
		motor.getBattery().receiveEnergy(1_000, false);

		helper.startSequence()
			.thenIdle(20)
			.thenExecute(() -> {
				helper.assertValueEqual(cable.getEnergyStored(), 0, "energy in the cable");
				helper.assertValueEqual(motor.getBattery().getEnergyStored(), 1_000, "energy in the motor");
			})
			.thenSucceed();
	}

	/**
	 * A cable between two consumers keeps feeding both: it alternates who it serves first, so neither starves
	 * while the supply is short, and it hands over exactly what it was given.
	 */
	@GameTest(template = ARENA, timeoutTicks = 100)
	public static void cableSharesBetweenConsumers(GameTestHelper helper) {
		PortableMotorBlockEntity west = idleMotor(helper, LEFT);
		PortableMotorBlockEntity east = idleMotor(helper, RIGHT);
		placeCable(helper, MIDDLE);
		IEnergyStorage cable = energyAt(helper, MIDDLE);

		int[] injected = { 0 };
		helper.onEachTick(() -> injected[0] += cable.receiveEnergy(1_000 - cable.getEnergyStored(), false));

		helper.startSequence()
			.thenIdle(30)
			.thenExecute(() -> {
				int inWest = west.getBattery().getEnergyStored();
				int inEast = east.getBattery().getEnergyStored();
				helper.assertTrue(inWest > 0, "the west consumer got no energy");
				helper.assertTrue(inEast > 0, "the east consumer got no energy");
				helper.assertValueEqual(inWest + inEast + cable.getEnergyStored(), injected[0],
					"energy delivered plus energy still in the cable");
			})
			.thenSucceed();
	}

	/** A motor set to 0 RPM draws nothing, so what it holds is exactly what the cable delivered. */
	private static PortableMotorBlockEntity idleMotor(GameTestHelper helper, BlockPos pos) {
		PortableMotorBlockEntity motor = placeMotor(helper, pos, MotorTier.BRASS);
		motor.generatedSpeed.setValue(0);
		return motor;
	}
}
