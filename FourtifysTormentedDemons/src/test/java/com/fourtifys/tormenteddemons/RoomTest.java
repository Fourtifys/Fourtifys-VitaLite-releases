package com.fourtifys.tormenteddemons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fourtifys.tormenteddemons.data.Room;
import java.util.Map;
import net.runelite.api.coords.WorldPoint;
import org.junit.jupiter.api.Test;

class RoomTest
{
    private static final Map<Room, int[][]> SPAWNS = Map.of(
            Room.PLINTH_CHAMBER, new int[][] {{4136, 4372}, {4117, 4385}, {4135, 4399}, {4148, 4381}, {4123, 4381}, {4146, 4366}, {4131, 4363}, {4140, 4392}},
            Room.CENTRAL_CHAMBER, new int[][] {{4071, 4426}, {4079, 4430}},
            Room.NORTH_WEST_ROOM, new int[][] {{4038, 4449}},
            Room.WEST_ROOM, new int[][] {{4040, 4419}},
            Room.SOUTH_WEST_ROOM, new int[][] {{4038, 4379}, {4039, 4391}, {4041, 4385}},
            Room.SOUTH_ROOM, new int[][] {{4088, 4379}, {4083, 4373}, {4088, 4367}},
            Room.NORTH_ROOM, new int[][] {{4112, 4466}, {4104, 4467}},
            Room.NORTH_EAST_ROOM, new int[][] {{4134, 4466}, {4141, 4464}},
            Room.EAST_ROOM, new int[][] {{4147, 4452}, {4139, 4450}},
            Room.LOWER_EAST_ROOM, new int[][] {{4149, 4430}, {4151, 4425}});

    @Test
    void eachRoomHoldsItsOwnDemonsAndNoOthers()
    {
        for (Room room : Room.values())
        {
            assertEquals(room.getDemons(), SPAWNS.get(room).length, room.name());
            for (Map.Entry<Room, int[][]> spawns : SPAWNS.entrySet())
            {
                for (int[] spawn : spawns.getValue())
                {
                    WorldPoint point = new WorldPoint(spawn[0], spawn[1], 0);
                    assertEquals(spawns.getKey() == room, room.getArea().contains(point), room + " and " + point);
                }
            }
        }
    }

    @Test
    void demonsStayInTheirRoomAsTheyWander()
    {
        Map<Room, int[][]> wandered = Map.of(
                Room.PLINTH_CHAMBER, new int[][] {{4112, 4389}, {4126, 4378}, {4116, 4388}},
                Room.CENTRAL_CHAMBER, new int[][] {{4070, 4423}, {4080, 4433}, {4072, 4426}},
                Room.WEST_ROOM, new int[][] {{4039, 4416}, {4040, 4421}},
                Room.SOUTH_ROOM, new int[][] {{4079, 4371}, {4081, 4369}, {4089, 4381}, {4088, 4366}});
        for (Map.Entry<Room, int[][]> room : wandered.entrySet())
        {
            for (int[] tile : room.getValue())
            {
                assertTrue(room.getKey().getArea().contains(new WorldPoint(tile[0], tile[1], 0)), room.getKey() + " at " + tile[0] + ", " + tile[1]);
            }
        }
    }

    @Test
    void roomsDoNotOverlapAndExcludeTheEntrance()
    {
        WorldPoint entrance = new WorldPoint(4061, 4466, 0);
        for (Room room : Room.values())
        {
            assertFalse(room.getArea().contains(entrance), room.name());
            for (Room other : Room.values())
            {
                assertTrue(room == other || room.getArea().toWorldPointList().stream().noneMatch(other.getArea()::contains), room + " and " + other);
            }
        }
    }

    @Test
    void theSettingNamesTheRoomAndItsDemons()
    {
        assertEquals("Plinth chamber (8 demons)", Room.PLINTH_CHAMBER.toString());
        assertEquals("West room (1 demon)", Room.WEST_ROOM.toString());
    }
}
