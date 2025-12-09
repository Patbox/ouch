package eu.pb4.ouch.mixin;

import eu.pb4.ouch.FloatingText;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStand.class)
public abstract class ArmorStandMixin extends LivingEntity {
    protected ArmorStandMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "causeDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ArmorStand;gameEvent(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/Entity;)V"))
    private void onDamageApplied(ServerLevel world, DamageSource source, float amount, CallbackInfo ci) {
        this.createText(source, amount);
    }

    @Inject(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ArmorStand;gameEvent(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/Entity;)V"))
    private void onDamageApplied2(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        this.createText(source, amount);
    }

    @Inject(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ArmorStand;kill(Lnet/minecraft/server/level/ServerLevel;)V"))
    private void onDamageApplied3(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        this.createText(source, amount);
    }

    @Unique
    private void createText(DamageSource source, float amount) {
        if (!this.level().isClientSide()) {
            amount = CombatRules.getDamageAfterAbsorb(this, amount, source, (float) this.getArmorValue(), (float) this.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
            FloatingText.createDamage(this, source, amount);
        }
    }

}
