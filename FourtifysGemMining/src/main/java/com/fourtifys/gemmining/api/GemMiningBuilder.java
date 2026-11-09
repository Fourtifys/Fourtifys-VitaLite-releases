package com.fourtifys.gemmining.api;

import com.fourtifys.gemmining.GemMiningPlugin;
import com.fourtifys.gemmining.data.Mine;
import com.tonic.api.entities.TileObjectAPI;
import com.tonic.api.widgets.DepositBoxAPI;
import com.tonic.api.widgets.InventoryAPI;
import com.tonic.api.widgets.WidgetAPI;
import com.tonic.data.wrappers.ItemEx;
import com.tonic.data.wrappers.PlayerEx;
import com.tonic.data.wrappers.TileObjectEx;
import com.tonic.util.handler.AbstractHandlerBuilder;
import com.tonic.util.handler.StepContext;
import com.tonic.util.handler.StepHandler;
import java.util.Random;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.ObjectID;

public class GemMiningBuilder extends AbstractHandlerBuilder<GemMiningBuilder>
{
    private static final int DEPOSIT_BOX_ITEMS = 24;
    private static final int CLICK_TICKS = 3;
    private static final int OPEN_TICKS = 10;
    private static final int WALK_ATTEMPTS = 3;
    private static final Random random = new Random();
    private final GemMiningPlugin plugin;
    private final Mine mine;

    private GemMiningBuilder(GemMiningPlugin plugin)
    {
        this.plugin = plugin;
        this.mine = plugin.getMine();
    }

    public static StepHandler mining(GemMiningPlugin plugin)
    {
        GemMiningBuilder builder = new GemMiningBuilder(plugin);
        return builder.enterMine()
                .travel("rocks", builder.mine.getRocks(), "the " + builder.mine + " gem rocks")
                .openGemBag()
                .mineRocks()
                .build();
    }

    public static StepHandler banking(GemMiningPlugin plugin)
    {
        GemMiningBuilder builder = new GemMiningBuilder(plugin);
        return builder.enterMine()
                .travel("bank", builder.mine.getBank(), "the deposit " + (builder.mine.isUnderground() ? "chest" : "box"))
                .depositGems()
                .build();
    }

    private GemMiningBuilder enterMine()
    {
        if (mine.isUnderground() == GemMiningAPI.isUnderground())
        {
            return this;
        }
        if (mine.isUnderground())
        {
            travel("ladder", Mine.SURFACE.getRocks(), "the gem mine ladder");
            return climb(ObjectID.ATJUN_LADDER_MULTI, "Climb-down");
        }
        return climb(ObjectID.ATJUN_MINE_LADDER, "Climb-up");
    }

    private GemMiningBuilder climb(int ladder, String action)
    {
        int step = currentStep;
        add(context -> {
            if (paused(context))
            {
                return step;
            }
            TileObjectEx object = TileObjectAPI.search().withId(ladder).nearest();
            if (object == null)
            {
                plugin.setStatus("Cannot find the gem mine ladder");
                return step;
            }
            object.interact(action);
            acted(context);
            plugin.setStatus(mine.isUnderground() ? "Climbing down to the underground mine" : "Climbing up to the surface");
            return step + 1;
        });
        add(context -> {
            if (mine.isUnderground() == GemMiningAPI.isUnderground())
            {
                return step + 2;
            }
            return waited(context, OPEN_TICKS) ? step : step + 1;
        });
        return this;
    }

    private GemMiningBuilder travel(String label, WorldArea area, String place)
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
        return this;
    }

    private GemMiningBuilder openGemBag()
    {
        int step = currentStep;
        add(context -> {
            ItemEx bag = InventoryAPI.getItem(ItemID.GEM_BAG);
            if (!plugin.getConfig().useGemBag() || bag == null)
            {
                return step + 1;
            }
            if (paused(context))
            {
                return step;
            }
            InventoryAPI.interact(bag, "Open");
            acted(context);
            plugin.setStatus("Opening the gem bag");
            return step + 1;
        });
        return this;
    }

    private GemMiningBuilder mineRocks()
    {
        int step = currentStep;
        add(context -> {
            if (InventoryAPI.isFull())
            {
                if (InventoryAPI.containsAny(GemMiningAPI.GEMS))
                {
                    return END_EXECUTION;
                }
                plugin.setStatus("The inventory is full of other items");
                return step;
            }
            if (!PlayerEx.getLocal().isIdle())
            {
                plugin.setStatus("Mining gem rocks");
                return step;
            }
            if (paused(context))
            {
                return step;
            }
            TileObjectEx rock = TileObjectAPI.search()
                    .withId(GemMiningAPI.GEM_ROCKS)
                    .keepIf(object -> mine.getRocks().contains(object.getWorldPoint()))
                    .nearest();
            if (rock == null)
            {
                plugin.setStatus("Waiting for a gem rock to respawn");
                return step;
            }
            rock.interact("Mine");
            acted(context);
            plugin.setStatus("Mining gem rocks");
            return step + 1;
        });
        add(context -> waited(context, CLICK_TICKS) || !PlayerEx.getLocal().isIdle() ? step : step + 1);
        return this;
    }

    private GemMiningBuilder depositGems()
    {
        int step = currentStep;
        add("open", context -> {
            if (DepositBoxAPI.isOpen())
            {
                return step + 2;
            }
            if (paused(context))
            {
                return step;
            }
            TileObjectEx box = TileObjectAPI.search().withId(mine.getDepositObject()).nearest();
            if (box == null)
            {
                plugin.setStatus("Cannot find the deposit " + (mine.isUnderground() ? "chest" : "box"));
                return step;
            }
            box.interact("Deposit");
            acted(context);
            plugin.setStatus("Opening the deposit " + (mine.isUnderground() ? "chest" : "box"));
            return step + 1;
        });
        add(context -> {
            if (DepositBoxAPI.isOpen())
            {
                return step + 2;
            }
            return waited(context, OPEN_TICKS) ? step : step + 1;
        });
        add(context -> {
            if (!DepositBoxAPI.isOpen())
            {
                return jump("open", context);
            }
            if (paused(context))
            {
                return step + 2;
            }
            ItemEx gem = InventoryAPI.getItem(item -> GemMiningAPI.isGem(item.getId()));
            if (gem != null)
            {
                int deposits = context.getOrDefault("deposits", 0) + 1;
                context.put("deposits", deposits);
                if (deposits > 2 * GemMiningAPI.GEMS.length)
                {
                    plugin.setStatus("Some gems will not deposit. Unlock their inventory slots, then disable and enable the plugin");
                    return step + 2;
                }
                depositBoxAction(context, gem, "Deposit-All");
                plugin.setStatus("Depositing gems");
                return step + 2;
            }
            ItemEx bag = InventoryAPI.getItem(item -> item.getId() == ItemID.GEM_BAG || item.getId() == ItemID.GEM_BAG_OPEN);
            if (plugin.getConfig().useGemBag() && bag != null && !context.contains("emptied"))
            {
                context.put("emptied", true);
                depositBoxAction(context, bag, "Empty");
                plugin.setStatus("Emptying the gem bag");
                return step + 2;
            }
            return END_EXECUTION;
        });
        return this;
    }

    private void depositBoxAction(StepContext context, ItemEx item, String action)
    {
        WidgetAPI.interact(WidgetAPI.get(InterfaceID.BANK_DEPOSITBOX, DEPOSIT_BOX_ITEMS, item.getSlot()), action);
        acted(context);
    }

    private boolean paused(StepContext context)
    {
        int remaining = context.getOrDefault("pause", 0);
        if (remaining <= 0)
        {
            return false;
        }
        if (PlayerEx.getLocal().getPlayer().getAnimation() == -1)
        {
            plugin.setStatus(GemMiningAPI.pauseStatus(remaining));
            context.put("pause", remaining - 1);
        }
        return true;
    }

    private void acted(StepContext context)
    {
        context.put("pause", GemMiningAPI.rollDelay(random, plugin.getConfig()));
        context.put("waited", 0);
    }

    private static boolean waited(StepContext context, int ticks)
    {
        int waited = context.getOrDefault("waited", 0) + 1;
        context.put("waited", waited);
        return waited >= ticks;
    }
}
