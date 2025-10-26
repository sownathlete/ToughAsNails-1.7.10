/*
 * Decompiled with CFR 0.148.
 */
package toughasnails.api.stat.capability;

import toughasnails.api.stat.IPlayerStat;

public interface IThirst
extends IPlayerStat {
    public void setThirst(int var1);

    public void setHydration(float var1);

    public void setExhaustion(float var1);

    public void addStats(int var1, float var2);

    public int getThirst();

    public float getHydration();

    public float getExhaustion();

    public void setChangeTime(int var1);

    public int getChangeTime();
}

