/*
 * Decompiled with CFR 0.148.
 */
package toughasnails.api.config;

import toughasnails.api.config.ISyncedOption;

public enum GameplayOption implements ISyncedOption
{
    ENABLE_LOWERED_STARTING_HEALTH("Enable Lowered Starting Health"),
    ENABLE_THIRST("Enable Thirst"),
    ENABLE_TEMPERATURE("Enable Body Temperature"),
    ENABLE_SEASONS("Enable Seasons");

    private final String optionName;

    private GameplayOption(String name) {
        this.optionName = name;
    }

    @Override
    public String getOptionName() {
        return this.optionName;
    }
}

