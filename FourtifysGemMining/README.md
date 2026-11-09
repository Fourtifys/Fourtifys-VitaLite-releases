# Fourtifys Gem Mining for VitaLite

Mines the gem rocks in the Shilo Village gem mine and deposits the gems. It works
the surface mine or the underground mine below it.

## What you need

- A members world.
- Level 40 Mining and the Shilo Village quest.
- A pickaxe you can use, carried or worn.
- For the underground mine: the Karamja medium diary, with its reward claimed from
  Pirate Jackie the Fruit. The ladder down appears only after that.
- Optional: a gem bag. The plugin opens it, so sapphires, emeralds, rubies, diamonds
  and dragonstones go straight into it, and empties it at each deposit.

## What it does

1. If needed, it climbs the mine ladder to reach the chosen mine.
2. It walks to the rocks and mines the nearest gem rock in that mine. Each rock gives
   one gem and then needs about a minute to come back.
3. When the inventory is full, it walks to the deposit box in the Shilo Village bank
   (surface mine) or the deposit chest in the underground mine.
4. It deposits only the uncut gems, empties the gem bag, and goes back to mining.
   Your pickaxe, gem bag and other items stay in the inventory.

Deposit boxes need no bank PIN.

## Settings

- **Show overlay:** runtime, mine, gems mined and status. The status says why the
  plugin is waiting.
- **General:** the mine, and whether to use a gem bag.
- **Timing:** the random delay before each action, in game ticks.

## Building

Build it with the Gradle wrapper: `bash gradlew build`. Gradle takes `com.tonic:api`
and `com.tonic:base-api` from Maven Local and `net.runelite:client` from the RuneLite
repository. To build against particular JARs instead, pass their paths with
`-PvitaliteClasspath=<jar>:<jar>:...`.
