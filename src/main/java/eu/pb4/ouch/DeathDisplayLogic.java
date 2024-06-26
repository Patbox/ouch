package eu.pb4.ouch;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.WrappedText;
import eu.pb4.predicate.api.BuiltinPredicates;
import eu.pb4.predicate.api.MinecraftPredicate;
import eu.pb4.predicate.api.PredicateContext;
import eu.pb4.predicate.api.PredicateRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record DeathDisplayLogic(Optional<RegistryEntryList<DamageType>> type,
                                MinecraftPredicate victimPredicate,
                                MinecraftPredicate attackerPredicate,
                                MinecraftPredicate sourcePredicate,
                                float chance,
                                WrappedText text,
                                FloatingText.DisplaySettings displaySettings) {
    static final ParserContext.Key<Function<String, Text>> PLACEHOLDER_KEY = DamageDisplayLogic.PLACEHOLDER_KEY;

    static final NodeParser PARSER = DamageDisplayLogic.PARSER;


    public static final Codec<DeathDisplayLogic> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.entryList(RegistryKeys.DAMAGE_TYPE).optionalFieldOf("type").forGetter(DeathDisplayLogic::type),
            PredicateRegistry.CODEC.optionalFieldOf("victim", BuiltinPredicates.alwaysTrue()).forGetter(DeathDisplayLogic::victimPredicate),
            PredicateRegistry.CODEC.optionalFieldOf("attacker", BuiltinPredicates.alwaysTrue()).forGetter(DeathDisplayLogic::attackerPredicate),
            PredicateRegistry.CODEC.optionalFieldOf("source", BuiltinPredicates.alwaysTrue()).forGetter(DeathDisplayLogic::sourcePredicate),
            Codec.floatRange(0, 1).optionalFieldOf("chance", 1f).forGetter(DeathDisplayLogic::chance),
            PARSER.codec().fieldOf("text").forGetter(DeathDisplayLogic::text),
            FloatingText.DisplaySettings.CODEC.orElse(FloatingText.DisplaySettings.GENERAL).forGetter(DeathDisplayLogic::displaySettings)
    ).apply(instance, DeathDisplayLogic::new));

    public static DeathDisplayLogic of(String format) {
        return new DeathDisplayLogic(Optional.empty(),
                BuiltinPredicates.alwaysTrue(),
                BuiltinPredicates.alwaysTrue(),
                BuiltinPredicates.alwaysTrue(),
                1,
                WrappedText.from(PARSER, format),
                FloatingText.DisplaySettings.DEATH
        );
    }

    public static DeathDisplayLogic of(RegistryWrapper.WrapperLookup wrapper, RegistryKey<DamageType> type, String format) {
        return new DeathDisplayLogic(Optional.of(RegistryEntryList.of(wrapper.getWrapperOrThrow(RegistryKeys.DAMAGE_TYPE).getOrThrow(type))),
                BuiltinPredicates.alwaysTrue(),
                BuiltinPredicates.alwaysTrue(),
                BuiltinPredicates.alwaysTrue(),
                1,
                WrappedText.from(PARSER, format),
                FloatingText.DisplaySettings.DEATH
        );
    }
    public static DeathDisplayLogic of(RegistryWrapper.WrapperLookup wrapper, List<RegistryKey<DamageType>> type, String format) {
        return new DeathDisplayLogic(Optional.of(RegistryEntryList.of(type.stream().map(wrapper.getWrapperOrThrow(RegistryKeys.DAMAGE_TYPE)::getOrThrow).toList())),
                BuiltinPredicates.alwaysTrue(),
                BuiltinPredicates.alwaysTrue(),
                BuiltinPredicates.alwaysTrue(),
                1,
                WrappedText.from(PARSER, format),
                FloatingText.DisplaySettings.DEATH
        );
    }

    public static DeathDisplayLogic of(RegistryWrapper.WrapperLookup wrapper, TagKey<DamageType> tag, String format) {
        return new DeathDisplayLogic(Optional.of(wrapper.getWrapperOrThrow(RegistryKeys.DAMAGE_TYPE).getOrThrow(tag)),
                BuiltinPredicates.alwaysTrue(),
                BuiltinPredicates.alwaysTrue(),
                BuiltinPredicates.alwaysTrue(),
                1,
                WrappedText.from(PARSER, format),
                FloatingText.DisplaySettings.DEATH
        );
    }

    public void provide(LivingEntity entity, DamageSource source, BiConsumer<Text, FloatingText.DisplaySettings> consumer) {
        consumer.accept(this.text.textNode().toText(PlaceholderContext.of(entity).asParserContext().with(PLACEHOLDER_KEY, key -> switch (key) {
            case "message" -> source.getDeathMessage(entity);
            case "victim" -> entity.getDisplayName();
            case "attacker" -> source.getAttacker() != null ? source.getAttacker().getDisplayName() : Text.empty();
            case null, default -> Text.empty();
        })), this.displaySettings);
    }

    public boolean match(LivingEntity entity, DamageSource source, PredicateContext predicateContext, PredicateContext attackerContext, PredicateContext sourceContext) {
        return (this.type.isEmpty() || this.type.get().contains(source.getTypeRegistryEntry()))
                && this.victimPredicate.test(predicateContext).success()
                && this.attackerPredicate.test(attackerContext).success()
                && this.sourcePredicate.test(sourceContext).success()
                && entity.getRandom().nextFloat() <= this.chance;
    }
}
