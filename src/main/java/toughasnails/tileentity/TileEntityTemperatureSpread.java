package toughasnails.tileentity;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import toughasnails.api.season.IDecayableCrop;
import toughasnails.api.stat.capability.ITemperature;
import toughasnails.api.temperature.ITemperatureRegulator;
import toughasnails.api.temperature.Temperature;
import toughasnails.api.temperature.TemperatureHelper;

/**
 * Temperature-spread tile (Forge 1.7.10 back-port).
 *
 * <p>No {@code ITickable}; we override {@code updateEntity()}.</p>
 */
public class TileEntityTemperatureSpread extends TileEntity
                                         implements ITemperatureRegulator {

    /* ---------------- constants ---------------- */
    private static final int MAX_SPREAD_DISTANCE = 50;
    private static final int RATE_MODIFIER       = -500;

    /* ---------------- state ---------------- */
    @SuppressWarnings("unchecked")
    private final Set<ChunkCoordinates>[] filledPositions =
            new Set[MAX_SPREAD_DISTANCE + 1];

    private final Set<ChunkCoordinates> obstructedPositions = Sets.newConcurrentHashSet();
    private final Set<Entity>           spawnedEntities     = Sets.newConcurrentHashSet(); // reserved for future use

    private int           updateTicks      = 0;
    private int           temperatureDelta = 0;
    private AxisAlignedBB maxSpreadBox;

    /* ---------------- ctor ---------------- */
    public TileEntityTemperatureSpread() { this(0); }

    public TileEntityTemperatureSpread(int delta) {
        this.temperatureDelta = delta;
        for (int i = 0; i <= MAX_SPREAD_DISTANCE; i++) {
            filledPositions[i] = Sets.newConcurrentHashSet();
        }
    }

    /* ============================================================
       • Tick logic  (1.7.10 => updateEntity)
       ============================================================ */
    @Override
    public void updateEntity() {
        if (worldObj == null || worldObj.isRemote) return;

        if (++updateTicks % 20 != 0) return;                       // once/second

        /* ensure cached flood-fill is valid */
        if (!verify()) fill();

        if (maxSpreadBox == null) {
            maxSpreadBox = AxisAlignedBB.getBoundingBox(
                    xCoord - MAX_SPREAD_DISTANCE, yCoord - MAX_SPREAD_DISTANCE, zCoord - MAX_SPREAD_DISTANCE,
                    xCoord + MAX_SPREAD_DISTANCE + 1, yCoord + MAX_SPREAD_DISTANCE + 1, zCoord + MAX_SPREAD_DISTANCE + 1);
        }

        @SuppressWarnings("unchecked")
        java.util.List<EntityPlayer> players =
                worldObj.getEntitiesWithinAABB(EntityPlayer.class, maxSpreadBox);

        for (EntityPlayer player : players) {
            if (!isPlayerInsideSpread(player)) continue;

            ITemperature temp = TemperatureHelper.getTemperatureData(player);
            if (temp != null) {
                temp.applyModifier("Climatisation",
                                   temperatureDelta,
                                   RATE_MODIFIER,
                                   100);
            }
        }
    }

    /* quick containment test */
    private boolean isPlayerInsideSpread(EntityPlayer player) {
        ChunkCoordinates p = new ChunkCoordinates(
                (int) player.posX, (int) player.posY, (int) player.posZ);

        for (int i = 0; i <= MAX_SPREAD_DISTANCE; i++)
            if (filledPositions[i].contains(p)) return true;

        return false;
    }

    /* ============================================================
       • Flood-fill
       ============================================================ */
    private void reset() {
        for (Set<ChunkCoordinates> s : filledPositions) s.clear();
        obstructedPositions.clear();
    }

    private void fill() {
        reset();

        /* start from each face around the TE */
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            int ox = xCoord + dir.offsetX;
            int oy = yCoord + dir.offsetY;
            int oz = zCoord + dir.offsetZ;
            if (canFill(ox, oy, oz))
                filledPositions[MAX_SPREAD_DISTANCE].add(new ChunkCoordinates(ox, oy, oz));
        }
        runStage(MAX_SPREAD_DISTANCE - 1);
    }

    private void runStage(int strength) {
        if (strength <= 0) return;

        for (ChunkCoordinates pos : filledPositions[strength + 1]) {
            spreadAround(pos, strength);
        }
        runStage(strength - 1);
    }

    private void spreadAround(ChunkCoordinates pos, int strength) {
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            ChunkCoordinates next = new ChunkCoordinates(
                    pos.posX + dir.offsetX,
                    pos.posY + dir.offsetY,
                    pos.posZ + dir.offsetZ);

            if (filledPositions[strength + 1].contains(next)) continue;
            setTrackedStrength(next, strength);
        }
    }

    private void setTrackedStrength(ChunkCoordinates pos, int strength) {
        if (canFill(pos.posX, pos.posY, pos.posZ))
            filledPositions[strength].add(pos);
        else
            obstructedPositions.add(pos);
    }

    /* ============================================================
       • Utility checks
       ============================================================ */
    private boolean verify() {
        for (Set<ChunkCoordinates> set : filledPositions)
            for (ChunkCoordinates p : set)
                if (!canFill(p.posX, p.posY, p.posZ)) return false;

        for (ChunkCoordinates p : obstructedPositions)
            if (canFill(p.posX, p.posY, p.posZ)) return false;

        return true;
    }

    /** air (or crop) and not open to sky */
    private boolean canFill(int x, int y, int z) {
        Block block = worldObj.getBlock(x, y, z);
        if (block.isOpaqueCube()) return false;                // solid

        boolean sky  = worldObj.canBlockSeeTheSky(x, y, z);
        boolean crop = block instanceof IDecayableCrop;
        return !sky || crop;
    }

    /* ============================================================
       • NBT persistence
       ============================================================ */
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        temperatureDelta = tag.getInteger("TemperatureModifier");
        rebuildAABB();

        NBTTagCompound filled = tag.getCompoundTag("FilledPositions");
        for (int i = 0; i <= MAX_SPREAD_DISTANCE; i++)
            filledPositions[i] = readPosSet(filled.getCompoundTag("Strength" + i));

        obstructedPositions.addAll(readPosSet(tag.getCompoundTag("ObstructedPositions")));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("TemperatureModifier", temperatureDelta);

        NBTTagCompound filled = new NBTTagCompound();
        for (int i = 0; i <= MAX_SPREAD_DISTANCE; i++) {
            NBTTagCompound st = new NBTTagCompound();
            writePosSet(st, filledPositions[i]);
            filled.setTag("Strength" + i, st);
        }
        tag.setTag("FilledPositions", filled);

        NBTTagCompound obstructed = new NBTTagCompound();
        writePosSet(obstructed, obstructedPositions);
        tag.setTag("ObstructedPositions", obstructed);
    }

    private void rebuildAABB() {
        maxSpreadBox = AxisAlignedBB.getBoundingBox(
                xCoord - MAX_SPREAD_DISTANCE, yCoord - MAX_SPREAD_DISTANCE, zCoord - MAX_SPREAD_DISTANCE,
                xCoord + MAX_SPREAD_DISTANCE + 1, yCoord + MAX_SPREAD_DISTANCE + 1, zCoord + MAX_SPREAD_DISTANCE + 1);
    }

    /* ----- helper (serialize pos sets) ----- */
    private static void writePosSet(NBTTagCompound tag, Set<ChunkCoordinates> set) {
        tag.setInteger("Count", set.size());
        int i = 0;
        for (ChunkCoordinates p : set) {
            NBTTagCompound pt = new NBTTagCompound();
            pt.setInteger("X", p.posX);
            pt.setInteger("Y", p.posY);
            pt.setInteger("Z", p.posZ);
            tag.setTag("Pos" + i++, pt);
        }
    }

    private static Set<ChunkCoordinates> readPosSet(NBTTagCompound tag) {
        HashSet<ChunkCoordinates> out = new HashSet<ChunkCoordinates>();
        int cnt = tag.getInteger("Count");
        for (int i = 0; i < cnt; i++) {
            NBTTagCompound pt = tag.getCompoundTag("Pos" + i);
            out.add(new ChunkCoordinates(pt.getInteger("X"),
                                         pt.getInteger("Y"),
                                         pt.getInteger("Z")));
        }
        return out;
    }

    /* ============================================================
       • ITemperatureRegulator
       ============================================================ */
    @Override
    public Temperature getRegulatedTemperature() {
        return new Temperature(temperatureDelta);
    }

    /** 1.7.10 interface form: raw coordinates. */
    @Override
    public boolean isPosRegulated(int x, int y, int z) {
        for (int i = 0; i <= MAX_SPREAD_DISTANCE; i++)
            for (ChunkCoordinates p : filledPositions[i])
                if (p.posX == x && p.posY == y && p.posZ == z) return true;
        return false;
    }
}
