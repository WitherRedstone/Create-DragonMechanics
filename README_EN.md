# Create: Ice and Fire

## Introduction

A small mod that connects *Ice and Fire: Dragons* with *Create*.
An Ice and Fire dragon forge only works while a tamed dragon breathes fire on it — hard to arrange, and it takes up a lot of space. Create already has a complete encased fan airflow system, and Ice and Fire already has dragon skulls. Put the two together and a skull becomes a fan catalyst: blow air through a dragon skull lying in the airflow and the stream turns into dragon breath.
So you no longer need to raise a dragon. Place a skull in the airflow and you get fire, ice or lightning breath, which you can use to process items, heat dragon forges and scorch whatever stands in the stream. The scope is deliberately narrow: only the "skull + encased fan + dragon forge" line, no new machines, blocks or items, and the recipes are Ice and Fire's own dragon forge recipes.

## Description

The mod turns the three Ice and Fire dragon skulls into Create fan processing types. While an air current is being built, every block along it is checked for a catalyst; once the flow passes a skull, the rest of the stream carries that element until the current ends or is blocked. The skull is an entity, so it never blocks the airflow — you can place it anywhere in the path, it does not occupy a block, and no extra blocks or block entities have to be registered.
The three breaths are independent: fire breath works with fire dragonsteel and fire dragon forges, ice with ice, lightning with lightning. A mismatched dragon type still produces a breath, it just has no effect on that particular forge.
Airflow strength (fan RPM) matters: the faster the fan, the quicker items are processed, the faster a forge heats up, the stronger the damage and elemental effects on creatures, and the denser the particles and sounds. At low speed the stream still carries the element, it simply advances slowly.
In JEI the mod registers one "fan processing" category per breath, reading exactly the three Ice and Fire dragon forge recipes. If a modpack changes those recipes or adds new ones, the categories follow automatically.

## Features

**Dragon skull catalysts**: place a fire, ice or lightning dragon skull in an encased fan's airflow and the stream picks up that breath. Catalysts are entities, not blocks, so they can float, sit on a slab, and never block the flow.
**Item processing with dragon forge recipes**: items drifting in the stream are processed using Ice and Fire's dragon forge recipes — an iron ingot turns into a dragonsteel ingot in fire breath, and blood recipes work the same way. Only the input item and the dragon type matter; no actual forge is needed, the airflow itself acts as an invisible dragon forge.
**Heating dragon forges**: blow the matching breath at a dragon forge and it behaves as if a real dragon were breathing on it. The forge's flame timer keeps getting refreshed and its cooking progress advances until the result pops out; this also works when the airflow hits the forge's outer shell instead of the opening. Nothing is wasted if the input is wrong or the output is full — the forge behaves exactly as it normally would.
**Effects on creatures in the stream**: anything standing in the breath takes damage of the matching type plus an elemental effect — fire sets it alight, ice freezes it, lightning knocks it back along the airflow. Damage and duration scale with airflow strength.
**Block transformation**: blocks that keep getting hit by the breath are transformed using Ice and Fire's own rules (scorched, frosted, struck by lightning and so on). This only happens when the `mobGriefing` game rule allows it, and dragon forges and dragon-proof blocks are skipped.
**Visuals and sounds**: elemental particles are spawned along the stream, skulls play their breath sound at intervals (fire, ice and lightning differ; ice is a little lower, lightning a little higher), and lightning breath also draws a jagged arc from in front of the skull all the way down the stream.
**JEI support**: three "fan processing" categories show the fire, ice and lightning dragon forge recipes, with the respective skull as the category icon and the skull itself rendered in front of the fan in the recipe screen.
**Dragon forge automation**: the forge core and bricks expose item handling, so hoppers, mechanical arms and similar devices can feed materials and blood in and pull results out without manual insertion.
**Configurable**: `config/create_iceandfire-common.toml` has five sections — whether each of the three breaths is enabled, whether item processing and forge heating are enabled, airflow strength and range, damage and effect duration for creatures, and whether block transformation is allowed. Everything is on by default.

## Usage

1. Build an encased fan, give it rotational power and set its direction so it blows a stream of air.
2. Place a dragon skull of the matching type in that stream — anywhere along the path, it does not have to touch the fan. The airflow picks up the breath right away; Create rebuilds the current about once a second, so placing or breaking a skull shows its effect within roughly a second.
3. To process items: drop items into the stream, or feed them in with mechanical arms, belts or hoppers. They are converted according to the dragon forge recipe of the matching type.
4. To heat a dragon forge: aim the stream at the forge's input opening (hitting the outer shell also works). The forge then cooks normally; it still has to be assembled the Ice and Fire way and loaded with materials.
5. To see what can be processed: search "fan processing" in JEI. The three categories correspond to the three breaths and list the available dragon forge recipes.
6. To adjust strength: raising the fan's RPM speeds up processing and heating and strengthens the effects on creatures; lowering it slows everything down. Range and damage can also be tuned in the config file.
7. If the stream seems to do nothing, check in order: is the skull really in the airflow, does the dragon type match the forge, is that breath enabled in the config, and does the game rule allow block transformation.
