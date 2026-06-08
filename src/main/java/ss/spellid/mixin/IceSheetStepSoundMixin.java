package ss.spellid.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ss.spellid.block.custom.IceSheetBlock;

@Mixin(Entity.class)
public abstract class IceSheetStepSoundMixin {

    @Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
    private void onPlayStepSound(BlockPos pos, BlockState state, CallbackInfo ci) {
        Entity self = (Entity)(Object)this;

        // Check if we are standing on an ice sheet
        BlockPos above = pos.above();
        BlockState aboveState = self.level().getBlockState(above);

        if (aboveState.getBlock() instanceof IceSheetBlock) {
            // Play ice step sound instead
            self.playSound(
                    net.minecraft.sounds.SoundEvents.GLASS_STEP,
                    0.15f,
                    1.0f
            );
            ci.cancel();
        }
    }
}