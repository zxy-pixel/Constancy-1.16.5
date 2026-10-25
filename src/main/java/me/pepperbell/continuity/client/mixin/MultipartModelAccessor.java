package me.pepperbell.continuity.client.mixin;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.MultipartBakedModel;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(MultipartBakedModel.class)
public interface MultipartModelAccessor {
    @Accessor("components") List<Pair<Predicate<BlockState>, BakedModel>> continuity$getComponents();
}
