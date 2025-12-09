package eu.pb4.ouch;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.ouch.api.FloatRange;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.TagLikeParser;
import eu.pb4.placeholders.api.parsers.WrappedText;
import eu.pb4.predicate.api.BuiltinPredicates;
import eu.pb4.predicate.api.MinecraftPredicate;
import eu.pb4.predicate.api.PredicateContext;
import eu.pb4.predicate.api.PredicateRegistry;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;

public record DamageDisplayLogic(Optional<HolderSet<DamageType>> type,
                                 MinecraftPredicate victimPredicate,
                                 MinecraftPredicate attackerPredicate,
                                 MinecraftPredicate sourcePredicate,
                                 FloatRange range,
                                 float chance,
                                 WrappedText text,
                                 FloatingText.DisplaySettings displaySettings) {
    static final ParserContext.Key<Function<String, Component>> PLACEHOLDER_KEY = ParserContext.Key.of("ouch:placeholder");

    public static final NodeParser PARSER = NodeParser.builder()
            .quickText()
            .placeholders(TagLikeParser.PLACEHOLDER_USER, PLACEHOLDER_KEY)
            .staticPreParsing()
            .build();

    public static final Codec<DamageDisplayLogic> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.DAMAGE_TYPE).optionalFieldOf("type").forGetter(DamageDisplayLogic::type),
            PredicateRegistry.CODEC.optionalFieldOf("victim", BuiltinPredicates.alwaysTrue()).forGetter(DamageDisplayLogic::victimPredicate),
            PredicateRegistry.CODEC.optionalFieldOf("attacker", BuiltinPredicates.alwaysTrue()).forGetter(DamageDisplayLogic::attackerPredicate),
            PredicateRegistry.CODEC.optionalFieldOf("source", BuiltinPredicates.alwaysTrue()).forGetter(DamageDisplayLogic::sourcePredicate),
            FloatRange.CODEC.orElse(FloatRange.ALL).forGetter(DamageDisplayLogic::range),
            Codec.floatRange(0, 1).optionalFieldOf("chance", 1f).forGetter(DamageDisplayLogic::chance),
            PARSER.codec().fieldOf("text").forGetter(DamageDisplayLogic::text),
            FloatingText.DisplaySettings.CODEC.orElse(FloatingText.DisplaySettings.GENERAL).forGetter(DamageDisplayLogic::displaySettings)
    ).apply(instance, DamageDisplayLogic::new));


    public static DamageDisplayLogic of(HolderLookup.Provider wrapper, String format, FloatRange range, float chance, MinecraftPredicate victimPredicate, MinecraftPredicate sourcePredicate, MinecraftPredicate attackerPredicate, ResourceKey<DamageType>... type) {
        var x = wrapper.lookupOrThrow(Registries.DAMAGE_TYPE);
        return new DamageDisplayLogic(type.length == 0 ? Optional.empty() : Optional.of(HolderSet.direct(Arrays.stream(type).map(x::getOrThrow).toList())),
                victimPredicate,
                attackerPredicate,
                sourcePredicate,
                range,
                chance,
                WrappedText.from(PARSER, format),
                FloatingText.DisplaySettings.GENERAL
        );
    }
    public static DamageDisplayLogic of(HolderLookup.Provider wrapper, String format, FloatRange range, float chance, MinecraftPredicate victimPredicate, MinecraftPredicate sourcePredicate, MinecraftPredicate attackerPredicate, TagKey<DamageType> tag) {
        return new DamageDisplayLogic(Optional.of(wrapper.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(tag)),
                victimPredicate,
                attackerPredicate,
                sourcePredicate,
                range,
                chance,
                WrappedText.from(PARSER, format),
                FloatingText.DisplaySettings.GENERAL
        );
    }

    public void provideDamage(LivingEntity entity, DamageSource source, float amount, BiConsumer<Component, FloatingText.DisplaySettings> consumer) {
        consumer.accept(this.text.textNode().toText(PlaceholderContext.of(entity).asParserContext().with(PLACEHOLDER_KEY, key -> switch (key) {
            case "value" -> Component.literal(Mth.floor(amount) + "." + (Mth.floor(amount * 10) % 10));
            case "value_rounded" -> Component.literal("" + Math.round(amount));
            case "value_raw" -> Component.literal("" + amount);
            case null, default -> Component.empty();
        })), this.displaySettings);
    }

    public void provideDeath(LivingEntity entity, DamageSource source, BiConsumer<Component, FloatingText.DisplaySettings> consumer) {
        consumer.accept(this.text.textNode().toText(PlaceholderContext.of(entity).asParserContext().with(PLACEHOLDER_KEY, key -> switch (key) {
            case "message" -> source.getLocalizedDeathMessage(entity);
            case "victim" -> entity.getDisplayName();
            case "attacker" -> source.getEntity() != null ? source.getEntity().getDisplayName() : Component.empty();
            case null, default -> Component.empty();
        })), this.displaySettings);
    }

    public boolean match(LivingEntity entity, float amount, DamageSource source, PredicateContext predicateContext, PredicateContext attackerContext, PredicateContext sourceContext) {
        return (this.type.isEmpty() || this.type.get().contains(source.typeHolder()))
                && this.range.test(amount)
                && this.victimPredicate.test(predicateContext).success()
                && this.attackerPredicate.test(attackerContext).success()
                && this.sourcePredicate.test(sourceContext).success()
                && entity.getRandom().nextFloat() <= this.chance;
    }
}
