package com.althair.create_pocketdynamo.registry;

import com.althair.create_pocketdynamo.PocketDynamo;
import com.althair.create_pocketdynamo.content.motor.MotorTier;
import com.althair.create_pocketdynamo.content.solar.SolarTier;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PDCreativeTabs {
	private static final DeferredRegister<CreativeModeTab> REGISTER =
		DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PocketDynamo.MOD_ID);

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = REGISTER.register("main",
		() -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup." + PocketDynamo.MOD_ID + ".main"))
			.icon(() -> chargedMotor(MotorTier.BRASS))
			.displayItems((params, output) -> {
				for (MotorTier tier : MotorTier.values()) {
					output.accept(PDBlocks.PORTABLE_MOTORS.get(tier).asStack()); // empty battery
					output.accept(chargedMotor(tier)); // full battery, handy for testing and showcase
				}
				for (SolarTier tier : SolarTier.values())
					output.accept(PDBlocks.SOLAR_PANELS.get(tier).asStack());
				output.accept(PDBlocks.ENERGY_CABLE.asStack());
			})
			.build());

	private static ItemStack chargedMotor(MotorTier tier) {
		ItemStack stack = PDBlocks.PORTABLE_MOTORS.get(tier).asStack();
		stack.set(PDDataComponents.STORED_ENERGY, tier.batteryCapacity());
		return stack;
	}

	public static void register(IEventBus modEventBus) {
		REGISTER.register(modEventBus);
	}
}
