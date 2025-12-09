package eu.pb4.ouch;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.ouch.api.FloatRange;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.WrappedText;
import eu.pb4.predicate.api.BuiltinPredicates;
import eu.pb4.predicate.api.MinecraftPredicate;
import eu.pb4.predicate.api.PredicateContext;
import eu.pb4.predicate.api.PredicateRegistry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public record HealDisplayLogic(MinecraftPredicate entityPredicate,
                               FloatRange range,
                               float chance,
                               WrappedText text, FloatingText.DisplaySettings displaySettings) {
    private static final ParserContext.Key<Function<String, Component>> PLACEHOLDER_KEY = DamageDisplayLogic.PLACEHOLDER_KEY;
    private static final NodeParser PARSER = DamageDisplayLogic.PARSER;


    public static final Codec<HealDisplayLogic> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PredicateRegistry.CODEC.optionalFieldOf("entity", BuiltinPredicates.alwaysTrue()).forGetter(HealDisplayLogic::entityPredicate),
            FloatRange.CODEC.orElse(FloatRange.ALL).forGetter(HealDisplayLogic::range),
            Codec.floatRange(0, 1).optionalFieldOf("chance", 1f).forGetter(HealDisplayLogic::chance),
            PARSER.codec().fieldOf("text").forGetter(HealDisplayLogic::text),
            FloatingText.DisplaySettings.CODEC.orElse(FloatingText.DisplaySettings.GENERAL).forGetter(HealDisplayLogic::displaySettings)
    ).apply(instance, HealDisplayLogic::new));

    public static HealDisplayLogic of(String format, FloatRange range, float chance, MinecraftPredicate predicate) {
        return new HealDisplayLogic(predicate, range, chance, WrappedText.from(PARSER, format), FloatingText.DisplaySettings.GENERAL);
    }

    public void provide(LivingEntity entity, float amount, BiConsumer<Component, FloatingText.DisplaySettings> consumer) {
        consumer.accept(this.text.textNode().toText(PlaceholderContext.of(entity).asParserContext().with(PLACEHOLDER_KEY, key -> switch (key) {
            case "value" -> Component.literal(Mth.floor(amount) + "." + (Mth.floor(amount * 10) % 10));
            case "value_rounded" -> Component.literal("" + Math.round(amount));
            case "value_raw" -> Component.literal("" + amount);
            case null, default -> Component.empty();
        })), this.displaySettings);
    }

    public boolean match(LivingEntity entity, float amount, PredicateContext predicateContext) {
        return this.entityPredicate.test(predicateContext).success() && this.range.test(amount) && entity.getRandom().nextFloat() <= this.chance;
    }
}
