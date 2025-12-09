package eu.pb4.ouch.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.ouch.FloatingText;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow public abstract float getHealth();

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;gameEvent(Lnet/minecraft/core/Holder;)V"))
    private void onDamageApplied(ServerLevel world, DamageSource source, float amount, CallbackInfo ci) {
        if (!this.level().isClientSide()) {
            FloatingText.createDamage((LivingEntity) (Object) this, source, amount);
        }
    }

    @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;gameEvent(Lnet/minecraft/core/Holder;)V"))
    private void onDamageApplied(DamageSource source, CallbackInfo ci) {
        if (!this.level().isClientSide()) {
            FloatingText.createDeath((LivingEntity) (Object) this, source);
        }
    }

    @Inject(method = "heal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V", shift = At.Shift.AFTER))
    private void onHealApplied(float amount, CallbackInfo ci, @Local(ordinal = 1) float oldHealth) {
        if (!this.level().isClientSide() && oldHealth != this.getHealth()) {
            FloatingText.createHealing((LivingEntity) (Object) this, this.getHealth() - oldHealth);
        }
    }
}
