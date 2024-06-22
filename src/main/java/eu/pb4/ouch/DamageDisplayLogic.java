package eu.pb4.ouch;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.TagLikeParser;
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
import net.minecraft.registry.entry.RegistryEntryListCodec;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record DamageDisplayLogic(Optional<RegistryEntryList<DamageType>> type,
                                 MinecraftPredicate victimPredicate,
                                 MinecraftPredicate attackerPredicate,
                                 MinecraftPredicate sourcePredicate,
                                 float chance,
                                 WrappedText text,
                                 FloatingText.DisplaySettings displaySettings) {
    static final ParserContext.Key<Function<String, Text>> PLACEHOLDER_KEY = ParserContext.Key.of("ouch:placeholder");

    static final NodeParser PARSER = NodeParser.builder()
            .quickText()
            .placeholders(TagLikeParser.PLACEHOLDER_USER, PLACEHOLDER_KEY)
            .staticPreParsing()
            .build();

    public static final Codec<DamageDisplayLogic> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.entryList(RegistryKeys.DAMAGE_TYPE).optionalFieldOf("type").forGetter(DamageDisplayLogic::type),
            PredicateRegistry.CODEC.optionalFieldOf("victim", BuiltinPredicates.alwaysTrue()).forGetter(DamageDisplayLogic::victimPredicate),
            PredicateRegistry.CODEC.optionalFieldOf("attacker", BuiltinPredicates.alwaysTrue()).forGetter(DamageDisplayLogic::attackerPredicate),
            PredicateRegistry.CODEC.optionalFieldOf("source", BuiltinPredicates.alwaysTrue()).forGetter(DamageDisplayLogic::sourcePredicate),
            Codec.floatRange(0, 1).optionalFieldOf("chance", 1f).forGetter(DamageDisplayLogic::chance),
            PARSER.codec().fieldOf("text").forGetter(DamageDisplayLogic::text),
            FloatingText.DisplaySettings.CODEC.orElse(FloatingText.DisplaySettings.GENERAL).forGetter(DamageDisplayLogic::displaySettings)
    ).apply(instance, DamageDisplayLogic::new));

    public static DamageDisplayLogic of(String format) {
        return new DamageDisplayLogic(Optional.empty(),
                BuiltinPredicates.alwaysTrue(),
                BuiltinPredicates.alwaysTrue(),
                BuiltinPredicates.alwaysTrue(),
                1,
                WrappedText.from(PARSER, format),
                FloatingText.DisplaySettings.GENERAL
        );
    }

    public static DamageDisplayLogic of(float chance, MinecraftPredicate predicate, String format) {
        return new DamageDisplayLogic(Optional.empty(),
                predicate,
                BuiltinPredicates.alwaysTrue(),
                BuiltinPredicates.alwaysTrue(),
                chance,
                WrappedText.from(PARSER, format),
                FloatingText.DisplaySettings.GENERAL
        );
    }

    public static DamageDisplayLogic of(RegistryWrapper.WrapperLookup wrapper, RegistryKey<DamageType> type, String format) {
        return new DamageDisplayLogic(Optional.of(RegistryEntryList.of(wrapper.getWrapperOrThrow(RegistryKeys.DAMAGE_TYPE).getOrThrow(type))),
                BuiltinPredicates.alwaysTrue(),
                BuiltinPredicates.alwaysTrue(),
                BuiltinPredicates.alwaysTrue(),
                1,
                WrappedText.from(PARSER, format),
                FloatingText.DisplaySettings.GENERAL
        );
    }
    public static DamageDisplayLogic of(RegistryWrapper.WrapperLookup wrapper, List<RegistryKey<DamageType>> type, String format) {
        return new DamageDisplayLogic(Optional.of(RegistryEntryList.of(type.stream().map(wrapper.getWrapperOrThrow(RegistryKeys.DAMAGE_TYPE)::getOrThrow).toList())),
                BuiltinPredicates.alwaysTrue(),
                BuiltinPredicates.alwaysTrue(),
                BuiltinPredicates.alwaysTrue(),
                1,
                WrappedText.from(PARSER, format),
                FloatingText.DisplaySettings.GENERAL
        );
    }

    public static DamageDisplayLogic of(RegistryWrapper.WrapperLookup wrapper, TagKey<DamageType> tag, String format) {
        return new DamageDisplayLogic(Optional.of(wrapper.getWrapperOrThrow(RegistryKeys.DAMAGE_TYPE).getOrThrow(tag)),
                BuiltinPredicates.alwaysTrue(),
                BuiltinPredicates.alwaysTrue(),
                BuiltinPredicates.alwaysTrue(),
                1,
                WrappedText.from(PARSER, format),
                FloatingText.DisplaySettings.GENERAL
        );
    }

    public void provide(LivingEntity entity, DamageSource source, float amount, BiConsumer<Text, FloatingText.DisplaySettings> consumer) {
        consumer.accept(this.text.textNode().toText(PlaceholderContext.of(entity).asParserContext().with(PLACEHOLDER_KEY, key -> switch (key) {
            case "value" -> Text.literal(MathHelper.floor(amount) + "." + (MathHelper.floor(amount * 10) % 10));
            case "value_rounded" -> Text.literal("" + Math.round(amount));
            case "value_raw" -> Text.literal("" + amount);
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
