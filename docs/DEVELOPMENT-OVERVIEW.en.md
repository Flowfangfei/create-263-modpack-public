# How Create263 was developed

[中文](DEVELOPMENT-OVERVIEW.md)

This overview is for developers maintaining the ports in Create263. It follows the work from recovering a maintainable Create base, through railway and physics migration, to a combination of eighteen direct runtime JARs. Sections follow dependencies and implementation problems. Installation is covered in the [README](../README.en.md); general migration techniques are in [Mod upgrade and testing](MOD-UPGRADE-AND-TESTING.en.md).

The current platform is Minecraft Java **26.3**, NeoForge **26.3.0.22-beta**, and Java **25**. Modules retain their separate upstream identities, mod IDs, and licences. Exact commits, reference releases, and licence scopes are recorded in [Sources and licences](SOURCE-AND-LICENSES.en.md) and the [source manifest](../manifests/sources.json).

## 1. Establishing a maintainable Create base

### Roles of the public source and reference releases

The Create base starts from public **26.2 / Adapted 0.996** source at commit `30ec1245a5af80228699453c8e59f3285a6be074`. The target reference is the **26.3 / 1.31** release; the **26.2 / 1.31** release helps narrow changes associated with the Minecraft version. Public source provides the project structure, names, and unchanged implementations. Release binaries provide the target interface and behaviour reference. [Input identities and SHA-256 hashes](SOURCE-AND-LICENSES.en.md)

Recovery merged the required differences into that public base without first reconstructing a complete intermediate 26.2 / 1.31 source tree. The project then continued fixing issues found during integration. This is a reconstructed implementation maintained here. It is not an official 26.3 source release from the original author, and byte-for-byte identity with the reference JAR is not claimed. [Source identity](SOURCE-AND-LICENSES.en.md)

### Narrowing changes at method level

Recovery treated Java classes, nested dependencies, resources, and metadata separately. Class comparison used several levels:

| Comparison | What it identifies |
| --- | --- |
| Raw class bytes | Classes that can be reused and classes needing further inspection |
| Class and member declarations | Inheritance, interfaces, field/method descriptors, visibility, generics, and constants |
| Method instructions | Expanded constant-pool references, literals, calls, branches, and exception-handler ranges |
| Namespaces and symbols | Compatibility-package moves versus changes to the underlying API contract |
| Dependencies and resources | Actual nested-JAR contents, resource paths, data schemas, and loading entrypoints |

Branches and exception handlers were compared by instruction position to reduce noise from constant-pool order, byte offsets, and compiler debug information. Comparing `invokedynamic` also requires expanding bootstrap methods and arguments. Decompiled code helped interpret and merge local changes while the public source remained the maintainable implementation. Matching an instruction representation covers that comparison only; external implementations, runtime verification, and lifecycle behaviour require separate checks. [Recovery method](SOURCE-AND-LICENSES.en.md)

The public tree contains the recovered source, build transformations, and fixed provenance manifests. The complete historical decompiler cache and per-method analysis outputs are not distributed with it. Links below point to the current implementations and subsequent validation records; historical single-file checks are kept distinct from whole-pack acceptance.

### A concrete behaviour change: atomic item replacement

[ResourceHandlerItemAdapter](../modules/create-adapted-263/src/main/java/com/simibubi/create/api/contraption/storage/item/simple/ResourceHandlerItemAdapter.java) keeps the old `IItemHandler` entrypoint and delegates to native NeoForge resource transactions. Two recovered behaviours affect item conservation:

- `extractItem` caps the request at the resource's actual maximum stack size. Simulation closes the transaction without committing.
- `setStackInSlot` fully removes the old contents and fully inserts the replacement in one transaction. Partial extraction or insertion returns early, allowing rollback to restore the previous state.

During recovery, this file was compiled on its own and checked against the target method structure. Full builds, dedicated-server execution, client execution, and save/reopen checks followed as separate stages. The later whole-combination scope is recorded in [initial integration validation](VALIDATION-v0.1.0-alpha.1.md); the earlier file-level comparison is narrower evidence.

## 2. Aligning NeoForge, dependency ABI, and resources

### Internal compatibility code connects old callers to native transactions

The 26.3 resource API changes affect many existing item and fluid calls. Create keeps compatibility interfaces under its own `compat/neoforge263` package and connects them to native `ResourceHandler`, `ItemResource`, `FluidResource`, and `Transaction`.

The [item adapter](../modules/create-adapted-263/src/main/java/com/simibubi/create/compat/neoforge263/items/ItemResourceHandlerAdapter.java) preserves the convention that insertion returns the remaining stack. The [fluid adapter](../modules/create-adapted-263/src/main/java/com/simibubi/create/compat/neoforge263/fluids/capability/FluidResourceHandlerAdapter.java) maps the old simulation/execution choice to whether the transaction commits. Filtering, capacity, component identity, and the accepted amount remain part of the respective contracts.

These internal classes include compatibility code derived from older NeoForge interfaces, with the original copyright and LGPL notices retained. Addons may use the bridge actually supplied by this Create build or implement the native resource API directly. The [compatibility-code notice](../LICENSES/CREATE-NeoForge-compat-NOTICE.txt) identifies the provenance and namespace scope.

### A Registrate version name does not establish its ABI

Create's build handles old `ResourceLocation` and target `Identifier` descriptors in Registrate. The current [prepareRegistrateCompileJar task](../modules/create-adapted-263/build.gradle) makes a dedicated copy of the dependency input, applies registry-entry patches and type mapping, checks key signatures, and compares the original input hash before and after transformation. The compiler classpath uses this task-owned output.

That task prepares the compile interface. Other tasks in the build perform separate release transformations and need their own audit. Registrate source and transformation provenance are retained in [vendor](../vendor/registrate-MC1.21-1.3.0+67/) and the [third-party licence manifest](../manifests/third-party-licenses.json).

Fuel registration also exposed a runtime ABI issue: the old `burnTime` path referenced a data-map field removed from the target loader. Current [AllItems](../modules/create-adapted-263/src/main/java/com/simibubi/create/AllItems.java) and [AllBlocks](../modules/create-adapted-263/src/main/java/com/simibubi/create/AllBlocks.java) set `DataComponents.COOKING_FUEL` in item properties and retain the intended burn durations. Compilation and actual class initialization cover different parts of this dependency migration.

### External assets have a loading API too

Create's official artwork is loaded through an external resource pack. [ExternalCreateAssets](../modules/create-adapted-263/src/main/java/com/simibubi/create/infrastructure/assets/ExternalCreateAssets.java) implements the target `openMetadata/openResources` methods, supplies compatibility resources for old item models and copycat blockstates, and selects visualization paths using the actual Flywheel backend.

Runtime assets and source code have separate pinned origins and hashes. The installer restores assets locally; locations and distribution scope are described in [EXTERNAL_ASSETS](../modules/create-adapted-263/EXTERNAL_ASSETS.md) and the [resource lock](../manifests/resources-lock.json).

## 3. Development dependencies and runtime namespaces

### Addons compile against this pack's actual Create outputs

Forks described as “Create 26.3” can expose different Java packages, registries, storage interfaces, and rendering systems. This pack uses `com.simibubi.create`; railway and aviation upstreams include Create Fly interfaces under `com.zurrtum.create`. Adapting those callers requires restoring the corresponding functional paths.

The [dependency staging script](../modules/steam-n-rails-263/tools/stage-create-adapted-deps.py) takes Create, Ponder, Catnip, Flywheel, Registrate, and access-transformer configuration from this repository's Create build and records their hashes. A separate compile-only compatibility JAR supplies legacy types needed during compilation. Addon release JARs must exclude these foreign classes. Later addons use the [shared platform configuration](../gradle/create263-addon.gradle) to consume the same development interfaces.

### Flywheel is isolated consistently during packaging

Source code retains development references to `dev.engine_room.flywheel`. Release transformation uses [IsolateFlywheel.java](../modules/create-adapted-263/scripts/IsolateFlywheel.java) to relocate the Java package, mod ID, resource namespace, Mixin/refmap names, and JarJar metadata according to their structure. Addon references undergo the corresponding transformation.

| Transform | Preserve |
| --- | --- |
| Java package `dev/engine_room/flywheel` | Create's own `content/kinetics/flywheel` package |
| Runtime mod ID and resource namespace to `create_flywheel` | The Create block ID `create:flywheel` |
| Flywheel Mixin, refmap, and nested-JAR paths | The internal shader directory `flywheel/` |
| References identified as belonging to the library | Unrelated strings, content model IDs, and existing isolation prefixes |

This is a structural relocation step. A global replacement of every occurrence of `flywheel` would alter content and internal paths. The [packager](../scripts/package.py) checks unisolated references, split Java packages, bundled foreign classes, loader metadata, and empty dependency ranges, then records input and output hashes. Shader compilation and drawing remain runtime checks.

## 4. Steam ’n’ Rails: reconnecting a different Create implementation

Railways starts from an existing community NeoForge 26.3 port and adapts it to this pack's Create. The work covers registration, track materials, behaviours, fluids, inventories, persistence, models, and render submission. Local bridges live under [internal/compat](../modules/steam-n-rails-263/common/src/main/java/com/railwayteam/railways/internal/compat/).

### Timing eager factories across registries

The inherited railway factories access several registries immediately. Running them in the ITEM event reaches some Create block-entity suppliers before binding. Current [RailwaysNeoForge](../modules/steam-n-rails-263/neoforge/src/main/java/com/railwayteam/railways/neoforge/RailwaysNeoForge.java) uses `GameData.getRegistrationOrder()` to run those factories in the final mutable `RegisterEvent`. The [initialization coordinator](../modules/steam-n-rails-263/neoforge/src/main/java/com/railwayteam/railways/neoforge/RailwaysNeoForgeInit.java) manages event-bus binding and queued callbacks.

This choice depends on the pinned loader's registry order and freeze rules. A future conversion of these eager factories to complete DeferredRegister usage should revisit their phases. A loader upgrade also needs to recheck this boundary.

### Data and rendering require separate adaptations

The [resource-processing tasks](../modules/steam-n-rails-263/build.gradle.kts) handle Create recipe fields, fluid inputs and outputs, paint components, loading conditions, advancements, and tags, with checks over generated results. Old Create Fly droplet quantities and the target mB convention must be converted at the boundary. The paint pitcher's [native container implementation](../modules/steam-n-rails-263/fabric/src/main/java/com/railwayteam/railways/content/palettes/painting/fabric/PaintPitcherFluidStorage.java) permits only whole paint levels to move.

The render bridge's [CompleteVertexConsumer](../modules/steam-n-rails-263/common/src/main/java/com/railwayteam/railways/internal/compat/client/render/CompleteVertexConsumer.java) supplies normals, overlay, extra UV, and line width for the old Catnip quad stream. Connected textures also require the correct atlas sources. Resource generation and the final JAR both belong to that check.

Railways' [world GameTests](../modules/steam-n-rails-263/neoforge/src/main/java/com/railwayteam/railways/neoforge/RailwaysGameTests.java) cover a real track network, the fuel tank, and bulk block/entity loading. Subsequent whole-pack records retain normal resource-build and joint-loading results. [Initial integration validation](VALIDATION-v0.1.0-alpha.1.md) keeps complete train operation, scheduling, couplers, and multiplayer conductor interactions as separate test scopes.

## 5. Sable → Simulated → Offroad / Aeronautics

The physics upstreams target Fabric 26.3. Here, Sable is migrated as a separate NeoForge dependency. Simulated supplies physical machinery and shared platform integration, with Offroad and Aeronautics built on top. Runtime code uses native NeoForge entrypoints, registries, events, networking, and transactions. Source-set rules exclude retained Fabric entrypoints and reference-only dependencies. [Sable build](../modules/sable-adapted-263/build.gradle), [shared aviation build](../modules/create-aeronautics-adapted-263/gradle/neoforge-module.gradle)

### Sable's plot format and native chunk events

A Sable plot uses its own chunk save format. [SablePlotPlatformImpl](../modules/sable-adapted-263/src/neoforge/java/dev/ryanhcode/sable/neoforge/platform/SablePlotPlatformImpl.java) restores auxiliary lighting and attachments, then uses `SerializableChunkData.copyOf` after the chunk and block entities have been restored. The resulting real chunk snapshot is supplied to the typed `ChunkDataEvent.Load`. Parsing plot NBT directly as vanilla region NBT would require fields absent from the plot schema.

[SableRuntimeProbe](../modules/sable-adapted-263/src/neoforge/java/dev/ryanhcode/sable/neoforge/SableRuntimeProbe.java) defines checks for gravity, terrain collision, mass, finite velocity, and restored body identity and block entities. Probe source specifies the acceptance conditions. Executed combined propulsion and reload results are recorded in [initial integration validation](VALIDATION-v0.1.0-alpha.1.md).

### Native transactions include business state

Simulated's [backend transaction table](../modules/create-aeronautics-adapted-263/simulated/src/neoforge/java/dev/simulated_team/simulated/multiloader/transfer/BackendTransactions.java) shares one SnapshotJournal among wrappers of the same actual inventory, tank, or battery. Rollback restores that backend, and business notifications run after the root transaction commits.

This covers quantities together with derived inventory indexes, connected endpoints, and business callbacks. [Transfer checks](../modules/create-aeronautics-adapted-263/simulated/src/neoforge/java/dev/simulated_team/simulated/neoforge/TransferRuntimeChecks.java) cover components, capacity, shared backends, nested transactions, and docking. The [aviation probe](../modules/create-aeronautics-adapted-263/aeronautics/src/neoforge/java/dev/eriksonn/aeronautics/neoforge/AeroRuntimeProbe.java) separately checks that simulated ammunition extraction preserves cannon loading and charge state.

### Rendering ownership for moving plots

Flywheel's decision to skip a vanilla block-entity renderer must match the Visual actually providing the geometry. During migration, some blocks in moving plots lacked complete instanced rendering. Skipping BER extraction early made geometry such as propeller blades disappear.

Current [SmartBlockEntityRenderer](../modules/create-aeronautics-adapted-263/simulated/src/main/java/dev/simulated_team/simulated/compat/create/SmartBlockEntityRenderer.java) stores plot identity in RenderState during extraction and restores the corresponding scope during submission, with cleanup in `finally`. Normal worlds retain Flywheel. Moving plots with this port's Smart renderer use real BER geometry. [Client factory registration](../modules/create-aeronautics-adapted-263/simulated/src/main/java/dev/simulated_team/simulated/SimulatedClient.java) connects that selection.

[SableCreateRenderContext](../modules/create-aeronautics-adapted-263/simulated/src/main/java/dev/simulated_team/simulated/compat/create/SableCreateRenderContext.java) also scopes explicit sublevel extraction so state does not leak between objects or render calls. Advanced visualization, every optimization-mod path, and all vehicle models require their own validation.

### Runtime data and datagen are maintained separately

Runtime builds keep the generated models, recipes, tags, and other functional resources. Older automatic data-generation APIs and callbacks are documented separately in [Runtime and datagen](../modules/create-aeronautics-adapted-263/docs/porting/RUNTIME-AND-DATAGEN.md). New content currently requires corresponding JSON maintenance. Excluded legacy generators and reference Mixins are outside the active runtime implementation.

Combined tests use real Create glue, physics assemblers, a 256 RPM motor, and a propeller to check assembly, climb, identity across save/reopen, and resumed propulsion. Separate checks cover burner gas supply, lift output, and the target returning to zero after shutdown. Complete balloon-vehicle movement, complex ground driving, and multiplayer synchronization retain separate boundaries. [Executed scenarios](VALIDATION-v0.1.0-alpha.1.md)

## 6. Integrating independently working modules

Combining the foundational modules exposed additional conflicts. The [nine-mod integration record](VALIDATION-v0.1.0-alpha.1.md) preserves these fixes and their tested scope:

| Integration issue | Implementation |
| --- | --- |
| Connected and its Simulated bridge shared a package, causing a split-package error | Move the bridge to `com.hlysine.create_connected_simulated` and update reflection and registration callers |
| The optional bridge had an empty dependency range | Use the actual Simulated version range; reject empty ranges during packaging |
| Sable overwrote the broadcast method, invalidating the original conductor-coordinate injection | [Conditionally wrap the position call](../modules/steam-n-rails-263/fabric/src/main/java/com/railwayteam/railways/fabric_mixin/conductor_possession/PlayerListSableMixin.java), preserving sublevel distance calculation and conductor camera position |
| New addons needed the real physics interfaces | Compile against the same Create/Sable/Simulated outputs and keep separate runtime mod IDs and metadata |

The [build entrypoint](../scripts/build.py) follows dependency order, and a [cross-process lock](../scripts/gradle_lock.py) limits Gradle writers. Compile inputs, isolated runtime JARs, and payloads stripped of restricted assets receive separate hashes. The [runtime manifest](../manifests/runtime-modules.json) describes eighteen direct JARs; Ponder, Catnip, Flywheel, and Registrate are supplied through Create's build and nested-dependency system.

## 7. Extending processing, logistics, storage, and electricity

Later modules reuse that base while adding resource transactions, persistence, networking, and business lifecycles. The [three-addon note](THREE-ADDONS-26.3.md) and [six-module note](FIVE-ADDONS-26.3.md) retain feature scope, provenance, and detailed technical entrypoints.

| Module group | Main implementation and effect |
| --- | --- |
| Liquid Fuel | [Container and burner transfer](../modules/create-liquid-fuel-adapted-263/src/main/java/com/forsteri/createliquidfuel/core/BurnerStomachHandler.java) extracts and inserts in one transaction, rolls back insufficient capacity, and returns the actual component-bearing remainder |
| Ore Excavation | The [port record](../modules/create-ore-excavation-adapted-263/docs/PORTING-26.3.md) covers native registration, FIFO transactions, persistence, recipe networking, and real drilling/pump transfer |
| Transmission & Linkage | The [port record](../modules/transmission-linkage-adapted-263/docs/PORTING-26.3.md) explains Sable constraints and endpoint remapping across assembly; runtime handles are separate from persisted UUIDs and coordinates |
| Electro, Dragons Plus, Enchantment Industry | Merge Minecraft migration points from fixed newer Fly sources into complete NeoForge feature trees, reconnecting native transactions, processing callbacks, and networking on both sides |
| Additional Logistics, FluidLogistics | The [logistics record](porting-notes/STORAGE-LOGISTICS-26.3.md) and [fluid record](porting-notes/FLUID-26.3.md) cover packages, component identity, shared capacity, transactions, and machine paths |

### Storage keeps large quantities in its own inventory

[StorageContents](../modules/create-storage-adapted-263/src/main/java/net/fxnt/fxntstorage/inventory/StorageContents.java) stores identity as a complete count-one item template and represents the logical quantity separately. Inputs are validated before state is replaced. Its [transfer adapter](../modules/create-storage-adapted-263/src/main/java/net/fxnt/fxntstorage/util/StorageItemTransfer.java) shares transaction journals by actual physical owner.

Vanilla ItemStack/ItemStackTemplate, chests, player slots, and the cursor keep native rules. A [dedicated menu synchronizer](../modules/create-storage-adapted-263/src/main/java/net/fxnt/fxntstorage/inventory/StorageMenuSynchronizer.java) handles private quantities while preserving native click-state checks. The real client completed `1024 → private slot 960 + cursor 64 → private slot 1024`, with matching server/client quantities and components. [Validation record](VALIDATION.md)

Automatic migration of old Storage worlds remains unverified. Current format, transaction, and network results apply to the tested inputs and combination. Unknown older formats need separate identification and migration.

## 8. Validation follows the functional path

The project uses static checks, complete builds, dedicated servers, clients, real business fixtures, and independent JVM restarts to check different concerns. Manifests bind final JARs to test inputs. Recompression can change an archive hash, requiring entry-content comparisons as well. [Validation table](VALIDATION.md), [machine-readable summary](../manifests/validation-summary.json)

| Stage | Questions answered by existing records | Separate scope |
| --- | --- | --- |
| Recovery and foundational integration | Source/dependency/metadata builds and joint loading; aviation assembly and transactional save/reopen | Complete railway/vehicle features, configurations, and optional integrations |
| Three addons | Real pumping, drilling, constraints, transaction rollback, and reload | Complex production lines and large constraint systems |
| Six modules and deeper gameplay | Inventory persistence/clicks, processing outputs, electrical load, and logistics timing | Multiplayer, long-running load, and complex moving sublevels |
| Later client fixes | Real GUIs, pipeline attachments, atlas reload, Ponder templates and animation | Frame-by-frame human review of every image |

[Deeper gameplay validation](GAMEPLAY-VALIDATION-26.3.md) used a real load to expose missing server start/stop hooks in Electro. [MinecraftServerMixin](../modules/create-electro-energetics-adapted-263/src/neoforge/java/com/george_vi/electroenergetics/mixins/MinecraftServerMixin.java) restores the simulator lifecycle. Power-off negative controls, reconnection, independent restart, and clean shutdown then check its effects. A wire network existing and a motor driving a load are covered by separate assertions.

Later Ponder fixes extend client validation to virtual-world data, render pipelines, and resource lifecycles. Legacy template conversion is limited to specific block entities and fields; pipeline declarations match actual colour targets; rope sprites register before the first atlas stitch and are checked after resource reload. Code, sampling methods, and historical coverage are recorded in the [Ponder audit](PONDER-AUDIT-20261104.md). Routine regression selects representatives of the same actual logic.

For further work, use the [general migration guide](MOD-UPGRADE-AND-TESTING.en.md) to choose checks and follow this page's links to current code and concrete reports. Complex moving physics, complete railway operations, multiplayer, long-running use, absent optional integrations, and automatic old-world migration each need their own test inputs and conclusions.
