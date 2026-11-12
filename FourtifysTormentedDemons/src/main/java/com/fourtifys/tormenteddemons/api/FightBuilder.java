package com.fourtifys.tormenteddemons.api;

import com.fourtifys.tormenteddemons.TormentedDemonsPlugin;
import com.fourtifys.tormenteddemons.data.CombatStyle;
import com.fourtifys.tormenteddemons.data.Places;
import com.tonic.Static;
import com.tonic.api.entities.NpcAPI;
import com.tonic.api.entities.TileItemAPI;
import com.tonic.api.game.MovementAPI;
import com.tonic.api.widgets.InventoryAPI;
import com.tonic.api.widgets.PrayerAPI;
import com.tonic.data.wrappers.ActorEx;
import com.tonic.data.wrappers.ItemEx;
import com.tonic.data.wrappers.NpcEx;
import com.tonic.data.wrappers.PlayerEx;
import com.tonic.data.wrappers.TileItemEx;
import com.tonic.util.handler.StepContext;
import com.tonic.util.handler.StepHandler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.runelite.api.Client;
import net.runelite.api.Projectile;
import net.runelite.api.TileItem;
import net.runelite.api.coords.WorldPoint;

public class FightBuilder extends TormentedDemonsBuilder<FightBuilder>
{
    private static final int CONSUME_TICKS = 3;
    private static final int PRAYER_TICKS = 2;
    private static final int ATTACK_TICKS = 2;
    private static final int LOOT_RANGE = 10;
    private final DemonTracker tracker = new DemonTracker();
    private final Map<Integer, Integer> wearSent = new HashMap<>();
    private String refused;

    private FightBuilder(TormentedDemonsPlugin plugin)
    {
        super(plugin);
    }

    public static StepHandler fighting(TormentedDemonsPlugin plugin)
    {
        FightBuilder builder = new FightBuilder(plugin);
        int step = builder.currentStep;
        return builder.add(context -> {
            return builder.fight(context, step);
        }).build();
    }

    private int fight(StepContext context, int step)
    {
        PlayerEx player = PlayerEx.getLocal();
        WorldPoint here = player.getWorldPoint();
        TileItemEx loot = loot();
        String leave = Combat.leaveReason(Supplies.foodCount(config), TormentedDemonsAPI.hitpointsPercent(),
                config.eatAt(), config.emergencyAt(), Supplies.restores(config).size(), TormentedDemonsAPI.prayer(), config.restoreAt(),
                InventoryAPI.isFull() && loot != null);
        if (leave != null)
        {
            TormentedDemonsAPI.teleportOut();
            plugin.setStatus(leave + ". Teleporting to the bank");
            return END_EXECUTION;
        }
        if (!Places.inTemple(here))
        {
            plugin.setStatus("Left the temple");
            return END_EXECUTION;
        }
        NpcEx demon = target(player);
        Set<WorldPoint> bombs = bombTiles();
        CombatStyle threat = demon == null ? tracker.getStyle() : tracker.track(demon.getIndex(), demon.getWorldPoint(),
                demon.getNpc().getAnimation(), player.hasGraphic(Combat.BIND) || !bombs.isEmpty(), demon.getWorldArea().distanceTo(here));
        protect(context, threat, demon != null);
        heal(context);
        if (bombs.contains(here))
        {
            dodge(here, bombs, demon);
            return step;
        }
        if (demon == null)
        {
            empower(context, null);
            lootOrEngage(context, loot);
            return step;
        }
        attack(context, player, demon);
        return step;
    }

    private NpcEx target(PlayerEx player)
    {
        ActorEx<?> interacting = player.getInteracting();
        if (interacting instanceof NpcEx && Combat.isDemon(((NpcEx) interacting).getId()) && !interacting.isDead())
        {
            return (NpcEx) interacting;
        }
        return NpcAPI.search().withIds(Combat.DEMONS).alive().interactingWith(player).nearest();
    }

    private Set<WorldPoint> bombTiles()
    {
        Client client = Static.getClient();
        Set<WorldPoint> tiles = new HashSet<>();
        for (Projectile projectile : client.getProjectiles())
        {
            if (projectile.getId() == Combat.FIRE_BOMB && projectile.getRemainingCycles() > 0 && projectile.getTargetPoint() != null)
            {
                tiles.add(projectile.getTargetPoint());
            }
        }
        return tiles;
    }

    private void protect(StepContext context, CombatStyle threat, boolean fighting)
    {
        PrayerAPI wanted = fighting ? (threat == null ? CombatStyle.MAGIC : threat).protection() : null;
        if (wanted == context.get("prayer") && !since(context, "prayed", PRAYER_TICKS))
        {
            return;
        }
        if (wanted != null && !wanted.isActive())
        {
            wanted.turnOn();
            context.put("prayer", wanted);
            stamp(context, "prayed");
            return;
        }
        for (CombatStyle style : CombatStyle.values())
        {
            if (wanted == null && style.protection().isActive())
            {
                style.protection().turnOff();
                context.remove("prayer");
                stamp(context, "prayed");
            }
        }
    }

    private void empower(StepContext context, CombatStyle style)
    {
        if (!config.offensivePrayer() || !since(context, "empowered", PRAYER_TICKS))
        {
            return;
        }
        PrayerAPI wanted = style == null ? null : style.offence();
        if (wanted != null && !wanted.isActive())
        {
            wanted.turnOn();
            stamp(context, "empowered");
            return;
        }
        for (CombatStyle each : CombatStyle.values())
        {
            PrayerAPI prayer = each.offence();
            if (wanted == null && prayer != null && prayer.isActive())
            {
                prayer.turnOff();
                stamp(context, "empowered");
            }
        }
    }

    private void heal(StepContext context)
    {
        if (TormentedDemonsAPI.hitpointsPercent() <= config.eatAt() && since(context, "ate", CONSUME_TICKS))
        {
            ItemEx food = Supplies.food(config);
            if (food != null)
            {
                InventoryAPI.interact(food, "Eat");
                stamp(context, "ate");
            }
        }
        if (TormentedDemonsAPI.prayer() <= config.restoreAt() && since(context, "drank", CONSUME_TICKS))
        {
            List<ItemEx> restores = Supplies.restores(config);
            if (!restores.isEmpty())
            {
                InventoryAPI.interact(restores.get(0), "Drink");
                stamp(context, "drank");
            }
        }
    }

    private void dodge(WorldPoint here, Set<WorldPoint> bombs, NpcEx demon)
    {
        WorldPoint safe = Combat.safeTile(here, bombs, demon == null ? null : demon.getWorldArea(), MovementAPI::canPathTo);
        if (safe == null)
        {
            plugin.setStatus("No safe tile from the fire bomb");
            return;
        }
        MovementAPI.walkToWorldPoint(safe.getX(), safe.getY(), true);
        plugin.setStatus("Stepping away from a fire bomb");
    }

    private void lootOrEngage(StepContext context, TileItemEx loot)
    {
        if (paused(context))
        {
            return;
        }
        if (loot != null)
        {
            TileItemAPI.interact(loot, "Take");
            acted(context);
            plugin.setStatus("Looting " + loot.getName());
            return;
        }
        if (dropPending())
        {
            plugin.setStatus("Waiting for the drop");
            return;
        }
        NpcEx next = NpcAPI.search()
                .withIds(Combat.DEMONS)
                .alive()
                .walkable()
                .keepIf(npc -> npc.getInteracting() == null && room.getArea().contains(npc.getWorldPoint()))
                .nearest();
        if (next == null)
        {
            plugin.setStatus("Waiting for a Tormented Demon in the " + room.getName().toLowerCase());
            return;
        }
        NpcAPI.interact(next, "Attack");
        acted(context);
        plugin.setStatus("Attacking a Tormented Demon");
    }

    private boolean dropPending()
    {
        Integer killed = tracker.getDemon();
        return killed != null && !NpcAPI.search().withIndex(killed).keepIf(NpcEx::isDead).isEmpty();
    }

    private void attack(StepContext context, PlayerEx player, NpcEx demon)
    {
        CombatStyle demonPrayer = tracker.prayer(Combat.demonPrayer(demon.getNpc().getOverheadSpriteIds()));
        CombatStyle style = Combat.chooseStyle(Supplies.armedStyles(config), demonPrayer);
        if (style == null)
        {
            plugin.setStatus("No carried gear can damage this demon");
            return;
        }
        boolean switched = equip(style);
        empower(context, style);
        boolean onTarget = demon.equals(player.getInteracting());
        if (switched || !onTarget && since(context, "attacked", ATTACK_TICKS))
        {
            NpcAPI.interact(demon, "Attack");
            stamp(context, "attacked");
        }
        plugin.setStatus(refused != null ? refused + " stays in the inventory after wearing it. Check the " + style + " gear setting"
                : "Fighting with " + style + (demonPrayer == null ? "" : ". The demon protects from " + demonPrayer));
    }

    private boolean equip(CombatStyle style)
    {
        List<String> tokens = Supplies.gear(Supplies.gearSetting(config, style));
        List<ItemEx> items = InventoryAPI.getItems();
        List<Integer> held = items.stream().map(ItemEx::getId).collect(Collectors.toList());
        Client client = Static.getClient();
        int tick = client.getTickCount();
        boolean sent = false;
        refused = null;
        for (ItemEx item : items)
        {
            if (tokens.stream().noneMatch(token -> Supplies.matches(token, item.getId(), item.getName())))
            {
                continue;
            }
            if (Supplies.wearNow(wearSent, held, item.getId(), tick))
            {
                InventoryAPI.interact(item, "Wield", "Wear", "Equip");
                sent = true;
            }
            else if (Supplies.wearRefused(wearSent, item.getId(), tick))
            {
                refused = item.getName();
            }
        }
        return sent;
    }

    private TileItemEx loot()
    {
        List<String> always = Supplies.gear(config.lootAlways());
        return TileItemAPI.search()
                .within(LOOT_RANGE)
                .keepIf(item -> item.getItem().getOwnership() == TileItem.OWNERSHIP_SELF
                        && Supplies.worthTaking(always, item.getId(), item.getName(),
                                Supplies.value(item.getCanonicalId(), item.getQuantity(), item.getGePrice()), config.lootMinimum()))
                .nearest();
    }
}
