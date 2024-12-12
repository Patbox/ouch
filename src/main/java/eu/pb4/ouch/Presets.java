package eu.pb4.ouch;

import com.mojang.datafixers.util.Pair;
import eu.pb4.ouch.api.*;
import eu.pb4.predicate.api.BuiltinPredicates;
import eu.pb4.predicate.api.MinecraftPredicate;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.TagKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
@SuppressWarnings("unchecked")
public interface Presets {
    String DEFAULT = "default";
    String MINIMAL = "minimal";

    static void setupPresets(BiConsumer<String, Preset> consumer, RegistryWrapper.WrapperLookup lookup) {
        consumer.accept(DEFAULT, createDefault(new BuilderImpl(lookup)).build());
        consumer.accept(MINIMAL, createMinimal(new BuilderImpl(lookup)).build());
    }

    static BuilderImpl createDefault(BuilderImpl builder) {
        builder.addDamage("<#ff0000>-${value}</><yellow>☀", DamageTypes.DRY_OUT);
        builder.addDamage("<#ff0000>-${value}</><aqua>\uD83D\uDD31", DamageTypes.TRIDENT);
        builder.addDamage("<#ff0000>-${value}</><gray>☃", DamageTypes.BAD_RESPAWN_POINT);
        builder.addDamage("<#ff0000>-${value}</><dark_gray>\uD83C\uDF56", DamageTypes.STARVE);
        builder.addDamage("<#ff0000>-${value}</><aqua>\uD83D\uDD31", DamageTypes.TRIDENT);
        builder.addDamage("<#ff0000>-${value}</><red>♦", DamageTypes.CRAMMING);
        builder.addDamage("<#ff0000>-${value}</><gray>☄", DamageTypes.FALL);
        builder.addDamage("<#ff0000>-${value}</><yellow>☄", DamageTypes.FLY_INTO_WALL);
        builder.addDamage("<#ff0000>-${value}</><gray>▒", DamageTypes.IN_WALL);
        builder.addDamage("<#ff0000>-${value}</><dark_aqua>☀", DamageTypes.SONIC_BOOM);
        builder.addDamage("<#ff0000>-${value}</><white>☄", DamageTypes.SPIT);
        builder.addDamage("<#ff0000>-${value}</><yellow>▽", DamageTypes.STING);
        builder.addDamage("<#ff0000>-${value}</><yellow>△", DamageTypes.THORNS);
        builder.addDamage("<#ff0000>-${value}</><blue>\uD83D\uDD28", DamageTypes.MACE_SMASH);

        builder.addDamage("<#ff0000>-${value}</><light_gray>\uD83D\uDDE1", DamageTypes.MOB_ATTACK, DamageTypes.PLAYER_ATTACK, DamageTypes.MOB_ATTACK_NO_AGGRO);
        builder.addDamage("<#ff0000>-${value}</><yellow>⚠", DamageTypes.OUT_OF_WORLD, DamageTypes.OUTSIDE_BORDER);
        builder.addDamage("<#ff0000>-${value}</><purple>\uD83E\uDDEA", DamageTypes.MAGIC, DamageTypes.INDIRECT_MAGIC, DamageTypes.DRAGON_BREATH);
        builder.addDamage("<#ff0000>-${value}</><dark_gray>\uD83E\uDDEA", DamageTypes.WITHER, DamageTypes.WITHER_SKULL);
        builder.addDamage("<#ff0000>-${value}</><dark_green>♦", DamageTypes.CACTUS, DamageTypes.SWEET_BERRY_BUSH);
        builder.addDamage("<#ff0000>-${value}</><red>☄", DamageTypes.FALLING_STALACTITE, DamageTypes.FALLING_ANVIL, DamageTypes.FALLING_BLOCK);

        builder.addDamage("<#ff0000>-${value}</><orange>\uD83D\uDD25", DamageTypeTags.IS_FIRE);
        builder.addDamage("<#ff0000>-${value}</><white>☀", DamageTypeTags.IS_EXPLOSION);
        builder.addDamage("<#ff0000>-${value}</><yellow>\uD83C\uDFF9", DamageTypeTags.IS_PROJECTILE);
        builder.addDamage("<#ff0000>-${value}</><blue>\uD83C\uDF0A", DamageTypeTags.IS_DROWNING);
        builder.addDamage("<#ff0000>-${value}</><white>❄", DamageTypeTags.IS_FREEZING);
        builder.addDamage("<#ff0000>-${value}</><yellow>⚡", DamageTypeTags.IS_LIGHTNING);
        builder.addDamage(0, 1000, "<#ff0000>-${value}</>", FloatRange.ALL, 1, BuiltinPredicates.alwaysTrue(), BuiltinPredicates.alwaysTrue(), BuiltinPredicates.alwaysTrue());


        builder.addDamage(0, -10, "<#ff0000>+${value} </><gray>¯\\\\_(ツ)_/¯", FloatRange.below(-0.0001f), 1, BuiltinPredicates.alwaysTrue(), BuiltinPredicates.alwaysTrue(), BuiltinPredicates.alwaysTrue());
        builder.addHealing(0, -10, "<#00FF00>${value} </><gray>¯\\\\_(ツ)_/¯", FloatRange.below(-0.0001f), 1, BuiltinPredicates.alwaysTrue());
        builder.addHealing(0, 1000, "<#00FF00>+${value}", FloatRange.ALL, 1, BuiltinPredicates.alwaysTrue());

        PresetCreationEvents.APPEND.invoker().append(builder, DEFAULT);
        return builder;
    }

    static BuilderImpl createMinimal(BuilderImpl builder) {
        builder.addDamage(0, 1000, "<#ff0000>-${value}</>", FloatRange.ALL, 1,  BuiltinPredicates.alwaysTrue(), BuiltinPredicates.alwaysTrue(), BuiltinPredicates.alwaysTrue());
        builder.addHealing(0, 1000, "<#00FF00>+${value}", FloatRange.ALL, 1, BuiltinPredicates.alwaysTrue());
        //PresetCreationEvents.APPEND_DISPLAY_LOGIC.invoker().append(builder, MINIMAL);
        return builder;
    }

    class BuilderImpl implements PresetCreationEvents.AppendDisplayLogic.Builder {
        RegistryWrapper.WrapperLookup lookup;
        Int2ObjectMap<List<Pair<Integer, DamageDisplayLogic>>> damageDisplays = new Int2ObjectOpenHashMap<>();
        Int2ObjectMap<List<Pair<Integer, HealDisplayLogic>>> healingDisplays = new Int2ObjectOpenHashMap<>();
        Int2ObjectMap<List<Pair<Integer, DamageDisplayLogic>>> deathDisplays = new Int2ObjectOpenHashMap<>();

        public BuilderImpl(RegistryWrapper.WrapperLookup lookup) {
            this.lookup = lookup;
        }

        public Preset build() {
            var death = new ArrayList<List<DamageDisplayLogic>>();
            var damage = new ArrayList<List<DamageDisplayLogic>>();
            var healing = new ArrayList<List<HealDisplayLogic>>();

            sortAndAdd(damage, this.damageDisplays, DamageDisplayLogic::chance, DamageDisplayLogic::type, DamageDisplayLogic::range);
            sortAndAdd(death, this.deathDisplays, DamageDisplayLogic::chance, DamageDisplayLogic::type, DamageDisplayLogic::range);
            sortAndAdd(healing, this.healingDisplays, HealDisplayLogic::chance, x -> Optional.empty(), HealDisplayLogic::range);

            return new Preset(damage, healing, death);
        }

        private static <T> void sortAndAdd(List<List<T>> out, Int2ObjectMap<List<Pair<Integer, T>>> entries, Function<T, Float> chance,
                                    Function<T, Optional<RegistryEntryList<DamageType>>> damageTypes, Function<T, FloatRange> range) {
            var entriesList = new ArrayList<>(entries.int2ObjectEntrySet());
            var comparator = Comparator.<Pair<Integer, T>>comparingInt(Pair::getFirst)
                    .thenComparingInt(x -> damageTypes.apply(x.getSecond()).map(registryEntries -> registryEntries.getTagKey().isPresent() ? 3 : 1).orElse(5))
                    .thenComparingDouble(x -> chance.apply(x.getSecond()))
                    .thenComparingDouble(x -> range.apply(x.getSecond()).size());

            entriesList.sort(Comparator.comparing(Int2ObjectMap.Entry::getIntKey));
            for (var x : entriesList) {
                x.getValue().sort(comparator);
                out.add(x.getValue().stream().map(Pair::getSecond).toList());
            }
        }

        @Override
        public void addDamage(int layer, int priority, String format, FloatRange range, float chance, MinecraftPredicate victimPredicate, MinecraftPredicate sourcePredicate, MinecraftPredicate attackerPredicate, RegistryKey<DamageType>... types) {
            this.damageDisplays.computeIfAbsent(layer, x -> new ArrayList<>())
                    .add(new Pair<>(priority, DamageDisplayLogic.of(lookup, format, range, chance, victimPredicate, sourcePredicate, attackerPredicate, types)));
        }

        @Override
        public void addDamage(int layer, int priority, String format, FloatRange range, float chance, MinecraftPredicate victimPredicate, MinecraftPredicate sourcePredicate, MinecraftPredicate attackerPredicate, TagKey<DamageType> tag) {
            this.damageDisplays.computeIfAbsent(layer, x -> new ArrayList<>())
                    .add(new Pair<>(priority, DamageDisplayLogic.of(lookup, format, range, chance, victimPredicate, sourcePredicate, attackerPredicate, tag)));
        }

        @Override
        public void addDeath(int layer, int priority, String format, FloatRange range, float chance, MinecraftPredicate victimPredicate, MinecraftPredicate sourcePredicate, MinecraftPredicate attackerPredicate, RegistryKey<DamageType>... types) {
            this.deathDisplays.computeIfAbsent(layer, x -> new ArrayList<>())
                    .add(new Pair<>(priority, DamageDisplayLogic.of(lookup, format, range, chance, victimPredicate, sourcePredicate, attackerPredicate, types)));
        }

        @Override
        public void addDeath(int layer, int priority, String format, FloatRange range,  float chance, MinecraftPredicate victimPredicate, MinecraftPredicate sourcePredicate, MinecraftPredicate attackerPredicate, TagKey<DamageType> tag) {
            this.deathDisplays.computeIfAbsent(layer, x -> new ArrayList<>())
                    .add(new Pair<>(priority, DamageDisplayLogic.of(lookup, format, range, chance, victimPredicate, sourcePredicate, attackerPredicate, tag)));
        }

        @Override
        public void addHealing(int layer, int priority, String format, FloatRange range, float chance, MinecraftPredicate entityPredicate) {
            this.healingDisplays.computeIfAbsent(layer, x -> new ArrayList<>())
                    .add(new Pair<>(priority, HealDisplayLogic.of(format, range, chance, entityPredicate)));
        }
    }
}
