/*
 * Decompiled with CFR 0.148.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.stats.Achievement
 *  net.minecraftforge.common.AchievementPage
 */
package toughasnails.init;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;
import toughasnails.api.TANBlocks;
import toughasnails.api.achievement.TANAchievements;
import toughasnails.api.item.TANItems;

public class ModAchievements {
    public static final AchievementPage achievementPage = new AchievementPage("Tough As Nails", new Achievement[0]);

    public static void init() {
        AchievementPage.registerAchievementPage((AchievementPage)achievementPage);
        ModAchievements.addAchievements();
    }

    private static void addAchievements() {
        TANAchievements.thirst_quencher = ModAchievements.addAchievement("achievement.thirst_quencher", "thirst_quencher", 0, 0, new ItemStack(TANItems.canteen), null);
        TANAchievements.thirst_ender = ModAchievements.addAchievement("achievement.thirst_ender", "thirst_ender", -2, -2, new ItemStack(TANItems.fruit_juice), TANAchievements.thirst_quencher);
        TANAchievements.campfire_song = ModAchievements.addAchievement("achievement.campfire_song", "campfire_song", -1, 2, new ItemStack(TANBlocks.campfire), TANAchievements.thirst_quencher);
        TANAchievements.life_or_death = ModAchievements.addAchievement("achievement.life_or_death", "life_or_death", 1, 3, new ItemStack(TANItems.lifeblood_crystal), TANAchievements.campfire_song);
        TANAchievements.into_ice = ModAchievements.addAchievement("achievement.into_ice", "into_ice", -3, 1, new ItemStack(TANItems.freeze_rod), TANAchievements.campfire_song);
        TANAchievements.hot_or_cold = ModAchievements.addAchievement("achievement.hot_or_cold", "hot_or_cold", -4, -1, new ItemStack(TANItems.thermometer), TANAchievements.into_ice);
        TANAchievements.that_time_of_year = ModAchievements.addAchievement("achievement.that_time_of_year", "that_time_of_year", 3, -1, new ItemStack(TANItems.season_clock), TANAchievements.thirst_quencher);
        TANAchievements.year_one = ModAchievements.addAchievement("achievement.year_one", "year_one", 0, -4, new ItemStack(TANItems.tan_icon), TANAchievements.that_time_of_year).setSpecial();
    }

    private static Achievement addAchievement(String unlocalizedName, String identifier, int column, int row, ItemStack iconStack, Achievement parent) {
        Achievement achievement = new Achievement(unlocalizedName, identifier, column, row, iconStack, parent);
        achievement.registerStat();
        achievementPage.getAchievements().add(achievement);
        return achievement;
    }
}

