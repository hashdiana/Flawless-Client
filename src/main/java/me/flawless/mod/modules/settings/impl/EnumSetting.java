package me.flawless.mod.modules.settings.impl;

import me.flawless.FlawLess;
import me.flawless.api.managers.ModuleManager;
import me.flawless.mod.modules.settings.EnumConverter;
import me.flawless.mod.modules.settings.Setting;

import java.util.function.Predicate;

public class EnumSetting<T extends Enum<T>> extends Setting {
    private T value;
    public boolean popped = false;
    public EnumSetting(String name, T defaultValue) {
        super(name, ModuleManager.lastLoadMod.getName() + "_" + name);
        value = defaultValue;
    }

    public EnumSetting(String name, T defaultValue, Predicate visibilityIn) {
        super(name, ModuleManager.lastLoadMod.getName() + "_" + name, visibilityIn);
        value = defaultValue;
    }

    public void increaseEnum() {
        value = (T) EnumConverter.increaseEnum(value);
    }

    public final T getValue() {
        return this.value;
    }
    public void setEnumValue(String value) {
        for (Enum e : this.value.getDeclaringClass().getEnumConstants()) {
            if (!e.name().equalsIgnoreCase(value)) continue;
            this.value = (T) e;
        }
    }
    @Override
    public void loadSetting() {
        EnumConverter converter = new EnumConverter(value.getClass());
        String enumString = FlawLess.CONFIG.getString(this.getLine());
        if (enumString == null) {
            return;
        }
        Enum value = converter.doBackward(enumString);
        if (value != null) {
            this.value = (T) value;
        }
    }
}