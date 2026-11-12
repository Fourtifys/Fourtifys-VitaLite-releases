package com.fourtifys.tormenteddemons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fourtifys.tormenteddemons.api.Combat;
import com.fourtifys.tormenteddemons.api.DemonTracker;
import com.fourtifys.tormenteddemons.data.CombatStyle;
import net.runelite.api.coords.WorldPoint;
import org.junit.jupiter.api.Test;

class DemonTrackerTest
{
    private static final WorldPoint TILE = new WorldPoint(4130, 4380, 0);

    @Test
    void theLastAttackAnimationIsTheStyleUntilAnotherDemonIsFought()
    {
        DemonTracker tracker = new DemonTracker();
        assertEquals(CombatStyle.RANGED, tracker.track(1, TILE, Combat.RANGED_ATTACK, false, 3));
        assertEquals(CombatStyle.RANGED, tracker.track(1, TILE, -1, false, 3));
        assertEquals(CombatStyle.MAGIC, tracker.prayer(CombatStyle.MAGIC));
        assertEquals(CombatStyle.MAGIC, tracker.prayer(null));
        assertNull(tracker.track(2, TILE, -1, false, 3));
        assertNull(tracker.prayer(null));
        assertEquals(2, tracker.getDemon());
    }

    @Test
    void afterTheSpecialTheDemonsMovementSetsTheNextStyle()
    {
        DemonTracker approaching = new DemonTracker();
        approaching.track(1, TILE, Combat.MAGIC_ATTACK, false, 3);
        approaching.track(1, TILE, -1, true, 3);
        assertEquals(CombatStyle.MELEE, approaching.track(1, TILE.dx(-1), -1, false, 2));

        DemonTracker standing = new DemonTracker();
        standing.track(1, TILE, Combat.MELEE_ATTACK, false, 3);
        standing.track(1, TILE, -1, true, 3);
        assertEquals(CombatStyle.MAGIC, standing.track(1, TILE, -1, false, 3));
    }
}
