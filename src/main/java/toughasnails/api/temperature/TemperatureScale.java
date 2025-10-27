// File: toughasnails/api/temperature/TemperatureScale.java
package toughasnails.api.temperature;

public final class TemperatureScale {

    // Build these via helper methods to avoid static init order pitfalls.
    private static final int   scaleTotal  = generateTotalScale();
    private static final int[] rangeStarts = generateRangeStarts();

    private TemperatureScale() {}

    public static TemperatureRange getTemperatureRange(int scalePos) {
        if (scalePos < 0 || scalePos > scaleTotal) return null;
        for (TemperatureRange range : TemperatureRange.values()) {
            int start = rangeStarts[range.ordinal()];
            if (scalePos <= start + range.getRangeSize() - 1) return range;
        }
        // Should never happen if inputs are clamped
        throw new RuntimeException("Could not find range for value " + scalePos + " (scaleTotal=" + scaleTotal + ").");
    }

    public static int getRangeIndex(int scalePos, boolean reverseEnd) {
        TemperatureRange tr = getTemperatureRange(scalePos);
        int start = rangeStarts[tr.ordinal()];
        int idx   = scalePos - start; // 0..size-1 within this range
        return Math.abs((reverseEnd ? tr.getRangeSize() - 1 : 0) - idx);
    }

    public static float getRangeDelta(int scalePos, boolean reverseEnd) {
        TemperatureRange tr = getTemperatureRange(scalePos);
        return (getRangeIndex(scalePos, reverseEnd) + 1) / (float) tr.getRangeSize();
    }

    public static boolean isScalePosInRange(int scalePos, TemperatureRange startRange, TemperatureRange endRange) {
        int lo = rangeStarts[startRange.ordinal()];
        int hi = rangeStarts[endRange.ordinal()] + endRange.getRangeSize() - 1;
        return scalePos >= lo && scalePos <= hi;
    }

    public static boolean isScalePosInRange(int scalePos, TemperatureRange range) {
        return isScalePosInRange(scalePos, range, range);
    }

    public static int getRangeStart(TemperatureRange range) {
        return rangeStarts[range.ordinal()];
    }

    public static int getScaleTotal() {
        return scaleTotal;
    }

    /* ===================== helpers ===================== */

    private static int generateTotalScale() {
        int total = 0;
        for (TemperatureRange range : TemperatureRange.values()) {
            total += range.getRangeSize();
        }
        // Total positions are 0..(total-1)
        return total - 1;
    }

    private static int[] generateRangeStarts() {
        TemperatureRange[] vals = TemperatureRange.values();
        int[] starts = new int[vals.length]; // <-- local array; DO NOT touch the static field during init
        for (int i = 0; i < vals.length; i++) {
            if (i == 0) {
                starts[i] = 0;
            } else {
                TemperatureRange prev = vals[i - 1];
                starts[i] = starts[prev.ordinal()] + prev.getRangeSize();
            }
        }
        return starts;
    }

    public static enum TemperatureRange {
        ICY(6),
        COOL(5),
        MILD(4),
        WARM(5),
        HOT(6);

        private final int rangeSize;

        private TemperatureRange(int rangeSize) {
            this.rangeSize = rangeSize;
        }

        public int getRangeSize() {
            return this.rangeSize;
        }
    }
}
