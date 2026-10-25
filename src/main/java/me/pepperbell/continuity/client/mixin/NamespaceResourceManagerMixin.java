// Modified 2026-10-22 for the unofficial Forge 1.16.5 backport.
package me.pepperbell.continuity.client.mixin;

import me.pepperbell.continuity.client.resource.ResourceRedirectHandler;
import net.minecraft.resource.NamespaceResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(NamespaceResourceManager.class)
abstract class NamespaceResourceManagerMixin {
    @ModifyVariable(method = {"getResource", "getAllResources", "containsResource"}, at = @At("HEAD"), argsOnly = true)
    private Identifier continuity$redirect(Identifier id) { return ResourceRedirectHandler.get(null).redirect(id); }
}
