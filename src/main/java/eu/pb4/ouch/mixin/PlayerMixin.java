package eu.pb4.ouch.mixin;

import eu.pb4.ouch.FloatingText;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends Entity {
    public PlayerMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;gameEvent(Lnet/minecraft/core/Holder;)V"))
    private void onDamageApplied(ServerLevel world, DamageSource source, float amount, CallbackInfo ci) {
        if (!this.level().isClientSide()) {
            FloatingText.createDamage((LivingEntity) (Object) this, source, amount);
        }
    }

}
