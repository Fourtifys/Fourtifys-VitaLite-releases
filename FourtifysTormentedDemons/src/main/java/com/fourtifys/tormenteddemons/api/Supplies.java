package com.fourtifys.tormenteddemons.api;

import com.fourtifys.tormenteddemons.TormentedDemonsConfig;
import com.fourtifys.tormenteddemons.data.CombatStyle;
import com.tonic.api.widgets.EquipmentAPI;
import com.tonic.api.widgets.InventoryAPI;
import com.tonic.data.wrappers.ItemEx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.runelite.api.gameval.ItemID;

public class Supplies
{
    public static final int TEMPLE_TELEPORT = ItemID.TELEPORTSCROLL_GUTHIXIAN_TEMPLE;
    public static final String RING = "Ring of dueling";
    public static final List<String> UNIQUES = List.of("Tormented synapse", "Burning claw");
    public static final int WEAR_TICKS = 2;

    public static List<String> gear(String setting)
    {
        return Arrays.stream(setting.split("[,\\n]"))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .collect(Collectors.toList());
    }

    public static String gearSetting(TormentedDemonsConfig config, CombatStyle style)
    {
        switch (style)
        {
            case MELEE:
                return config.meleeGear();
            case RANGED:
                return config.rangedGear();
            default:
                return config.magicGear();
        }
    }

    public static boolean matches(String token, int id, String name)
    {
        return token.matches("\\d+") ? Integer.parseInt(token) == id : token.equalsIgnoreCase(name);
    }

    public static List<CombatStyle> armedStyles(TormentedDemonsConfig config)
    {
        List<ItemEx> carried = Stream.concat(InventoryAPI.getItems().stream(), EquipmentAPI.getAll().stream())
                .collect(Collectors.toList());
        List<CombatStyle> armed = new ArrayList<>();
        for (CombatStyle style : CombatStyle.values())
        {
            List<String> tokens = gear(gearSetting(config, style));
            boolean all = !tokens.isEmpty() && tokens.stream()
                    .allMatch(token -> carried.stream().anyMatch(item -> matches(token, item.getId(), item.getName())));
            if (all)
            {
                armed.add(style);
            }
        }
        return armed;
    }

    public static boolean wearNow(Map<Integer, Integer> sentAt, Collection<Integer> held, int id, int tick)
    {
        sentAt.keySet().retainAll(held);
        return sentAt.putIfAbsent(id, tick) == null;
    }

    public static boolean wearRefused(Map<Integer, Integer> sentAt, int id, int tick)
    {
        return tick - sentAt.getOrDefault(id, tick) >= WEAR_TICKS;
    }

    public static boolean worthTaking(List<String> always, int id, String name, long value, long minimum)
    {
        return always.stream().anyMatch(token -> matches(token, id, name)) || value > 0 && value >= minimum;
    }

    public static long value(int canonicalId, int quantity, long gePrice)
    {
        return canonicalId == ItemID.COINS || canonicalId == ItemID.PLATINUM ? gePrice : gePrice * quantity;
    }

    public static Map<String, Integer> restock(String setting)
    {
        Map<String, Integer> wanted = new LinkedHashMap<>();
        for (String entry : gear(setting))
        {
            int colon = entry.lastIndexOf(':');
            String amount = colon < 0 ? "" : entry.substring(colon + 1).trim();
            if (colon > 0 && amount.matches("\\d{1,9}") && Integer.parseInt(amount) > 0)
            {
                wanted.put(entry.substring(0, colon).trim(), Integer.parseInt(amount));
            }
        }
        return wanted;
    }

    public static String badRestock(String setting)
    {
        Map<String, Integer> wanted = restock(setting);
        return gear(setting).stream()
                .filter(entry -> entry.lastIndexOf(':') <= 0 || !wanted.containsKey(entry.substring(0, entry.lastIndexOf(':')).trim()))
                .findFirst()
                .orElse(null);
    }

    public static boolean isKept(TormentedDemonsConfig config, int id, String name)
    {
        if (name == null)
        {
            return false;
        }
        Stream<String> tokens = Stream.of(
                        gear(config.meleeGear()), gear(config.rangedGear()), gear(config.magicGear()),
                        gear(config.keepItems()), new ArrayList<>(restock(config.restockItems()).keySet()))
                .flatMap(List::stream);
        return id == TEMPLE_TELEPORT || name.startsWith(RING) || name.equalsIgnoreCase(config.food())
                || doseBase(name).equalsIgnoreCase(doseBase(config.restore()))
                || tokens.anyMatch(token -> matches(token, id, name));
    }

    public static int carried(String token)
    {
        return InventoryAPI.getItems().stream()
                .filter(item -> matches(token, item.getId(), item.getName()))
                .mapToInt(ItemEx::getQuantity)
                .sum();
    }

    public static String doseBase(String name)
    {
        return name.replaceAll("\\(\\d\\)$", "");
    }

    public static ItemEx food(TormentedDemonsConfig config)
    {
        return InventoryAPI.getItem(item -> config.food().equalsIgnoreCase(item.getName()));
    }

    public static int foodCount(TormentedDemonsConfig config)
    {
        return (int) InventoryAPI.getItems().stream().filter(item -> config.food().equalsIgnoreCase(item.getName())).count();
    }

    public static List<ItemEx> restores(TormentedDemonsConfig config)
    {
        String base = doseBase(config.restore());
        return InventoryAPI.getItems().stream()
                .filter(item -> item.getName() != null && doseBase(item.getName()).equalsIgnoreCase(base))
                .sorted(Comparator.comparing(ItemEx::getName))
                .collect(Collectors.toList());
    }

    public static boolean hasRing()
    {
        return ring() != null || EquipmentAPI.isEquipped(item -> item.getName() != null && item.getName().startsWith(RING));
    }

    public static ItemEx ring()
    {
        return InventoryAPI.getItem(item -> item.getName() != null && item.getName().startsWith(RING));
    }
}
