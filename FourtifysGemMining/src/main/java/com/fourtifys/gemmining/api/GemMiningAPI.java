package com.fourtifys.gemmining.api;

import com.fourtifys.gemmining.GemMiningConfig;
import com.fourtifys.gemmining.data.Mine;
import com.tonic.Static;
import com.tonic.api.entities.TileObjectAPI;
import com.tonic.api.game.QuestAPI;
import com.tonic.api.game.SkillAPI;
import com.tonic.api.game.VarAPI;
import com.tonic.api.widgets.EquipmentAPI;
import com.tonic.api.widgets.InventoryAPI;
import com.tonic.data.wrappers.ItemEx;
import com.tonic.data.wrappers.PlayerEx;
import com.tonic.data.wrappers.TileObjectEx;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import net.runelite.api.WorldType;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.ObjectID;

public class GemMiningAPI
{
    public static final int GEM_ROCK_LEVEL = 40;
    public static final int UNDERGROUND_REGION = 11410;
    public static final int KARAMJA_MEDIUM_REWARD_VARBIT = 3598;
    public static final int[] GEM_ROCKS = {ObjectID.GEMROCK1, ObjectID.GEMROCK};
    public static final int[] GEMS = {
            ItemID.UNCUT_OPAL,
            ItemID.UNCUT_JADE,
            ItemID.UNCUT_RED_TOPAZ,
            ItemID.UNCUT_SAPPHIRE,
            ItemID.UNCUT_EMERALD,
            ItemID.UNCUT_RUBY,
            ItemID.UNCUT_DIAMOND,
            ItemID.UNCUT_DRAGONSTONE
    };
    public static final Map<Integer, Integer> PICKAXES = Map.ofEntries(
            Map.entry(ItemID.BRONZE_PICKAXE, 1),
            Map.entry(ItemID.IRON_PICKAXE, 1),
            Map.entry(ItemID.STEEL_PICKAXE, 6),
            Map.entry(ItemID.BLACK_PICKAXE, 11),
            Map.entry(ItemID.MITHRIL_PICKAXE, 21),
            Map.entry(ItemID.ADAMANT_PICKAXE, 31),
            Map.entry(ItemID.RUNE_PICKAXE, 41),
            Map.entry(ItemID.TRAIL_GILDED_PICKAXE, 41),
            Map.entry(ItemID.DRAGON_PICKAXE, 61),
            Map.entry(ItemID.DRAGON_PICKAXE_PRETTY, 61),
            Map.entry(ItemID.ZALCANO_PICKAXE, 61),
            Map.entry(ItemID._3A_PICKAXE, 61),
            Map.entry(ItemID.INFERNAL_PICKAXE, 61),
            Map.entry(ItemID.INFERNAL_PICKAXE_EMPTY, 61),
            Map.entry(ItemID.TRAILBLAZER_PICKAXE, 61),
            Map.entry(ItemID.TRAILBLAZER_PICKAXE_EMPTY, 61),
            Map.entry(ItemID.TRAILBLAZER_PICKAXE_NO_INFERNAL, 61),
            Map.entry(ItemID.TRAILBLAZER_RELOADED_PICKAXE, 61),
            Map.entry(ItemID.TRAILBLAZER_RELOADED_PICKAXE_EMPTY, 61),
            Map.entry(ItemID.TRAILBLAZER_RELOADED_PICKAXE_NO_INFERNAL, 61),
            Map.entry(ItemID.CRYSTAL_PICKAXE, 71)
    );
    private static final Pattern GEM_MESSAGE = Pattern.compile(
            "You just (mined|found) (a|an|a piece of) (Opal|Jade|Red Topaz|Sapphire|Emerald|Ruby|Diamond|Dragonstone)!");

    public static String missingRequirement(Mine mine)
    {
        Client client = Static.getClient();
        boolean members = Static.invoke(() -> client.getWorldType().contains(WorldType.MEMBERS));
        Set<Integer> carried = Stream.concat(InventoryAPI.getItems().stream(), EquipmentAPI.search().collect().stream())
                .map(ItemEx::getId)
                .collect(Collectors.toSet());
        int level = SkillAPI.getLevel(Skill.MINING);
        return missingRequirement(mine, members, level, QuestAPI.isCompleted(Quest.SHILO_VILLAGE),
                mine != Mine.UNDERGROUND || canEnterUnderground(), hasPickaxe(carried, level));
    }

    public static String missingRequirement(Mine mine, boolean members, int miningLevel, boolean shiloVillage,
                                            boolean mineAccess, boolean pickaxe)
    {
        if (!members)
        {
            return "Gem mining needs a members world";
        }
        if (miningLevel < GEM_ROCK_LEVEL)
        {
            return "Gem rocks need level " + GEM_ROCK_LEVEL + " Mining";
        }
        if (!shiloVillage)
        {
            return "Complete Shilo Village to reach the gem mine";
        }
        if (!mineAccess)
        {
            return "The " + mine + " mine needs the Karamja medium diary reward. Claim it from Pirate Jackie the Fruit";
        }
        if (!pickaxe)
        {
            return "Carry or wear a pickaxe you can use";
        }
        return null;
    }

    public static boolean hasPickaxe(Set<Integer> carried, int miningLevel)
    {
        return carried.stream().anyMatch(id -> PICKAXES.getOrDefault(id, Integer.MAX_VALUE) <= miningLevel);
    }

    private static boolean canEnterUnderground()
    {
        if (isUnderground())
        {
            return true;
        }
        TileObjectEx ladder = TileObjectAPI.search().withId(ObjectID.ATJUN_LADDER_MULTI).first();
        return ladder != null ? ladder.hasAction("Climb-down") : VarAPI.getVar(KARAMJA_MEDIUM_REWARD_VARBIT) == 1;
    }

    public static boolean isUnderground()
    {
        return PlayerEx.getLocal().getWorldPoint().getRegionID() == UNDERGROUND_REGION;
    }

    public static boolean isGem(int itemId)
    {
        return Arrays.stream(GEMS).anyMatch(gem -> gem == itemId);
    }

    public static boolean tripFull()
    {
        return InventoryAPI.isFull() && InventoryAPI.containsAny(GEMS);
    }

    public static boolean isGemMessage(String message)
    {
        return GEM_MESSAGE.matcher(message).matches();
    }

    public static int rollDelay(Random random, GemMiningConfig config)
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
