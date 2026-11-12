package com.fourtifys.tormenteddemons.api;

import com.fourtifys.tormenteddemons.TormentedDemonsPlugin;
import com.fourtifys.tormenteddemons.data.Places;
import com.tonic.api.entities.TileObjectAPI;
import com.tonic.api.widgets.InventoryAPI;
import com.tonic.data.wrappers.ItemEx;
import com.tonic.data.wrappers.PlayerEx;
import com.tonic.data.wrappers.TileObjectEx;
import com.tonic.util.handler.StepContext;
import com.tonic.util.handler.StepHandler;
import net.runelite.api.coords.WorldPoint;

public class TravelBuilder extends TormentedDemonsBuilder<TravelBuilder>
{
    private static final int CLIMB_TICKS = 10;

    private TravelBuilder(TormentedDemonsPlugin plugin)
    {
        super(plugin);
    }

    public static StepHandler travelling(TormentedDemonsPlugin plugin)
    {
        TravelBuilder builder = new TravelBuilder(plugin);
        return builder.enterTemple()
                .travel("room", builder.room.getArea(), "the " + builder.room.getName().toLowerCase())
                .build();
    }

    private TravelBuilder enterTemple()
    {
        int step = currentStep;
        add(context -> {
            WorldPoint here = PlayerEx.getLocal().getWorldPoint();
            if (Places.inTemple(here))
            {
                return step + 1;
            }
            WorldPoint from = context.get("from");
            if (from != null && from.getPlane() == here.getPlane() && !waited(context, CLIMB_TICKS))
            {
                return step;
            }
            if (paused(context))
            {
                return step;
            }
            if (Places.nearChasm(here))
            {
                return climb(context, here, step);
            }
            ItemEx scroll = InventoryAPI.getItem(Supplies.TEMPLE_TELEPORT);
            if (scroll == null)
            {
                plugin.setStatus("No Guthixian temple teleport to reach the temple");
                return step;
            }
            if (context.contains("teleported") && !waited(context, TELEPORT_TICKS))
            {
                return step;
            }
            InventoryAPI.interact(scroll, "Teleport");
            acted(context);
            context.put("teleported", true);
            plugin.setStatus("Teleporting to the Guthixian temple");
            return step;
        });
        return this;
    }

    private int climb(StepContext context, WorldPoint here, int step)
    {
        TileObjectEx way;
        if (here.getPlane() == 0)
        {
            way = TileObjectAPI.search().withId(Places.LOWER_WALL).nearest();
        }
        else if (here.getPlane() == 1)
        {
            way = TileObjectAPI.search().withId(Places.UPPER_WALL).nearest();
        }
        else
        {
            way = TileObjectAPI.search().withAction("Climb-through").nearest();
        }
        if (way == null)
        {
            plugin.setStatus("Cannot find the way into the temple");
            return step;
        }
        way.interact(here.getPlane() < 2 ? "Climb-up" : "Climb-through");
        acted(context);
        context.put("from", here);
        plugin.setStatus("Climbing into the temple");
        return step;
    }
}
