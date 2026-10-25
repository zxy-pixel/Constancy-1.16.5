// Modified 2026-10-22 for the unofficial Forge 1.16.5 backport.
package me.pepperbell.continuity.client.processor;

import java.util.EnumSet;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import me.pepperbell.continuity.api.client.ProcessingDataProvider;
import me.pepperbell.continuity.client.properties.BaseCtmProperties;
import me.pepperbell.continuity.client.util.biome.BiomeRetriever;
import org.thinkingstudio.constancy.render.api.mesh.QuadView;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.texture.Sprite;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.Biome;

public class BaseProcessingPredicate implements ProcessingPredicate {
	@Nullable
	protected EnumSet<Direction> faces;
	@Nullable
	protected Predicate<Biome> biomePredicate;
	@Nullable
	protected IntPredicate heightPredicate;
	@Nullable
	protected Predicate<String> blockEntityNamePredicate;

	public BaseProcessingPredicate(@Nullable EnumSet<Direction> faces, @Nullable Predicate<Biome> biomePredicate, @Nullable IntPredicate heightPredicate, @Nullable Predicate<String> blockEntityNamePredicate) {
		this.faces = faces;
		this.biomePredicate = biomePredicate;
		this.heightPredicate = heightPredicate;
		this.blockEntityNamePredicate = blockEntityNamePredicate;
	}

	@Override
	public boolean shouldProcessQuad(QuadView quad, Sprite sprite, BlockRenderView blockView, BlockState appearanceState, BlockState state, BlockPos pos, ProcessingDataProvider dataProvider) {
		if (heightPredicate != null) {
			if (!heightPredicate.test(pos.getY())) {
				return false;
			}
		}
		if (faces != null) {
			Direction face = quad.lightFace();
 			if (appearanceState.contains(Properties.AXIS)) {
 				Direction.Axis axis = appearanceState.get(Properties.AXIS);
 				if (axis == Direction.Axis.X) {
					face = org.thinkingstudio.constancy.render.LegacyMath.rotateClockwise(face, Direction.Axis.Z);
				} else if (axis == Direction.Axis.Z) {
					face = org.thinkingstudio.constancy.render.LegacyMath.rotateCounterclockwise(face, Direction.Axis.X);
				}
			}
			if (!faces.contains(face)) {
				return false;
			}
		}
		if (biomePredicate != null) {
			Biome biome = dataProvider.getData(ProcessingDataKeys.BIOME_CACHE).get(blockView, pos);
			if (biome == null || !biomePredicate.test(biome)) {
				return false;
			}
		}
		if (blockEntityNamePredicate != null) {
			String blockEntityName = dataProvider.getData(ProcessingDataKeys.BLOCK_ENTITY_NAME_CACHE).get(blockView, pos);
			if (blockEntityName == null || !blockEntityNamePredicate.test(blockEntityName)) {
				return false;
			}
		}
		return true;
	}

	public static BaseProcessingPredicate fromProperties(BaseCtmProperties properties) {
		return new BaseProcessingPredicate(properties.getFaces(), properties.getBiomePredicate(), properties.getHeightPredicate(), properties.getBlockEntityNamePredicate());
	}

	public static class BiomeCache {
		@Nullable
		protected Biome biome;
		protected boolean invalid = true;

		@Nullable
		public Biome get(BlockRenderView blockView, BlockPos pos) {
			if (invalid) {
				biome = BiomeRetriever.getBiome(blockView, pos);
				invalid = false;
			}
			return biome;
		}

		public void reset() {
			invalid = true;
		}
	}

	public static class BlockEntityNameCache {
		@Nullable
		protected String blockEntityName;
		protected boolean invalid = true;

		@Nullable
		public String get(BlockRenderView blockView, BlockPos pos) {
			if (invalid) {
				BlockEntity blockEntity = blockView.getBlockEntity(pos);
				if (blockEntity instanceof Nameable) {
                    Nameable nameable = (Nameable) blockEntity;
					if (nameable.hasCustomName()) {
						blockEntityName = nameable.getCustomName().getString();
					} else {
						blockEntityName = null;
					}
				} else {
					blockEntityName = null;
				}
				invalid = false;
			}
			return blockEntityName;
		}

		public void reset() {
			invalid = true;
		}
	}
}
