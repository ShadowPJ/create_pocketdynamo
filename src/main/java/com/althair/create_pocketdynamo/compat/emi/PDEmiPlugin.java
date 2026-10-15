package com.althair.create_pocketdynamo.compat.emi;

import java.util.List;

import com.althair.create_pocketdynamo.PocketDynamo;
import com.althair.create_pocketdynamo.compat.PDRecipeInfo;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiStack;

/** EMI integration: one info page per item. Only loaded when EMI is installed. */
@EmiEntrypoint
public class PDEmiPlugin implements EmiPlugin {

	@Override
	public void register(EmiRegistry registry) {
		for (PDRecipeInfo.Page page : PDRecipeInfo.pages())
			registry.addRecipe(new EmiInfoRecipe(
				List.of(EmiStack.of(page.item())),
				page.text(),
				PocketDynamo.asResource("/info/" + page.id())));
	}
}
