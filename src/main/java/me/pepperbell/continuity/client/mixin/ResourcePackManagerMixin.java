package me.pepperbell.continuity.client.mixin;

import net.minecraft.resource.ResourcePackManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.thinkingstudio.constancy.ported.resource.ResourceManagerHelper;

@Mixin(ResourcePackManager.class)
public abstract class ResourcePackManagerMixin {
    @Inject(method = "scanPacks", at = @At("HEAD"))
    private void continuity$builtinPacks(CallbackInfo ci) {
        ResourceManagerHelper.installProviders((ResourcePackManager) (Object) this);
    }
}
