package eu.pb4.ouch;

import com.mojang.datafixers.util.Pair;
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
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record HealDisplayLogic(MinecraftPredicate entityPredicate,
                               float chance,
                               WrappedText text, FloatingText.DisplaySettings displaySettings) {
    private static final ParserContext.Key<Function<String, Text>> PLACEHOLDER_KEY = DamageDisplayLogic.PLACEHOLDER_KEY;
    private static final NodeParser PARSER = DamageDisplayLogic.PARSER;


    public static final Codec<HealDisplayLogic> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PredicateRegistry.CODEC.optionalFieldOf("entity", BuiltinPredicates.alwaysTrue()).forGetter(HealDisplayLogic::entityPredicate),
            Codec.floatRange(0, 1).optionalFieldOf("chance", 1f).forGetter(HealDisplayLogic::chance),
            PARSER.codec().fieldOf("text").forGetter(HealDisplayLogic::text),
            FloatingText.DisplaySettings.CODEC.orElse(FloatingText.DisplaySettings.GENERAL).forGetter(HealDisplayLogic::displaySettings)
    ).apply(instance, HealDisplayLogic::new));

    public static HealDisplayLogic of(String format) {
        return new HealDisplayLogic(BuiltinPredicates.alwaysTrue(), 1, WrappedText.from(PARSER, format), FloatingText.DisplaySettings.GENERAL);
    }

    public void provide(LivingEntity entity, float amount, BiConsumer<Text, FloatingText.DisplaySettings> consumer) {
        consumer.accept(this.text.textNode().toText(PlaceholderContext.of(entity).asParserContext().with(PLACEHOLDER_KEY, key -> switch (key) {
            case "value" -> Text.literal(MathHelper.floor(amount) + "." + (MathHelper.floor(amount * 10) % 10));
            case "value_rounded" -> Text.literal("" + Math.round(amount));
            case "value_raw" -> Text.literal("" + amount);
            case null, default -> Text.empty();
        })), this.displaySettings);
    }

    public boolean match(LivingEntity entity, PredicateContext predicateContext) {
        return this.entityPredicate.test(predicateContext).success() && entity.getRandom().nextFloat() <= this.chance;
    }
}
