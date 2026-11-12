package com.fourtifys.tormenteddemons;

import com.fourtifys.tormenteddemons.data.Room;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(TormentedDemonsConfig.GROUP)
public interface TormentedDemonsConfig extends Config
{
    String GROUP = "fourtifyTormentedDemons";
    String RUN = "run";

    @ConfigSection(name = "General", description = "The room, offensive prayer and the gear for each combat style. Set at least two styles.", position = 20, closedByDefault = false)
    String generalSection = "general";

    @ConfigSection(name = "Supplies", description = "Food and prayer restores to bring and when to use them.", position = 40, closedByDefault = true)
    String suppliesSection = "supplies";

    @ConfigSection(name = "Loot", description = "Which drops to pick up.", position = 50, closedByDefault = true)
    String lootSection = "loot";

    @ConfigSection(name = "Emergency teleport", description = "When to leave for the bank at once.", position = 60, closedByDefault = true)
    String emergencySection = "emergency";

    @ConfigSection(name = "Timing", description = "Adjust action delays. One game tick is about 600 milliseconds.", position = 80, closedByDefault = true)
    String timingSection = "advancedTiming";

    @ConfigItem(keyName = "enableUI", name = "Show overlay", description = "Show runtime, kills and status. The status line is the only place the plugin explains why it is waiting, so keep it on if the plugin seems stuck.", position = 0)
    default boolean enableUI()
    {
        return true;
    }

    @ConfigItem(keyName = RUN, name = "Run", description = "Switch on to start, and off to stop. It switches off whenever the plugin is enabled, so the plugin never starts on its own.", position = 1)
    default boolean run()
    {
        return false;
    }

    @ConfigItem(keyName = "room", name = "Room", description = "The temple room to fight in. The plugin walks there and attacks only the demons in it.", position = 0, section = generalSection)
    default Room room()
    {
        return Room.PLINTH_CHAMBER;
    }

    @ConfigItem(keyName = "offensivePrayer", name = "Offensive prayer", description = "Use the best attack prayer you have for the style you attack with, such as Piety, Rigour or Augury once unlocked.", position = 1, section = generalSection)
    default boolean offensivePrayer()
    {
        return false;
    }

    @ConfigItem(keyName = "meleeGear", name = "Melee gear", description = "Item names or ids for melee, separated by commas. Leave empty to not use melee.", position = 2, section = generalSection)
    default String meleeGear()
    {
        return "";
    }

    @ConfigItem(keyName = "rangedGear", name = "Ranged gear", description = "Item names or ids for ranged, separated by commas. Leave empty to not use ranged.", position = 3, section = generalSection)
    default String rangedGear()
    {
        return "";
    }

    @ConfigItem(keyName = "magicGear", name = "Magic gear", description = "Item names or ids for magic, separated by commas. Leave empty to not use magic.", position = 4, section = generalSection)
    default String magicGear()
    {
        return "";
    }

    @ConfigItem(keyName = "food", name = "Food", description = "The food to eat and to take from the bank.", position = 0, section = suppliesSection)
    default String food()
    {
        return "Shark";
    }

    @Range(min = 1, max = 28)
    @ConfigItem(keyName = "foodCount", name = "Food to bring", description = "How much food to have when leaving the bank.", position = 1, section = suppliesSection)
    default int foodCount()
    {
        return 10;
    }

    @Range(min = 1, max = 99)
    @ConfigItem(keyName = "eatAt", name = "Eat at Hitpoints %", description = "Eat when Hitpoints fall to this share of the maximum.", position = 2, section = suppliesSection)
    default int eatAt()
    {
        return 55;
    }

    @ConfigItem(keyName = "restore", name = "Prayer restore", description = "The full-dose potion to drink and to take from the bank, such as Prayer potion(4).", position = 3, section = suppliesSection)
    default String restore()
    {
        return "Prayer potion(4)";
    }

    @Range(min = 1, max = 28)
    @ConfigItem(keyName = "restoreCount", name = "Restores to bring", description = "How many prayer restores to have when leaving the bank.", position = 4, section = suppliesSection)
    default int restoreCount()
    {
        return 6;
    }

    @Range(min = 1, max = 98)
    @ConfigItem(keyName = "restoreAt", name = "Restore at Prayer points", description = "Drink a restore when Prayer points fall to this.", position = 5, section = suppliesSection)
    default int restoreAt()
    {
        return 25;
    }

    @Range(min = 1, max = 100)
    @ConfigItem(keyName = "scrollCount", name = "Temple teleports to bring", description = "How many Guthixian temple teleports to have when leaving the bank.", position = 6, section = suppliesSection)
    default int scrollCount()
    {
        return 5;
    }

    @ConfigItem(keyName = "keepItems", name = "Other items to keep", description = "Item names or ids, separated by commas, that banking leaves in the inventory, such as a rune pouch.", position = 7, section = suppliesSection)
    default String keepItems()
    {
        return "";
    }

    @ConfigItem(keyName = "restockItems", name = "Extra supplies", description = "Items to take from the bank as name:quantity, separated by commas, such as Death rune:500. An id works in place of the name.", position = 8, section = suppliesSection)
    default String restockItems()
    {
        return "";
    }

    @Range(min = 0, max = 100000000)
    @ConfigItem(keyName = "lootMinimum", name = "Minimum value", description = "Pick up drops worth at least this many coins at Grand Exchange prices. Drops without a known price are taken only from the list below.", position = 0, section = lootSection)
    default int lootMinimum()
    {
        return 5000;
    }

    @ConfigItem(keyName = "lootAlways", name = "Always take", description = "Item names or ids, separated by commas, to pick up whatever their value.", position = 1, section = lootSection)
    default String lootAlways()
    {
        return "Tormented synapse, Burning claw, Guthixian temple teleport";
    }

    @Range(min = 1, max = 99)
    @ConfigItem(keyName = "emergencyAt", name = "At or below Hitpoints %", description = "Teleport to Ferox Enclave with the ring of dueling when Hitpoints fall to this share of the maximum, even with food left.", position = 0, section = emergencySection)
    default int emergencyAt()
    {
        return 25;
    }

    @Range(min = 0, max = 10)
    @ConfigItem(keyName = "tickDelayMin", name = "Minimum delay (ticks)", description = "Lowest action delay, in game ticks.", position = 0, section = timingSection)
    default int tickDelayMin()
    {
        return 0;
    }

    @Range(min = 0, max = 10)
    @ConfigItem(keyName = "tickDelayMax", name = "Maximum delay (ticks)", description = "Highest action delay, in game ticks.", position = 1, section = timingSection)
    default int tickDelayMax()
    {
        return 1;
    }

    @Range(min = 0, max = 10)
    @ConfigItem(keyName = "tickDelayTarget", name = "Target delay (ticks)", description = "Target value used to generate random action delays, in game ticks.", position = 2, section = timingSection)
    default int tickDelayTarget()
    {
        return 1;
    }

    @Range(min = 0, max = 10)
    @ConfigItem(keyName = "tickDelayDeviation", name = "Delay variation (ticks)", description = "Amount of random variation around the target delay, in game ticks.", position = 3, section = timingSection)
    default int tickDelayDeviation()
    {
        return 0;
    }

    @ConfigItem(keyName = "tickDelayWeightedDistribution", name = "Weighted distribution", description = "Change how random delays are distributed within the minimum and maximum.", position = 4, section = timingSection)
    default boolean tickDelayWeightedDistribution()
    {
        return false;
    }
}
