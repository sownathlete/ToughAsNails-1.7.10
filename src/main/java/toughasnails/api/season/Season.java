/*
 * Decompiled with CFR 0.148.
 */
package toughasnails.api.season;

public enum Season {
    SPRING,
    SUMMER,
    AUTUMN,
    WINTER;


    public static enum SubSeason {
        EARLY_SPRING(SPRING, 7831687, 0.85f, 7307663, 0.85f),
        MID_SPRING(SPRING, 7307663, 6259871),
        LATE_SPRING(SPRING, 6783639, 4164031),
        EARLY_SUMMER(SUMMER, 7569547, 6259871),
        MID_SUMMER(SUMMER, 16777215, 16777215),
        LATE_SUMMER(SUMMER, 8877943, 10444639),
        EARLY_AUTUMN(AUTUMN, 9400175, 12011335),
        MID_AUTUMN(AUTUMN, 10444639, 13578031),
        LATE_AUTUMN(AUTUMN, 11489103, 0.85f, 12533567, 0.85f),
        EARLY_WINTER(WINTER, 10444639, 0.6f, 10966871, 0.6f),
        MID_WINTER(WINTER, 9400175, 0.45f, 10444639, 0.45f),
        LATE_WINTER(WINTER, 16777215, 0.6f, 9400175, 0.6f);

        private Season season;
        private int grassOverlay;
        private float grassSaturationMultiplier;
        private int foliageOverlay;
        private float foliageSaturationMultiplier;

        private SubSeason(Season season, int grassColour, float grassSaturation, int foliageColour, float foliageSaturation) {
            this.season = season;
            this.grassOverlay = grassColour;
            this.grassSaturationMultiplier = grassSaturation;
            this.foliageOverlay = foliageColour;
            this.foliageSaturationMultiplier = foliageSaturation;
        }

        private SubSeason(Season season, int grassColour, int foliageColour) {
            this(season, grassColour, -1.0f, foliageColour, -1.0f);
        }

        public Season getSeason() {
            return this.season;
        }

        public int getGrassOverlay() {
            return this.grassOverlay;
        }

        public float getGrassSaturationMultiplier() {
            return this.grassSaturationMultiplier;
        }

        public int getFoliageOverlay() {
            return this.foliageOverlay;
        }

        public float getFoliageSaturationMultiplier() {
            return this.foliageSaturationMultiplier;
        }
    }

}

