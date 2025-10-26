/*
 * Decompiled with CFR 0.148.
 */
package toughasnails.api.temperature;

import toughasnails.api.temperature.TemperatureScale;

public class Temperature {
    private int rawValue;
    private TemperatureScale.TemperatureRange temperatureRange;
    private int rangeIndex;
    private float rangeDelta;

    public Temperature(int scalePos) {
        this.rawValue = scalePos;
        this.temperatureRange = TemperatureScale.getTemperatureRange(this.getRawValue());
        this.rangeIndex = -1;
        this.rangeDelta = -1.0f;
    }

    public int getRawValue() {
        return this.rawValue;
    }

    public TemperatureScale.TemperatureRange getRange() {
        return this.temperatureRange;
    }

    public int getRangeIndex(boolean reverseEnd) {
        if (this.rangeIndex == -1) {
            return TemperatureScale.getRangeIndex(this.getRawValue(), reverseEnd);
        }
        return this.rangeIndex;
    }

    public float getRangeDelta(boolean reverseEnd) {
        if (this.rangeDelta == -1.0f) {
            this.rangeDelta = TemperatureScale.getRangeDelta(this.getRawValue(), reverseEnd);
        }
        return this.rangeDelta;
    }
}

