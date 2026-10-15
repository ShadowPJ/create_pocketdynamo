package com.althair.create_pocketdynamo.registry;

import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

import java.util.EnumMap;
import java.util.Map;

import com.althair.create_pocketdynamo.PocketDynamo;
import com.althair.create_pocketdynamo.content.cable.EnergyCableBlock;
import com.althair.create_pocketdynamo.content.motor.MotorTier;
import com.althair.create_pocketdynamo.content.motor.PortableMotorBlock;
import com.althair.create_pocketdynamo.content.motor.PortableMotorGenerator;
import com.althair.create_pocketdynamo.content.motor.PortableMotorItem;
import com.althair.create_pocketdynamo.content.solar.SolarPanelBlock;
import com.althair.create_pocketdynamo.content.solar.SolarPanelItem;
import com.althair.create_pocketdynamo.content.solar.SolarTier;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.api.stress.BlockStressValues.GeneratedRpm;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.minecraft.core.Direction;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;

public class PDBlocks {
	private static final CreateRegistrate REGISTRATE = PocketDynamo.registrate();

	/** One motor block per tier; all share the same block entity type. */
	public static final Map<MotorTier, BlockEntry<PortableMotorBlock>> PORTABLE_MOTORS = new EnumMap<>(MotorTier.class);

	static {
		PORTABLE_MOTORS.put(MotorTier.COPPER, motor(MotorTier.COPPER, MapColor.COLOR_ORANGE,
			(ctx, prov) -> ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ctx.get())
				.pattern("CTC")
				.pattern("RKR")
				.pattern(" S ")
				.define('C', AllItems.COPPER_SHEET.get())
				.define('T', AllItems.ELECTRON_TUBE.get())
				.define('R', Items.REDSTONE_BLOCK)
				.define('K', AllBlocks.COPPER_CASING.get())
				.define('S', AllBlocks.SHAFT.get())
				.unlockedBy("has_electron_tube", RegistrateRecipeProvider.has(AllItems.ELECTRON_TUBE.get()))
				.save(prov)));

		PORTABLE_MOTORS.put(MotorTier.BRASS, motor(MotorTier.BRASS, MapColor.COLOR_YELLOW,
			(ctx, prov) -> ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ctx.get())
				.pattern("BTB")
				.pattern("RKR")
				.pattern("GSG")
				.define('B', AllItems.BRASS_SHEET.get())
				.define('T', AllItems.ELECTRON_TUBE.get())
				.define('R', Items.REDSTONE_BLOCK)
				.define('K', AllBlocks.BRASS_CASING.get())
				.define('S', AllBlocks.SHAFT.get())
				.define('G', Items.GOLD_INGOT)
				.unlockedBy("has_brass_sheet", RegistrateRecipeProvider.has(AllItems.BRASS_SHEET.get()))
				.save(prov)));

		PORTABLE_MOTORS.put(MotorTier.DIAMOND, motor(MotorTier.DIAMOND, MapColor.DIAMOND,
			(ctx, prov) -> ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ctx.get())
				.pattern("DPD")
				.pattern("RKR")
				.pattern("DSD")
				.define('D', Items.DIAMOND)
				.define('P', AllItems.PRECISION_MECHANISM.get())
				.define('R', Items.REDSTONE_BLOCK)
				.define('K', AllBlocks.BRASS_CASING.get())
				.define('S', AllBlocks.SHAFT.get())
				.unlockedBy("has_precision_mechanism", RegistrateRecipeProvider.has(AllItems.PRECISION_MECHANISM.get()))
				.save(prov)));
	}

	/** One solar panel block per tier; all share the same block entity type. */
	public static final Map<SolarTier, BlockEntry<SolarPanelBlock>> SOLAR_PANELS = new EnumMap<>(SolarTier.class);

	static {
		SOLAR_PANELS.put(SolarTier.COPPER, solarPanel(SolarTier.COPPER, MapColor.COLOR_ORANGE,
			(ctx, prov) -> ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ctx.get())
				.pattern("GGG")
				.pattern("LLL")
				.pattern("CCC")
				.define('G', Items.GLASS)
				.define('L', Items.LAPIS_LAZULI)
				.define('C', AllItems.COPPER_SHEET.get())
				.unlockedBy("has_copper_sheet", RegistrateRecipeProvider.has(AllItems.COPPER_SHEET.get()))
				.save(prov)));

		SOLAR_PANELS.put(SolarTier.BRASS, solarPanel(SolarTier.BRASS, MapColor.COLOR_YELLOW,
			(ctx, prov) -> ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ctx.get())
				.pattern("GGG")
				.pattern("LTL")
				.pattern("BBB")
				.define('G', Items.GLASS)
				.define('L', Items.LAPIS_LAZULI)
				.define('T', AllItems.ELECTRON_TUBE.get())
				.define('B', AllItems.BRASS_SHEET.get())
				.unlockedBy("has_brass_sheet", RegistrateRecipeProvider.has(AllItems.BRASS_SHEET.get()))
				.save(prov)));

		SOLAR_PANELS.put(SolarTier.DIAMOND, solarPanel(SolarTier.DIAMOND, MapColor.DIAMOND,
			(ctx, prov) -> ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ctx.get())
				.pattern("GGG")
				.pattern("DPD")
				.pattern("BBB")
				.define('G', Items.GLASS)
				.define('D', Items.DIAMOND)
				.define('P', AllItems.PRECISION_MECHANISM.get())
				.define('B', AllItems.BRASS_SHEET.get())
				.unlockedBy("has_precision_mechanism", RegistrateRecipeProvider.has(AllItems.PRECISION_MECHANISM.get()))
				.save(prov)));
	}

	public static final BlockEntry<EnergyCableBlock> ENERGY_CABLE =
		REGISTRATE.block("energy_cable", EnergyCableBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.mapColor(MapColor.COLOR_ORANGE).noOcclusion())
			.transform(pickaxeOnly())
			.blockstate((ctx, prov) -> {
				MultiPartBlockStateBuilder builder = prov.getMultipartBuilder(ctx.get());
				builder.part()
					.modelFile(prov.models().getExistingFile(PocketDynamo.asResource("block/energy_cable/core")))
					.addModel()
					.end();
				for (Direction dir : Direction.values())
					builder.part()
						.modelFile(prov.models().getExistingFile(
							PocketDynamo.asResource("block/energy_cable/arm_" + dir.getName())))
						.addModel()
						.condition(PipeBlock.PROPERTY_BY_DIRECTION.get(dir), true)
						.end();
			})
			.recipe((ctx, prov) -> ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ctx.get(), 6)
				.pattern("CCC")
				.pattern("RRR")
				.define('C', AllItems.COPPER_SHEET.get())
				.define('R', Items.REDSTONE)
				.unlockedBy("has_copper_sheet", RegistrateRecipeProvider.has(AllItems.COPPER_SHEET.get()))
				.save(prov))
			.item()
			.model((ctx, prov) -> prov.withExistingParent(ctx.getName(),
				PocketDynamo.asResource("block/energy_cable/inventory")))
			.build()
			.register();

	/**
	 * Registers one tier of portable motor: {@code <tier>_portable_motor}.
	 * <p>
	 * Addons cannot use Create's CStress (it asserts the block belongs to Create), so stress values come from this
	 * addon's own server config through {@link BlockStressValues}.
	 */
	private static BlockEntry<PortableMotorBlock> motor(MotorTier tier, MapColor color,
		NonNullBiConsumer<DataGenContext<Block, PortableMotorBlock>, RegistrateRecipeProvider> recipe) {
		return REGISTRATE.block(tier.id() + "_portable_motor", p -> new PortableMotorBlock(p, tier))
			.initialProperties(SharedProperties::copperMetal)
			.properties(p -> p.mapColor(color).forceSolidOn())
			.transform(pickaxeOnly())
			.blockstate(new PortableMotorGenerator(tier)::generate)
			.onRegister(block -> {
				BlockStressValues.CAPACITIES.register(block, tier::stressCapacityPerRpm);
				BlockStressValues.RPM.register(block, new GeneratedRpm(tier.defaultMaxRpm, true));
			})
			.loot((lt, block) -> lt.add(block, LootTable.lootTable()
				.withPool(LootPool.lootPool()
					.when(ExplosionCondition.survivesExplosion())
					.setRolls(ConstantValue.exactly(1))
					.add(LootItem.lootTableItem(block)
						.apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
							.include(PDDataComponents.STORED_ENERGY)
							.include(PDDataComponents.TARGET_SPEED))))))
			.recipe(recipe)
			.item(PortableMotorItem::new)
			.model((ctx, prov) -> prov.withExistingParent(ctx.getName(),
				PocketDynamo.asResource("block/portable_motor/" + tier.id())))
			.build()
			.register();
	}

	/** Registers one tier of solar panel: {@code <tier>_solar_panel}. */
	private static BlockEntry<SolarPanelBlock> solarPanel(SolarTier tier, MapColor color,
		NonNullBiConsumer<DataGenContext<Block, SolarPanelBlock>, RegistrateRecipeProvider> recipe) {
		return REGISTRATE.block(tier.id() + "_solar_panel", p -> new SolarPanelBlock(p, tier))
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.mapColor(color).noOcclusion())
			.transform(pickaxeOnly())
			.blockstate((ctx, prov) -> prov.simpleBlock(ctx.get(),
				prov.models().getExistingFile(PocketDynamo.asResource("block/solar_panel/" + tier.id()))))
			.recipe(recipe)
			.item(SolarPanelItem::new)
			.model((ctx, prov) -> prov.withExistingParent(ctx.getName(),
				PocketDynamo.asResource("block/solar_panel/" + tier.id())))
			.build()
			.register();
	}

	/** Touch this class during mod construction so the static registrations run. */
	public static void register() {
	}
}
