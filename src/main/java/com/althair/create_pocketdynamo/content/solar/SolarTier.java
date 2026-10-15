package com.althair.create_pocketdynamo.content.solar;

import java.util.Locale;

import com.althair.create_pocketdynamo.config.PDConfig;

/**
 * Solar panel tiers. Constants carry the default balance; live values come from
 * {@link PDConfig.Server#solarPanels}, with these as fallback before the config has loaded.
 * <p>
 * For scale: a brass motor at 64 RPM under full load drains roughly 115 FE/t, a copper one about 60 FE/t.
 */
public enum SolarTier {
	//     peak FE/t at full sun, buffer FE
	COPPER(20, 2_000),
	BRASS(60, 6_000),
	DIAMOND(180, 18_000);

	public final int defaultPeakFePerTick;
	public final int defaultBuffer;

	SolarTier(int peakFePerTick, int buffer) {
		this.defaultPeakFePerTick = peakFePerTick;
		this.defaultBuffer = buffer;
	}

	public String id() {
		return name().toLowerCase(Locale.ROOT);
	}

	public int peakFePerTick() {
		return PDConfig.orDefault(() -> PDConfig.SERVER.solarPanels.get(this).peakFePerTick.get(),
			defaultPeakFePerTick);
	}

	public int bufferCapacity() {
		return PDConfig.orDefault(() -> PDConfig.SERVER.solarPanels.get(this).bufferCapacity.get(), defaultBuffer);
	}
}
