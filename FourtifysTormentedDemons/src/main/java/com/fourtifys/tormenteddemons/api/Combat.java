package com.fourtifys.tormenteddemons.api;

import com.fourtifys.tormenteddemons.data.CombatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;

public class Combat
{
    public static final int[] DEMONS = {13599, 13600};
    public static final int MELEE_ATTACK = 11392;
    public static final int RANGED_ATTACK = 11389;
    public static final int MAGIC_ATTACK = 11388;
    public static final int FIRE_BOMB = 2855;
    public static final int[] BIND = {177, 178, 179};

    public static CombatStyle demonPrayer(short[] sprites)
    {
        if (sprites == null)
        {
            return null;
        }
        for (short sprite : sprites)
        {
            if (sprite != -1)
            {
                return CombatStyle.fromHeadIcon(sprite);
            }
        }
        return null;
    }

    public static CombatStyle chooseStyle(List<CombatStyle> armed, CombatStyle demonPrayer)
    {
        return armed.stream().filter(style -> style != demonPrayer).findFirst().orElse(null);
    }

    public static CombatStyle afterSpecial(CombatStyle last, boolean approaching, int stillTicks, int distance)
    {
        if (approaching)
        {
            return CombatStyle.MELEE;
        }
        if (stillTicks >= 3 && distance >= 2)
        {
            return last == CombatStyle.MAGIC ? CombatStyle.RANGED : CombatStyle.MAGIC;
        }
        return null;
    }

    public static WorldPoint safeTile(WorldPoint player, Set<WorldPoint> bombs, WorldArea demon, Predicate<WorldPoint> reachable)
    {
        WorldPoint best = null;
        int bestScore = Integer.MAX_VALUE;
        for (int dx = -2; dx <= 2; dx++)
        {
            for (int dy = -2; dy <= 2; dy++)
            {
                WorldPoint tile = player.dx(dx).dy(dy);
                if (bombs.contains(tile) || demon != null && demon.contains(tile) || !reachable.test(tile))
                {
                    continue;
                }
                int score = Math.max(Math.abs(dx), Math.abs(dy)) * 100 + (demon == null ? 0 : demon.distanceTo(tile));
                if (score < bestScore)
                {
                    best = tile;
                    bestScore = score;
                }
            }
        }
        return best;
    }

    public static String leaveReason(int food, int hitpointsPercent, int eatAt, int emergencyAt, int restores, int prayer, int restoreAt, boolean fullWithLoot)
    {
        if (hitpointsPercent <= emergencyAt)
        {
            return "Hitpoints low";
        }
        if (food == 0 && hitpointsPercent <= eatAt)
        {
            return "Out of food";
        }
        if (restores == 0 && prayer <= restoreAt)
        {
            return "Out of prayer restores";
        }
        if (fullWithLoot)
        {
            return "Inventory full";
        }
        return null;
    }

    public static boolean isDemon(int npcId)
    {
        return Arrays.stream(DEMONS).anyMatch(id -> id == npcId);
    }
}
