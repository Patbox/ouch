package eu.pb4.ouch;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.predicate.api.PredicateContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public record Preset(List<List<DamageDisplayLogic>> damage, List<List<HealDisplayLogic>> healing, List<List<DamageDisplayLogic>> death) {
    public static final Preset EMPTY = new Preset(List.of(), List.of(), List.of());
    public static Preset get() {
        return ModInit.config;
    }

    public static final Codec<Preset> SELF_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DamageDisplayLogic.CODEC.listOf().listOf().fieldOf("damage").forGetter(Preset::damage),
            HealDisplayLogic.CODEC.listOf().listOf().fieldOf("healing").forGetter(Preset::healing),
            DamageDisplayLogic.CODEC.listOf().listOf().fieldOf("death").forGetter(Preset::death)
    ).apply(instance, Preset::new));

    public static final Codec<Either<String, Preset>> CODEC = Codec.either(Codec.STRING.fieldOf("preset").codec(), SELF_CODEC);

    public void selectDamage(LivingEntity entity, DamageSource source, float amount, BiConsumer<Text, FloatingText.DisplaySettings> consumer) {
        var predicateContext = PredicateContext.of(entity);
        var attackerContext = source.getAttacker() != null ? PredicateContext.of(source.getAttacker()) : PredicateContext.of(Objects.requireNonNull(entity.getServer()));
        var sourceContext = source.getSource() != null ? PredicateContext.of(source.getSource()) : PredicateContext.of(Objects.requireNonNull(entity.getServer()));
        for (var a : this.damage) {
            for (var e : a) {
                if (e.match(entity, amount, source, predicateContext, attackerContext, sourceContext)) {
                    e.provideDamage(entity, source, amount, consumer);
                    break;
                }
            }
        }
    }

    public void selectDeath(LivingEntity entity, DamageSource source, BiConsumer<Text, FloatingText.DisplaySettings> consumer) {
        var predicateContext = PredicateContext.of(entity);
        var attackerContext = source.getAttacker() != null ? PredicateContext.of(source.getAttacker()) : PredicateContext.of(Objects.requireNonNull(entity.getServer()));
        var sourceContext = source.getSource() != null ? PredicateContext.of(source.getSource()) : PredicateContext.of(Objects.requireNonNull(entity.getServer()));
        for (var a : this.death) {
            for (var e : a) {
                if (e.match(entity, 0, source, predicateContext, attackerContext, sourceContext)) {
                    e.provideDeath(entity, source, consumer);
                    break;
                }
            }
        }

    }

    public void selectHealing(LivingEntity entity, float amount, BiConsumer<Text, FloatingText.DisplaySettings> consumer) {
        var predicateContext = PredicateContext.of(entity);
        for (var a : this.healing) {
            for (var e : a) {
                if (e.match(entity, amount, predicateContext)) {
                    e.provide(entity, amount, consumer);
                    break;
                }
            }
        }
    }
}
