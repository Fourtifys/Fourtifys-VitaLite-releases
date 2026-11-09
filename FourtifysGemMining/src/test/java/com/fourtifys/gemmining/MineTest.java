package com.fourtifys.gemmining;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fourtifys.gemmining.data.Mine;
import net.runelite.api.coords.WorldPoint;
import org.junit.jupiter.api.Test;

class MineTest
{
    private static final int[][] SURFACE_ROCKS = {
            {2820, 2998}, {2825, 3001}, {2821, 2998}, {2821, 3000}, {2823, 2999}, {2823, 3002}, {2825, 3003}
    };
    private static final int[][] UNDERGROUND_CORNERS = {
            {2826, 9389}, {2851, 9377}, {2832, 9399}, {2857, 9385}, {2857, 9382}
    };

    @Test
    void eachMineHoldsItsRocks()
    {
        for (int[] rock : SURFACE_ROCKS)
        {
            assertTrue(Mine.SURFACE.getRocks().contains(new WorldPoint(rock[0], rock[1], 0)));
            assertFalse(Mine.UNDERGROUND.getRocks().contains(new WorldPoint(rock[0], rock[1], 0)));
        }
        for (int[] rock : UNDERGROUND_CORNERS)
        {
            assertTrue(Mine.UNDERGROUND.getRocks().contains(new WorldPoint(rock[0], rock[1], 0)));
        }
    }

    @Test
    void theLadderAndTheDepositObjectsAreWhereThePlayerWalks()
    {
        assertTrue(Mine.SURFACE.getRocks().contains(new WorldPoint(2825, 2998, 0)));
        assertTrue(Mine.UNDERGROUND.getRocks().contains(new WorldPoint(2838, 9387, 0)));
        assertTrue(Mine.SURFACE.getBank().contains(new WorldPoint(2852, 2951, 0)));
        assertTrue(Mine.UNDERGROUND.getBank().contains(new WorldPoint(2842, 9382, 0)));
        assertTrue(Mine.UNDERGROUND.isUnderground() && !Mine.SURFACE.isUnderground());
    }
}
