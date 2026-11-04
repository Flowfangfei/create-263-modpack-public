# 1.2.0+fabric.26.2

- Ported the build and runtime registration to Fabric, Minecraft 26.2, Java 25 and Create Fly 6.0.9-2.
- Migrated electrical simulation, machines, panels, networking, configurations, saved data and registry remaps to the current APIs.
- Migrated train and carriage electrical persistence, synchronization, schematics and railway integrations to Create Fly.
- Replaced Forge Energy capabilities with transactional Fabric Energy storage.
- Migrated rendering, Flywheel visuals, Ponder, screens, tools, fluids and item definitions to Minecraft 26.2.
- Updated recipes, fluid quantities, resource conditions, models and particle textures; restored missing train sound samples.
- Added fluid-output basin compacting with recipe synchronization and JEI presentation.
- Migrated optional CC:Tweaked peripherals and JEI integration.
- Isolated integrations whose upstream mods have no compatible Fabric 26.2 release under `src/unsupported`.
- Added unit, server GameTest and client rendering coverage.
- Replaced automatic publishing workflows with a read-only build and test workflow.
