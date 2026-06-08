package ss.spellid.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ss.spellid.block.custom.IceSheetBlock;

@Mixin(Entity.class)
public abstract class IceSheetGroundDetectionMixin {

    @Inject(method = "getBlockPosBelowThatAffectsMyMovement", at = @At("HEAD"), cancellable = true)
    private void onGetBlockPosBelowThatAffectsMyMovement(CallbackInfoReturnable<BlockPos> cir) {
        Entity self = (Entity)(Object)this;

        if (!self.onGround()) return;

        BlockPos groundPos = BlockPos.containing(self.getX(), self.getY() - 0.2, self.getZ());
        BlockState state = self.level().getBlockState(groundPos);

        if (state.getBlock() instanceof IceSheetBlock) {
            cir.setReturnValue(groundPos);
            return;
        }

        BlockPos above = groundPos.above();
        state = self.level().getBlockState(above);
        if (state.getBlock() instanceof IceSheetBlock) {
            cir.setReturnValue(above);
        }
    }
}