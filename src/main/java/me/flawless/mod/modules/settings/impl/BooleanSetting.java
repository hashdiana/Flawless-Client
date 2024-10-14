package me.flawless.mod.modules.settings.impl;

import me.flawless.FlawLess;
import me.flawless.api.managers.ModuleManager;
import me.flawless.mod.modules.settings.Setting;

import java.util.function.Predicate;

public class BooleanSetting extends Setting {
	public boolean parent = false;
	public boolean popped = false;
	private boolean value;
	public final boolean defaultValue;

	public BooleanSetting(String name, boolean defaultValue) {
		super(name, ModuleManager.lastLoadMod.getName() + "_" + name);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public BooleanSetting(String name, boolean defaultValue, Predicate visibilityIn) {
		super(name, ModuleManager.lastLoadMod.getName() + "_" + name, visibilityIn);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public final boolean getValue() {
		return this.value;
	}
	
	public final void setValue(boolean value) {
		this.value = value;
	}
	
	public final void toggleValue() {
		this.value = !value;
	}
	public final boolean isOpen() {
		if (parent) {
			return popped;
		} else {
			return true;
		}
	}
	@Override
	public void loadSetting() {
		this.value = FlawLess.CONFIG.getBoolean(this.getLine(), value);
	}

	public BooleanSetting setParent() {
		parent = true;
		return this;
	}
}
