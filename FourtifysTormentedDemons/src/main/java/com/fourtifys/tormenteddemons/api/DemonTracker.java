package com.fourtifys.tormenteddemons.api;

import com.fourtifys.tormenteddemons.data.CombatStyle;
import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

public class DemonTracker
{
    @Getter
    private Integer demon;
    @Getter
    private CombatStyle style;
    private CombatStyle prayer;
    private boolean special;
    private WorldPoint lastTile;
    private int stillTicks;

    public CombatStyle track(int index, WorldPoint tile, int animation, boolean specialSeen, int distance)
    {
        if (demon == null || demon != index)
        {
            demon = index;
            style = null;
            prayer = null;
            special = false;
        }
        WorldPoint last = lastTile == null ? tile : lastTile;
        stillTicks = tile.equals(last) ? stillTicks + 1 : 0;
        lastTile = tile;
        CombatStyle seen = CombatStyle.fromAnimation(animation);
        if (seen != null)
        {
            style = seen;
            special = false;
        }
        else if (specialSeen)
        {
            special = true;
        }
        else if (special)
        {
            CombatStyle guess = Combat.afterSpecial(style, !tile.equals(last) && distance > 1, stillTicks, distance);
            if (guess != null)
            {
                style = guess;
            }
        }
        return style;
    }

    public CombatStyle prayer(CombatStyle shown)
    {
        if (shown != null)
        {
            prayer = shown;
        }
        return prayer;
    }
}
