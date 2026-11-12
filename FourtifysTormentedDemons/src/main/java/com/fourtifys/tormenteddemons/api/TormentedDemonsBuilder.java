package com.fourtifys.tormenteddemons.api;

import com.fourtifys.tormenteddemons.TormentedDemonsConfig;
import com.fourtifys.tormenteddemons.TormentedDemonsPlugin;
import com.fourtifys.tormenteddemons.data.Room;
import com.tonic.Static;
import com.tonic.data.wrappers.PlayerEx;
import com.tonic.util.handler.AbstractHandlerBuilder;
import com.tonic.util.handler.StepContext;
import java.util.Random;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldArea;

public abstract class TormentedDemonsBuilder<T extends TormentedDemonsBuilder<T>> extends AbstractHandlerBuilder<T>
{
    protected static final int TELEPORT_TICKS = 8;
    private static final int WALK_ATTEMPTS = 3;
    private static final Random random = new Random();
    protected final TormentedDemonsPlugin plugin;
    protected final TormentedDemonsConfig config;
    protected final Room room;

    protected TormentedDemonsBuilder(TormentedDemonsPlugin plugin)
    {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.room = plugin.getRoom();
    }

    protected T travel(String label, WorldArea area, String place)
    {
        add(label, () -> plugin.setStatus("Walking to " + place));
        walkTo(area);
        int step = currentStep;
        add(context -> {
            if (area.contains(PlayerEx.getLocal().getWorldPoint()))
            {
                return step + 1;
            }
            int attempts = context.getOrDefault(label, 0) + 1;
            context.put(label, attempts);
            if (attempts >= WALK_ATTEMPTS)
            {
                plugin.setStatus("Could not walk to " + place + ". Move closer, then disable and enable the plugin");
                return step;
            }
            return jump(label, context);
        });
        return self();
    }

    protected static boolean since(StepContext context, String key, int ticks)
    {
        Client client = Static.getClient();
        return client.getTickCount() - context.getOrDefault(key, Integer.MIN_VALUE / 2) >= ticks;
    }

    protected static void stamp(StepContext context, String key)
    {
        Client client = Static.getClient();
        context.put(key, client.getTickCount());
    }

    protected boolean paused(StepContext context)
    {
        int remaining = context.getOrDefault("pause", 0);
        if (remaining <= 0)
        {
            return false;
        }
        if (PlayerEx.getLocal().getPlayer().getAnimation() == -1)
        {
            plugin.setStatus(TormentedDemonsAPI.pauseStatus(remaining));
            context.put("pause", remaining - 1);
        }
        return true;
    }

    protected void acted(StepContext context)
    {
        context.put("pause", TormentedDemonsAPI.rollDelay(random, config));
        context.put("waited", 0);
    }

    protected static boolean waited(StepContext context, int ticks)
    {
        int waited = context.getOrDefault("waited", 0) + 1;
        context.put("waited", waited);
        return waited >= ticks;
    }
}
