package me.flawless.mod.modules.impl.misc;

import me.flawless.mod.modules.Module;

public class ShulkerViewer extends Module {
	public static ShulkerViewer INSTANCE;
	public ShulkerViewer() {
		super("ShulkerViewer", Category.Misc);
		INSTANCE = this;
	}
}