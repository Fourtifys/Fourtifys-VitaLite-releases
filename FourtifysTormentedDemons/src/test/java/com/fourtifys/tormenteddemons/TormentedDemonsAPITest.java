package com.fourtifys.tormenteddemons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fourtifys.tormenteddemons.api.Combat;
import com.fourtifys.tormenteddemons.api.Supplies;
import com.fourtifys.tormenteddemons.api.TormentedDemonsAPI;
import com.fourtifys.tormenteddemons.data.CombatStyle;
import com.fourtifys.tormenteddemons.data.Places;
import com.fourtifys.tormenteddemons.data.State;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import org.junit.jupiter.api.Test;

class TormentedDemonsAPITest
{
    private static final TormentedDemonsConfig DEFAULTS = new TormentedDemonsConfig()
    {
    };

    @Test
    void theRunSwitchIsOffUntilThePlayerSwitchesItOn()
    {
        assertFalse(new TormentedDemonsConfig()
        {
        }.run());
    }

    @Test
    void eachGearItemIsWornOnceUntilItLeavesTheInventory()
    {
        Map<Integer, Integer> sent = new HashMap<>();
        assertTrue(Supplies.wearNow(sent, List.of(1, 2), 1, 10));
        assertTrue(Supplies.wearNow(sent, List.of(1, 2), 2, 10));
        assertFalse(Supplies.wearNow(sent, List.of(1, 2), 1, 11));
        assertFalse(Supplies.wearRefused(sent, 1, 11));
        assertFalse(Supplies.wearNow(sent, List.of(1, 2), 1, 12));
        assertTrue(Supplies.wearRefused(sent, 1, 12));
        assertFalse(Supplies.wearNow(sent, List.of(2), 2, 20));
        assertFalse(sent.containsKey(1));
        assertFalse(Supplies.wearRefused(sent, 1, 20));
        assertTrue(Supplies.wearNow(sent, List.of(1, 2), 1, 30));
    }

    @Test
    void theDemonsPrayerIsTheFirstShownOverheadIcon()
    {
        assertNull(Combat.demonPrayer(null));
        assertNull(Combat.demonPrayer(new short[]{-1, -1}));
        assertEquals(CombatStyle.MELEE, Combat.demonPrayer(new short[]{0}));
        assertEquals(CombatStyle.RANGED, Combat.demonPrayer(new short[]{-1, 1}));
        assertEquals(CombatStyle.MAGIC, Combat.demonPrayer(new short[]{2, -1}));
    }

    @Test
    void eachAttackAnimationNamesItsStyleAndProtection()
    {
        assertEquals(CombatStyle.MELEE, CombatStyle.fromAnimation(Combat.MELEE_ATTACK));
        assertEquals(CombatStyle.RANGED, CombatStyle.fromAnimation(Combat.RANGED_ATTACK));
        assertEquals(CombatStyle.MAGIC, CombatStyle.fromAnimation(Combat.MAGIC_ATTACK));
        assertNull(CombatStyle.fromAnimation(11387));
        assertNull(CombatStyle.fromAnimation(-1));
        assertEquals("PROTECT_FROM_MELEE", CombatStyle.MELEE.protection().name());
        assertEquals("PROTECT_FROM_MISSILES", CombatStyle.RANGED.protection().name());
        assertEquals("PROTECT_FROM_MAGIC", CombatStyle.MAGIC.protection().name());
    }

    @Test
    void thePlayerUsesTheFirstArmedStyleTheDemonDoesNotProtectFrom()
    {
        List<CombatStyle> armed = List.of(CombatStyle.MELEE, CombatStyle.RANGED);
        assertEquals(CombatStyle.MELEE, Combat.chooseStyle(armed, null));
        assertEquals(CombatStyle.MELEE, Combat.chooseStyle(armed, CombatStyle.MAGIC));
        assertEquals(CombatStyle.RANGED, Combat.chooseStyle(armed, CombatStyle.MELEE));
        assertEquals(CombatStyle.MELEE, Combat.chooseStyle(armed, CombatStyle.RANGED));
        assertNull(Combat.chooseStyle(List.of(CombatStyle.MELEE), CombatStyle.MELEE));
    }

    @Test
    void afterTheSpecialTheDemonsMovementPredictsItsNextStyle()
    {
        assertEquals(CombatStyle.MELEE, Combat.afterSpecial(CombatStyle.RANGED, true, 0, 4));
        assertEquals(CombatStyle.MAGIC, Combat.afterSpecial(CombatStyle.RANGED, false, 3, 2));
        assertEquals(CombatStyle.RANGED, Combat.afterSpecial(CombatStyle.MAGIC, false, 3, 2));
        assertEquals(CombatStyle.MAGIC, Combat.afterSpecial(CombatStyle.MELEE, false, 4, 3));
        assertEquals(CombatStyle.MAGIC, Combat.afterSpecial(null, false, 3, 2));
        assertNull(Combat.afterSpecial(CombatStyle.RANGED, false, 2, 4));
        assertNull(Combat.afterSpecial(CombatStyle.RANGED, false, 5, 1));
    }

    @Test
    void aSafeTileIsTheNearestReachableTileOffTheBombsAndOutsideTheDemon()
    {
        WorldPoint player = new WorldPoint(100, 100, 0);
        WorldArea demon = new WorldArea(102, 99, 3, 3, 0);
        Set<WorldPoint> bombs = Set.of(player, new WorldPoint(101, 100, 0));
        WorldPoint safe = Combat.safeTile(player, bombs, demon, tile -> true);
        assertEquals(1, Math.max(Math.abs(safe.getX() - 100), Math.abs(safe.getY() - 100)));
        assertFalse(bombs.contains(safe));
        assertFalse(demon.contains(safe));
        assertEquals(1, demon.distanceTo(safe));
        WorldPoint far = Combat.safeTile(player, bombs, demon, tile -> Math.abs(tile.getX() - 100) == 2);
        assertEquals(new WorldPoint(102, 98, 0), far);
        assertNull(Combat.safeTile(player, bombs, demon, tile -> false));
        assertFalse(bombs.contains(Combat.safeTile(player, bombs, null, tile -> true)));
    }

    @Test
    void thePlayerLeavesWhenOutOfFoodRestoresOrRoom()
    {
        assertNull(Combat.leaveReason(5, 40, 55, 25, 3, 10, 25, false));
        assertNull(Combat.leaveReason(0, 80, 55, 25, 0, 40, 25, false));
        assertEquals("Hitpoints low", Combat.leaveReason(5, 25, 55, 25, 3, 50, 25, false));
        assertNull(Combat.leaveReason(5, 26, 55, 25, 3, 50, 25, false));
        assertEquals("Out of food", Combat.leaveReason(0, 55, 55, 25, 0, 10, 25, true));
        assertEquals("Out of prayer restores", Combat.leaveReason(0, 60, 55, 25, 0, 25, 25, true));
        assertEquals("Inventory full", Combat.leaveReason(4, 90, 55, 25, 2, 50, 25, true));
    }

    @Test
    void thePoolIsDrunkUntilHitpointsPrayerAndRunAreFull()
    {
        assertTrue(TormentedDemonsAPI.restored(99, 99, 99, 99, 100));
        assertTrue(TormentedDemonsAPI.restored(108, 99, 99, 99, 100));
        assertFalse(TormentedDemonsAPI.restored(20, 99, 99, 99, 100));
        assertFalse(TormentedDemonsAPI.restored(99, 99, 98, 99, 100));
        assertFalse(TormentedDemonsAPI.restored(99, 99, 99, 99, 99));
    }

    @Test
    void requirementsAreCheckedInOrder()
    {
        assertEquals("Tormented Demons need a members world", TormentedDemonsAPI.missingRequirement(false, false, 0, false));
        assertEquals("Complete While Guthix Sleeps to reach the Tormented Demons", TormentedDemonsAPI.missingRequirement(true, false, 0, false));
        assertEquals("Set up and carry or wear gear for two combat styles", TormentedDemonsAPI.missingRequirement(true, true, 1, false));
        assertEquals("Carry or wear a ring of dueling to reach the bank", TormentedDemonsAPI.missingRequirement(true, true, 2, false));
        assertNull(TormentedDemonsAPI.missingRequirement(true, true, 3, true));
    }

    @Test
    void gearSettingsListNamesAndIds()
    {
        assertEquals(List.of("Abyssal whip", "Dragon defender", "4587"), Supplies.gear(" Abyssal whip, Dragon defender\n4587 ,"));
        assertTrue(Supplies.gear("").isEmpty());
        assertTrue(Supplies.matches("abyssal WHIP", 4151, "Abyssal whip"));
        assertTrue(Supplies.matches("4151", 4151, "Abyssal whip"));
        assertFalse(Supplies.matches("4152", 4151, "Abyssal whip"));
        assertFalse(Supplies.matches("Abyssal", 4151, "Abyssal whip"));
    }

    @Test
    void lootIsTakenWhenListedOrWorthTheMinimum()
    {
        List<String> always = Supplies.gear(DEFAULTS.lootAlways());
        assertTrue(Supplies.worthTaking(always, 29580, "Tormented synapse", 0, 5000));
        assertTrue(Supplies.worthTaking(always, Supplies.TEMPLE_TELEPORT, "Guthixian temple teleport", 0, 5000));
        assertTrue(Supplies.worthTaking(always, 560, "Death rune", 5000, 5000));
        assertFalse(Supplies.worthTaking(always, 560, "Death rune", 4999, 5000));
        assertFalse(Supplies.worthTaking(always, 526, "Bones", 0, 0));
        assertEquals(12_000, Supplies.value(560, 60, 200));
        assertEquals(4_000, Supplies.value(995, 4_000, 4_000));
        assertEquals(3_000, Supplies.value(13204, 3, 3_000));
    }

    @Test
    void extraSuppliesAreNamesWithQuantities()
    {
        assertEquals(Map.of("Super combat potion(4)", 2, "12625", 1), Supplies.restock("Super combat potion(4):2, 12625:1"));
        assertEquals(List.of("Super combat potion(4)", "12625"), List.copyOf(Supplies.restock("Super combat potion(4):2\n12625:1").keySet()));
        assertNull(Supplies.badRestock(" Super combat potion(4) : 2 ,"));
        assertEquals("Shark", Supplies.badRestock("Shark"));
        assertEquals("Shark:0", Supplies.badRestock("Ranging potion(4):1, Shark:0"));
        assertEquals(":3", Supplies.badRestock(":3"));
        assertEquals("Shark:x", Supplies.badRestock("Shark:x"));
    }

    @Test
    void suppliesGearAndKeptItemsAreNotBankedAsLoot()
    {
        assertEquals("Prayer potion", Supplies.doseBase("Prayer potion(3)"));
        assertEquals("Shark", Supplies.doseBase("Shark"));
        TormentedDemonsConfig config = new TormentedDemonsConfig()
        {
            @Override
            public String meleeGear()
            {
                return "Abyssal whip";
            }

            @Override
            public String magicGear()
            {
                return "12899";
            }

            @Override
            public String keepItems()
            {
                return "Rune pouch";
            }

            @Override
            public String restockItems()
            {
                return "Super combat potion(4):2";
            }
        };
        assertTrue(Supplies.isKept(config, 0, "Prayer potion(1)"));
        assertTrue(Supplies.isKept(config, 0, "shark"));
        assertTrue(Supplies.isKept(config, 0, "Ring of dueling(5)"));
        assertTrue(Supplies.isKept(config, Supplies.TEMPLE_TELEPORT, "Guthixian temple teleport"));
        assertTrue(Supplies.isKept(config, 4151, "Abyssal whip"));
        assertTrue(Supplies.isKept(config, 12899, "Trident of the swamp"));
        assertTrue(Supplies.isKept(config, 12791, "Rune pouch"));
        assertTrue(Supplies.isKept(config, 0, "Super combat potion(4)"));
        assertFalse(Supplies.isKept(config, 0, "Super combat potion(3)"));
        assertFalse(Supplies.isKept(config, 29580, "Tormented synapse"));
        assertFalse(Supplies.isKept(config, 0, "Manta ray"));
        assertFalse(Supplies.isKept(config, 0, null));
    }

    @Test
    void theNewSettingsStartAtTheirDefaults()
    {
        assertFalse(DEFAULTS.offensivePrayer());
        assertEquals(5, DEFAULTS.scrollCount());
        assertEquals(5000, DEFAULTS.lootMinimum());
        assertEquals(25, DEFAULTS.emergencyAt());
        assertEquals(List.of("Tormented synapse", "Burning claw", "Guthixian temple teleport"), Supplies.gear(DEFAULTS.lootAlways()));
    }

    @Test
    void placesAreRecognisedByRegionAndDistance()
    {
        assertTrue(Places.inTemple(new WorldPoint(4061, 4466, 0)));
        assertFalse(Places.inTemple(new WorldPoint(4061, 4466, 1)));
        assertFalse(Places.inTemple(new WorldPoint(4063, 4557, 0)));
        assertTrue(Places.nearChasm(new WorldPoint(4061, 4554, 1)));
        assertFalse(Places.nearChasm(new WorldPoint(4061, 4466, 0)));
        assertTrue(Places.nearFerox(new WorldPoint(3131, 3630, 0)));
        assertFalse(Places.nearFerox(new WorldPoint(4063, 4557, 0)));
        assertTrue(Combat.isDemon(13599));
        assertTrue(Combat.isDemon(13600));
        assertFalse(Combat.isDemon(13601));
    }

    @Test
    void theRandomDelayStaysWithinItsBoundsAndShowsAsAShortPause()
    {
        Random random = new Random(7);
        for (int i = 0; i < 1000; i++)
        {
            int delay = TormentedDemonsAPI.rollDelay(random, DEFAULTS);
            assertTrue(delay >= 0 && delay <= 1, String.valueOf(delay));
        }
        assertEquals("Short pause (1 tick)", TormentedDemonsAPI.pauseStatus(1));
        assertEquals("Short pause (3 ticks)", TormentedDemonsAPI.pauseStatus(3));
    }

    @Test
    void theStatesRunInATripOrder()
    {
        assertEquals(State.TRAVELLING, State.BANKING.next());
        assertEquals(State.FIGHTING, State.TRAVELLING.next());
        assertEquals(State.BANKING, State.FIGHTING.next());
    }
}
