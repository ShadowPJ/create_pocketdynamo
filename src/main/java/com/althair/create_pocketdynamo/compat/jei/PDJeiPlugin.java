package com.althair.create_pocketdynamo.compat.jei;

import com.althair.create_pocketdynamo.PocketDynamo;
import com.althair.create_pocketdynamo.compat.PDRecipeInfo;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** JEI integration: one "Information" page per item. Only loaded when JEI is installed. */
@JeiPlugin
public class PDJeiPlugin implements IModPlugin {

	@Override
	public ResourceLocation getPluginUid() {
		return PocketDynamo.asResource("jei_plugin");
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		for (PDRecipeInfo.Page page : PDRecipeInfo.pages())
			registration.addIngredientInfo(page.item(), page.text().toArray(Component[]::new));
	}
}
