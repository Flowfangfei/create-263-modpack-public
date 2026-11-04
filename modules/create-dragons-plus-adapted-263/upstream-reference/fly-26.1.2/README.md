# Create: Dragons Plus — Create-Fly

Create: Dragons Plus is a shared library for DragonsPlusMinecraft Create add-ons. This fork targets **Minecraft 26.1.2**, **Fabric**, and [Create-Fly](https://modrinth.com/mod/create-fly) `6.0.9-4`.

The active source is [MarbleGateKeeper/CreateDragonsPlus-Fly](https://github.com/MarbleGateKeeper/CreateDragonsPlus-Fly). The eight optional integrations from the NeoForge project, including Ars Nouveau, are not included in this Fabric release.

## Dependency

```groovy
repositories {
    maven { url = 'https://maven.dragons.plus/releases' }
}

dependencies {
    modImplementation("plus.dragons.createdragonsplus:create-dragons-plus-fly-26.1.2:${create_dragons_plus_version}")
}
```

The published library exposes Fabric API and Create-Fly through Gradle `api`/Maven compile dependencies. Consumers normally do not need to declare them again. The supported Create-Fly range is `>=6.0.9-4 <6.1.0`.

The current release is `1.11.8`. It includes server-safe Blaze machine foundations, stress configuration for downstream addons, and dedicated Automated Coloring recipes in JEI. Blaze renderers, Visuals, model providers, contraption render attachments, and JEI integration are client-only APIs.

Add-ons that provide dye variants should declare the early entrypoint below. Providers are applied in mod-ID order, duplicate variant IDs are rejected, and the registry is frozen before blocks and fluids are bootstrapped.

```json
{
  "entrypoints": {
    "create_dragons_plus": [
      "example.addon.ExampleDragonsPlusPlugin"
    ]
  }
}
```

## Development

Java 25 is required. The main verification commands are:

```text
./gradlew spotlessCheck test compileApiTestJava build
./gradlew runDatagen
```

Create-Fly is published on CurseForge as project [1346281](https://www.curseforge.com/minecraft/mc-mods/create-fly). Modrinth and CurseForge releases of this project declare `create-fly` as a required dependency.
