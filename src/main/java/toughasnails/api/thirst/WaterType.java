/*
 * Decompiled with CFR 0.148.
 */
package toughasnails.api.thirst;

public enum WaterType {
    DIRTY("Dirty Water", 3, 0.1f, 0.8f),
    FILTERED("Filtered Water", 5, 0.25f, 0.4f),
    CLEAN("Clean Water", 7, 0.5f, 0.0f);

    private String description;
    private int thirst;
    private float hydration;
    private float poisonChance;

    private WaterType(String description, int thirst, float hydration, float poisonChance) {
        this.description = description;
        this.thirst = thirst;
        this.hydration = hydration;
        this.poisonChance = poisonChance;
    }

    public String getDescription() {
        return this.description;
    }

    public int getThirst() {
        return this.thirst;
    }

    public float getHydration() {
        return this.hydration;
    }

    public float getPoisonChance() {
        return this.poisonChance;
    }
}

