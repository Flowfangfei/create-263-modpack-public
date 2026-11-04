# Ponder templates, rendering and regression details

[中文](PONDER-PORTING-NOTES.md) · [General upgrade methods](MOD-UPGRADE-AND-TESTING.en.md) · [Recorded audit](PONDER-AUDIT-20261104.md)

This topic retains exact virtual-world, field-compatibility and client-regression methods. It covers a client subsystem; use the general guide and development overview for transactions, persistence, physics and actual production chains. Commands require matching artifacts, independent launch templates and fixtures; the source archive does not supply seed worlds.

## 1. NBT, data components, and Ponder virtual worlds

World saves, network packets, resource templates, and Ponder's in-memory backups can use different formats. Identify the data origin and read/write chain before choosing an adaptation boundary. Legacy NBT in a resource template can be converted at template loading. This usually needs no change to real block entity saves or general item and fluid codecs.

A reviewable adapter constrains registry IDs, fields, legacy signatures, and allowed keys, and produces the same result when run again. Preserve valid modern data, unrelated additional data, and conflicting representations for reporting and further handling. These are confirmed examples; their field rules should not become global NBT replacements:

| Origin and symptom | Scoped conversion | What must be preserved |
| --- | --- | --- |
| A legacy large-inventory template fails decoding | Generate the private format only for confirmed inventory IDs and legacy fields | Slots, actual quantities, item components, filters |
| Mechanical arm or brass tunnel fails to deserialize AIR | Only in `create:mechanical_arm/HeldItem` and `create:brass_tunnel/StackToDistribute`, replace an old stack with `{}` when its only keys are `id` and `Count`, its ID is `minecraft:air`, and its integral count is 0 or 1 | Non-AIR items, modern formats, extra keys, other counts, conflicting data |
| Legacy fluid fields differ from the target format | Convert IDs, quantities, and components for confirmed containers and fields | Potion effects, bottle type, original quantity; treat capacity separately |
| Controller position or material structure changes | Adapt only confirmed block entity IDs and fields | Position semantics, direction, material identity, unrelated metadata |

The [legacy item placeholder adapter](../modules/create-aeronautics-adapted-263/simulated/src/main/java/dev/simulated_team/simulated/ponder/LegacyPonderTemplateItems.java) implements the AIR rule above. If Ponder backup creation emits an old structure again, apply the same constrained handling at its own in-memory backup boundary. The [backup adaptation entry point](../modules/create-aeronautics-adapted-263/simulated/src/main/java/dev/simulated_team/simulated/mixin/new_ponder/PonderLevelBackupMixin.java) leaves real-world save behavior intact.

Static tests can enumerate templates, compare fields before and after conversion, verify unknown-format preservation, and check repeated conversion. Dynamic tests must also exercise virtual-world restoration, script mutations, camera rendering, and backup restoration. Ponder can manually set kinetic speed without a real source. A neutral rendering factor for a confirmed source-less state, while retaining the sourced calculation, can address that display case without changing the global kinetic network.

## 2. Rendering migrations: pipelines, PiP, and texture lifecycle

Trace extraction, submission, vertex writing, materials, atlases, and output targets. Deferred rendering should use a snapshot for that frame; mutable objects may change between submission and drawing. Vertex format, shader inputs, samplers, uniform bindings, blending, depth, culling, and output attachments form a shared contract.

The main world, item previews, Ponder, and other picture-in-picture (PiP) targets can provide different attachment counts. Testing only the main world can miss pipeline validation failures in preview windows. One 26.3 flame pipeline required color outputs that matched the targets actually supplied by both the main world and PiP; copying another pipeline's multiple-target layout caused a mismatch. [Target-version pipeline example](../modules/create-aeronautics-adapted-263/aeronautics/src/main/java/dev/eriksonn/aeronautics/index/client/AeroRenderTypes.java)

A texture file also needs sprite shift registration and atlas loading. If a shift is lazily initialized during its first render after atlas stitching, `SpriteShiftEntry.getOriginal()` can remain null. The observed rope failure was fixed by calling `SimSpriteShifts.init()` early in the physical client constructor. Existing Catnip atlas events continue updating sprites on resource reload. A dedicated server does not load that client entry point. [Client initialization](../modules/create-aeronautics-adapted-263/simulated/src/neoforge/java/dev/simulated_team/simulated/neoforge/SimulatedNeoForgeClient.java)

Verify both first load and F3+T resource reload. Wait for reload completion, assert that original and target sprites exist, then display the object. A test calling `Minecraft.reloadResourcePacks()` must also wait for asynchronous completion. Inspect the real GUI, relevant error logs, and screenshots afterward. Skipping a draw when a sprite is null leaves the object missing. Check every sprite shift sharing that registration path, resource paths, case, atlas directory coverage, and material cache rebuilding.

## 3. Animation completion and script tails

Distinguish intentionally persistent background instructions from operations expected to finish. A rotating background can run until a scene closes. Once it receives a slowdown or stop request, the test should wait for the actual stop. Identify persistence by the specific class, fields, and parameters; treating all timeouts or all instructions from a mod as normal hides failures.

Completion should measure the controlled quantity. A stopped drill can retain any final angle. Testing whether the absolute rotation vector has near-zero length leaves a stopped animation in the queue indefinitely. In the observed implementation, the block entity explicitly snaps angular speed to zero below its low-speed threshold. The instruction therefore completes at `startSlowing && currentRotationRate == 0`, retaining its final pose. An implementation that only approaches zero needs a condition based on its units, tolerance, and stability rules. [Stop instruction implementation](../modules/create-aeronautics-adapted-263/offroad/src/main/java/dev/ryanhcode/offroad/content/ponder/instructions/ChangeBoreheadAndContraptionSpeedInstruction.java)

After the counted timeline ends, continue executing finite instructions in the actual active schedule. Seeking to an end, observing `markAsFinished`, or reaching an estimated duration can miss inventory clearing, deceleration, fades, and cleanup scripts. Bound finite tails with useful diagnostics, reporting instruction classes, relevant state, and whether stop was requested. Exclude confirmed persistent backgrounds only under their precise conditions. Instances with a slowdown controller or a stop request must still finish.

Restoration should also cover no target, a previous index of `-1`, an empty list, and a disappearing target. Bounds checks needed only for client animation belong in that branch, preserving server behavior and save formats.

## Select representatives by actual logic

The default Ponder sampling unit is **a group with the same actual logic within one mod**. Group by storyboard implementation and meaningful captured parameters. Select one representative when entries differ only by item or material aliases. Callback object identity, duplicate scene names, and list indices across JVMs cannot independently establish semantic groups. Keep separate samples for materially different rendering, data formats, configuration branches, or behavior affected by the change.

For each change, verify the affected representative groups and add a small cross-mod smoke sample. Full registration indexes and template scans are inexpensive enough to retain as static checks. An unrelated JAR hash change records a new input; by itself it should not trigger full-pack playback. A changed test framework, shared rendering or serialization infrastructure, or invalid grouping assumptions may justify broader coverage. Record the reason for expanding it.

A representative manifest should identify the mod, implementation, meaningful parameters, equivalent aliases, selected scene, selection reason, input hashes, execution status, and uncovered branches. Check selected programs against each fresh scan instead of assuming an old index refers to the same scene. The current script selects by index and count; reviewers still record semantic grouping.

One historical Ponder regression recorded **389 programs, 3,458 timeline sample points, and 38,592 GUI frames**, with zero relevant errors. It covered specified contents, finite script cleanup, resource reload, and normal save and exit. These counts describe that set of inputs and samples; frame counts do not establish pixel-by-pixel review or coverage of every natural time step. Multiplayer, sustained load, all configurations, optional mod combinations, and the full set of gameplay features require their own validation. [Historical audit and limitations](PONDER-AUDIT-20261104.md)

Ponder may catch an exception, log ERROR, and continue. Passing criteria should include process exit, timeout, probe assertions, expected versus actual tasks, relevant ERROR events, and input hashes. A log parser should tolerate attribute order changes and fail explicitly if the expected format is missing. Exclude necessary environment messages by exact logger and message, with a recorded reason.

## Scan and representative regression entrypoints

For a Ponder representative, select an index from the new scan:

```powershell
$storageJar = Read-Host 'Candidate Storage JAR under runtime/'
python -X utf8 -B scripts/test_all_ponder_client.py scan --storage $storageJar
$scan = (Get-Content -LiteralPath .local/last-all-ponder-scan.json -Raw | ConvertFrom-Json).result
$jobIndex = [int](Read-Host 'Representative index in this scan render_jobs')
python -X utf8 -B scripts/test_all_ponder_client.py render --storage $storageJar --scan $scan --start $jobIndex --limit 1 --reload-resources
```

This also needs a matching seed world, `package-manifest.json` and fixtures. Pass extra candidates via `--candidate-mod` to both scan and render. An old list index does not identify the same program in a new scan.
