package com.fourtifys.tormenteddemons;

import com.fourtifys.tormenteddemons.api.Combat;
import com.fourtifys.tormenteddemons.api.Supplies;
import com.fourtifys.tormenteddemons.api.TormentedDemonsAPI;
import com.fourtifys.tormenteddemons.data.Room;
import com.fourtifys.tormenteddemons.data.State;
import com.google.inject.Provides;
import com.tonic.api.widgets.InventoryAPI;
import com.tonic.util.handler.StepHandler;
import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.time.DurationFormatUtils;

@PluginDescriptor(
        name = "Fourtifys Tormented Demons",
        description = "Kills Tormented Demons in the Guthixian temple, switching prayers and gear, and banks at Ferox Enclave",
        tags = {"fourtifys", "combat", "tormented", "demons", "bossing"},
        enabledByDefault = false
)
public class TormentedDemonsPlugin extends Plugin
{
    private static final String DEATH_MESSAGE = "Oh dear, you are dead!";
    private static final String STOPPED = "Switch on Run in the settings to start";
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private TormentedDemonsOverlay overlay;
    @Inject
    @Getter
    private TormentedDemonsConfig config;
    @Getter
    @Setter
    private String status;
    @Getter
    private Room room;
    private State state;
    private StepHandler handler;
    private Instant start;
    private int kills;
    private long lootValue;
    private int uniques;
    private Long carriedValue;
    private int carriedUniques;

    @Provides
    TormentedDemonsConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(TormentedDemonsConfig.class);
    }

    @Override
    protected void startUp()
    {
        configManager.setConfiguration(TormentedDemonsConfig.GROUP, TormentedDemonsConfig.RUN, false);
        stop(STOPPED);
        if (config.enableUI())
        {
            overlayManager.add(overlay);
        }
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        stop(STOPPED);
    }

    private void stop(String reason)
    {
        reset();
        start = null;
        status = reason;
    }

    private void reset()
    {
        if (handler != null)
        {
            handler.reset();
        }
        handler = null;
        state = null;
        status = "Starting";
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (!config.run())
        {
            if (start != null)
            {
                stop(STOPPED);
            }
        }
        else
        {
            if (start == null)
            {
                start = Instant.now();
                kills = 0;
                lootValue = 0;
                uniques = 0;
                carriedValue = null;
            }
            step();
        }
        long millis = start == null ? 0 : Duration.between(start, Instant.now()).toMillis();
        int killsPerHour = millis < 60_000 ? 0 : (int) (kills * 3_600_000L / millis);
        overlay.update(DurationFormatUtils.formatDuration(millis, "HH:mm:ss"), kills, killsPerHour, lootValue, uniques, status);
    }

    private void step()
    {
        if (client.getGameState() != GameState.LOGGED_IN || client.getLocalPlayer() == null)
        {
            status = "Waiting for login";
            return;
        }
        countGains();
        if (handler != null && room != config.room())
        {
            reset();
        }
        if (handler == null)
        {
            room = config.room();
            String missing = TormentedDemonsAPI.missingRequirement(config);
            if (missing != null)
            {
                status = missing;
                return;
            }
            if (room.getArea().contains(client.getLocalPlayer().getWorldLocation()))
            {
                state = State.FIGHTING;
            }
            else
            {
                state = TormentedDemonsAPI.inTemple() || TormentedDemonsAPI.readyToTravel(config) ? State.TRAVELLING : State.BANKING;
            }
            handler = state.handler(this);
        }
        if (!handler.step())
        {
            state = state.next();
            handler = state.handler(this);
        }
    }

    private void countGains()
    {
        if (!TormentedDemonsAPI.inTemple())
        {
            carriedValue = null;
            return;
        }
        long value = InventoryAPI.getItems().stream()
                .filter(item -> !Supplies.isKept(config, item.getId(), item.getName()))
                .mapToLong(item -> Supplies.value(item.getCanonicalId(), item.getQuantity(), item.getGePrice()))
                .sum();
        int carried = Supplies.UNIQUES.stream().mapToInt(Supplies::carried).sum();
        if (carriedValue != null)
        {
            lootValue += Math.max(0, value - carriedValue);
            uniques += Math.max(0, carried - carriedUniques);
        }
        carriedValue = value;
        carriedUniques = carried;
    }

    @Subscribe
    public void onActorDeath(ActorDeath event)
    {
        Player player = client.getLocalPlayer();
        if (!(event.getActor() instanceof NPC) || player == null)
        {
            return;
        }
        NPC npc = (NPC) event.getActor();
        if (start != null && Combat.isDemon(npc.getId()) && (npc.getInteracting() == player || player.getInteracting() == npc))
        {
            kills++;
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        if (start != null && event.getType() == ChatMessageType.GAMEMESSAGE && DEATH_MESSAGE.equals(event.getMessage()))
        {
            configManager.setConfiguration(TormentedDemonsConfig.GROUP, TormentedDemonsConfig.RUN, false);
            stop("You died. Switch on Run to go again");
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (TormentedDemonsConfig.GROUP.equals(event.getGroup()) && "enableUI".equals(event.getKey()))
        {
            overlayManager.remove(overlay);
            if (config.enableUI())
            {
                overlayManager.add(overlay);
            }
        }
    }
}
