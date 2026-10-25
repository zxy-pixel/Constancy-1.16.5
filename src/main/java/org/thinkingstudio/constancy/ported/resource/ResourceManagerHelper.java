// Modified 2026-10-22 for the unofficial Forge 1.16.5 backport.
package org.thinkingstudio.constancy.ported.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import org.thinkingstudio.constancy.Constancy;

public final class ResourceManagerHelper {
    private static final List<ResourcePackProvider> BUILTINS = new ArrayList<>();
    private static ResourcePackManager registeredManager;
    public static void registerBuiltinResourcePack(String modId, IEventBus bus, Identifier id, Text title, ResourceType type) {
        BUILTINS.add((consumer, factory) -> {
            ModFile file = ModList.get().getModFileById(Constancy.ID).getFile();
            Path root = file.getLocator().findPath(file, "resourcepacks", id.getPath());
            ResourcePackProfile profile = ResourcePackProfile.of("continuity/" + id.getPath(), false,
                    () -> new BuiltinPack(root, title.getString()), factory,
                    ResourcePackProfile.InsertionPosition.TOP, ResourcePackSource.PACK_SOURCE_BUILTIN);
            if (profile != null) consumer.accept(profile);
        });
    }

    /** Install before the initial options/pack scan, earlier than FMLClientSetupEvent. */
    public static void installProviders(ResourcePackManager manager) {
        if (manager != MinecraftClient.getInstance().getResourcePackManager() || manager == registeredManager) return;
        for (ResourcePackProvider provider : BUILTINS) manager.addPackFinder(provider);
        registeredManager = manager;
    }

    public static void registerReloadListener(IEventBus bus, ResourceType type, ResourceReloader listener) {
        ((ReloadableResourceManagerImpl) MinecraftClient.getInstance().getResourceManager()).registerReloader(listener);
    }

    private static final class BuiltinPack extends AbstractFileResourcePack {
        private final Path root;
        private final String name;
        BuiltinPack(Path root, String name) { super(new File("constancy-builtin")); this.root = root.toAbsolutePath().normalize(); this.name = name; }
        @Override protected InputStream openFile(String path) throws IOException { return Files.newInputStream(root.resolve(path)); }
        @Override protected boolean containsFile(String path) { return Files.isRegularFile(root.resolve(path)); }
        @Override public String getName() { return name; }
        @Override public Set<String> getNamespaces(ResourceType type) {
            Path folder = root.resolve(type.getDirectory());
            if (!Files.isDirectory(folder)) return Collections.emptySet();
            try (Stream<Path> paths = Files.list(folder)) {
                // Java 8's ZIP filesystem retains a trailing slash on directory names.
                return paths.filter(Files::isDirectory).map(p -> p.getFileName().toString().replaceAll("[/\\\\]+$", "")).collect(Collectors.toSet());
            } catch (IOException e) { return Collections.emptySet(); }
        }
        @Override public Collection<Identifier> findResources(ResourceType type, String namespace, String prefix, int depth, Predicate<String> filter) {
            Path folder = root.resolve(type.getDirectory()).resolve(namespace);
            Path start = folder.resolve(prefix);
            if (!Files.isDirectory(start)) return Collections.emptyList();
            try (Stream<Path> paths = Files.walk(start, depth)) {
                return paths.filter(Files::isRegularFile).filter(p -> filter.test(p.getFileName().toString()))
                        .map(p -> new Identifier(namespace, folder.relativize(p).toString().replace('\\', '/'))).collect(Collectors.toList());
            } catch (IOException e) { return Collections.emptyList(); }
        }
        @Override public void close() { }
    }
}
