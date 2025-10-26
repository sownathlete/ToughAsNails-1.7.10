package toughasnails.util;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

/**
 * Simple brewing-recipe holder for Forge 1.7.10.
 *
 * <p>Minecraft 1.7.10 has no {@code net.minecraftforge.common.brewing.BrewingRecipe}
 * class, so we implement the bare-minimum logic ourselves while keeping the
 * **public API identical** to the 1.9+ version used by Tough As Nails.</p>
 *
 * <ul>
 *   <li>Constructor signature is unchanged: <br>
 *       {@code new PotionBrewingRecipe(input, ingredient, output)}</li>
 *   <li>Methods exposed by the original `BrewingRecipe`
 *       (<em>isInput</em>, <em>isIngredient</em>, <em>getOutput</em>)
 *       are provided so existing TAN code compiles unmodified.</li>
 * </ul>
 */
public class PotionBrewingRecipe {

    private final ItemStack input;
    private final ItemStack ingredient;
    private final ItemStack output;

    /* ------------------------------------------------------------ */
    public PotionBrewingRecipe(@Nonnull ItemStack input,
                               @Nonnull ItemStack ingredient,
                               @Nonnull ItemStack output) {
        this.input      = input.copy();
        this.ingredient = ingredient.copy();
        this.output     = output.copy();
    }

    /* ------------------------------------------------------------ */

    /** True if {@code stack} matches the recipe’s base input. */
    public boolean isInput(@Nonnull ItemStack stack) {
        return stacksEqual(input, stack);
    }

    /** True if {@code stack} matches the ingredient. */
    public boolean isIngredient(@Nonnull ItemStack stack) {
        return stacksEqual(ingredient, stack);
    }

    /**
     * Returns a <em>copy</em> of the output <em>if</em> the supplied input+
     * ingredient match this recipe, otherwise {@code null}.
     */
    public ItemStack getOutput(@Nonnull ItemStack in, @Nonnull ItemStack ing) {
        return (isInput(in) && isIngredient(ing)) ? output.copy() : null;
    }

    /* ------------------------------------------------------------ */

    /** Vanilla-style “loose” equality (item, meta, and NBT tags). */
    private static boolean stacksEqual(ItemStack a, ItemStack b) {
        if (a == null || b == null) return false;
        return  a.getItem()       == b.getItem() &&
                a.getItemDamage() == b.getItemDamage() &&
                ItemStack.areItemStackTagsEqual(a, b);
    }
}
