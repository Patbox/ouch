package eu.pb4.ouch.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.ouch.FloatingText;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow public abstract float getHealth();

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;emitGameEvent(Lnet/minecraft/registry/entry/RegistryEntry;)V"))
    private void onDamageApplied(DamageSource source, float amount, CallbackInfo ci) {
        if (!this.getWorld().isClient) {
            FloatingText.createDamage((LivingEntity) (Object) this, source, amount);
        }
    }

    @Inject(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;emitGameEvent(Lnet/minecraft/registry/entry/RegistryEntry;)V"))
    private void onDamageApplied(DamageSource source, CallbackInfo ci) {
        if (!this.getWorld().isClient) {
            FloatingText.createDeath((LivingEntity) (Object) this, source);
        }
    }

    @Inject(method = "heal", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setHealth(F)V", shift = At.Shift.AFTER))
    private void onHealApplied(float amount, CallbackInfo ci, @Local(ordinal = 1) float oldHealth) {
        if (!this.getWorld().isClient && oldHealth != this.getHealth()) {
            FloatingText.createHealing((LivingEntity) (Object) this, this.getHealth() - oldHealth);
        }
    }
}
