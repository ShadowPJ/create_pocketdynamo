package com.althair.create_pocketdynamo.content.motor;

import java.util.Locale;

import com.althair.create_pocketdynamo.config.PDConfig;

/**
 * Motor tiers. The constants carry the <b>default</b> balance; the live values come from the server config
 * ({@link PDConfig.Server#motors}), with these defaults as fallback before the config has loaded.
 * <p>
 * Reference points for stress: Water Wheel = 32 SU/RPM (8 RPM), Steam Engine = 1024 SU/RPM (64 RPM).
 * Total capacity = SU per RPM x current RPM.
 */
public enum MotorTier {
	//     max RPM, SU per RPM, battery FE, max receive FE/t, speed cost FE/t per RPM, load cost FE/t per SU
	//     Speed cost at max RPM: copper 9.6, brass 16, diamond ~51 FE/t, paid even with nothing connected.
	COPPER(32, 512.0, 200_000, 1_000, 0.30, 0.0030),
	BRASS(64, 768.0, 400_000, 2_000, 0.25, 0.0020),
	DIAMOND(256, 1024.0, 2_000_000, 8_000, 0.20, 0.0012);

	public final int defaultMaxRpm;
	public final double defaultStressPerRpm;
	public final int defaultBattery;
	public final int defaultMaxReceive;
	public final double defaultIdleFePerRpm;
	public final double defaultFePerStress;

	MotorTier(int maxRpm, double stressPerRpm, int battery, int maxReceive, double idleFePerRpm,
		double fePerStress) {
		this.defaultMaxRpm = maxRpm;
		this.defaultStressPerRpm = stressPerRpm;
		this.defaultBattery = battery;
		this.defaultMaxReceive = maxReceive;
		this.defaultIdleFePerRpm = idleFePerRpm;
		this.defaultFePerStress = fePerStress;
	}

	/** Lower-case id, used in registry names and config section names. */
	public String id() {
		return name().toLowerCase(Locale.ROOT);
	}

	public int maxRpm() {
		return PDConfig.orDefault(() -> PDConfig.SERVER.motors.get(this).maxRpm.get(), defaultMaxRpm);
	}

	public double stressCapacityPerRpm() {
		return PDConfig.orDefault(() -> PDConfig.SERVER.motors.get(this).stressCapacityPerRpm.get(),
			defaultStressPerRpm);
	}

	public int batteryCapacity() {
		return PDConfig.orDefault(() -> PDConfig.SERVER.motors.get(this).batteryCapacity.get(), defaultBattery);
	}

	public int maxReceivePerTick() {
		return PDConfig.orDefault(() -> PDConfig.SERVER.motors.get(this).maxReceive.get(), defaultMaxReceive);
	}

	/** FE per tick drained for each RPM of selected speed, regardless of load. */
	public double idleFePerRpm() {
		return PDConfig.orDefault(() -> PDConfig.SERVER.motors.get(this).idleFePerRpmPerTick.get(),
			defaultIdleFePerRpm);
	}

	public double fePerStressUnit() {
		return PDConfig.orDefault(() -> PDConfig.SERVER.motors.get(this).fePerStressUnit.get(), defaultFePerStress);
	}
}
