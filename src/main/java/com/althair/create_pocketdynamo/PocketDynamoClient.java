package com.althair.create_pocketdynamo;

import com.althair.create_pocketdynamo.ponder.PDPonderPlugin;

import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only setup. Kept in its own class so no client classes are touched on a dedicated server. */
@EventBusSubscriber(modid = PocketDynamo.MOD_ID, value = Dist.CLIENT)
public class PocketDynamoClient {

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> PonderIndex.addPlugin(new PDPonderPlugin()));
	}
}
