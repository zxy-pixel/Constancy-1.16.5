package org.thinkingstudio.constancy.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import me.pepperbell.continuity.api.client.CachingPredicates;
import me.pepperbell.continuity.client.config.ContinuityConfig;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BasicBakedModel;
import net.minecraft.util.math.Direction;
import me.pepperbell.continuity.client.model.CtmBakedModel;
import me.pepperbell.continuity.client.model.EmissiveBakedModel;
import me.pepperbell.continuity.client.model.QuadProcessors;
import me.pepperbell.continuity.client.mixinterface.SpriteExtension;
import me.pepperbell.continuity.client.resource.CtmPropertiesLoader;
import me.pepperbell.continuity.client.resource.CustomBlockLayers;
import me.pepperbell.continuity.client.resource.EmissiveSuffixLoader;
import me.pepperbell.continuity.client.resource.ResourceRedirectHandler;
import me.pepperbell.continuity.client.util.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.client.event.ModelBakeEvent;
import org.thinkingstudio.constancy.render.support.SpriteFinderImpl;

/** The 1.16 atlas lifecycle replaces the 1.20 AtlasLoader/SpriteLoader pipeline. */
public final class NativeResources {
    private static CtmPropertiesLoader.LoadingResult loading;
    private static final Map<Identifier, Identifier> EMISSIVE_IDS = new HashMap<>();
    private static SpriteAtlasTexture blockAtlas;
    private static volatile boolean active;
    public static boolean hasRendering() { return active; }
    private static volatile Map<BlockState, RenderInfo> renderInfo = Collections.emptyMap();
    private static final class RenderInfo {
        final boolean ctm, emissive;
        final int overlayMask;
        RenderInfo(boolean ctm, boolean emissive, int mask) { this.ctm = ctm; this.emissive = emissive; overlayMask = mask; }
    }
    public static boolean needsExtraLayer(BlockState state, RenderLayer layer) {
        RenderInfo info = renderInfo.get(state);
        if (info == null) return false;
        if (info.emissive && ContinuityConfig.INSTANCE.emissiveTextures.get()) return true;
        if (!info.ctm || !ContinuityConfig.INSTANCE.connectedTextures.get()) return false;
        int bit = layer == RenderLayer.getSolid() ? 2 : layer == RenderLayer.getCutoutMipped() ? 4
                : layer == RenderLayer.getCutout() ? 8 : layer == RenderLayer.getTranslucent() ? 16 : 0;
        return (info.overlayMask & bit) != 0;
    }

    public static void beginAtlas(ResourceManager manager, Identifier atlasId) {
        if (!SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE.equals(atlasId)) return;
        ResourceRedirectHandler.reset();
        EMISSIVE_IDS.clear();
        loading = CtmPropertiesLoader.loadAllWithState(manager);
        EmissiveSuffixLoader.load(manager);
        CustomBlockLayers.reload(manager);
    }

    public static void addSprites(ResourceManager manager, Identifier atlasId, Set<Identifier> ids) {
        if (!SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE.equals(atlasId) || loading == null) return;
        Set<Identifier> extra = loading.getTextureDependencies().get(atlasId);
        if (extra != null) ids.addAll(extra);
        String suffix = EmissiveSuffixLoader.getEmissiveSuffix();
        if (suffix == null || suffix.isEmpty()) return;
        for (Identifier id : new HashSet<>(ids)) {
            Identifier emissive = new Identifier(id.getNamespace(), id.getPath() + suffix);
            Identifier location = new Identifier(emissive.getNamespace(), "textures/" + emissive.getPath() + ".png");
            location = ResourceRedirectHandler.get(manager).redirect(location);
            if (manager.containsResource(location)) {
                ids.add(emissive);
                EMISSIVE_IDS.put(id, emissive);
            }
        }
    }

    public static void atlasUploaded(SpriteAtlasTexture atlas, Map<Identifier, Sprite> sprites) {
        if (!SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE.equals(atlas.getId())) return;
        blockAtlas = atlas;
        RenderUtil.setSpriteFinder(new SpriteFinderImpl(sprites, atlas));
        EMISSIVE_IDS.forEach((id, emissiveId) -> {
            Sprite sprite = sprites.get(id);
            Sprite emissive = sprites.get(emissiveId);
            if (sprite != null && emissive != null) ((SpriteExtension) sprite).continuity$setEmissiveSprite(emissive);
        });
    }

    public static void onModelBake(ModelBakeEvent event) {
        if (loading == null || blockAtlas == null) return;
        List<QuadProcessors.ProcessorHolder> processors = loading.createProcessorHolders(spriteId ->
                event.getModelLoader().getSpriteMap().getSprite(spriteId));
        QuadProcessors.reload(processors);
        me.pepperbell.continuity.client.util.SpriteCalculator.clearCache();
        Map<ModelIdentifier, BlockState> states = new HashMap<>();
        for (Block block : Registry.BLOCK) {
            Identifier id = Registry.BLOCK.getId(block);
            for (BlockState state : block.getStateManager().getStates()) states.put(BlockModels.getModelId(id, state), state);
        }
        boolean ctm = !loading.getTextureDependencies().isEmpty();
        boolean emissive = !EMISSIVE_IDS.isEmpty();
        boolean emissiveTiles = false;
        for (Set<Identifier> dependencies : loading.getTextureDependencies().values()) {
            for (Identifier tile : dependencies) if (EMISSIVE_IDS.containsKey(tile)) emissiveTiles = true;
        }
        final boolean ctmTilesEmissive = emissiveTiles;
        active = ctm || emissive;
        Map<BlockState, RenderInfo> nextInfo = new IdentityHashMap<>();
        event.getModelRegistry().replaceAll((id, model) -> {
            if (model == null || model.isBuiltin() || id.equals(new ModelIdentifier(MissingSprite.getMissingSpriteId(), "missing"))) return model;
            BakedModel result = model;
            BlockState state = states.get(id);
            boolean modelCtm = false, modelEmissive = emissive;
            int extraMask = 0;
            if (state != null && ctm) for (QuadProcessors.ProcessorHolder holder : processors) {
                CachingPredicates predicate = holder.predicates();
                if (!predicate.affectsBlockStates() || predicate.affectsBlockState(state)) {
                    modelCtm = true;
                    extraMask |= holder.processor().getExtraLayerMask();
                }
            }
            List<BakedQuad> staticQuads = new ArrayList<>();
            if (collectStaticQuads(model, state, staticQuads)) {
                boolean matches = false, emits = false;
                for (BakedQuad quad : staticQuads) {
                    Sprite sprite = quad.func_187508_a();
                    if (state != null && modelCtm && QuadProcessors.getCache(state).apply(sprite).processors().length > 0) matches = true;
                    if (emissive && ((SpriteExtension)sprite).continuity$getEmissiveSprite() != null) emits = true;
                }
                modelCtm &= matches;
                modelEmissive &= emits;
            }
            if (modelCtm && ctmTilesEmissive) modelEmissive = true;
            if (state != null) nextInfo.put(state, new RenderInfo(modelCtm, modelEmissive, extraMask));
            if (modelCtm && state != null) result = new CtmBakedModel(result, state);
            if (modelEmissive) result = new EmissiveBakedModel(result);
            return result;
        });
        renderInfo = Collections.unmodifiableMap(nextInfo);
        me.pepperbell.continuity.client.ContinuityClient.LOGGER.info("Loaded {} CTM processors and {} emissive sprites", processors.size(), EMISSIVE_IDS.size());
    }

    /** Inspect every vanilla variant; sampling a weighted model can miss an emissive texture. */
    private static boolean collectStaticQuads(BakedModel model, BlockState state, List<BakedQuad> output) {
        if (model instanceof BasicBakedModel) {
            for (int side = 0; side < 7; side++) output.addAll(model.getQuads(state,
                    side == 6 ? null : Direction.values()[side], new Random(42)));
            return true;
        }
        if (model instanceof me.pepperbell.continuity.client.mixin.WeightedModelAccessor) {
            for (Object entry : ((me.pepperbell.continuity.client.mixin.WeightedModelAccessor) model).continuity$getModels()) {
                BakedModel child = ((me.pepperbell.continuity.client.mixin.WeightedEntryAccessor) entry).continuity$getModel();
                if (!collectStaticQuads(child, state, output)) return false;
            }
            return true;
        }
        if (state != null && model instanceof me.pepperbell.continuity.client.mixin.MultipartModelAccessor) {
            for (org.apache.commons.lang3.tuple.Pair<java.util.function.Predicate<BlockState>, BakedModel> component :
                    ((me.pepperbell.continuity.client.mixin.MultipartModelAccessor) model).continuity$getComponents()) {
                if (component.getLeft().test(state) && !collectStaticQuads(component.getRight(), state, output)) return false;
            }
            return true;
        }
        // Unknown Forge models may vary with IModelData; keep their feature wrappers.
        return false;
    }
}
