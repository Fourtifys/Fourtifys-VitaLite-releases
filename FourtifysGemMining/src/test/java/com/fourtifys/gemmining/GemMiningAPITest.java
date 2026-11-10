package com.fourtifys.gemmining;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fourtifys.gemmining.api.GemMiningAPI;
import com.fourtifys.gemmining.data.Mine;
import com.fourtifys.gemmining.data.State;
import java.util.Random;
import java.util.Set;
import net.runelite.api.gameval.ItemID;
import org.junit.jupiter.api.Test;

class GemMiningAPITest
{
    @Test
    void theRunSwitchIsOffUntilThePlayerSwitchesItOn()
    {
        assertFalse(new GemMiningConfig()
        {
        }.run());
    }

    @Test
    void everyGemLineCountsAndOtherLinesDoNot()
    {
        assertTrue(GemMiningAPI.isGemMessage("You just mined an Opal!"));
        assertTrue(GemMiningAPI.isGemMessage("You just mined a piece of Jade!"));
        assertTrue(GemMiningAPI.isGemMessage("You just mined a Red Topaz!"));
        assertTrue(GemMiningAPI.isGemMessage("You just mined a Diamond!"));
        assertTrue(GemMiningAPI.isGemMessage("You just found a Sapphire!"));
        assertTrue(GemMiningAPI.isGemMessage("You just found an Emerald!"));
        assertFalse(GemMiningAPI.isGemMessage("You put it straight into your open gem bag."));
        assertFalse(GemMiningAPI.isGemMessage("You just found a clue geode!"));
        assertFalse(GemMiningAPI.isGemMessage("You manage to mine some copper."));
    }

    @Test
    void requirementsAreReportedInOrder()
    {
        assertEquals("Gem mining needs a members world",
                GemMiningAPI.missingRequirement(Mine.SURFACE, false, 30, false, false, false));
        assertEquals("Gem rocks need level 40 Mining",
                GemMiningAPI.missingRequirement(Mine.SURFACE, true, 39, false, false, false));
        assertEquals("Complete Shilo Village to reach the gem mine",
                GemMiningAPI.missingRequirement(Mine.SURFACE, true, 40, false, false, false));
        assertEquals("The Underground mine needs the Karamja medium diary reward. Claim it from Pirate Jackie the Fruit",
                GemMiningAPI.missingRequirement(Mine.UNDERGROUND, true, 40, true, false, false));
        assertEquals("Carry or wear a pickaxe you can use",
                GemMiningAPI.missingRequirement(Mine.UNDERGROUND, true, 40, true, true, false));
        assertNull(GemMiningAPI.missingRequirement(Mine.SURFACE, true, 40, true, true, true));
    }

    @Test
    void aPickaxeCountsOnlyAtItsMiningLevel()
    {
        assertFalse(GemMiningAPI.hasPickaxe(Set.of(ItemID.RUNE_PICKAXE), 40));
        assertTrue(GemMiningAPI.hasPickaxe(Set.of(ItemID.RUNE_PICKAXE), 41));
        assertTrue(GemMiningAPI.hasPickaxe(Set.of(ItemID.UNCUT_OPAL, ItemID.MITHRIL_PICKAXE), 40));
        assertFalse(GemMiningAPI.hasPickaxe(Set.of(ItemID.CRYSTAL_PICKAXE_INACTIVE), 99));
        assertFalse(GemMiningAPI.hasPickaxe(Set.of(ItemID.GEM_BAG), 99));
    }

    @Test
    void onlyUncutGemsAreDeposited()
    {
        for (int gem : GemMiningAPI.GEMS)
        {
            assertTrue(GemMiningAPI.isGem(gem));
        }
        assertFalse(GemMiningAPI.isGem(ItemID.GEM_BAG_OPEN));
        assertFalse(GemMiningAPI.isGem(ItemID.RUNE_PICKAXE));
        assertFalse(GemMiningAPI.isGem(ItemID.SAPPHIRE));
    }

    @Test
    void delaysStayWithinTheConfiguredRange()
    {
        Random random = new Random(7);
        GemMiningConfig wide = timing(2, 6, 4, 3, false);
        GemMiningConfig weighted = timing(1, 5, 2, 2, true);
        for (int i = 0; i < 500; i++)
        {
            int delay = GemMiningAPI.rollDelay(random, wide);
            assertTrue(delay >= 2 && delay <= 6, String.valueOf(delay));
            int skewed = GemMiningAPI.rollDelay(random, weighted);
            assertTrue(skewed >= 1 && skewed <= 5, String.valueOf(skewed));
        }
        assertEquals(1, GemMiningAPI.rollDelay(random, new GemMiningConfig()
        {
        }));
    }

    @Test
    void thePauseNamesItsTicks()
    {
        assertEquals("Short pause (1 tick)", GemMiningAPI.pauseStatus(1));
        assertEquals("Short pause (3 ticks)", GemMiningAPI.pauseStatus(3));
    }

    @Test
    void miningAndBankingAlternate()
    {
        assertEquals(State.BANKING, State.MINING.next());
        assertEquals(State.MINING, State.BANKING.next());
    }

    private static GemMiningConfig timing(int min, int max, int target, int deviation, boolean weighted)
    {
        return new GemMiningConfig()
        {
            @Override
            public int tickDelayMin()
            {
                return min;
            }

            @Override
            public int tickDelayMax()
            {
                return max;
            }

            @Override
            public int tickDelayTarget()
            {
                return target;
            }

            @Override
            public int tickDelayDeviation()
            {
                return deviation;
            }

            @Override
            public boolean tickDelayWeightedDistribution()
            {
                return weighted;
            }
        };
    }
}
