package toughasnails.item;

import java.util.List;
import java.util.Map;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

import toughasnails.api.item.IColoredItem;
import toughasnails.init.ModEntities;

/**
 * Tough As Nails custom spawn egg (Forge 1.7.10).
 * - Uses ModEntities.ENTITY_EGGS and ENTITY_NAMES
 * - Multi-pass colored rendering like vanilla spawn eggs
 * - Works on spawners and in-world/liquid placement
 */
public class ItemTANSpawnEgg extends Item implements IColoredItem {

    @SideOnly(Side.CLIENT) private IIcon iconBase;
    @SideOnly(Side.CLIENT) private IIcon iconOverlay;

    public ItemTANSpawnEgg() {
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
    }

    // ---------- Creative subitems ----------
    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (Map.Entry<Integer, EntityList.EntityEggInfo> e : ModEntities.ENTITY_EGGS.entrySet()) {
            list.add(new ItemStack(item, 1, e.getKey()));
        }
    }

    // ---------- Spawning ----------
    public static Entity spawnTANCreature(World world, int entityID, double x, double y, double z) {
        Entity entity = ModEntities.createEntityByID(entityID, world);
        if (entity instanceof EntityLivingBase) {
            EntityLiving living = (EntityLiving) entity;
            entity.setLocationAndAngles(
                x, y, z,
                MathHelper.wrapAngleTo180_float(world.rand.nextFloat() * 360.0F),
                0.0F
            );
            living.rotationYawHead = living.rotationYaw;
            living.renderYawOffset = living.rotationYaw;
            world.spawnEntityInWorld(entity);
            living.playLivingSound();
        }
        return entity;
    }

    // ---------- Naming ----------
    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int id = stack.getItemDamage();
        String name = ModEntities.ENTITY_NAMES.get(id);
        return super.getUnlocalizedName(stack) + "_" + (name != null ? name : "unknown");
    }

    // ---------- Coloring & Icons (1.7.10 multi-pass) ----------
    @Override
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        // Reuse vanilla spawn egg icons for base/overlay
        this.iconBase    = reg.registerIcon("spawn_egg");
        this.iconOverlay = reg.registerIcon("spawn_egg_overlay");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamageForRenderPass(int damage, int pass) {
        return pass == 0 ? iconBase : iconOverlay;
    }

    // ✅ Fix: IColoredItem expects an IIcon here, not an int.
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromPass(int pass) {
        return pass == 0 ? iconBase : iconOverlay;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int pass) {
        EntityList.EntityEggInfo info = ModEntities.ENTITY_EGGS.get(stack.getItemDamage());
        if (info == null) return 0xFFFFFF;
        return pass == 0 ? info.primaryColor : info.secondaryColor;
    }

    // ---------- Use on blocks (spawner or ground) ----------
    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
                             int x, int y, int z, int side,
                             float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;

        Block block = world.getBlock(x, y, z);

        // If clicking a mob spawner, set its entity
        if (block == Blocks.mob_spawner) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityMobSpawner) {
                MobSpawnerBaseLogic logic = ((TileEntityMobSpawner) te).func_145881_a();
                String name = ModEntities.ENTITY_NAMES.get(stack.getItemDamage());
                if (name != null) {
                    logic.setEntityName(name);
                }
                te.markDirty();
                world.markBlockForUpdate(x, y, z);
                if (!player.capabilities.isCreativeMode) --stack.stackSize;
                return true;
            }
        }

        // Otherwise spawn the entity in front of the clicked face
        x += EnumFacing.values()[side].getFrontOffsetX();
        y += EnumFacing.values()[side].getFrontOffsetY();
        z += EnumFacing.values()[side].getFrontOffsetZ();

        double yOffset = 0.0D;
        if (side == 1 && block instanceof BlockFence) {
            yOffset = 0.5D;
        }

        Entity e = spawnTANCreature(world, stack.getItemDamage(), x + 0.5D, y + yOffset, z + 0.5D);
        if (e != null) {
            if (e instanceof EntityLivingBase && stack.hasDisplayName()) {
                ((EntityLiving) e).setCustomNameTag(stack.getDisplayName());
            }
            if (!player.capabilities.isCreativeMode) --stack.stackSize;
        }
        return true;
    }

    // ---------- Right-click in air (e.g., into water) ----------
    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote) return stack;

        MovingObjectPosition mop = this.getMovingObjectPositionFromPlayer(world, player, true);
        if (mop == null) return stack;

        if (mop.typeOfHit == MovingObjectType.BLOCK) {
            int x = mop.blockX, y = mop.blockY, z = mop.blockZ;

            if (!world.canMineBlock(player, x, y, z)) return stack;
            if (!player.canPlayerEdit(x, y, z, mop.sideHit, stack)) return stack;

            if (world.getBlock(x, y, z) instanceof BlockLiquid) {
                Entity e = spawnTANCreature(world, stack.getItemDamage(), x + 0.5D, y + 0.5D, z + 0.5D);
                if (e != null) {
                    if (e instanceof EntityLivingBase && stack.hasDisplayName()) {
                        ((EntityLiving) e).setCustomNameTag(stack.getDisplayName());
                    }
                    if (!player.capabilities.isCreativeMode) --stack.stackSize;
                }
            }
        }
        return stack;
    }
}
