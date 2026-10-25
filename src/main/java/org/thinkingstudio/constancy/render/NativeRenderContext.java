package org.thinkingstudio.constancy.render;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;
import me.pepperbell.continuity.client.config.ContinuityConfig;
import me.pepperbell.continuity.client.resource.CustomBlockLayers;
import me.pepperbell.continuity.client.util.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.thinkingstudio.constancy.render.api.material.BlendMode;
import org.thinkingstudio.constancy.render.api.material.RenderMaterial;
import org.thinkingstudio.constancy.render.api.mesh.Mesh;
import org.thinkingstudio.constancy.render.api.mesh.QuadEmitter;
import org.thinkingstudio.constancy.render.api.mesh.QuadView;
import org.thinkingstudio.constancy.render.api.model.FabricBakedModel;
import org.thinkingstudio.constancy.render.api.model.ForwardingBakedModel;
import org.thinkingstudio.constancy.render.api.render.RenderContext;
import org.thinkingstudio.constancy.render.impl.IndigoRenderer;
import org.thinkingstudio.constancy.render.impl.mesh.EncodingFormat;
import org.thinkingstudio.constancy.render.impl.mesh.MutableQuadViewImpl;

/** A render-local bridge; no world or position is stored on shared baked models. */
public final class NativeRenderContext implements RenderContext {
    private final BlockRenderView world;
    private final BlockState state;
    private final BlockPos pos;
    private final IModelData modelData;
    private final long seed;
    private final boolean cull;
    private final RenderLayer layer;
    private final Deque<QuadTransform> transforms = new ArrayDeque<>();
    private final List<BakedQuad>[] quads;
    private final OutputEmitter emitter = new OutputEmitter();

    @SuppressWarnings("unchecked")
    private NativeRenderContext(BlockRenderView world, BlockState state, BlockPos pos, long seed,
            IModelData modelData, boolean cull, RenderLayer layer) {
        this.world = world; this.state = state; this.pos = pos; this.seed = seed;
        this.modelData = modelData; this.cull = cull; this.layer = layer;
        quads = new List[7];
        for (int i = 0; i < quads.length; i++) quads[i] = new ArrayList<>();
    }

    public static BakedModel block(BakedModel model, BlockRenderView world, BlockState state, BlockPos pos,
            long seed, IModelData data, boolean cull) {
        if (!(model instanceof FabricBakedModel) || model instanceof PreparedModel) return model;
        if (((FabricBakedModel) model).isVanillaAdapter()) return model;
        if (world.getBlockState(pos) != state) return model;
        IModelData modelData = model.getModelData(world, pos, state, data);
        NativeRenderContext context = new NativeRenderContext(world, state, pos, seed, modelData, cull, MinecraftForgeClient.getRenderLayer());
        ((FabricBakedModel) model).emitBlockQuads(world, state, pos, () -> new Random(seed), context);
        return context.new PreparedModel(model);
    }

    public static BakedModel item(BakedModel model, ItemStack stack) {
        if (!(model instanceof FabricBakedModel) || model instanceof PreparedModel) return model;
        if (((FabricBakedModel) model).isVanillaAdapter()) return model;
        NativeRenderContext context = new NativeRenderContext(null, null, null, 42, EmptyModelData.INSTANCE, false, null);
        ((FabricBakedModel) model).emitItemQuads(stack, () -> new Random(42), context);
        return context.new PreparedModel(model);
    }

    @Override public Consumer<Mesh> meshConsumer() { return mesh -> mesh.outputTo(emitter); }
    @Override public Consumer<BakedModel> fallbackConsumer() { return this::fallback; }
    @Override public QuadEmitter getEmitter() { return emitter; }
    @Override public void pushTransform(QuadTransform transform) { transforms.push(transform); }
    @Override public void popTransform() { transforms.pop(); }
    @Override public boolean isFaceCulled(Direction face) {
        return cull && face != null && world != null && !Block.shouldDrawSide(state, world, pos, face);
    }

    private void fallback(BakedModel model) {
        for (int side = 0; side < 7; side++) {
            Direction face = side == 6 ? null : Direction.values()[side];
            for (BakedQuad quad : model.getQuads(state, face, new Random(seed), modelData)) {
                emitter.fromVanilla(quad, IndigoRenderer.MATERIAL_STANDARD, face);
                emitter.emit();
            }
        }
    }

    private RenderLayer defaultLayer() {
        RenderLayer custom = ContinuityConfig.INSTANCE.customBlockLayers.get() ? CustomBlockLayers.getLayer(state) : null;
        return custom != null ? custom : RenderLayers.getBlockLayer(state);
    }

    private final class OutputEmitter extends MutableQuadViewImpl {
        OutputEmitter() { begin(new int[EncodingFormat.TOTAL_STRIDE], 0); }
        @Override public QuadEmitter emit() {
            try {
                for (QuadTransform transform : transforms) if (!transform.transform(this)) return this;
                if (isFaceCulled(cullFace())) return this;
                RenderMaterial material = material();
                if (world != null && layer != null) {
                    RenderLayer selected = material.blendMode() == BlendMode.DEFAULT ? defaultLayer() : material.blendMode().blockRenderLayer;
                    if (layer != selected) return this;
                }
                int[] vertices = new int[QuadView.VANILLA_QUAD_STRIDE];
                toVanilla(0, vertices, 0, world == null);
                if (material.emissive(0)) for (int i = 0; i < 4; i++) vertices[i * QuadView.VANILLA_VERTEX_STRIDE + 6] = 0xF000F0;
                BakedQuad quad = new NativeQuad(vertices, material.disableColorIndex(0) ? -1 : colorIndex(), lightFace(),
                        RenderUtil.getSpriteFinder().find(this), hasShade(), material);
                Direction face = cullFace();
                quads[face == null ? 6 : face.ordinal()].add(quad);
                return this;
            } finally { clear(); }
        }
    }

    private final class PreparedModel extends ForwardingBakedModel {
        PreparedModel(BakedModel original) { wrapped = original; }
        @Override public List<BakedQuad> getQuads(BlockState state, Direction face, Random random) { return quads[face == null ? 6 : face.ordinal()]; }
        @Override public List<BakedQuad> getQuads(BlockState state, Direction face, Random random, IModelData data) { return getQuads(state, face, random); }
        @Override public IModelData getModelData(BlockRenderView world, BlockPos pos, BlockState state, IModelData data) { return modelData; }
        @Override public boolean isVanillaAdapter() { return true; }
    }
}
