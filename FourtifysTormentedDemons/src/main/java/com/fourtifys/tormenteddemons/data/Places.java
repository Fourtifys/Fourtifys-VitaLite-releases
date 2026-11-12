package com.fourtifys.tormenteddemons.data;

import java.util.Arrays;
import net.runelite.api.coords.WorldPoint;

public class Places
{
    public static final int[] TEMPLE_REGIONS = {16196, 16197, 16452, 16453};
    public static final int LOWER_WALL = 53623;
    public static final int UPPER_WALL = 53624;
    public static final WorldPoint CHASM_LANDING = new WorldPoint(4063, 4557, 0);
    public static final int CHASM_RADIUS = 20;
    public static final WorldPoint FEROX_LANDING = new WorldPoint(3151, 3636, 0);
    public static final int FEROX_RADIUS = 40;
    public static final String FEROX = "Ferox Enclave";
    public static final int POOL = 39651;

    public static boolean inTemple(WorldPoint point)
    {
        return point.getPlane() == 0 && Arrays.stream(TEMPLE_REGIONS).anyMatch(region -> region == point.getRegionID());
    }

    public static boolean nearChasm(WorldPoint point)
    {
        return point.distanceTo2D(CHASM_LANDING) <= CHASM_RADIUS;
    }

    public static boolean nearFerox(WorldPoint point)
    {
        return point.distanceTo(FEROX_LANDING) <= FEROX_RADIUS;
    }
}
