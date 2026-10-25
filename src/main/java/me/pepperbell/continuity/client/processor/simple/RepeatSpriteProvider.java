// Modified 2026-10-22 for the unofficial Forge 1.16.5 backport.
package me.pepperbell.continuity.client.processor.simple;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import me.pepperbell.continuity.api.client.ProcessingDataProvider;
import me.pepperbell.continuity.client.processor.OrientationMode;
import me.pepperbell.continuity.client.processor.Symmetry;
import me.pepperbell.continuity.client.properties.RepeatCtmProperties;
import org.thinkingstudio.constancy.render.api.mesh.QuadView;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import java.util.Random;
import net.minecraft.world.BlockRenderView;

public class RepeatSpriteProvider implements SpriteProvider {
	protected Sprite[] sprites;
	protected int width;
	protected int height;
	protected Symmetry symmetry;
	protected OrientationMode orientationMode;

	public RepeatSpriteProvider(Sprite[] sprites, int width, int height, Symmetry symmetry, OrientationMode orientationMode) {
		this.sprites = sprites;
		this.width = width;
		this.height = height;
		this.symmetry = symmetry;
		this.orientationMode = orientationMode;
	}

	@Override
	@Nullable
	public Sprite getSprite(QuadView quad, Sprite sprite, BlockRenderView blockView, BlockState appearanceState, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, ProcessingDataProvider dataProvider) {
		Direction face = symmetry.apply(quad.lightFace());

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		int spriteX;
		int spriteY;
		switch (face) {
			case DOWN: {
				// MCPatcher uses a different formula for the down face.
				// It is not used here to maintain Optifine parity.
				// spriteX = -x;
				// spriteY = -z;
				spriteX = x;
				spriteY = -z - 1;
                break;
			}
			case UP: {
				spriteX = x;
				spriteY = z;
                break;
			}
			case NORTH: {
				spriteX = -x - 1;
				spriteY = -y;
                break;
			}
			case SOUTH: {
				spriteX = x;
				spriteY = -y;
                break;
			}
			case WEST: {
				spriteX = z;
				spriteY = -y;
                break;
			}
			case EAST: {
				spriteX = -z - 1;
				spriteY = -y;
                break;
			}
			default: {
				spriteX = 0;
				spriteY = 0;
                break;
			}
		}

		switch (orientationMode.getOrientation(quad, appearanceState)) {
			case 1: {
				int temp = spriteX;
				spriteX = -spriteY - 1;
				spriteY = temp;
                break;
			}
			case 2: {
				spriteX = -spriteX - 1;
				spriteY = -spriteY - 1;
                break;
			}
			case 3: {
				int temp = spriteX;
				spriteX = spriteY;
				spriteY = -temp - 1;
                break;
			}
			case 4: {
				int temp = spriteX;
				spriteX = spriteY;
				spriteY = temp;
                break;
			}
			case 5: {
				spriteY = -spriteY - 1;
                break;
			}
			case 6: {
				int temp = spriteX;
				spriteX = -spriteY - 1;
				spriteY = -temp - 1;
                break;
			}
			case 7: {
				spriteX = -spriteX - 1;
                break;
			}
		}

		spriteX %= width;
		if (spriteX < 0) {
			spriteX += width;
		}
		spriteY %= height;
		if (spriteY < 0) {
			spriteY += height;
		}

		return sprites[width * spriteY + spriteX];
	}

	public static class Factory implements SpriteProvider.Factory<RepeatCtmProperties> {
		@Override
		public SpriteProvider createSpriteProvider(Sprite[] sprites, RepeatCtmProperties properties) {
			return new RepeatSpriteProvider(sprites, properties.getWidth(), properties.getHeight(), properties.getSymmetry(), properties.getOrientationMode());
		}

		@Override
		public int getTextureAmount(RepeatCtmProperties properties) {
			return properties.getWidth() * properties.getHeight();
		}
	}
}
