package toughasnails.item;

import net.minecraft.item.ItemFood;

/** Simple food: Ice Cream. */
public class ItemIceCream extends ItemFood {

    public ItemIceCream() {
        super(5, 0.8F, false); // 5 hunger, good saturation
        setUnlocalizedName("ice_cream");
        setAlwaysEdible();      // can eat even when nearly full, feels right
    }
}
