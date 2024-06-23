package eu.pb4.ouch;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.predicate.api.PredicateContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public record Config(List<List<DamageDisplayLogic>> damage, List<List<HealDisplayLogic>> healing, List<List<DeathDisplayLogic>> death) {
    public static final Config EMPTY = new Config(List.of(), List.of(), List.of());
    public static Config get() {
        return ModInit.config != null ? ModInit.config : EMPTY;
    }

    public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DamageDisplayLogic.CODEC.listOf().listOf().fieldOf("damage").forGetter(Config::damage),
            HealDisplayLogic.CODEC.listOf().listOf().fieldOf("healing").forGetter(Config::healing),
            DeathDisplayLogic.CODEC.listOf().listOf().fieldOf("death").forGetter(Config::death)
    ).apply(instance, Config::new));

    public void selectDamage(LivingEntity entity, DamageSource source, float amount, BiConsumer<Text, FloatingText.DisplaySettings> consumer) {
        var predicateContext = PredicateContext.of(entity);
        var attackerContext = source.getAttacker() != null ? PredicateContext.of(source.getAttacker()) : PredicateContext.of(Objects.requireNonNull(entity.getServer()));
        var sourceContext = source.getSource() != null ? PredicateContext.of(source.getSource()) : PredicateContext.of(Objects.requireNonNull(entity.getServer()));
        for (var a : this.damage) {
            for (var e : a) {
                if (e.match(entity, source, predicateContext, attackerContext, sourceContext)) {
                    e.provide(entity, source, amount, consumer);
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
                if (e.match(entity, source, predicateContext, attackerContext, sourceContext)) {
                    e.provide(entity, source, consumer);
                    break;
                }
            }
        }

    }

    public void selectHealing(LivingEntity entity, float amount, BiConsumer<Text, FloatingText.DisplaySettings> consumer) {
        var predicateContext = PredicateContext.of(entity);
        for (var a : this.healing) {
            for (var e : a) {
                if (e.match(entity, predicateContext)) {
                    e.provide(entity, amount, consumer);
                    break;
                }
            }
        }
    }
}
