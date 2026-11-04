package dev.engine_room.flywheel.backend.glsl;

import dev.engine_room.flywheel.backend.compile.FlwPrograms;
import dev.engine_room.flywheel.lib.util.StringUtil;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.VisibleForTesting;

public class ShaderSources {
   public static final String SHADER_DIR = "flywheel/";
   @VisibleForTesting
   protected final Map<Identifier, LoadResult> cache;

   public ShaderSources(ResourceManager manager) {
      ShaderSources.SourceFinder sourceFinder = new ShaderSources.SourceFinder(manager);
      long loadStart = System.nanoTime();
      manager.listResources("flywheel", ShaderSources::isShader).forEach(sourceFinder::rootLoad);
      long loadEnd = System.nanoTime();
      FlwPrograms.LOGGER.info("Loaded {} shader sources in {}", sourceFinder.results.size(), StringUtil.formatTime(loadEnd - loadStart));
      this.cache = sourceFinder.results;
   }

   private static Identifier locationWithoutFlywheelPrefix(Identifier loc) {
      return Identifier.fromNamespaceAndPath(loc.getNamespace(), loc.getPath().substring("flywheel/".length()));
   }

   public LoadResult find(Identifier location) {
      return this.cache.computeIfAbsent(location, loc -> new LoadResult.Failure(new LoadError.ResourceError(loc)));
   }

   public SourceFile get(Identifier location) {
      return this.find(location).unwrap();
   }

   private static boolean isShader(Identifier loc) {
      String path = loc.getPath();
      return path.endsWith(".glsl") || path.endsWith(".vert") || path.endsWith(".frag") || path.endsWith(".comp");
   }

   private static class SourceFinder {
      private final Deque<Identifier> findStack = new ArrayDeque<>();
      private final Map<Identifier, LoadResult> results = new HashMap<>();
      private final ResourceManager manager;

      public SourceFinder(ResourceManager manager) {
         this.manager = manager;
      }

      public void rootLoad(Identifier loc, Resource resource) {
         Identifier strippedLoc = ShaderSources.locationWithoutFlywheelPrefix(loc);
         if (!this.results.containsKey(strippedLoc)) {
            this.results.put(strippedLoc, this.readResource(strippedLoc, resource));
         }
      }

      public LoadResult recursiveLoad(Identifier location) {
         if (this.findStack.contains(location)) {
            this.findStack.addLast(location);
            List<Identifier> copy = List.copyOf(this.findStack);
            this.findStack.removeLast();
            return new LoadResult.Failure(new LoadError.CircularDependency(location, copy));
         } else {
            this.findStack.addLast(location);
            LoadResult out = this._find(location);
            this.findStack.removeLast();
            return out;
         }
      }

      private LoadResult _find(Identifier location) {
         LoadResult out = this.results.get(location);
         if (out == null) {
            out = this.load(location);
            this.results.put(location, out);
         }

         return out;
      }

      private LoadResult load(Identifier loc) {
         return this.manager
            .getResource(loc.withPrefix("flywheel/"))
            .map(resource -> this.readResource(loc, resource))
            .orElseGet(() -> new LoadResult.Failure(new LoadError.ResourceError(loc)));
      }

      private LoadResult readResource(Identifier loc, Resource resource) {
         try (InputStream stream = resource.open()) {
            String sourceString = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            return SourceFile.parse(this::recursiveLoad, loc, sourceString.replaceAll("(?<![a-z0-9_.-])flywheel:", "flywheel:"));
         } catch (IOException e) {
            return new LoadResult.Failure(new LoadError.IOError(loc, e));
         }
      }
   }
}
