package me.pepperbell.continuity.client.mixin;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.thinkingstudio.constancy.client.NativeResources;
import org.thinkingstudio.constancy.render.support.SpriteFinderImpl;

@Mixin(SpriteAtlasTexture.class)
abstract class NativeAtlasMixin implements SpriteFinderImpl.SpriteFinderAccess {
    @Shadow private Map<Identifier, Sprite> sprites;
    @Shadow public abstract Identifier getId();
    @Unique private ResourceManager continuity$manager;
    @Unique private volatile SpriteFinderImpl continuity$finder;

    @Inject(method = "stitch", at = @At("HEAD"))
    private void continuity$begin(ResourceManager manager, Stream<Identifier> ids, Profiler profiler, int mipmap,
            CallbackInfoReturnable<SpriteAtlasTexture.Data> cir) {
        continuity$manager = manager;
        NativeResources.beginAtlas(manager, getId());
    }

    @ModifyArg(method = "stitch", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/SpriteAtlasTexture;loadSprites(Lnet/minecraft/resource/ResourceManager;Ljava/util/Set;)Ljava/util/Collection;"), index = 1)
    private Set<Identifier> continuity$addSprites(Set<Identifier> ids) {
        NativeResources.addSprites(continuity$manager, getId(), ids);
        return ids;
    }

    @Inject(method = "upload", at = @At("RETURN"))
    private void continuity$uploaded(SpriteAtlasTexture.Data data, CallbackInfo ci) {
        continuity$finder = null;
        continuity$manager = null;
        NativeResources.atlasUploaded((SpriteAtlasTexture) (Object) this, sprites);
    }

    @Override public SpriteFinderImpl fabric_spriteFinder() {
        SpriteFinderImpl finder = continuity$finder;
        if (finder == null) continuity$finder = finder = new SpriteFinderImpl(sprites, (SpriteAtlasTexture) (Object) this);
        return finder;
    }
}
