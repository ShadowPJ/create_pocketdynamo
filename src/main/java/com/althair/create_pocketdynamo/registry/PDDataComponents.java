package com.althair.create_pocketdynamo.registry;

import java.util.function.UnaryOperator;

import com.althair.create_pocketdynamo.PocketDynamo;
import com.mojang.serialization.Codec;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Item data components that let the portable motor keep its state while it is a dropped or carried item.
 * 1.21 uses data components, not NBT, for item state.
 */
public class PDDataComponents {
	private static final DeferredRegister.DataComponents DATA_COMPONENTS =
		DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, PocketDynamo.MOD_ID);

	/** Stored battery charge, in FE. */
	public static final DataComponentType<Integer> STORED_ENERGY = register("stored_energy",
		builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT));

	/** Selected RPM; the sign is the rotation direction. */
	public static final DataComponentType<Integer> TARGET_SPEED = register("target_speed",
		builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

	private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
		DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
		DATA_COMPONENTS.register(name, () -> type);
		return type;
	}

	public static void register(IEventBus modEventBus) {
		DATA_COMPONENTS.register(modEventBus);
	}
}
