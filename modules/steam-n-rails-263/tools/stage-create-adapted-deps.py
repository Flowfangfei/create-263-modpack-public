#!/usr/bin/env python3
"""Stage this integration repository's real Create development outputs; never package shims."""
from pathlib import Path
import argparse,hashlib,json,zipfile,tomllib
ROOT=Path(__file__).resolve().parents[1]
PINS=[{'source': 'build/libs/create-26.3-1.31-recovered.1.jar', 'filename': 'create-26.3-1.31-recovered.1.jar'}, {'source': 'Ponder/NeoForge/build/libs/ponder-neoforge-1.0.100046-adapted-fork-early+mc26.3.jar', 'filename': 'ponder-neoforge-1.0.100046-adapted-fork-early+mc26.3.jar'}, {'source': 'build/generated/jarJar/META-INF/jarjar/catnip-neoforge-1.0.100046-adapted-fork-early+mc26.3.jar', 'filename': 'catnip-neoforge-1.0.100046-adapted-fork-early+mc26.3.jar'}, {'source': 'build/flywheel-runtime/flywheel-neoforge-26.3-1.0.6.jar', 'filename': 'flywheel-neoforge-26.3-1.0.6.jar'}, {'source': 'build/compat/registrate/Registrate-MC1.21-1.3.0+67-compile.jar', 'filename': 'Registrate-MC1.21-1.3.0+67-compile.jar'}]
SHIMS=['net/minecraft/MethodsReturnNonnullByDefault.class', 'net/minecraft/ResourceLocationException.class', 'net/minecraft/Util$OS.class', 'net/minecraft/Util.class', 'net/minecraft/client/color/block/BlockColor.class', 'net/minecraft/client/color/item/ItemColor.class', 'net/minecraft/client/gui/GuiGraphics.class', 'net/minecraft/client/gui/LayeredDraw$Layer.class', 'net/minecraft/client/gui/LayeredDraw.class', 'net/minecraft/client/model/AgeableListModel.class', 'net/minecraft/client/model/HierarchicalModel.class', 'net/minecraft/client/renderer/BlockEntityWithoutLevelRenderer.class', 'net/minecraft/client/renderer/PanoramaRenderer.class', 'net/minecraft/client/renderer/RenderType.class', 'net/minecraft/client/renderer/block/BlockModelShaper.class', 'net/minecraft/client/renderer/block/BlockRenderDispatcher.class', 'net/minecraft/client/renderer/block/model/BakedQuad.class', 'net/minecraft/client/renderer/block/model/ItemOverrides.class', 'net/minecraft/client/renderer/block/model/ItemTransforms.class', 'net/minecraft/client/renderer/entity/ItemRenderer.class', 'net/minecraft/client/renderer/entity/player/PlayerRenderer.class', 'net/minecraft/client/renderer/item/ItemProperties$ItemPropertyFunction.class', 'net/minecraft/client/renderer/item/ItemProperties.class', 'net/minecraft/client/resources/model/BakedModel.class', 'net/minecraft/client/resources/model/ModelResourceLocation.class', 'net/minecraft/client/resources/model/SimpleBakedModel.class', 'net/minecraft/data/tags/IntrinsicHolderTagsProvider$IntrinsicTagAppender.class', 'net/minecraft/data/tags/IntrinsicHolderTagsProvider.class', 'net/minecraft/network/protocol/game/DebugPackets.class', 'net/minecraft/resources/ResourceKey$InternKey.class', 'net/minecraft/resources/ResourceKey.class', 'net/minecraft/resources/ResourceLocation.class', 'net/minecraft/util/FastColor$ABGR32.class', 'net/minecraft/util/FastColor$ARGB32.class', 'net/minecraft/util/FastColor.class', 'net/minecraft/world/InteractionResultHolder.class', 'net/minecraft/world/ItemInteractionResult.class', 'net/minecraft/world/inventory/ClickType.class', 'net/minecraft/world/item/ArmorItem$Type.class', 'net/minecraft/world/item/ArmorItem.class', 'net/minecraft/world/item/ArmorMaterial$Layer.class', 'net/minecraft/world/item/ArmorMaterial.class', 'net/minecraft/world/item/ArmorMaterials.class', 'net/minecraft/world/item/Equipable.class', 'net/minecraft/world/item/FireworkStarItem.class', 'net/minecraft/world/item/SwordItem.class', 'net/minecraft/world/item/Tier.class', 'net/minecraft/world/item/UseAnim.class', 'net/neoforged/fml/loading/FMLEnvironment.class', 'net/neoforged/neoforge/client/ChunkRenderTypeSet.class', 'net/neoforged/neoforge/client/event/RenderHighlightEvent$Block.class', 'net/neoforged/neoforge/client/event/RenderHighlightEvent.class', 'net/neoforged/neoforge/client/model/BakedModelWrapper.class', 'net/neoforged/neoforge/client/model/QuadTransformers$QuadTransformer.class', 'net/neoforged/neoforge/client/model/QuadTransformers.class', 'net/neoforged/neoforge/client/model/generators/BlockModelBuilder.class', 'net/neoforged/neoforge/client/model/generators/BlockModelProvider.class', 'net/neoforged/neoforge/client/model/generators/BlockStateProvider.class', 'net/neoforged/neoforge/client/model/generators/ConfiguredModel$Builder.class', 'net/neoforged/neoforge/client/model/generators/ConfiguredModel.class', 'net/neoforged/neoforge/client/model/generators/IGeneratedBlockState.class', 'net/neoforged/neoforge/client/model/generators/ItemModelBuilder.class', 'net/neoforged/neoforge/client/model/generators/ItemModelProvider.class', 'net/neoforged/neoforge/client/model/generators/ModelBuilder.class', 'net/neoforged/neoforge/client/model/generators/ModelFile$ExistingModelFile.class', 'net/neoforged/neoforge/client/model/generators/ModelFile$UncheckedModelFile.class', 'net/neoforged/neoforge/client/model/generators/ModelFile.class', 'net/neoforged/neoforge/client/model/generators/ModelProvider.class', 'net/neoforged/neoforge/client/model/generators/MultiPartBlockStateBuilder.class', 'net/neoforged/neoforge/client/model/generators/VariantBlockStateBuilder.class', 'net/neoforged/neoforge/common/data/ExistingFileHelper.class', 'net/neoforged/neoforge/common/util/INBTSerializable.class', 'net/neoforged/neoforge/registries/DeferredHolder.class']
def digest(data):return hashlib.sha256(data).hexdigest()
def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--create-project',type=Path,default=ROOT.parent/'create-adapted-263')
    parser.add_argument('--check',action='store_true')
    args=parser.parse_args();source=args.create_project.resolve();dest=ROOT/'tooling/create-adapted';libs=dest/'libs'
    inputs=[(pin,(source/pin['source']).read_bytes()) for pin in PINS]
    with zipfile.ZipFile(source/PINS[0]['source']) as jar:
        meta=tomllib.loads(jar.read('META-INF/neoforge.mods.toml').decode('utf-8'))
        version=next(m['version'] for m in meta['mods'] if m['modId']=='create')
        if version!='6.0.11-adapted-1.31-recovered.1':raise SystemExit('Unexpected Create version: '+version)
        at=jar.read('META-INF/accesstransformer.cfg')
    shims=[(name,(source/'build/classes/java/main'/name).read_bytes()) for name in SHIMS]
    if args.check:
        for pin,data in inputs:
            if (libs/pin['filename']).read_bytes()!=data:raise SystemExit('Staged API changed: '+pin['filename'])
        with zipfile.ZipFile(libs/'create-adapted-compile-compat.jar') as jar:
            if set(jar.namelist())!=set(SHIMS):raise SystemExit('Compile shim set mismatch')
            for name,data in shims:
                if jar.read(name)!=data:raise SystemExit('Compile shim changed: '+name)
        if (dest/'create-accesstransformer.cfg').read_bytes()!=at:raise SystemExit('Create AT changed')
        print('Verified source-built Create development APIs, 73 compile shims and AT');return
    libs.mkdir(parents=True,exist_ok=True)
    manifest={'schema':2,'source':'../create-adapted-263 current source build','create_mod_version':version,'dependencies':[],'compile_only_shims':[]}
    for pin,data in inputs:
        (libs/pin['filename']).write_bytes(data)
        manifest['dependencies'].append({**pin,'sha256':digest(data)})
    with zipfile.ZipFile(libs/'create-adapted-compile-compat.jar','w',compression=zipfile.ZIP_DEFLATED) as jar:
        for name,data in shims:
            info=zipfile.ZipInfo(name,(1980,1,1,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;info.external_attr=0o100644<<16
            jar.writestr(info,data);manifest['compile_only_shims'].append({'path':name,'sha256':digest(data)})
    (dest/'create-accesstransformer.cfg').write_bytes(at)
    (dest/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n',encoding='utf-8')
    print('Staged source-built Create development APIs and 73 compile-only shims')
if __name__=='__main__':main()
