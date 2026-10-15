package com.althair.create_pocketdynamo.gametest;

import static com.althair.create_pocketdynamo.gametest.PDGameTests.ARENA;
import static com.althair.create_pocketdynamo.gametest.PDGameTests.energyAt;
import static com.althair.create_pocketdynamo.gametest.PDGameTests.placeSolarPanel;

import java.util.EnumMap;
import java.util.Map;

import com.althair.create_pocketdynamo.PocketDynamo;
import com.althair.create_pocketdynamo.config.PDConfig;
import com.althair.create_pocketdynamo.content.solar.SolarTier;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Solar output by day, at night and in the rain. Time and weather belong to the whole level, so each condition is its
 * own batch: the batches run one after another and every test inside one sees the same sky.
 */
@GameTestHolder(PocketDynamo.MOD_ID)
@PrefixGameTestTemplate(false)
public class SolarPanelTests {
	private static final String DAY = "pd_solar_day";
	private static final String NIGHT = "pd_solar_night";
	private static final String RAIN = "pd_solar_rain";

	private static final int NOON = 6_000;
	private static final int MIDNIGHT = 18_000;

	/** Height of the barrier ceiling the test harness puts over the arena (relative to the test). */
	private static final int BARRIER_CEILING_Y = 7;

	private static final BlockPos COPPER = new BlockPos(1, 2, 1);
	private static final BlockPos BRASS = new BlockPos(3, 2, 1);
	private static final BlockPos DIAMOND = new BlockPos(5, 2, 1);

	// --- Sky conditions ---

	@BeforeBatch(batch = DAY)
	public static void setNoon(ServerLevel level) {
		setSky(level, NOON, false);
	}

	@BeforeBatch(batch = NIGHT)
	public static void setMidnight(ServerLevel level) {
		setSky(level, MIDNIGHT, false);
	}

	@BeforeBatch(batch = RAIN)
	public static void setRainAtNoon(ServerLevel level) {
		setSky(level, NOON, true);
	}

	@AfterBatch(batch = DAY)
	public static void afterDay(ServerLevel level) {
		setSky(level, NOON, false);
	}

	@AfterBatch(batch = NIGHT)
	public static void afterNight(ServerLevel level) {
		setSky(level, NOON, false);
	}

	@AfterBatch(batch = RAIN)
	public static void afterRain(ServerLevel level) {
		setSky(level, NOON, false);
	}

	private static void setSky(ServerLevel level, int dayTime, boolean raining) {
		level.setDayTime(dayTime);
		level.setWeatherParameters(raining ? 0 : 6_000, raining ? 6_000 : 0, raining, false);
		level.setRainLevel(raining ? 1f : 0f);
		level.setThunderLevel(0f);
	}

	// --- Tests ---

	/** At noon every tier makes exactly its peak output. */
	@GameTest(template = ARENA, batch = DAY, timeoutTicks = 100)
	public static void producesPeakOutputAtNoon(GameTestHelper helper) {
		Map<SolarTier, Integer> output = new EnumMap<>(SolarTier.class);
		measureAllTiers(helper, output,
			() -> helper.assertTrue(helper.getLevel().getSkyDarken() == 0, "waiting for full daylight"),
			() -> {
				for (SolarTier tier : SolarTier.values())
					helper.assertValueEqual(output.get(tier), tier.peakFePerTick(), tier + " output at noon");
			});
	}

	/** A block over the panel hides the sky: it makes nothing, even at noon. */
	@GameTest(template = ARENA, batch = DAY, timeoutTicks = 100)
	public static void producesNothingUnderACover(GameTestHelper helper) {
		helper.setBlock(COPPER.above(2), Blocks.STONE);

		helper.startSequence()
			.thenIdle(5) // let the light engine see the cover before the panel measures the sky
			.thenExecute(() -> placeSolarPanel(helper, COPPER, SolarTier.COPPER))
			.thenIdle(10)
			.thenExecute(() -> helper.assertValueEqual(energyAt(helper, COPPER).getEnergyStored(), 0,
				"energy of a covered panel"))
			.thenSucceed();
	}

	/** At midnight the panels still run, but far below their peak. */
	@GameTest(template = ARENA, batch = NIGHT, timeoutTicks = 100)
	public static void producesLittleAtNight(GameTestHelper helper) {
		Map<SolarTier, Integer> output = new EnumMap<>(SolarTier.class);
		measureAllTiers(helper, output,
			() -> helper.assertTrue(helper.getLevel().getSkyDarken() > 0, "waiting for the sky to darken"),
			() -> {
				for (SolarTier tier : SolarTier.values())
					helper.assertTrue(output.get(tier) < tier.peakFePerTick() / 2.0,
						tier + " makes " + output.get(tier) + " FE/t at night, peak is " + tier.peakFePerTick());
			});
	}

	/** In the rain a panel makes at most the rain share of its peak, and still something (unless configured to 0). */
	@GameTest(template = ARENA, batch = RAIN, timeoutTicks = 100)
	public static void producesLessInTheRain(GameTestHelper helper) {
		double rainShare = PDConfig.orDefault(() -> PDConfig.SERVER.solarRainMultiplier.get(), 0.25);
		Map<SolarTier, Integer> output = new EnumMap<>(SolarTier.class);
		measureAllTiers(helper, output,
			() -> helper.assertTrue(helper.getLevel().isRainingAt(helper.absolutePos(COPPER.above())),
				"waiting for the rain"),
			() -> {
				for (SolarTier tier : SolarTier.values()) {
					int out = output.get(tier);
					helper.assertTrue(out <= Math.ceil(tier.peakFePerTick() * rainShare),
						tier + " makes " + out + " FE/t in the rain, peak is " + tier.peakFePerTick()
							+ " and the rain share " + rainShare);
					helper.assertTrue(out > 0 || rainShare == 0, tier + " makes nothing in the rain");
				}
			});
	}

	/**
	 * Waits for the sky to reach the condition, places one panel of each tier, and measures what each one generates
	 * in a single tick (the buffer is empty at the start and nothing is connected to drain it).
	 */
	private static void measureAllTiers(GameTestHelper helper, Map<SolarTier, Integer> output, Runnable skyReady,
		Runnable check) {
		BlockPos[] spots = { COPPER, BRASS, DIAMOND };
		SolarTier[] tiers = { SolarTier.COPPER, SolarTier.BRASS, SolarTier.DIAMOND };
		int[] before = new int[tiers.length];

		// The test harness walls the arena in with barriers, rain included: take them away over the panels.
		for (BlockPos spot : spots)
			helper.setBlock(new BlockPos(spot.getX(), BARRIER_CEILING_Y, spot.getZ()), Blocks.AIR);

		GameTestSequence sequence = helper.startSequence()
			.thenWaitUntil(skyReady)
			.thenExecute(() -> {
				for (int i = 0; i < tiers.length; i++)
					placeSolarPanel(helper, spots[i], tiers[i]);
			})
			.thenIdle(3) // the panels compute their output on their first tick
			.thenExecute(() -> {
				for (int i = 0; i < tiers.length; i++)
					before[i] = energyAt(helper, spots[i]).getEnergyStored();
			})
			.thenIdle(1)
			.thenExecute(() -> {
				for (int i = 0; i < tiers.length; i++)
					output.put(tiers[i], energyAt(helper, spots[i]).getEnergyStored() - before[i]);
				check.run();
			});
		sequence.thenSucceed();
	}
}
