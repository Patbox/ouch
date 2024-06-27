package eu.pb4.ouch.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.FloatPredicate;

public record FloatRange(float min, float max) implements FloatPredicate {
    public static final MapCodec<FloatRange> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("min_value", Float.NEGATIVE_INFINITY).forGetter(FloatRange::min),
            Codec.FLOAT.optionalFieldOf("max_value", Float.POSITIVE_INFINITY).forGetter(FloatRange::max)
    ).apply(instance, FloatRange::new));
    public static final FloatRange ALL = new FloatRange(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);

    public static FloatRange below(float i) {
        return new FloatRange(Float.NEGATIVE_INFINITY, i);
    }

    public static FloatRange above(float i) {
        return new FloatRange(i, Float.POSITIVE_INFINITY);
    }

    @Override
    public boolean test(float t) {
        return t >= min && t <= max;
    }

    public double size() {
        return max - min;
    }
}
