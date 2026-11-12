package com.fourtifys.tormenteddemons.api;

import com.fourtifys.tormenteddemons.TormentedDemonsConfig;
import com.fourtifys.tormenteddemons.data.Places;
import com.tonic.Static;
import com.tonic.api.game.MovementAPI;
import com.tonic.api.game.QuestAPI;
import com.tonic.api.game.SkillAPI;
import com.tonic.api.widgets.EquipmentAPI;
import com.tonic.api.widgets.InventoryAPI;
import com.tonic.data.wrappers.ItemEx;
import com.tonic.data.wrappers.PlayerEx;
import java.util.Random;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import net.runelite.api.WorldType;

public class TormentedDemonsAPI
{
    public static String missingRequirement(TormentedDemonsConfig config)
    {
        Client client = Static.getClient();
        boolean members = Static.invoke(() -> client.getWorldType().contains(WorldType.MEMBERS));
        String badSupply = Supplies.badRestock(config.restockItems());
        if (badSupply != null)
        {
            return "Write extra supplies as name:quantity, not \"" + badSupply + "\"";
        }
        return missingRequirement(members, QuestAPI.isCompleted(Quest.WHILE_GUTHIX_SLEEPS), Supplies.armedStyles(config).size(), Supplies.hasRing());
    }

    public static String missingRequirement(boolean members, boolean whileGuthixSleeps, int armedStyles, boolean ring)
    {
        if (!members)
        {
            return "Tormented Demons need a members world";
        }
        if (!whileGuthixSleeps)
        {
            return "Complete While Guthix Sleeps to reach the Tormented Demons";
        }
        if (armedStyles < 2)
        {
            return "Set up and carry or wear gear for two combat styles";
        }
        if (!ring)
        {
            return "Carry or wear a ring of dueling to reach the bank";
        }
        return null;
    }

    public static boolean inTemple()
    {
        return Places.inTemple(PlayerEx.getLocal().getWorldPoint());
    }

    public static boolean teleportOut()
    {
        ItemEx worn = EquipmentAPI.getItem(item -> item.getName() != null && item.getName().startsWith(Supplies.RING));
        if (worn != null)
        {
            EquipmentAPI.interact(worn, Places.FEROX);
            return true;
        }
        ItemEx carried = Supplies.ring();
        if (carried != null)
        {
            InventoryAPI.interact(carried, "Rub");
            return true;
        }
        return false;
    }

    public static int hitpointsPercent()
    {
        return SkillAPI.getBoostedLevel(Skill.HITPOINTS) * 100 / Math.max(1, SkillAPI.getLevel(Skill.HITPOINTS));
    }

    public static int prayer()
    {
        return SkillAPI.getBoostedLevel(Skill.PRAYER);
    }

    public static boolean restored(int hitpoints, int maxHitpoints, int prayer, int maxPrayer, int runEnergy)
    {
        return hitpoints >= maxHitpoints && prayer >= maxPrayer && runEnergy >= 100;
    }

    public static boolean restored()
    {
        return restored(SkillAPI.getBoostedLevel(Skill.HITPOINTS), SkillAPI.getLevel(Skill.HITPOINTS), prayer(),
                SkillAPI.getLevel(Skill.PRAYER), MovementAPI.getRunEnergy());
    }

    public static boolean readyToTravel(TormentedDemonsConfig config)
    {
        return Places.nearChasm(PlayerEx.getLocal().getWorldPoint())
                || InventoryAPI.count(Supplies.TEMPLE_TELEPORT) > 0 && Supplies.foodCount(config) > 0 && !Supplies.restores(config).isEmpty()
                && hitpointsPercent() > config.emergencyAt();
    }

    public static int rollDelay(Random random, TormentedDemonsConfig config)
    {
        double value = config.tickDelayWeightedDistribution()
                ? -Math.log(Math.abs(random.nextGaussian())) * config.tickDelayDeviation() + config.tickDelayTarget()
                : Math.round(random.nextGaussian() * config.tickDelayDeviation() + config.tickDelayTarget());
        return (int) Math.max(config.tickDelayMin(), Math.min(config.tickDelayMax(), value));
    }

    public static String pauseStatus(int remaining)
    {
        return "Short pause (" + remaining + (remaining == 1 ? " tick)" : " ticks)");
    }
}
