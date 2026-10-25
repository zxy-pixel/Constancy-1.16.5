package me.pepperbell.continuity.client.mixin;
import net.minecraft.client.render.model.BakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(targets = "net.minecraft.client.render.model.WeightedBakedModel$Entry")
public interface WeightedEntryAccessor {
    @Accessor("model") BakedModel continuity$getModel();
}
