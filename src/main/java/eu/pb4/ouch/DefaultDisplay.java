package eu.pb4.ouch;

import eu.pb4.ouch.api.DefaultDisplayEvents;
import eu.pb4.predicate.api.BuiltinPredicates;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;

public interface DefaultDisplay {
    static void createDefault(List<DamageDisplayLogic> damage, List<HealDisplayLogic> heal, List<DeathDisplayLogic> death,
                              ArrayList<DamageDisplayLogic> damageExtra, DynamicRegistryManager.Immutable lookup) {
        damage.add(DamageDisplayLogic.of(lookup, DamageTypes.DRY_OUT, "<#ff0000>-${value}</><yellow>☀"));
        damage.add(DamageDisplayLogic.of(lookup, DamageTypes.TRIDENT, "<#ff0000>-${value}</><aqua>\uD83D\uDD31"));
        damage.add(DamageDisplayLogic.of(lookup, DamageTypes.BAD_RESPAWN_POINT, "<#ff0000>-${value}</><gray>☃"));
        damage.add(DamageDisplayLogic.of(lookup, DamageTypes.STARVE, "<#ff0000>-${value}</><dark_gray>\uD83C\uDF56"));
        damage.add(DamageDisplayLogic.of(lookup, DamageTypes.TRIDENT, "<#ff0000>-${value}</><aqua>\uD83D\uDD31"));
        damage.add(DamageDisplayLogic.of(lookup, DamageTypes.CRAMMING, "<#ff0000>-${value}</><red>♦"));
        damage.add(DamageDisplayLogic.of(lookup, DamageTypes.FALL, "<#ff0000>-${value}</><gray>☄"));
        damage.add(DamageDisplayLogic.of(lookup, DamageTypes.FLY_INTO_WALL, "<#ff0000>-${value}</><yellow>☄"));
        damage.add(DamageDisplayLogic.of(lookup, DamageTypes.IN_WALL, "<#ff0000>-${value}</><gray>▒"));
        damage.add(DamageDisplayLogic.of(lookup, DamageTypes.SONIC_BOOM, "<#ff0000>-${value}</><dark_aqua>☀"));
        damage.add(DamageDisplayLogic.of(lookup, DamageTypes.SPIT, "<#ff0000>-${value}</><white>☄"));
        damage.add(DamageDisplayLogic.of(lookup, DamageTypes.STING, "<#ff0000>-${value}</><yellow>▽"));
        damage.add(DamageDisplayLogic.of(lookup, DamageTypes.THORNS, "<#ff0000>-${value}</><yellow>△"));

        damage.add(DamageDisplayLogic.of(lookup, List.of(DamageTypes.MOB_ATTACK, DamageTypes.PLAYER_ATTACK, DamageTypes.MOB_ATTACK_NO_AGGRO), "<#ff0000>-${value}</><light_gray>\uD83D\uDDE1"));
        damage.add(DamageDisplayLogic.of(lookup, List.of(DamageTypes.OUT_OF_WORLD, DamageTypes.OUTSIDE_BORDER), "<#ff0000>-${value}</><yellow>⚠"));
        damage.add(DamageDisplayLogic.of(lookup, List.of(DamageTypes.MAGIC, DamageTypes.INDIRECT_MAGIC, DamageTypes.DRAGON_BREATH), "<#ff0000>-${value}</><purple>\uD83E\uDDEA"));
        damage.add(DamageDisplayLogic.of(lookup, List.of(DamageTypes.WITHER, DamageTypes.WITHER_SKULL), "<#ff0000>-${value}</><dark_gray>\uD83E\uDDEA"));
        damage.add(DamageDisplayLogic.of(lookup, List.of(DamageTypes.CACTUS, DamageTypes.SWEET_BERRY_BUSH), "<#ff0000>-${value}</><dark_green>♦"));
        damage.add(DamageDisplayLogic.of(lookup, List.of(DamageTypes.FALLING_STALACTITE, DamageTypes.FALLING_ANVIL, DamageTypes.FALLING_BLOCK),
                "<#ff0000>-${value}</><red>☄"));

        damage.add(DamageDisplayLogic.of(lookup, DamageTypeTags.IS_FIRE, "<#ff0000>-${value}</><orange>\uD83D\uDD25"));
        damage.add(DamageDisplayLogic.of(lookup, DamageTypeTags.IS_EXPLOSION, "<#ff0000>-${value}</><white>☀"));
        damage.add(DamageDisplayLogic.of(lookup, DamageTypeTags.IS_PROJECTILE, "<#ff0000>-${value}</><yellow>\uD83C\uDFF9"));
        damage.add(DamageDisplayLogic.of(lookup, DamageTypeTags.IS_DROWNING, "<#ff0000>-${value}</><blue>\uD83C\uDF0A"));
        damage.add(DamageDisplayLogic.of(lookup, DamageTypeTags.IS_FREEZING, "<#ff0000>-${value}</><white>❄"));
        damage.add(DamageDisplayLogic.of(lookup, DamageTypeTags.IS_LIGHTNING, "<#ff0000>-${value}</><yellow>⚡"));
        damage.add(DamageDisplayLogic.of("<#ff0000>-${value}</>"));

        heal.add(HealDisplayLogic.of("<#00FF00>+${value}"));

        LogicsImpl logics = new LogicsImpl();
        DefaultDisplayEvents.APPEND_DISPLAY_LOGIC.invoker().append(lookup, logics);
        logics.getLogics().forEach(pair -> damage.add(DamageDisplayLogic.of(lookup, pair.getLeft(), pair.getRight())));
    }

    static void createShowcase(List<DamageDisplayLogic> damage, List<HealDisplayLogic> heal, List<DeathDisplayLogic> death,
                               ArrayList<DamageDisplayLogic> damageExtra, DynamicRegistryManager.Immutable lookup) {
        createDefault(damage, heal, death, damageExtra, lookup);

        death.add(DeathDisplayLogic.of("<red>${message}"));
        damageExtra.add(DamageDisplayLogic.of(0.03f, BuiltinPredicates.alwaysTrue(), "<white>Ouch!"));
    }

    class LogicsImpl implements DefaultDisplayEvents.AppendDisplayLogic.Logics {

        final List<Pair<List<RegistryKey<DamageType>>, String>> logics = new ArrayList<>();

        @Override
        @SafeVarargs
        public final void add(String format, RegistryKey<DamageType>... types) {
            this.logics.add(new Pair<>(List.of(types), format));
        }

        List<Pair<List<RegistryKey<DamageType>>, String>> getLogics() {
            return this.logics;
        }
    }
}
