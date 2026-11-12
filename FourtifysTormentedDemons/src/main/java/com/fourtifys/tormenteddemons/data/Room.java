package com.fourtifys.tormenteddemons.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.coords.WorldArea;

@Getter
@RequiredArgsConstructor
public enum Room
{
    PLINTH_CHAMBER("Plinth chamber", 8, new WorldArea(4108, 4357, 49, 48, 0)),
    CENTRAL_CHAMBER("Central chamber", 2, new WorldArea(4062, 4417, 27, 30, 0)),
    NORTH_WEST_ROOM("North-west room", 1, new WorldArea(4034, 4446, 15, 10, 0)),
    WEST_ROOM("West room", 1, new WorldArea(4036, 4413, 13, 14, 0)),
    SOUTH_WEST_ROOM("South-west room", 3, new WorldArea(4035, 4373, 14, 23, 0)),
    SOUTH_ROOM("South room", 3, new WorldArea(4074, 4365, 21, 21, 0)),
    NORTH_ROOM("North room", 2, new WorldArea(4100, 4458, 22, 16, 0)),
    NORTH_EAST_ROOM("North-east room", 2, new WorldArea(4128, 4459, 25, 12, 0)),
    EAST_ROOM("East room", 2, new WorldArea(4135, 4441, 23, 16, 0)),
    LOWER_EAST_ROOM("Lower east room", 2, new WorldArea(4140, 4419, 19, 15, 0));

    private final String name;
    private final int demons;
    private final WorldArea area;

    @Override
    public String toString()
    {
        return name + " (" + demons + (demons == 1 ? " demon)" : " demons)");
    }
}
