package toughasnails.handler.thirst;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.world.BlockEvent;
import toughasnails.thirst.ThirstHandler;

/**
 * Backported ThirstStatHandler for Forge 1.7.10.
 * Adds exhaustion to thirst on player actions such as jumping, attacking, and breaking blocks.
 */
public class ThirstStatHandler {

    @SubscribeEvent
    public void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.entity instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.entity;
        World world = player.worldObj;
        if (world.isRemote) return;

        ThirstHandler thirst = ThirstHandler.get(player);
        if (thirst == null) return;

        if (player.isSprinting()) {
            thirst.addExhaustion(0.8F);
        } else {
            thirst.addExhaustion(0.2F);
        }

        ThirstHandler.save(player, thirst);
    }

    @SubscribeEvent
    public void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.entity instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.entity;
        World world = player.worldObj;
        if (world.isRemote) return;
        if (event.ammount <= 0.0F) return;

        ThirstHandler thirst = ThirstHandler.get(player);
        if (thirst == null) return;

        thirst.addExhaustion(event.source.getHungerDamage());
        ThirstHandler.save(player, thirst);
    }

    @SubscribeEvent
    public void onPlayerAttackEntity(AttackEntityEvent event) {
        EntityPlayer player = event.entityPlayer;
        World world = player.worldObj;
        if (world.isRemote) return;

        Entity target = event.target;
        if (target == null || target == player) return;

        // Basic check: attacking with an item or fists should drain a bit of thirst
        ThirstHandler thirst = ThirstHandler.get(player);
        if (thirst == null) return;

        // Any melee hit triggers minor exhaustion
        thirst.addExhaustion(0.3F);
        ThirstHandler.save(player, thirst);
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        EntityPlayer player = event.getPlayer();
        if (player == null) return;

        World world = event.world;
        if (world.isRemote) return;
        if (player.capabilities.isCreativeMode) return;

        int x = event.x;
        int y = event.y;
        int z = event.z;
        Block block = world.getBlock(x, y, z);

        if (block == null) return;
        if (!block.canHarvestBlock(player, event.blockMetadata)) return;

        ThirstHandler thirst = ThirstHandler.get(player);
        if (thirst == null) return;

        thirst.addExhaustion(0.025F);
        ThirstHandler.save(player, thirst);
    }
}
