package me.pepperbell.continuity.client.mixin;
import java.util.List;
import net.minecraft.client.render.model.WeightedBakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(WeightedBakedModel.class)
public interface WeightedModelAccessor {
    @Accessor("models") List<?> continuity$getModels();
}
