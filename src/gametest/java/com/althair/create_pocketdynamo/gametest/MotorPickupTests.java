package com.althair.create_pocketdynamo.gametest;

import static com.althair.create_pocketdynamo.gametest.PDGameTests.ARENA;
import static com.althair.create_pocketdynamo.gametest.PDGameTests.placeMotor;

import com.althair.create_pocketdynamo.PocketDynamo;
import com.althair.create_pocketdynamo.content.motor.MotorTier;
import com.althair.create_pocketdynamo.content.motor.PortableMotorBlockEntity;
import com.althair.create_pocketdynamo.foundation.energy.PDEnergyStorage;
import com.althair.create_pocketdynamo.registry.PDBlocks;
import com.althair.create_pocketdynamo.registry.PDDataComponents;
import com.simibubi.create.AllItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Picking the motor up with a wrench (sneaking, like a player does) and placing it again: charge and selected RPM
 * must survive the round trip. These run in a single tick, so the motor never gets to drain in between.
 */
@GameTestHolder(PocketDynamo.MOD_ID)
@PrefixGameTestTemplate(false)
public class MotorPickupTests {
	private static final BlockPos MOTOR = new BlockPos(2, 2, 2);

	@GameTest(template = ARENA)
	public static void keepsChargeAndRpmWhenPickedUpWithWrench(GameTestHelper helper) {
		for (MotorTier tier : MotorTier.values())
			roundTrip(helper, tier, tier.batteryCapacity() / 2 + 7, -(tier.maxRpm() / 2));
		helper.succeed();
	}

	@GameTest(template = ARENA)
	public static void emptyMotorKeepsItsRpmToo(GameTestHelper helper) {
		roundTrip(helper, MotorTier.COPPER, 0, 24);
		helper.succeed();
	}

	/** The picked-up item has no charge bar when empty and shows one when charged. */
	@GameTest(template = ARENA)
	public static void itemShowsItsCharge(GameTestHelper helper) {
		Player player = survivalPlayer(helper);
		for (int charge : new int[] { 0, 1_000 }) {
			ItemStack stack = pickUp(helper, player, MotorTier.COPPER, charge, 16);
			helper.assertValueEqual(stack.isBarVisible(), charge > 0, "charge bar with " + charge + " FE");
			clearInventory(player);
		}
		helper.succeed();
	}

	private static void roundTrip(GameTestHelper helper, MotorTier tier, int charge, int rpm) {
		Player player = survivalPlayer(helper);
		ItemStack stack = pickUp(helper, player, tier, charge, rpm);

		helper.assertValueEqual(stack.get(PDDataComponents.STORED_ENERGY), charge, tier + " item charge");
		helper.assertValueEqual(stack.get(PDDataComponents.TARGET_SPEED), rpm, tier + " item RPM");

		// Place it again, on the floor, like a player would.
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
		BlockPos floor = helper.absolutePos(MOTOR.below());
		BlockHitResult onFloor = new BlockHitResult(Vec3.atCenterOf(floor).add(0, 0.5, 0), Direction.UP, floor, false);
		InteractionResult placed = stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, onFloor));
		helper.assertTrue(placed.consumesAction(), tier + " motor was not placed again: " + placed);
		helper.assertTrue(helper.getBlockState(MOTOR).is(PDBlocks.PORTABLE_MOTORS.get(tier).get()),
			tier + " motor block after placing, found " + helper.getBlockState(MOTOR));

		PortableMotorBlockEntity motor = helper.getBlockEntity(MOTOR);
		helper.assertValueEqual(motor.getBattery().getEnergyStored(), charge, tier + " charge after placing");
		helper.assertValueEqual(motor.generatedSpeed.getValue(), rpm, tier + " RPM after placing");

		helper.setBlock(MOTOR, Blocks.AIR);
	}

	/** Places a motor with the given charge and RPM, then picks it up with a sneaking wrench use. */
	private static ItemStack pickUp(GameTestHelper helper, Player player, MotorTier tier, int charge, int rpm) {
		PortableMotorBlockEntity motor = placeMotor(helper, MOTOR, tier);
		((PDEnergyStorage) motor.getBattery()).setEnergy(charge);
		motor.generatedSpeed.setValue(rpm);

		ItemStack wrench = AllItems.WRENCH.asStack();
		player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
		player.setShiftKeyDown(true);
		BlockPos abs = helper.absolutePos(MOTOR);
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);
		InteractionResult result = wrench.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
		helper.assertTrue(result.consumesAction(), "the wrench did not pick the motor up: " + result);
		helper.assertTrue(helper.getBlockState(MOTOR).isAir(), "the motor block is still there");

		ItemStack picked = player.getInventory().items.stream()
			.filter(s -> s.is(PDBlocks.PORTABLE_MOTORS.get(tier).get().asItem()))
			.findFirst()
			.orElseGet(() -> {
				helper.fail("The " + tier + " motor did not end up in the inventory");
				return ItemStack.EMPTY;
			});
		player.getInventory().removeItem(picked);
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		return picked;
	}

	private static Player survivalPlayer(GameTestHelper helper) {
		return helper.makeMockPlayer(GameType.SURVIVAL);
	}

	private static void clearInventory(Player player) {
		player.getInventory().clearContent();
	}
}
