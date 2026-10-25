// Modified 2026-10-22 for the unofficial Forge 1.16.5 backport.
package me.pepperbell.continuity.client.processor.simple;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import me.pepperbell.continuity.api.client.ProcessingDataProvider;
import me.pepperbell.continuity.client.properties.BaseCtmProperties;
import org.thinkingstudio.constancy.render.api.mesh.QuadView;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import java.util.Random;
import net.minecraft.world.BlockRenderView;

public class FixedSpriteProvider implements SpriteProvider {
	protected Sprite sprite;

	public FixedSpriteProvider(Sprite sprite) {
		this.sprite = sprite;
	}

	@Override
	@Nullable
	public Sprite getSprite(QuadView quad, Sprite sprite, BlockRenderView blockView, BlockState appearanceState, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, ProcessingDataProvider dataProvider) {
		return this.sprite;
	}

	public static class Factory implements SpriteProvider.Factory<BaseCtmProperties> {
		@Override
		public SpriteProvider createSpriteProvider(Sprite[] sprites, BaseCtmProperties properties) {
			return new FixedSpriteProvider(sprites[0]);
		}

		@Override
		public int getTextureAmount(BaseCtmProperties properties) {
			return 1;
		}
	}
}
