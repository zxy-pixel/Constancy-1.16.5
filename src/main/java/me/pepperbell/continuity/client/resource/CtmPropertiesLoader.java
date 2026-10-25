// Modified 2026-10-22 for the unofficial Forge 1.16.5 backport.
package me.pepperbell.continuity.client.resource;

import java.io.InputStream;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.pepperbell.continuity.api.client.CachingPredicates;
import me.pepperbell.continuity.api.client.CtmLoader;
import me.pepperbell.continuity.api.client.CtmLoaderRegistry;
import me.pepperbell.continuity.api.client.CtmProperties;
import me.pepperbell.continuity.api.client.QuadProcessor;
import me.pepperbell.continuity.client.ContinuityClient;
import me.pepperbell.continuity.client.model.QuadProcessors;
import me.pepperbell.continuity.client.util.BooleanState;
import me.pepperbell.continuity.client.util.biome.BiomeHolderManager;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class CtmPropertiesLoader {
	private final ResourceManager resourceManager;
	private final List<LoadingContainer<?>> containers = new ObjectArrayList<>();
	private final Map<Identifier, Set<Identifier>> textureDependencies = new Object2ObjectOpenHashMap<>();

	private CtmPropertiesLoader(ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
	}

	public static LoadingResult loadAllWithState(ResourceManager resourceManager) {
		// TODO: move these to the very beginning of resource reload
		BiomeHolderManager.clearCache();

		LoadingResult result = loadAll(resourceManager);

		// TODO: move these to the very end of resource reload
		BiomeHolderManager.refreshHolders();

		return result;
	}

	public static LoadingResult loadAll(ResourceManager resourceManager) {
		return new CtmPropertiesLoader(resourceManager).loadAll();
	}

	private LoadingResult loadAll() {
		int packPriority = 0;
		Iterator<ResourcePack> iterator = resourceManager.streamResourcePacks().iterator();
		BooleanState invalidIdentifierState = InvalidIdentifierStateHolder.get();
		invalidIdentifierState.enable();
		try {
			while (iterator.hasNext()) {
				ResourcePack pack = iterator.next();
				loadAll(pack, packPriority++);
			}
		} finally {
			invalidIdentifierState.disable();
		}

		containers.sort(Comparator.reverseOrder());

		return new LoadingResult(containers, textureDependencies);
	}

    private void loadAll(ResourcePack pack, int packPriority) {
        for (String namespace : pack.getNamespaces(ResourceType.CLIENT_RESOURCES)) {
            for (Identifier resourceId : pack.findResources(ResourceType.CLIENT_RESOURCES, namespace,
                    "optifine/ctm", Integer.MAX_VALUE, name -> name.endsWith(".properties"))) {
                try (InputStream stream = pack.open(ResourceType.CLIENT_RESOURCES, resourceId)) {
                    Properties properties = new Properties();
                    properties.load(stream);
                    load(properties, resourceId, pack, packPriority);
                } catch (Exception e) {
                    ContinuityClient.LOGGER.error("Failed to load CTM properties {} in pack {}", resourceId, pack.getName(), e);
                }
            }
        }
    }

	private void load(Properties properties, Identifier resourceId, ResourcePack pack, int packPriority) {
		String method = properties.getProperty("method", "ctm").trim();
		CtmLoader<?> loader = CtmLoaderRegistry.get().getLoader(method);
		if (loader != null) {
			load(loader, properties, resourceId, pack, packPriority, method);
		} else {
			ContinuityClient.LOGGER.error("Unknown 'method' value '" + method + "' in file '" + resourceId + "' in pack '" + pack.getName() + "'");
		}
	}

	private <T extends CtmProperties> void load(CtmLoader<T> loader, Properties properties, Identifier resourceId, ResourcePack pack, int packPriority, String method) {
		T ctmProperties = loader.getPropertiesFactory().createProperties(properties, resourceId, pack, packPriority, resourceManager, method);
		if (ctmProperties != null) {
			LoadingContainer<T> container = new LoadingContainer<>(loader, ctmProperties);
			containers.add(container);
			for (SpriteIdentifier spriteId : ctmProperties.getTextureDependencies()) {
				Set<Identifier> atlasTextureDependencies = textureDependencies.computeIfAbsent(spriteId.getAtlasId(), id -> new ObjectOpenHashSet<>());
				atlasTextureDependencies.add(spriteId.getTextureId());
			}
		}
	}

	private static final class LoadingContainer<T extends CtmProperties> implements Comparable<LoadingContainer<?>> {
        private final CtmLoader<T> loader;
        private final T properties;
        private LoadingContainer(CtmLoader<T> loader, T properties) {
            this.loader = loader;
            this.properties = properties;
        }
		public QuadProcessors.ProcessorHolder toProcessorHolder(Function<SpriteIdentifier, Sprite> textureGetter) {
			QuadProcessor processor = loader.getProcessorFactory().createProcessor(properties, textureGetter);
			CachingPredicates predicates = loader.getPredicatesFactory().createPredicates(properties, textureGetter);
			return new QuadProcessors.ProcessorHolder(processor, predicates);
		}

		@Override
		public int compareTo(@NotNull LoadingContainer<?> o) {
			return properties.compareTo(o.properties);
		}
	}

	public static class LoadingResult {
		private final List<LoadingContainer<?>> containers;
		private final Map<Identifier, Set<Identifier>> textureDependencies;

		private LoadingResult(List<LoadingContainer<?>> containers, Map<Identifier, Set<Identifier>> textureDependencies) {
			this.containers = containers;
			this.textureDependencies = textureDependencies;
		}

		public List<QuadProcessors.ProcessorHolder> createProcessorHolders(Function<SpriteIdentifier, Sprite> textureGetter) {
			List<QuadProcessors.ProcessorHolder> processorHolders = new ObjectArrayList<>();
			for (LoadingContainer<?> container : containers) {
				processorHolders.add(container.toProcessorHolder(textureGetter));
			}
			return processorHolders;
		}

		public Map<Identifier, Set<Identifier>> getTextureDependencies() {
			return textureDependencies;
		}
	}
}
