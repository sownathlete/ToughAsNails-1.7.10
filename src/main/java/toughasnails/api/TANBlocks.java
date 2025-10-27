/*
 * Decompiled with CFR 0.148.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 */
package toughasnails.api;

import net.minecraft.block.Block;

public class TANBlocks {
    public static Block campfire;
    public static Block gas;
    public static Block temperature_coil;
    public static Block rain_collector;
    public static Block torch_new;
    public static Block glowstone_torch;
    public static Block dead_crops;
    public static Block[] season_sensors;
    
    public static Block thermoregulator;
    public static Block temperature_gauge;
    public static Block rain_collector2; // or reuse rain_collector if you prefer
    public static Block water_purifier;

    static {
        season_sensors = new Block[4];
    }
}

