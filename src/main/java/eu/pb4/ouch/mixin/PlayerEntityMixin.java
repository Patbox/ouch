package eu.pb4.ouch.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.ouch.FloatingText;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends Entity {
    public PlayerEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;emitGameEvent(Lnet/minecraft/registry/entry/RegistryEntry;)V"))
    private void onDamageApplied(ServerWorld world, DamageSource source, float amount, CallbackInfo ci) {
        if (!this.getWorld().isClient) {
            FloatingText.createDamage((LivingEntity) (Object) this, source, amount);
        }
    }

}
