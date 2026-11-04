package dev.ryanhcode.sable.neoforge.platform;
import dev.ryanhcode.sable.platform.SableLoaderPlatform;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;
import java.nio.file.Path;
public final class SableLoaderPlatformImpl implements SableLoaderPlatform {
    @Override public boolean isModLoaded(String id) {
        return LoadingModList.get().getMods().stream().anyMatch(mod -> mod.getModId().equals(id));
    }
    @Override public Path getGameDirectory() { return FMLPaths.GAMEDIR.get(); }
    @Override public String getModVersion(String id) {
        return LoadingModList.get().getMods().stream().filter(mod -> mod.getModId().equals(id))
            .findFirst().orElseThrow().getVersion().toString();
    }
}
