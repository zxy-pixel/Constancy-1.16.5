// Modified 2026-10-22 for the unofficial Forge 1.16.5 backport.
package me.pepperbell.continuity.client.processor;

import java.util.function.Supplier;

import me.pepperbell.continuity.api.client.QuadProcessor;
import org.thinkingstudio.constancy.render.api.mesh.MutableQuadView;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import java.util.Random;
import net.minecraft.world.BlockRenderView;

public abstract class AbstractQuadProcessor implements QuadProcessor {
    @Override public int getExtraLayerMask() { return 0; }
	protected Sprite[] sprites;
	protected ProcessingPredicate processingPredicate;

	public AbstractQuadProcessor(Sprite[] sprites, ProcessingPredicate processingPredicate) {
		this.sprites = sprites;
		this.processingPredicate = processingPredicate;
	}

	@Override
	public ProcessingResult processQuad(MutableQuadView quad, Sprite sprite, BlockRenderView blockView, BlockState appearanceState, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, int pass, ProcessingContext context) {
		if (!processingPredicate.shouldProcessQuad(quad, sprite, blockView, appearanceState, state, pos, context)) {
			return ProcessingResult.NEXT_PROCESSOR;
		}
		return processQuadInner(quad, sprite, blockView, appearanceState, state, pos, randomSupplier, pass, context);
	}

	public abstract ProcessingResult processQuadInner(MutableQuadView quad, Sprite sprite, BlockRenderView blockView, BlockState appearanceState, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, int pass, ProcessingContext context);
}
