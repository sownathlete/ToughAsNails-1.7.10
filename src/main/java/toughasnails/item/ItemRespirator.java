package toughasnails.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemArmor;

public class ItemRespirator extends ItemArmor {

    public ItemRespirator(ArmorMaterial material, int renderIndex) {
        // In 1.7.10, armorType: 0 = helmet, 1 = chestplate, 2 = leggings, 3 = boots
        super(material, renderIndex, 0); // 0 → HEAD slot
        this.setCreativeTab((CreativeTabs) null);
    }
}
