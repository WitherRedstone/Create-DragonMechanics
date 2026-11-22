# Create: Ice and Fire

## Introduction

Use a Create Encased Fan to speed up the Ice and Fire Dragon Forge.
An Ice and Fire dragon forge only works while a tamed dragon breathes fire on it — hard to arrange, and it takes up a lot of space. Create already has a complete encased fan airflow system, and Ice and Fire already has dragon skulls. Put the two together and a skull becomes a fan catalyst: blow air through a dragon skull lying in the airflow and the stream turns into dragon breath.
So you no longer need to raise a dragon. Place a skull in the airflow and you get fire, ice or lightning breath, which you can use to heat dragon forges, scorch whatever stands in the stream and corrode the blocks along it. The scope is deliberately narrow: only the "skull + encased fan + dragon forge" line, no new machines, blocks or items.
Requirements: Minecraft 1.21.1, NeoForge 21.1 or newer, Create 6.0.10 or newer, Ice and Fire: Dragons (CE edition).

## Description

The mod turns the three Ice and Fire dragon skulls into Create fan processing types. While an air current is being built, every block along it is checked for a catalyst; once the flow passes a skull, the rest of the stream carries that element until the current ends or is blocked. The skull is an entity, so it never blocks the airflow — you can place it anywhere in the path, it does not occupy a block, and no extra blocks or block entities have to be registered.
The three breaths are independent: fire breath works with fire dragon forges, ice with ice, lightning with lightning. A mismatched dragon type still produces a breath, it just has no effect on that particular forge.
Breath only heats and affects; it never substitutes. Anything that has to be smelted in a dragon forge still needs a real forge — the airflow will not turn an iron ingot into a dragonsteel ingot on its own. That keeps Ice and Fire's progression intact and leaves the fan as a convenient way to fire the forge.
Airflow strength (fan RPM) matters: the faster the fan, the faster a forge heats up, the stronger the damage and elemental effects on creatures, and the denser the particles and sounds. At low speed the stream still carries the element, it simply advances slowly.

## Features

**Dragon skull catalysts**: place a fire, ice or lightning dragon skull in an encased fan's airflow and the stream picks up that breath. Catalysts are entities, not blocks, so they can float, sit on a slab, and never block the flow.
**Heating dragon forges**: blow the matching breath at a dragon forge and it behaves as if a real dragon were breathing on it. The forge's flame timer keeps getting refreshed and its cooking progress advances until the result pops out; this also works when the airflow hits the forge's outer shell instead of the opening. Nothing is wasted if the input is wrong or the output is full — the forge behaves exactly as it normally would. Only skulls of **stage 4 or higher** can do this: a hatchling's skull still harms creatures and corrodes blocks, but it cannot drive a forge.
**Effects on creatures in the stream**: anything standing in the breath takes damage of the matching type plus an elemental effect — fire sets it alight, ice freezes it, lightning knocks it back along the airflow. Damage and duration scale with airflow strength.
**Block transformation**: blocks that keep getting hit by the breath are transformed using Ice and Fire's own rules (scorched, frosted, struck by lightning and so on). This only happens when the `mobGriefing` game rule allows it, and dragon forges and dragon-proof blocks are skipped.
**Visuals and sounds**: elemental particles are spawned along the stream, skulls play their breath sound at intervals (fire, ice and lightning differ; ice is a little lower, lightning a little higher), and lightning breath also draws a jagged arc from in front of the skull all the way down the stream.
**Dragon forge automation**: the forge core and bricks expose item handling, so hoppers, mechanical arms and similar devices can feed materials and blood in and pull results out without manual insertion.
**Configurable**: `config/create_iceandfire-common.toml` has five sections — whether each of the three breaths is enabled, whether forge heating is enabled, airflow strength and range, damage and effect duration for creatures, and whether block transformation is allowed. Everything is on by default.

## Usage

1. Build an encased fan, give it rotational power and set its direction so it blows a stream of air.
2. Place a dragon skull of the matching type in that stream — anywhere along the path, it does not have to touch the fan. The airflow picks up the breath right away; Create rebuilds the current about once a second, so placing or breaking a skull shows its effect within roughly a second.
3. Build and assemble an Ice and Fire dragon forge, then load it with the input materials and dragon blood.
4. Aim the breath stream at the forge's input opening (hitting the outer shell also works). The forge then cooks normally, exactly as it would with a real dragon. Note that only stage 4 or higher skulls can heat a forge — a hatchling's skull just harms and corrodes.
5. To adjust strength: raising the fan's RPM speeds up heating and strengthens the effects on creatures; lowering it slows everything down. Range and damage can also be tuned in the config file.
6. If the stream seems to do nothing, check in order: is the skull really in the airflow, does the dragon type match the forge, is that breath enabled in the config, and does the game rule allow block transformation.
