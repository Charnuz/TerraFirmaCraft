/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.capabilities.food;

import net.minecraft.nbt.CompoundTag;

public record FoodRecord(int hunger, float saturation, float water, float[] nutrients, float decayModifier, boolean alwaysRotten)
{
    public static final FoodRecord EMPTY = new FoodRecord(0, 0, 0, new float[Nutrient.TOTAL], 0);

    public FoodRecord(int hunger, float saturation, float water, float[] nutrients, float decayModifier)
    {
        this(hunger, saturation, water, nutrients, decayModifier, false);
    }

    public FoodRecord(CompoundTag nbt)
    {
        this(nbt.getInt("food"), nbt.getFloat("sat"), nbt.getFloat("water"), Nutrient.readNbt(nbt), nbt.getFloat("decay"));
    }

    public float getNutrient(Nutrient nutrient)
    {
        return nutrients[nutrient.ordinal()];
    }

    public float getNutrient(int i)
    {
        return nutrients[i];
    }

    public CompoundTag write()
    {
        final CompoundTag nbt = new CompoundTag();
        nbt.putInt("food", hunger);
        nbt.putFloat("sat", saturation);
        nbt.putFloat("water", water);
        nbt.putFloat("decay", decayModifier);
        Nutrient.writeNbt(nbt, nutrients);
        return nbt;
    }
}
