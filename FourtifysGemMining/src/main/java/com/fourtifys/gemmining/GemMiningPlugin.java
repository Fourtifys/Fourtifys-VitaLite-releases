package com.fourtifys.gemmining;

import com.fourtifys.gemmining.api.GemMiningAPI;
import com.fourtifys.gemmining.data.Mine;
import com.fourtifys.gemmining.data.State;
import com.google.inject.Provides;
import com.tonic.util.handler.StepHandler;
import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
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
        name = "Fourtifys Gem Mining",
        description = "Mines the Shilo Village gem rocks and banks the gems",
        tags = {"fourtifys", "mining", "gems", "shilo"},
        enabledByDefault = false
)
public class GemMiningPlugin extends Plugin
{
    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private GemMiningOverlay overlay;
    @Inject
    @Getter
    private GemMiningConfig config;
    @Getter
    @Setter
    private String status;
    private State state;
    @Getter
    private Mine mine;
    private StepHandler handler;
    private Instant start;
    private int gems;

    @Provides
    GemMiningConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(GemMiningConfig.class);
    }

    @Override
    protected void startUp()
    {
        start = Instant.now();
        gems = 0;
        reset();
        if (config.enableUI())
        {
            overlayManager.add(overlay);
        }
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        reset();
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
        step();
        String runtime = DurationFormatUtils.formatDuration(Duration.between(start, Instant.now()).toMillis(), "HH:mm:ss");
        overlay.update(runtime, config.mine().toString(), gems, status);
    }

    private void step()
    {
        if (client.getGameState() != GameState.LOGGED_IN || client.getLocalPlayer() == null)
        {
            status = "Waiting for login";
            return;
        }
        if (handler != null && mine != config.mine())
        {
            reset();
        }
        if (handler == null)
        {
            mine = config.mine();
            String missing = GemMiningAPI.missingRequirement(mine);
            if (missing != null)
            {
                status = missing;
                return;
            }
            state = GemMiningAPI.tripFull() ? State.BANKING : State.MINING;
            handler = state.handler(this);
        }
        if (!handler.step())
        {
            state = state.next();
            handler = state.handler(this);
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        if (event.getType() == ChatMessageType.SPAM || event.getType() == ChatMessageType.GAMEMESSAGE)
        {
            gems += GemMiningAPI.isGemMessage(event.getMessage()) ? 1 : 0;
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (GemMiningConfig.GROUP.equals(event.getGroup()) && "enableUI".equals(event.getKey()))
        {
            overlayManager.remove(overlay);
            if (config.enableUI())
            {
                overlayManager.add(overlay);
            }
        }
    }
}
