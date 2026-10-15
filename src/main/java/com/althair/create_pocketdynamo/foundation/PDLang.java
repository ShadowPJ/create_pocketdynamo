package com.althair.create_pocketdynamo.foundation;

import com.althair.create_pocketdynamo.PocketDynamo;

import net.createmod.catnip.lang.LangBuilder;
import net.createmod.catnip.lang.LangNumberFormat;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;

/** Lang helpers bound to this addon's namespace, so tooltips use {@code create_pocketdynamo.*} keys. */
public class PDLang {

	public static LangBuilder builder() {
		return new LangBuilder(PocketDynamo.MOD_ID);
	}

	public static LangBuilder translate(String key, Object... args) {
		return builder().translate(key, args);
	}

	public static LangBuilder number(double d) {
		return builder().text(LangNumberFormat.format(d));
	}

	public static LangBuilder text(String text) {
		return builder().text(text);
	}

	public static MutableComponent translateDirect(String key, Object... args) {
		return Component.translatable(PocketDynamo.MOD_ID + "." + key, LangBuilder.resolveBuilders(args));
	}

	private PDLang() {
	}
}
