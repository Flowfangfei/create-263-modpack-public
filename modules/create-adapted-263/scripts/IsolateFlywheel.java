import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

/** Reconstructed release packaging step; source-facing Flywheel APIs remain intact. */
public final class IsolateFlywheel {
    private static final String OLD = "dev/engine_room/flywheel";
    private static final String NEW = "dev/engine_room/create_flywheel";
    private static final Pattern RESOURCE_NAMESPACE = Pattern.compile("(?<![a-z0-9_.-])flywheel:");
    // Exact class/literal changes measured against the 26.3 embedded Flywheel.
    // In particular, shader directory "flywheel/" and Create's block id stay intact.
    private static final Map<String, Map<String, String>> LITERALS = Map.ofEntries(
        Map.entry(OLD + "/api/Flywheel", Map.of("flywheel", "create_flywheel")),
        Map.entry(OLD + "/backend/engine/DrawManager", Map.of("flywheel.modelWarnings", "create_flywheel.modelWarnings")),
        Map.entry(OLD + "/backend/glsl/ShaderSources$SourceFinder", Map.of("flywheel:", "create_flywheel:")),
        Map.entry(OLD + "/impl/BackendArgument", Map.of("flywheel:off", "create_flywheel:off")),
        Map.entry(OLD + "/impl/BackendManagerImpl", Map.of("No backends registered, defaulting to 'flywheel:off'", "No backends registered, defaulting to 'create_flywheel:off'")),
        Map.entry(OLD + "/impl/FlwCommands", Map.of("flywheel", "create_flywheel")),
        Map.entry(OLD + "/impl/FlwImpl", Map.of("flywheel", "create_flywheel")),
        Map.entry(OLD + "/impl/FlywheelNeoForge", Map.of("flywheel", "create_flywheel")),
        Map.entry(OLD + "/lib/model/baked/PartialModel", Map.of("flywheel:partial/\u0001", "create_flywheel:partial/\u0001")),
        Map.entry(OLD + "/lib/util/ResourceUtil", Map.of("flywheel", "create_flywheel"))
    );
    private static String oldVersion, newVersion;

    private static String namespace(String value) {
        if (value == null) return null;
        return value.replace(OLD, NEW).replace(OLD.replace('/', '.'), NEW.replace('/', '.'));
    }
    private static String version(String value) {
        return oldVersion == null ? value : value.replace(oldVersion, newVersion);
    }
    private static String mixinNames(String value) {
        return value.replaceAll("(?<![A-Za-z0-9_])flywheel(?=\\.(?:backend\\.mixins|impl(?:\\.neoforge)?\\.mixins|refmap)\\.json)", "create_flywheel");
    }
    private static String entryName(String value) {
        String result = namespace(value);
        if (result.startsWith("assets/flywheel/")) result = "assets/create_flywheel/" + result.substring("assets/flywheel/".length());
        if (result.startsWith("data/flywheel/")) result = "data/create_flywheel/" + result.substring("data/flywheel/".length());
        result = result.replace("META-INF/jarjar/flywheel-", "META-INF/jarjar/create_flywheel-");
        return version(mixinNames(result));
    }
    private static byte[] transformClass(byte[] data) {
        ClassReader reader = new ClassReader(data);
        String owner = reader.getClassName();
        Map<String, String> literals = LITERALS.getOrDefault(owner, Map.of());
        ClassWriter writer = new ClassWriter(0);
        Remapper remapper = new Remapper() {
            @Override public String map(String name) { return namespace(name); }
            @Override public String mapPackageName(String name) { return namespace(name); }
            @Override public Object mapValue(Object value) {
                if (value instanceof String text) return version(namespace(literals.getOrDefault(text, text)));
                return super.mapValue(value);
            }
        };
        reader.accept(new ClassRemapper(writer, remapper), 0);
        return writer.toByteArray();
    }
    private static boolean isText(String name) {
        return name.startsWith("META-INF/services/") || name.matches("(?i).*\\.(json|toml|cfg|properties|glsl|vert|frag|comp|fsh|vsh|mcmeta|txt|md|mf)$");
    }
    private static byte[] transformText(String name, byte[] data) {
        String text = namespace(new String(data, StandardCharsets.UTF_8));
        if (name.equals("META-INF/jarjar/metadata.json")) {
            text = text.replace("dev.engine-room.flywheel", "dev.engine-room.create_flywheel")
                .replace("META-INF/jarjar/flywheel-", "META-INF/jarjar/create_flywheel-")
                .replace("\"flywheel-neoforge-", "\"create_flywheel-neoforge-")
                .replace("\"flywheel\"", "\"create_flywheel\"");
        }
        if (name.equals("META-INF/neoforge.mods.toml")) {
            text = text.replaceAll("(?m)(modId\\s*=\\s*[\"'])flywheel([\"'])", "$1create_flywheel$2")
                .replace("dependencies.flywheel", "dependencies.create_flywheel")
                .replace("dependencies.\"flywheel\"", "dependencies.\"create_flywheel\"")
                .replace("dependencies.'flywheel'", "dependencies.'create_flywheel'");
        }
        text = mixinNames(text);
        if (name.startsWith("assets/flywheel/") || name.startsWith("data/flywheel/")) {
            text = RESOURCE_NAMESPACE.matcher(text).replaceAll("create_flywheel:");
            text = text.replace("\"flywheel\"", "\"create_flywheel\"");
        }
        return version(text).getBytes(StandardCharsets.UTF_8);
    }
    private static byte[] transformJar(byte[] archive) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        Set<String> paths = new HashSet<>();
        try (ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(archive));
             ZipOutputStream output = new ZipOutputStream(result)) {
            ZipEntry entry;
            while ((entry = input.getNextEntry()) != null) {
                String sourceName = entry.getName();
                String name = entryName(sourceName);
                if (!paths.add(name)) throw new IOException("Duplicate relocated entry: " + name);
                byte[] data = input.readAllBytes();
                if (!entry.isDirectory()) {
                    if (name.endsWith(".class")) data = transformClass(data);
                    else if (name.endsWith(".jar")) data = transformJar(data);
                    else if (isText(name)) data = transformText(sourceName, data);
                }
                ZipEntry target = new ZipEntry(name);
                target.setTime(0L);
                output.putNextEntry(target);
                output.write(data);
                output.closeEntry();
            }
        }
        return result.toByteArray();
    }
    public static void main(String[] args) throws IOException {
        if (args.length != 2 && args.length != 4)
            throw new IllegalArgumentException("Usage: IsolateFlywheel input.jar output.jar [oldVersion newVersion]");
        Path input = Path.of(args[0]).toAbsolutePath().normalize();
        Path output = Path.of(args[1]).toAbsolutePath().normalize();
        if (input.equals(output)) throw new IllegalArgumentException("Use a separate output file");
        if (args.length == 4) { oldVersion = args[2]; newVersion = args[3]; }
        byte[] transformed = transformJar(Files.readAllBytes(input));
        Files.createDirectories(output.getParent());
        Files.write(output, transformed);
        System.out.println("Isolated release bundle: " + output);
    }
}
