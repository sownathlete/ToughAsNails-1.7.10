// File: toughasnails/api/temperature/TemperatureHelper.java
package toughasnails.api.temperature;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import toughasnails.api.stat.capability.ITemperature;
import toughasnails.temperature.TemperatureHandler;
import toughasnails.temperature.modifier.TemperatureModifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 1.7.10 helper: stores the whole TemperatureHandler state in the player's EntityData NBT.
 * Persists:
 *  - temperatureLevel
 *  - temperatureTimer
 *  - externalModifiers (name, amount, rate, endTime)
 *
 * Also exposes small utility methods used elsewhere in the mod:
 *  - bootstrap()                             : currently a no-op (kept for API parity).
 *  - getTemperatureData(EntityPlayer)        : returns the current ITemperature view.
 *  - isPosClimatisedForTemp(World,x,y,z, T)  : checks nearby ITemperatureRegulator TEs.
 */
public final class TemperatureHelper {

    /* -------- NBT keys -------- */
    private static final String ROOT_KEY            = "TAN_Temperature";
    private static final String K_TEMPERATURE_LEVEL = "temperatureLevel";
    private static final String K_TEMPERATURE_TIMER = "temperatureTimer";
    private static final String K_EXTERNAL_LIST     = "externalModifiers";
    private static final String K_NAME              = "name";
    private static final String K_AMOUNT            = "amount";
    private static final String K_RATE              = "rate";
    private static final String K_END_TIME          = "endTime";

    private TemperatureHelper() {}

    /* ======================================================================
       Public API used throughout the mod
       ====================================================================== */

    /** Present for compatibility with proxy/core bootstrap calls. No-op on 1.7.10. */
    public static void bootstrap() {
        // Intentionally empty; tick handlers are registered on the FML bus (StatTickHandler).
    }

    /** Get a live temperature object for this player (loads or creates persisted state). */
    public static ITemperature getTemperatureData(EntityPlayer player) {
        return getOrCreate(player);
    }

    /**
     * Returns true if any nearby ITemperatureRegulator claims the position AND provides
     * at least the requested absolute temperature delta (needed.getRawValue()).
     *
     * This iterates world.loadedTileEntityList and defers the spatial check to the
     * regulator’s isPosRegulated(x,y,z) method.
     */
    public static boolean isPosClimatisedForTemp(World world, int x, int y, int z, Temperature needed) {
        if (world == null) return false;

        @SuppressWarnings("unchecked")
        List<TileEntity> tes = world.loadedTileEntityList;
        if (tes == null || tes.isEmpty()) return false;

        int required = Math.abs(needed == null ? 0 : needed.getRawValue());

        for (TileEntity te : tes) {
            if (!(te instanceof ITemperatureRegulator)) continue;

            ITemperatureRegulator reg = (ITemperatureRegulator) te;
            if (!reg.isPosRegulated(x, y, z)) continue;

            Temperature offered = reg.getRegulatedTemperature();
            int strength = Math.abs(offered == null ? 0 : offered.getRawValue());
            if (strength >= required) return true;
        }
        return false;
    }

    /* ======================================================================
       Persistence: load/save the handler to the player's EntityData NBT
       ====================================================================== */

    /** Load handler from NBT or create/save defaults if missing. */
    public static TemperatureHandler getOrCreate(EntityPlayer player) {
        if (player == null) return new TemperatureHandler();

        NBTTagCompound entity = player.getEntityData();
        if (!entity.hasKey(ROOT_KEY)) {
            TemperatureHandler h = new TemperatureHandler();
            // save defaults so future reads have the tag
            save(player, h);
            return h;
        }

        NBTTagCompound tag = entity.getCompoundTag(ROOT_KEY);
        return readFromNBT(tag);
    }

    /** Save current handler state to player NBT. Call at the end of any state change/tick. */
    public static void save(EntityPlayer player, TemperatureHandler handler) {
        if (player == null || handler == null) return;

        NBTTagCompound root = new NBTTagCompound();
        writeToNBT(root, handler);

        NBTTagCompound entity = player.getEntityData();
        entity.setTag(ROOT_KEY, root);
    }

    /** Remove our sub-NBT (rarely needed, e.g., for resets). */
    public static void clear(EntityPlayer player) {
        if (player == null) return;
        NBTTagCompound entity = player.getEntityData();
        if (entity.hasKey(ROOT_KEY)) {
            entity.removeTag(ROOT_KEY);
        }
    }

    /* ----------------------- NBT (de)serialization ----------------------- */

    private static TemperatureHandler readFromNBT(NBTTagCompound tag) {
        TemperatureHandler h = new TemperatureHandler();

        // Level & timer
        if (tag.hasKey(K_TEMPERATURE_LEVEL)) {
            h.setTemperature(new Temperature(tag.getInteger(K_TEMPERATURE_LEVEL)));
        }
        if (tag.hasKey(K_TEMPERATURE_TIMER)) {
            h.setChangeTime(tag.getInteger(K_TEMPERATURE_TIMER));
        }

        // External modifiers
        Map<String, TemperatureModifier.ExternalModifier> map = new HashMap<String, TemperatureModifier.ExternalModifier>();
        if (tag.hasKey(K_EXTERNAL_LIST, NBT.TAG_LIST)) {
            NBTTagList list = tag.getTagList(K_EXTERNAL_LIST, NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound e = list.getCompoundTagAt(i);
                String name  = e.getString(K_NAME);
                int amount   = e.getInteger(K_AMOUNT);
                int rate     = e.getInteger(K_RATE);
                int endTime  = e.getInteger(K_END_TIME);

                TemperatureModifier.ExternalModifier m =
                        new TemperatureModifier.ExternalModifier(name, amount, rate, endTime);
                map.put(name, m);
            }
        }
        h.setExternalModifiers(map);

        return h;
    }

    private static void writeToNBT(NBTTagCompound tag, TemperatureHandler h) {
        tag.setInteger(K_TEMPERATURE_LEVEL, h.getTemperature().getRawValue());
        tag.setInteger(K_TEMPERATURE_TIMER, h.getChangeTime());

        NBTTagList list = new NBTTagList();
        for (TemperatureModifier.ExternalModifier m : h.getExternalModifiers().values()) {
            NBTTagCompound e = new NBTTagCompound();
            e.setString(K_NAME,      m.getName());
            e.setInteger(K_AMOUNT,   m.getAmount());
            e.setInteger(K_RATE,     m.getRate());
            e.setInteger(K_END_TIME, m.getEndTime());
            list.appendTag(e);
        }
        tag.setTag(K_EXTERNAL_LIST, list);
    }
}
