package com.althair.create_pocketdynamo.registry;

import com.althair.create_pocketdynamo.PocketDynamo;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Sound events. Each id is mapped to actual audio in {@code assets/create_pocketdynamo/sounds.json}. The audio is
 * taken from vanilla Minecraft on purpose, by reference: the jar ships no audio file, so nothing of Mojang's is
 * redistributed. To use custom audio instead, drop .ogg files under
 * {@code assets/create_pocketdynamo/sounds/} and point the entry in sounds.json at them. No code changes needed.
 */
public class PDSoundEvents {
	private static final DeferredRegister<SoundEvent> REGISTER =
		DeferredRegister.create(Registries.SOUND_EVENT, PocketDynamo.MOD_ID);

	/** A motor that was out of charge gets energy and starts spinning again. */
	public static final DeferredHolder<SoundEvent, SoundEvent> MOTOR_START = register("motor.start");

	private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
		return REGISTER.register(name, () -> SoundEvent.createVariableRangeEvent(PocketDynamo.asResource(name)));
	}

	public static void register(IEventBus modEventBus) {
		REGISTER.register(modEventBus);
	}

	private PDSoundEvents() {
	}
}
