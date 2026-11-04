# Modpack and datapack notes

This Create-Fly fork targets Minecraft 26.1.2 and currently ships the core Create: Dragons Plus features only. Optional integrations from the NeoForge release are not included.

## Required mods

- Fabric Loader 0.19.3 or newer
- Fabric API 0.155.2 for Minecraft 26.1.2 or newer
- Create-Fly 6.0.9-4 (`>=6.0.9-4 <6.1.0`)
- JEI 29.6.2.38 is optional but supported

## Configuration

Configuration is stored in JSON:

- `config/create_dragons_plus/common.json`
- `config/create_dragons_plus/server.json`
- `config/create_dragons_plus/client.json`

The common configuration controls feature availability, the server configuration controls recipes and fluid behavior, and the client configuration controls fluid fog distance. NeoForge TOML files are not read.

## Tags

### Fluids

- `c:dyes` contains all registered dye fluids.
- `c:dyes/<color>` contains a specific vanilla dye fluid, such as `c:dyes/red`.
- `c:dragon_breath` contains Liquid Dragon's Breath.
- `create_dragons_plus:swimmable` contains CDP fluids that use swimming/contact behavior.

### Items

- `c:buckets/dye` contains all registered dye-fluid buckets.
- `c:buckets/dye/<color>` contains a specific vanilla dye-fluid bucket.
- `c:buckets/dragon_breath` contains the Dragon's Breath bucket.
- `create_dragons_plus:dyes/minecraft/<color>` contains source dye items used by dye-fluid recipes.
- `create_dragons_plus:not_applicable_for_coloring` excludes items from automatic Bulk Coloring detection.

### Blocks

- `create_dragons_plus:passive_block_freezers` contains Bulk Freezing catalysts.
- `create_dragons_plus:fan_processing_catalysts/sanding` contains Bulk Sanding catalysts and keeps the 26.1 convention tag `c:sands`.
- `create_dragons_plus:fan_processing_catalysts/ending` contains Bulk Ending catalysts.
- `create_dragons_plus:not_applicable_for_polishing` excludes blocks from generated polishing recipes.

## Bulk Coloring catalyst data

Bulk Coloring catalysts use reloadable JSON data at these paths:

- `data/create_dragons_plus/data_maps/fluid/fan_processing_catalysts/coloring.json`
- `data/create_dragons_plus/data_maps/block/fan_processing_catalysts/coloring.json`

The format retains the existing `replace` and `values` fields. A selector may be a direct registry ID or a tag prefixed with `#`; the value is a registered dye-variant ID.

```json
{
  "replace": false,
  "values": {
    "#c:dyes/red": "minecraft:red",
    "minecraft:red_concrete": "minecraft:red"
  }
}
```

Higher-priority packs override lower-priority selectors. Invalid selectors, missing registry entries, and unknown dye variants are logged and skipped. Changes are resolved after tags load and synchronized to connected clients after `/reload`.

## Recipes and processing

- `create_dragons_plus:coloring` provides Bulk Coloring.
- `create_dragons_plus:freezing` provides Bulk Freezing.
- `create_dragons_plus:sanding` provides Bulk Sanding.
- `create_dragons_plus:ending` provides Bulk Ending.

Automatic Bulk Coloring detects supported vanilla dyeing recipes. Automatic Bulk Sanding rebuilds polishing, de-oxidizing, and wax-removal recipes after recipe reload. Dragon heads only activate Bulk Ending when they face the incoming air current.

With JEI installed, dye-fluid mixer recipes appear in a dedicated Automated Coloring category. Looking up a dye item, or any equivalent item in its dye tag, shows both Automated Coloring and Bulk Coloring recipes.

Fabric/Create-Fly fluid amounts use 81,000 units per bucket. A quarter bucket is 20,250 units, and automatic dye-fluid coloring consumes 2,532 units per item.

## Fluids and Fluid Hatch

Dye fluids and Liquid Dragon's Breath support buckets, cauldrons, open pipes, entity/item contact behavior, and environment-specific vaporization. Dragon's Breath also supports dripstone cauldron filling when enabled.

Fluid Hatch proxies Fabric `FluidStorage.SIDED` from its exposed side to the adjacent target. It supports Fabric fluid containers and Create-Fly's generic emptying/filling recipes, and its filter accepts fluid filters.
