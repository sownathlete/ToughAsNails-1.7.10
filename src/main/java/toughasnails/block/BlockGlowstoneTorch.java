package toughasnails.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import toughasnails.api.ITANBlock;
import toughasnails.item.ItemTANBlock;

/**
 * Glowstone Torch — 1.7.10 backport.
 * Metadata:
 *   1 = EAST (attached to west face)
 *   2 = WEST (attached to east face)
 *   3 = SOUTH (attached to north face)
 *   4 = NORTH (attached to south face)
 *   5 = UP   (standing on top)
 */
public class BlockGlowstoneTorch extends Block implements ITANBlock {

    public BlockGlowstoneTorch() {
        super(Material.circuits);
        setTickRandomly(true);
        setHardness(0.0F);
        setLightLevel(1.0F);
        setStepSound(soundTypeWood);
        // default bounds for item render
        setBlockBounds(0.35F, 0.0F, 0.35F, 0.65F, 0.6F, 0.65F);
    }

    // ---------- ITANBlock (1.7.10) ----------
    @Override
    public Class<? extends net.minecraft.item.ItemBlock> getItemClass() {
        return ItemTANBlock.class;
    }

    @Override
    public String[] getPresetProperties() {
        return new String[0];
    }

    @Override
    public String[] getNonRenderingProperties() {
        return null;
    }

    @Override
    public String getStateName(int meta) {
        switch (meta) {
            case 1: return "east";
            case 2: return "west";
            case 3: return "south";
            case 4: return "north";
            case 5: return "up";
            default: return "up";
        }
    }

    // ---------- Rendering / Bounds ----------
    @Override
    public boolean isOpaqueCube() { return false; }

    @Override
    public boolean renderAsNormalBlock() { return false; }

    @Override
    public int getRenderType() { return 2; } // torch-like

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return null; // no collision box (like torch)
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setBlockBounds(0.35F, 0.0F, 0.35F, 0.65F, 0.6F, 0.65F);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        float f = 0.1875F; // 3/16, similar to torch head offset
        if (meta == 1) { // east (attached west face)
            setBlockBounds(0.0F, 0.2F, 0.35F, f, 0.8F, 0.65F);
        } else if (meta == 2) { // west
            setBlockBounds(1.0F - f, 0.2F, 0.35F, 1.0F, 0.8F, 0.65F);
        } else if (meta == 3) { // south
            setBlockBounds(0.35F, 0.2F, 0.0F, 0.65F, 0.8F, f);
        } else if (meta == 4) { // north
            setBlockBounds(0.35F, 0.2F, 1.0F - f, 0.65F, 0.8F, 1.0F);
        } else { // up (standing)
            setBlockBounds(0.35F, 0.0F, 0.35F, 0.65F, 0.6F, 0.65F);
        }
    }

    // ---------- Placement / Support ----------
    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return canPlaceOn(world, x, y - 1, z)
            || world.isSideSolid(x - 1, y, z, ForgeDirection.EAST)
            || world.isSideSolid(x + 1, y, z, ForgeDirection.WEST)
            || world.isSideSolid(x, y, z - 1, ForgeDirection.SOUTH)
            || world.isSideSolid(x, y, z + 1, ForgeDirection.NORTH);
    }

    private boolean canPlaceOn(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        // top-surface or torch-on-top allowed
        return World.doesBlockHaveSolidTopSurface(world, x, y, z) || block.canPlaceTorchOnTop(world, x, y, z);
    }

    private boolean canStay(World world, int x, int y, int z, int meta) {
        if (meta == 1) return world.isSideSolid(x - 1, y, z, ForgeDirection.EAST);
        if (meta == 2) return world.isSideSolid(x + 1, y, z, ForgeDirection.WEST);
        if (meta == 3) return world.isSideSolid(x, y, z - 1, ForgeDirection.SOUTH);
        if (meta == 4) return world.isSideSolid(x, y, z + 1, ForgeDirection.NORTH);
        return meta == 5 && canPlaceOn(world, x, y - 1, z);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block changed) {
        int meta = world.getBlockMetadata(x, y, z);
        if (!canStay(world, x, y, z, meta)) {
            dropBlockAsItem(world, x, y, z, meta, 0);
            world.setBlockToAir(x, y, z);
        }
    }

    // ---------- Orientation from placement ----------
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int meta = determineFacing(world, x, y, z, placer);
        world.setBlockMetadataWithNotify(x, y, z, meta, 2);
    }

    private int determineFacing(World world, int x, int y, int z, EntityLivingBase entity) {
        // If looking steeply up/down, prefer standing
        if (MathHelper.abs(entity.rotationPitch) > 65F && canPlaceOn(world, x, y - 1, z)) {
            return 5; // up/standing
        }
        int dir = MathHelper.floor_double(entity.rotationYaw * 4F / 360F + 0.5D) & 3;
        // Try walls in order of player facing
        switch (dir) {
            case 0: // south -> place on north wall (meta 4)
                if (world.isSideSolid(x, y, z + 1, ForgeDirection.NORTH)) return 4;
                break;
            case 1: // west -> place on east wall (meta 1)
                if (world.isSideSolid(x - 1, y, z, ForgeDirection.EAST)) return 1;
                break;
            case 2: // north -> place on south wall (meta 3)
                if (world.isSideSolid(x, y, z - 1, ForgeDirection.SOUTH)) return 3;
                break;
            case 3: // east -> place on west wall (meta 2)
                if (world.isSideSolid(x + 1, y, z, ForgeDirection.WEST)) return 2;
                break;
        }
        // Fallback to standing if possible
        if (canPlaceOn(world, x, y - 1, z)) return 5;
        // Final fallback: north wall if available, else default standing
        if (world.isSideSolid(x, y, z + 1, ForgeDirection.NORTH)) return 4;
        return 5;
    }

    // ---------- Visuals ----------
    @Override
    public int getRenderBlockPass() { return 1; } // cutout

    @Override
    public net.minecraft.util.IIcon getIcon(int side, int meta) {
        // Reuse the vanilla torch icon
        return Blocks.torch.getIcon(0, 0);
    }
}
