/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.capabilities.food;

import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.StringRepresentable;

public enum Nutrient implements StringRepresentable
{
    GRAIN(ChatFormatting.GOLD),
    FRUIT(ChatFormatting.GREEN),
    VEGETABLES(ChatFormatting.DARK_GREEN),
    PROTEIN(ChatFormatting.RED),
    DAIRY(ChatFormatting.DARK_PURPLE);

    public static final int TOTAL = values().length;
    public static final Nutrient[] VALUES = values();

    public static Nutrient valueOf(int i)
    {
        return i >= 0 && i < VALUES.length ? VALUES[i] : GRAIN;
    }

    private final String serializedName;
    private final ChatFormatting color;

    Nutrient(ChatFormatting color)
    {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        this.color = color;
    }

    @Override
    public String getSerializedName()
    {
        return serializedName;
    }

    public ChatFormatting getColor()
    {
        return color;
    }

    public static float[] readNbt(CompoundTag nbt)
    {
        float[] nutrients = new float[Nutrient.TOTAL];
        nutrients[Nutrient.GRAIN.ordinal()] = nbt.getFloat("grain");
        nutrients[Nutrient.VEGETABLES.ordinal()] = nbt.getFloat("veg");
        nutrients[Nutrient.FRUIT.ordinal()] = nbt.getFloat("fruit");
        nutrients[Nutrient.PROTEIN.ordinal()] = nbt.getFloat("meat");
        nutrients[Nutrient.DAIRY.ordinal()] = nbt.getFloat("dairy");
        return nutrients;
    }

    public static void writeNbt(CompoundTag nbt, float[] nutrients)
    {
        nbt.putFloat("grain", nutrients[Nutrient.GRAIN.ordinal()]);
        nbt.putFloat("veg", nutrients[Nutrient.VEGETABLES.ordinal()]);
        nbt.putFloat("fruit", nutrients[Nutrient.FRUIT.ordinal()]);
        nbt.putFloat("meat", nutrients[Nutrient.PROTEIN.ordinal()]);
        nbt.putFloat("dairy", nutrients[Nutrient.DAIRY.ordinal()]);
    }
}
