package eu.pb4.ouch.mixin;

import eu.pb4.ouch.FloatingText;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStandEntity.class)
public abstract class ArmorStandEntityMixin extends LivingEntity {
    protected ArmorStandEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "updateHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/decoration/ArmorStandEntity;emitGameEvent(Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/entity/Entity;)V"))
    private void onDamageApplied(ServerWorld world, DamageSource source, float amount, CallbackInfo ci) {
        this.createText(source, amount);
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/decoration/ArmorStandEntity;emitGameEvent(Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/entity/Entity;)V"))
    private void onDamageApplied2(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        this.createText(source, amount);
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/decoration/ArmorStandEntity;kill(Lnet/minecraft/server/world/ServerWorld;)V"))
    private void onDamageApplied3(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        this.createText(source, amount);
    }

    @Unique
    private void createText(DamageSource source, float amount) {
        if (!this.getWorld().isClient) {
            amount = DamageUtil.getDamageLeft(this, amount, source, (float) this.getArmor(), (float) this.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS));
            FloatingText.createDamage(this, source, amount);
        }
    }

}
