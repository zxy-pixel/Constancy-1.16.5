// Modified 2026-10-22 for the unofficial Forge 1.16.5 backport.
package me.pepperbell.continuity.client.model;

import java.util.List;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import me.pepperbell.continuity.api.client.CachingPredicates;
import me.pepperbell.continuity.api.client.QuadProcessor;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;

public final class QuadProcessors {
	private static ProcessorHolder[] processorHolders = new ProcessorHolder[0];
	private static final BlockStateKeyCache CACHE = new BlockStateKeyCache();

	public static Function<Sprite, Slice> getCache(BlockState state) {
		return CACHE.apply(state);
	}

	private static Slice computeSlice(BlockState state, Sprite sprite) {
		List<QuadProcessor> processorList = new ObjectArrayList<>();
		List<QuadProcessor> multipassProcessorList = new ObjectArrayList<>();

		for (ProcessorHolder holder : processorHolders) {
			QuadProcessor processor = holder.processor();
			CachingPredicates predicates = holder.predicates();
			if (!predicates.affectsBlockStates() || predicates.affectsBlockState(state)) {
				if (predicates.affectsSprites()) {
					if (predicates.affectsSprite(sprite)) {
						processorList.add(processor);
						if (predicates.isValidForMultipass()) {
							multipassProcessorList.add(processor);
						}
					}
				} else {
					processorList.add(processor);
				}
			}
		}

		QuadProcessor[] processors = processorList.toArray(new QuadProcessor[0]);
		QuadProcessor[] multipassProcessors = multipassProcessorList.toArray(new QuadProcessor[0]);
		return new Slice(processors, multipassProcessors);
	}

	@ApiStatus.Internal
	public static void reload(List<QuadProcessors.ProcessorHolder> processorHolders) {
		QuadProcessors.processorHolders = processorHolders.toArray(new ProcessorHolder[0]);
		CACHE.clear();
	}

	public static final class ProcessorHolder {
        private final QuadProcessor processor;
        private final CachingPredicates predicates;
        public ProcessorHolder(QuadProcessor processor, CachingPredicates predicates) {
            this.processor = processor;
            this.predicates = predicates;
        }
        public QuadProcessor processor() { return processor; }
        public CachingPredicates predicates() { return predicates; }
    }

	public static final class Slice {
        private final QuadProcessor[] processors;
        private final QuadProcessor[] multipassProcessors;
        public Slice(QuadProcessor[] processors, QuadProcessor[] multipassProcessors) {
            this.processors = processors;
            this.multipassProcessors = multipassProcessors;
        }
        public QuadProcessor[] processors() { return processors; }
        public QuadProcessor[] multipassProcessors() { return multipassProcessors; }
    }

	private static class BlockStateKeyCache {
		private final Reference2ReferenceOpenHashMap<BlockState, SpriteKeyCache> map = new Reference2ReferenceOpenHashMap<>();
		private final StampedLock lock = new StampedLock();

		public SpriteKeyCache apply(BlockState state) {
			SpriteKeyCache innerCache;

			long optimisticReadStamp = lock.tryOptimisticRead();
			if (optimisticReadStamp != 0L) {
				try {
					// This map read could happen at the same time as a map write, so catch any exceptions.
					// This is safe due to the map implementation used, which is guaranteed to not mutate the map during
					// a read.
					innerCache = map.get(state);
					if (innerCache != null && lock.validate(optimisticReadStamp)) {
						return innerCache;
					}
				} catch (Exception e) {
					//
				}
			}

			long readStamp = lock.readLock();
			try {
				innerCache = map.get(state);
			} finally {
				lock.unlockRead(readStamp);
			}

			if (innerCache == null) {
				long writeStamp = lock.writeLock();
				try {
					innerCache = map.get(state);
					if (innerCache == null) {
						innerCache = new SpriteKeyCache(state);
						map.put(state, innerCache);
					}
				} finally {
					lock.unlockWrite(writeStamp);
				}
			}

			return innerCache;
		}

		public void clear() {
			long writeStamp = lock.writeLock();
			try {
				map.values().forEach(SpriteKeyCache::clear);
			} finally {
				lock.unlockWrite(writeStamp);
			}
		}
	}

	private static class SpriteKeyCache implements Function<Sprite, Slice> {
		private final Reference2ReferenceOpenHashMap<Sprite, Slice> map = new Reference2ReferenceOpenHashMap<>(4, Hash.FAST_LOAD_FACTOR);
		private final StampedLock lock = new StampedLock();
		private final BlockState state;

		public SpriteKeyCache(BlockState state) {
			this.state = state;
		}

		@Override
		public Slice apply(Sprite sprite) {
			Slice slice;

			long optimisticReadStamp = lock.tryOptimisticRead();
			if (optimisticReadStamp != 0L) {
				try {
					// This map read could happen at the same time as a map write, so catch any exceptions.
					// This is safe due to the map implementation used, which is guaranteed to not mutate the map during
					// a read.
					slice = map.get(sprite);
					if (slice != null && lock.validate(optimisticReadStamp)) {
						return slice;
					}
				} catch (Exception e) {
					//
				}
			}

			long readStamp = lock.readLock();
			try {
				slice = map.get(sprite);
			} finally {
				lock.unlockRead(readStamp);
			}

			if (slice == null) {
				long writeStamp = lock.writeLock();
				try {
					slice = map.get(sprite);
					if (slice == null) {
						slice = computeSlice(state, sprite);
						map.put(sprite, slice);
					}
				} finally {
					lock.unlockWrite(writeStamp);
				}
			}

			return slice;
		}

		public void clear() {
			long writeStamp = lock.writeLock();
			try {
				map.clear();
			} finally {
				lock.unlockWrite(writeStamp);
			}
		}
	}
}
