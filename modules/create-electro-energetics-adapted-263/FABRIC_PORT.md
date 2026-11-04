# Fabric 26.2 development reference

The source was ported from upstream commit `4828c34` on `1.21.1`. This standalone repository uses the branch `fabric-26.2`; the initial Git history is prepared separately from upstream.

## Runtime dependencies

| Dependency | Version used |
| --- | --- |
| Minecraft | 26.2 |
| Java | 25 |
| Fabric Loader | 0.19.5 |
| Fabric API | 0.161.0+26.2 |
| Create Fly | 26.2-6.0.9-2, mod version 6.0.9-2 |
| Fabric Energy API | 5.0.0, bundled |
| CC:Tweaked, optional | 1.120.2 for Fabric 26.2 |
| JEI, optional | 30.42.0.236 for Fabric 26.2 |

The build pins these versions in `gradle.properties` and `build.gradle`. Create Fly supplies its own Flywheel, Ponder and Catnip implementations. Install the Fabric 26.2 builds of the dependencies on both sides where required.

## Build and run

Use the checked-in Gradle wrapper, version 9.7.1, with a Java 25 JDK. The daemon JVM criteria in `gradle/gradle-daemon-jvm.properties` select Java 25 even if an older Java is the shell default.

```sh
./gradlew build
./gradlew runClient
./gradlew runServer
```

Use `.\gradlew.bat` instead of `./gradlew` in PowerShell. A normal dedicated server requires the operator to review Minecraft's EULA. The automated GameTest server uses Fabric's test runner.

The playable artifact is `build/libs/electroenergetics-fabric-26.2-1.2.0+fabric.26.2.jar`. The sources artifact has the `-sources.jar` suffix.

```sh
./gradlew test
./gradlew runGameTest
./gradlew runClientGameTest
./gradlew runGameTest runClientGameTest -PwithOptionalCompat
./gradlew runDatagen
```

`-PwithOptionalCompat` adds the pinned CC:Tweaked and JEI releases to the development runtime. They remain optional and are not bundled into the release JAR. `build` runs JUnit and server GameTests. Client tests need an OpenGL-capable display; on Linux use `xvfb-run -a ./gradlew build runClientGameTest`.

Datagen writes to `src/generated/resources`. Converted, checked-in recipe JSON is the editable source for recipe providers; regeneration preserves Fabric load conditions and the fluid-processing schema. Other shipped models, tags, loot tables and translations remain checked in. Do not remove these resources before regenerating.

## Configuration and saved data

Server and client settings are stored in `config/electroenergetics/server.json` and `client.json`. The server sends its settings to connected clients. Reload retains configuration object identities used by running devices; disconnect clears the remote settings overlay.

Block entities keep their original CEE NBT fields through the 26.2 value-input/output APIs. Infrastructure and device saved data use namespaced codecs and import legacy `.dat` files on first access, leaving the legacy file available. Train and carriage persistence and network codecs include their electrical fields. The old `electric_motor` identifier and renamed panel attachments resolve through Fabric registry aliases.

These checks cover CEE's data. A complete NeoForge modpack world also depends on the availability and migration of its other mods. Review world upgrades using a copy of the world.

## Integration boundaries

Core electricity, machines, wires, panels, Create contraptions, railway power and pantographs, schematics, Ponder, displays and client tools use Fabric/Create Fly APIs. The converter exposes transactional Fabric Energy storage. CC:Tweaked peripherals and JEI recipe/ghost ingredient integrations load when those mods are present.

No compatible Fabric 26.2 upstream artifacts were available for the original Sable, Sable Companion, Aeronautics, Offroad, Simulated or Strut Your Stuff integrations when this port was prepared. Their foreign APIs are not bundled or imitated. Sable assembly/fan mixins and the Strut block source/resources are retained under `src/unsupported`, outside the runtime source set. Normal-world coordinate and sensor behavior uses `SpatialIntegration`; a compatible moving-world addon can register its provider there.

The plant-oil press recipe uses `electroenergetics:fluid_compacting`, because Create Fly's native compacting recipe supports item output. This recipe retains basin/press execution, Fabric synchronization, fluid quantities and JEI presentation.

## Verification coverage

JUnit covers NBT value-view round trips and RMS sample handling. Dedicated-server GameTests exercise block-entity construction and persistence, powered motor/open-circuit behavior, alternator output, loaded transformer ratio changes, bulb burnout, fuse trips, panel attachment state, converter transaction rollback and commit, saved-data reload and legacy migration, registry aliases, catenary, schematic rotation, train/carriage persistence and networking, asynchronous simulation delivery, configuration reload, and actual plant-oil processing.

The client GameTest starts an integrated world, checks registered block/item/partial models, renders machines and both wire backends, opens the CEE screens, compiles Ponder scenes and renders a scene, and captures world, UI and hand-tool screenshots in `build/run/clientGameTest/screenshots/electroenergetics`. The test source set is excluded from the release JAR.

The GitHub workflow builds and tests with read-only repository permissions. Publishing, releases, tags and remote changes remain manual.

## API references

- [Create Fly 26.2 source](https://github.com/ZurrTum/Create-Fly/tree/26.2)
- [Fabric migration to 26.2](https://docs.fabricmc.net/develop/porting/26.2)
- [Fabric networking](https://docs.fabricmc.net/develop/networking)
- [Fabric data generation setup](https://docs.fabricmc.net/develop/data-generation/setup)
- [Fabric API Loom DSL](https://docs.fabricmc.net/develop/loom/fabric-api)
- [Fabric Energy API source](https://github.com/TechReborn/Energy)
