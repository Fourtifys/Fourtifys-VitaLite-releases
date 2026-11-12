# Fourtifys Tormented Demons for VitaLite

Kills Tormented Demons in the Ancient Guthixian Temple, one demon at a time. It
switches protection prayers against the demon's attacks, swaps gear away from the
style the demon protects from, steps off fire bombs, eats and restores prayer,
loots the drops, and banks at Ferox Enclave.

## What you need

- A members world.
- While Guthix Sleeps complete.
- Gear for at least two combat styles, worn or carried. The demon protects from one
  style at a time, so one style alone cannot keep damaging it.
- A ring of dueling, worn or carried, to reach the Ferox Enclave bank. With
  VitaLite 1.13.6_8 a bank trip can use two charges: the walker rubs the ring
  again on its way from the ring's landing to the bank.
- Guthixian temple teleports, food and prayer restores, carried or in the bank.

## What it does

Enabling the plugin does not start it. Switch on **Run** at the top of its settings
to begin, and off to stop. Run switches off again whenever the plugin is enabled, and
when you die.

1. If it is not ready, it teleports to Ferox Enclave with the ring of dueling,
   deposits everything it does not need to keep, and takes the food, prayer
   restores, temple teleports, any extra supplies and, if needed, a ring of
   dueling. Then it drinks from the Pool of Refreshment to restore Hitpoints,
   Prayer and run energy.
2. It reads a Guthixian temple teleport, climbs the two walls of the chasm and goes
   through the opening into the temple.
3. It walks to the chosen room and attacks the nearest free demon in it. Each
   tick it:
   - keeps the protection prayer for the demon's last attack style on. After the
     demon's binding special attack, which always changes its style, it predicts
     the next style from the demon's movement: a demon walking in will use melee,
     one that stays back will use ranged or magic;
   - steps off a tile a fire bomb is falling on, even while run is off;
   - eats and drinks a prayer restore below the set thresholds;
   - equips the gear of the first style the demon does not protect from, and
     attacks again. An item that is still in the inventory after it is worn,
     such as a second copy of a worn item, is worn once and named in the status;
   - if you choose, keeps the best attack prayer you have for that style on, such
     as Piety, Rigour or Augury.
4. After a kill it waits for the demon to fall and drop its loot, picks up its own
   drops that are on the always-take list or worth the minimum value, then
   attacks the next demon in the room.
5. It teleports out to bank when Hitpoints fall to the emergency level, when it
   runs out of food or prayer restores, or when the inventory is full of loot.

Prayer, gear switches, eating and attacking are sent in the same game tick. The
timing delay applies to travel, banking and looting.

## Settings

- **Show overlay:** runtime, kills and kills per hour, the value of the loot picked
  up, the Tormented synapses and Burning claws picked up, and the status. The status
  says why the plugin is waiting.
- **General:** the room to fight in, whether to use an attack prayer, and the gear for melee, ranged and magic, as
  item names or ids separated by commas. Leave a style empty to not use it. Styles
  are used in that order. The rooms are the plinth chamber in the south-east (8
  demons, the default), the central chamber by the entrance (2), the north-west and
  west rooms (1 each), the south-west and south rooms (3 each), and the north,
  north-east, east and lower east rooms (2 each).
- **Supplies:** the food and prayer restore to bring, how many, and when to use
  them; how many temple teleports to bring (5); other items banking leaves in the
  inventory, such as a rune pouch; and extra supplies to take, as name:quantity
  separated by commas, such as `Death rune:500`.
- **Loot:** the minimum value of a drop to pick up, at Grand Exchange prices (5,000
  coins), and items to always take (Tormented synapse, Burning claw and Guthixian
  temple teleport).
- **Emergency teleport:** the Hitpoints share at or below which it teleports to
  Ferox Enclave even with food left (25%).
- **Timing:** the random delay before each action outside the fight, in game ticks.

## Building

Build it with the Gradle wrapper: `bash gradlew build`. Gradle takes `com.tonic:api`
and `com.tonic:base-api` from Maven Local and `net.runelite:client` from the RuneLite
repository. To build against particular JARs instead, pass their paths with
`-PvitaliteClasspath=<jar>:<jar>:...`.
