package com.fourtifys.gemmining;

import com.fourtifys.gemmining.data.Mine;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(GemMiningConfig.GROUP)
public interface GemMiningConfig extends Config
{
    String GROUP = "fourtifyGemMining";
    String RUN = "run";

    @ConfigSection(name = "General", description = "Choose a mine and how to store gems.", position = 20, closedByDefault = false)
    String generalSection = "general";

    @ConfigSection(name = "Timing", description = "Adjust action delays. One game tick is about 600 milliseconds.", position = 80, closedByDefault = true)
    String timingSection = "advancedTiming";

    @ConfigItem(keyName = "enableUI", name = "Show overlay", description = "Show runtime, mine, gems and status. The status line is the only place the plugin explains why it is waiting, so keep it on if the plugin seems stuck.", position = 0)
    default boolean enableUI()
    {
        return true;
    }

    @ConfigItem(keyName = RUN, name = "Run", description = "Switch on to start, and off to stop. It switches off whenever the plugin is enabled, so the plugin never starts on its own.", position = 1)
    default boolean run()
    {
        return false;
    }

    @ConfigItem(keyName = "mine", name = "Mine", description = "The Shilo Village gem mine to work.", position = 0, section = generalSection)
    default Mine mine()
    {
        return Mine.SURFACE;
    }

    @ConfigItem(keyName = "useGemBag", name = "Use gem bag", description = "Fill a carried gem bag when the inventory is full and empty it at the bank.", position = 1, section = generalSection)
    default boolean useGemBag()
    {
        return true;
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
