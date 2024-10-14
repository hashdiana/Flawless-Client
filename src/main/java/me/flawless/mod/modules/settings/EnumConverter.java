package me.flawless.mod.modules.settings;

import com.google.common.base.Converter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

public class EnumConverter
        extends Converter<Enum, JsonElement>  {
    private final Class<? extends Enum> clazz;

    public EnumConverter(Class<? extends Enum> clazz) {
        this.clazz = clazz;
    }

    public static int currentEnum(Enum clazz) {
        for (int i = 0; i < clazz.getDeclaringClass().getEnumConstants().length; ++i) {
            Enum e = (Enum) clazz.getDeclaringClass().getEnumConstants()[i];
            if (!e.name().equalsIgnoreCase(clazz.name())) continue;
            return i;
        }
        return -1;
    }

    public static Enum increaseEnum(Enum clazz) {
        int index = currentEnum(clazz);
        for (int i = 0; i < clazz.getDeclaringClass().getEnumConstants().length; ++i) {
            if (i != index + 1) continue;
            return (Enum) clazz.getDeclaringClass().getEnumConstants()[i];
        }
        return (Enum) clazz.getDeclaringClass().getEnumConstants()[0];
    }

    public static String getProperName(Enum clazz) {
        return Character.toUpperCase(clazz.name().charAt(0)) + clazz.name().toLowerCase().substring(1);
    }

    public @NotNull JsonElement doForward(Enum anEnum) {
        return new JsonPrimitive(anEnum.toString());
    }

    public Enum doBackward(String string) {
        try {
            return Enum.valueOf(clazz, string);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    public @NotNull Enum doBackward(JsonElement jsonElement) {
        try {
            return Enum.valueOf(clazz, jsonElement.getAsString());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

