package toughasnails.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import toughasnails.api.HealthHelper;

public class ItemLifebloodCrystal extends Item {

    public ItemLifebloodCrystal() {
        this.setMaxStackSize(16);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (HealthHelper.addActiveHearts(player, 1)) {

            // Spawn heart particles client-side
            for (int i = 0; i < 8; ++i) {
                double d0 = world.rand.nextGaussian() * 0.02D;
                double d1 = world.rand.nextGaussian() * 0.02D;
                double d2 = world.rand.nextGaussian() * 0.02D;
                double px = player.posX + (world.rand.nextFloat() * player.width * 2.0F) - player.width;
                double py = player.posY + 0.5D + (world.rand.nextFloat() * player.height);
                double pz = player.posZ + (world.rand.nextFloat() * player.width * 2.0F) - player.width;
                world.spawnParticle("heart", px, py, pz, d0, d1, d2);
            }

            // Play a pleasant chime
            world.playSoundEffect(
                player.posX, player.posY, player.posZ,
                "random.levelup", 0.75F, 1.0F
            );

            if (!player.capabilities.isCreativeMode) {
                stack.stackSize--;
            }
        }

        return stack;
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }
}
