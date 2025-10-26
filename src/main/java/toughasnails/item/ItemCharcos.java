package toughasnails.item;

import net.minecraft.item.ItemFood;

/** Breakfast of champions: Charc-O's. */
public class ItemCharcos extends ItemFood {

    public ItemCharcos() {
        super(4, 0.6F, false);  // 4 hunger, decent saturation
        setUnlocalizedName("charc_os");
    }
}
