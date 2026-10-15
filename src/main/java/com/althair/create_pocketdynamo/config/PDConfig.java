package com.althair.create_pocketdynamo.config;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.althair.create_pocketdynamo.content.motor.MotorTier;
import com.althair.create_pocketdynamo.content.solar.SolarTier;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server-side balance config. Every number that affects gameplay lives here, nothing is hardcoded in the content classes.
 * File: {@code serverconfig/create_pocketdynamo-server.toml}.
 */
public class PDConfig {
	public static final ModConfigSpec SERVER_SPEC;
	public static final Server SERVER;

	static {
		Pair<Server, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(Server::new);
		SERVER = pair.getLeft();
		SERVER_SPEC = pair.getRight();
	}

	/** Config values throw if read before the config is loaded (e.g. item tooltips in the main menu). */
	public static <T> T orDefault(Supplier<T> value, T fallback) {
		return SERVER_SPEC.isLoaded() ? value.get() : fallback;
	}

	/** One section per motor tier: {@code [portable_motor.copper]}, {@code [portable_motor.brass]}, ... */
	public static class MotorConfig {
		public final ModConfigSpec.IntValue maxRpm;
		public final ModConfigSpec.DoubleValue stressCapacityPerRpm;
		public final ModConfigSpec.IntValue batteryCapacity;
		public final ModConfigSpec.IntValue maxReceive;
		public final ModConfigSpec.DoubleValue idleFePerRpmPerTick;
		public final ModConfigSpec.DoubleValue fePerStressUnit;

		MotorConfig(ModConfigSpec.Builder builder, MotorTier tier) {
			builder.comment(tier.id() + " portable motor").push(tier.id());
			maxRpm = builder
				.comment("Maximum selectable RPM (in either direction).")
				.defineInRange("maxRpm", tier.defaultMaxRpm, 1, 256);
			stressCapacityPerRpm = builder
				.comment("Stress capacity provided per RPM, in SU. Total capacity = this x current RPM.",
					"For reference: Steam Engine = 1024, Water Wheel = 32.")
				.defineInRange("stressCapacityPerRpm", tier.defaultStressPerRpm, 0.0, 1_000_000.0);
			batteryCapacity = builder
				.comment("Internal battery size in FE.")
				.defineInRange("batteryCapacity", tier.defaultBattery, 1, Integer.MAX_VALUE);
			maxReceive = builder
				.comment("Maximum FE the motor can accept per tick.")
				.defineInRange("maxReceivePerTick", tier.defaultMaxReceive, 1, Integer.MAX_VALUE);
			idleFePerRpmPerTick = builder
				.comment("FE drained per tick for each RPM of selected speed, even with nothing connected.",
					"Example: 0.25 at 64 RPM = 16 FE/t. Faster costs more.")
				.defineInRange("idleFePerRpmPerTick", tier.defaultIdleFePerRpm, 0.0, 1000.0);
			fePerStressUnit = builder
				.comment("Extra FE drained per tick for each SU of stress the motor's share of the network is carrying.",
					"Lower means more efficient.")
				.defineInRange("fePerStressUnitPerTick", tier.defaultFePerStress, 0.0, 1000.0);
			builder.pop();
		}
	}

	/** One section per solar tier: {@code [solar_panel.copper]}, {@code [solar_panel.brass]}, ... */
	public static class SolarConfig {
		public final ModConfigSpec.IntValue peakFePerTick;
		public final ModConfigSpec.IntValue bufferCapacity;

		SolarConfig(ModConfigSpec.Builder builder, SolarTier tier) {
			builder.comment(tier.id() + " solar panel").push(tier.id());
			peakFePerTick = builder
				.comment("FE generated per tick at full daylight with open sky.")
				.defineInRange("peakFePerTick", tier.defaultPeakFePerTick, 0, 1_000_000);
			bufferCapacity = builder
				.comment("Internal buffer in FE. The panel pushes this out to adjacent cables and batteries.")
				.defineInRange("bufferCapacity", tier.defaultBuffer, 1, Integer.MAX_VALUE);
			builder.pop();
		}
	}

	public static class Server {
		public final Map<MotorTier, MotorConfig> motors = new EnumMap<>(MotorTier.class);
		public final Map<SolarTier, SolarConfig> solarPanels = new EnumMap<>(SolarTier.class);

		// --- Solar panels (shared) ---
		public final ModConfigSpec.DoubleValue solarRainMultiplier;

		// --- Energy cable ---
		public final ModConfigSpec.IntValue cableTransferPerTick;

		Server(ModConfigSpec.Builder builder) {
			builder.comment("Portable Motors, one section per tier").push("portable_motor");
			for (MotorTier tier : MotorTier.values())
				motors.put(tier, new MotorConfig(builder, tier));
			builder.pop();

			builder.comment("Solar Panels, one section per tier").push("solar_panel");
			solarRainMultiplier = builder
				.comment("Output multiplier while it rains or thunders at the panel's position (all tiers).")
				.defineInRange("rainMultiplier", 0.25, 0.0, 1.0);
			for (SolarTier tier : SolarTier.values())
				solarPanels.put(tier, new SolarConfig(builder, tier));
			builder.pop();

			builder.comment("Energy Cable").push("energy_cable");
			cableTransferPerTick = builder
				.comment("Maximum FE moved per tick through one cable segment.")
				.defineInRange("transferPerTick", 2_000, 1, Integer.MAX_VALUE);
			builder.pop();
		}
	}
}
