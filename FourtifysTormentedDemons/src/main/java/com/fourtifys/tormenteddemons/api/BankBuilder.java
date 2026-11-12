package com.fourtifys.tormenteddemons.api;

import com.fourtifys.tormenteddemons.TormentedDemonsPlugin;
import com.fourtifys.tormenteddemons.data.Places;
import com.tonic.api.entities.NpcAPI;
import com.tonic.api.entities.TileObjectAPI;
import com.tonic.api.widgets.BankAPI;
import com.tonic.api.widgets.DialogueAPI;
import com.tonic.api.widgets.InventoryAPI;
import com.tonic.api.widgets.WidgetAPI;
import com.tonic.data.locatables.BankLocations;
import com.tonic.data.wrappers.ItemEx;
import com.tonic.data.wrappers.NpcEx;
import com.tonic.data.wrappers.PlayerEx;
import com.tonic.data.wrappers.TileObjectEx;
import com.tonic.util.handler.StepContext;
import com.tonic.util.handler.StepHandler;
import java.util.Map;
import net.runelite.api.gameval.InterfaceID;

public class BankBuilder extends TormentedDemonsBuilder<BankBuilder>
{
    private static final int OPEN_TICKS = 10;
    private static final int TELEPORT_GRACE_TICKS = 5;

    private BankBuilder(TormentedDemonsPlugin plugin)
    {
        super(plugin);
    }

    public static StepHandler banking(TormentedDemonsPlugin plugin)
    {
        return new BankBuilder(plugin)
                .teleportToFerox()
                .travel("bank", BankLocations.FEROX_ENCLAVE_BANK.getArea(), "the Ferox Enclave bank")
                .openBank()
                .restock()
                .closeBank()
                .refresh()
                .build();
    }

    private BankBuilder teleportToFerox()
    {
        int step = currentStep;
        add(context -> {
            if (Places.nearFerox(PlayerEx.getLocal().getWorldPoint()))
            {
                return step + 1;
            }
            if (DialogueAPI.optionPresent(Places.FEROX))
            {
                DialogueAPI.selectOption(Places.FEROX);
                context.put("waited", 0);
                return step;
            }
            if (!context.contains("teleported"))
            {
                context.put("teleported", true);
                context.put("waited", TELEPORT_TICKS - TELEPORT_GRACE_TICKS);
                return step;
            }
            if (!waited(context, TELEPORT_TICKS))
            {
                return step;
            }
            if (!TormentedDemonsAPI.teleportOut())
            {
                plugin.setStatus("No ring of dueling to teleport to the bank");
                return step;
            }
            context.put("teleported", true);
            context.put("waited", 0);
            plugin.setStatus("Teleporting to Ferox Enclave");
            return step;
        });
        return this;
    }

    private BankBuilder openBank()
    {
        int step = currentStep;
        add("open", context -> {
            if (bankOpen())
            {
                return step + 2;
            }
            if (paused(context))
            {
                return step;
            }
            TileObjectEx booth = TileObjectAPI.search().withAction("Bank").nearest();
            NpcEx banker = booth == null ? NpcAPI.search().withAction("Bank").nearest() : null;
            if (booth == null && banker == null)
            {
                plugin.setStatus("Cannot find a bank");
                return step;
            }
            if (booth != null)
            {
                booth.interact("Bank");
            }
            else
            {
                NpcAPI.interact(banker, "Bank");
            }
            acted(context);
            plugin.setStatus("Opening the bank");
            return step + 1;
        });
        add(context -> {
            if (bankOpen())
            {
                return step + 2;
            }
            return waited(context, OPEN_TICKS) ? step : step + 1;
        });
        return this;
    }

    private BankBuilder restock()
    {
        int step = currentStep;
        add(context -> {
            if (!bankOpen())
            {
                return jump("open", context);
            }
            if (paused(context))
            {
                return step;
            }
            ItemEx loot = InventoryAPI.getItem(item -> !Supplies.isKept(config, item.getId(), item.getName()));
            if (loot != null)
            {
                BankAPI.deposit(loot.getId(), -1);
                acted(context);
                plugin.setStatus("Depositing loot");
                return step;
            }
            return withdrawSupplies(context) ? step : step + 1;
        });
        return this;
    }

    private boolean withdrawSupplies(StepContext context)
    {
        if (!Supplies.hasRing() && !context.contains("ring"))
        {
            ItemEx ring = BankAPI.search().withNameContains(Supplies.RING).first();
            return ring == null ? missing("No ring of dueling in the bank") : withdraw(context, "ring", () -> BankAPI.withdraw(ring, 1, false));
        }
        int carriedScrolls = Supplies.carried(String.valueOf(Supplies.TEMPLE_TELEPORT));
        if (carriedScrolls < config.scrollCount() && !context.contains("scrolls"))
        {
            return !BankAPI.contains(Supplies.TEMPLE_TELEPORT) && carriedScrolls == 0
                    ? missing("No Guthixian temple teleports in the bank")
                    : withdraw(context, "scrolls", () -> BankAPI.withdraw(Supplies.TEMPLE_TELEPORT, config.scrollCount() - carriedScrolls, false));
        }
        int carriedFood = Supplies.foodCount(config);
        if (carriedFood < config.foodCount() && !context.contains("food"))
        {
            return !BankAPI.contains(config.food()) && carriedFood == 0
                    ? missing("No " + config.food() + " in the bank")
                    : withdraw(context, "food", () -> BankAPI.withdraw(config.food(), config.foodCount() - carriedFood, false));
        }
        int carriedRestores = Supplies.restores(config).size();
        if (carriedRestores < config.restoreCount() && !context.contains("restores"))
        {
            return !BankAPI.contains(config.restore()) && carriedRestores == 0
                    ? missing("No " + config.restore() + " in the bank")
                    : withdraw(context, "restores", () -> BankAPI.withdraw(config.restore(), config.restoreCount() - carriedRestores, false));
        }
        for (Map.Entry<String, Integer> extra : Supplies.restock(config.restockItems()).entrySet())
        {
            String key = "extra " + extra.getKey();
            int carried = Supplies.carried(extra.getKey());
            if (carried >= extra.getValue() || context.contains(key))
            {
                continue;
            }
            ItemEx banked = BankAPI.search().keepIf(item -> Supplies.matches(extra.getKey(), item.getId(), item.getName())).first();
            if (banked != null)
            {
                return withdraw(context, key, () -> BankAPI.withdraw(banked, extra.getValue() - carried, false));
            }
            if (carried == 0)
            {
                return missing("No " + extra.getKey() + " in the bank");
            }
        }
        return false;
    }

    private boolean missing(String supply)
    {
        plugin.setStatus(supply + ". Restock the bank, then disable and enable the plugin");
        return true;
    }

    private boolean withdraw(StepContext context, String supply, Runnable action)
    {
        action.run();
        context.put(supply, true);
        acted(context);
        plugin.setStatus("Taking supplies from the bank");
        return true;
    }

    private BankBuilder closeBank()
    {
        int step = currentStep;
        add(context -> {
            if (!bankOpen())
            {
                return step + 1;
            }
            if (!context.contains("closed") || waited(context, OPEN_TICKS))
            {
                WidgetAPI.closeInterface();
                context.put("closed", true);
                context.put("waited", 0);
                plugin.setStatus("Closing the bank");
            }
            return step;
        });
        return this;
    }

    private BankBuilder refresh()
    {
        int step = currentStep;
        add(context -> {
            if (TormentedDemonsAPI.restored())
            {
                return END_EXECUTION;
            }
            if (paused(context) || context.contains("refreshing") && !waited(context, OPEN_TICKS))
            {
                return step;
            }
            TileObjectEx pool = TileObjectAPI.search().withId(Places.POOL).nearest();
            if (pool == null)
            {
                plugin.setStatus("Cannot find the Pool of Refreshment");
                return step;
            }
            pool.interact("Drink");
            acted(context);
            context.put("refreshing", true);
            plugin.setStatus("Drinking from the Pool of Refreshment");
            return step;
        });
        return this;
    }

    private static boolean bankOpen()
    {
        return WidgetAPI.isVisible(InterfaceID.BANKMAIN, 0);
    }
}
