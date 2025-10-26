/*
 * Decompiled with CFR 0.148.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package toughasnails.api.stat.capability;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import toughasnails.api.stat.IPlayerStat;
import toughasnails.api.temperature.Temperature;
import toughasnails.temperature.modifier.TemperatureModifier;

public interface ITemperature
extends IPlayerStat {
    public void setTemperature(Temperature var1);

    public void addTemperature(Temperature var1);

    public void applyModifier(String var1, int var2, int var3, int var4);

    public boolean hasModifier(String var1);

    public Temperature getTemperature();

    public void setChangeTime(int var1);

    public int getChangeTime();

    public ImmutableMap<String, TemperatureModifier.ExternalModifier> getExternalModifiers();

    public void setExternalModifiers(Map<String, TemperatureModifier.ExternalModifier> var1);
}

