/*
 * Decompiled with CFR 0.148.
 */
package toughasnails.api.temperature;

public class TemperatureScale {
    private static int scaleTotal = TemperatureScale.generateTotalScale();
    private static int[] rangeStarts = TemperatureScale.generateRangeStarts();

    public static TemperatureRange getTemperatureRange(int scalePos) {
        if (scalePos < 0 || scalePos > scaleTotal) {
            return null;
        }
        for (TemperatureRange range : TemperatureRange.values()) {
            if (scalePos > rangeStarts[range.ordinal()] + range.rangeSize - 1) continue;
            return range;
        }
        throw new RuntimeException("Could not find range for value " + scalePos + ". This should never happen!");
    }

    public static int getRangeIndex(int scalePos, boolean reverseEnd) {
        TemperatureRange temperatureRange = TemperatureScale.getTemperatureRange(scalePos);
        return Math.abs((reverseEnd ? temperatureRange.getRangeSize() - 1 : 0) - (scalePos - rangeStarts[temperatureRange.ordinal()]));
    }

    public static float getRangeDelta(int scalePos, boolean reverseEnd) {
        return (float)(TemperatureScale.getRangeIndex(scalePos, reverseEnd) + 1) / (float)TemperatureScale.getTemperatureRange(scalePos).getRangeSize();
    }

    public static boolean isScalePosInRange(int scalePos, TemperatureRange startRange, TemperatureRange endRange) {
        return scalePos >= rangeStarts[startRange.ordinal()] && scalePos <= rangeStarts[endRange.ordinal()] + endRange.rangeSize - 1;
    }

    public static boolean isScalePosInRange(int scalePos, TemperatureRange range) {
        return TemperatureScale.isScalePosInRange(scalePos, range, range);
    }

    public static int getRangeStart(TemperatureRange range) {
        return rangeStarts[range.ordinal()];
    }

    public static int getScaleTotal() {
        return scaleTotal;
    }

    private static int generateTotalScale() {
        int totalRange = 0;
        for (TemperatureRange range : TemperatureRange.values()) {
            totalRange += range.getRangeSize();
        }
        return totalRange - 1;
    }

    private static int[] generateRangeStarts() {
        int[] generatedStarts = new int[TemperatureRange.values().length];
        for (int index = 0; index < TemperatureRange.values().length; ++index) {
            if (index > 0) {
                TemperatureRange previousRange = TemperatureRange.values()[index - 1];
                generatedStarts[index] = generatedStarts[previousRange.ordinal()] + previousRange.rangeSize;
                continue;
            }
            generatedStarts[index] = 0;
        }
        return generatedStarts;
    }

    public static enum TemperatureRange {
        ICY(6),
        COOL(5),
        MILD(4),
        WARM(5),
        HOT(6);

        private int rangeSize;

        private TemperatureRange(int rangeSize) {
            this.rangeSize = rangeSize;
        }

        public int getRangeSize() {
            return this.rangeSize;
        }
    }

}

