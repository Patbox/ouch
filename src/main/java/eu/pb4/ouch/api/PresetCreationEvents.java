package eu.pb4.ouch.api;

import eu.pb4.predicate.api.BuiltinPredicates;
import eu.pb4.predicate.api.MinecraftPredicate;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import org.jetbrains.annotations.ApiStatus;

public final class PresetCreationEvents {

	/**
	 * This event allows to append a format applied on one or more {@link DamageType} for the default configuration.
	 * Example:
	 * <pre>
	 * {@code
	 *  PresetCreationEvents.APPEND.register((builder, preset) -> {
	 *      logics.addDamage(firstDamageFormat, firstDamageTypes...);
	 *      logics.addDamage(secondDamageFormat, secondDamageTypes...);
	 *      logics.addDeath(thirdDamageFormat, thirdDamageTypes...);
	 *      return logics;
	 *  });
	 * }
	 * </pre>
	 */
	public static final Event<AppendDisplayLogic> APPEND = EventFactory.createArrayBacked(AppendDisplayLogic.class, callbacks -> (builder, preset) -> {
		for (AppendDisplayLogic callback : callbacks) {
			callback.append(builder, preset);
		}
	});

	@FunctionalInterface
	public interface AppendDisplayLogic {

		void append(Builder builder, String preset);

		@ApiStatus.NonExtendable
		@SuppressWarnings("unchecked")
		interface Builder {
			default void addDamage(String format, ResourceKey<DamageType>... types) {
				addDamage(format, 1, types);
			};
			default void addDamage(int layer, String format, ResourceKey<DamageType>... types) {
				addDamage(layer, format, 1, types);
			}
			default void addDamage(String format, TagKey<DamageType> tag) {
				addDamage(format, 1, tag);
			};;
			default void addDamage(int layer, String format, TagKey<DamageType> tag) {
				addDamage(layer, format, 1, tag);
			}

			default void addDamage(String format, float chance, ResourceKey<DamageType>... types) {
				addDamage(0, format, chance, types);
			}
			default void addDamage(String format, float chance, TagKey<DamageType> tag) {
				addDamage(0, format, chance, tag);
			}
			default void addDamage(int layer, String format, float chance, ResourceKey<DamageType>... types) {
				addDamage(0, format, chance, BuiltinPredicates.alwaysTrue(), types);
			};
			default void addDamage(int layer, String format, float chance, TagKey<DamageType> tag) {
				addDamage(0, format, chance, BuiltinPredicates.alwaysTrue(), tag);
			}

			default void addDamage(int layer, String format, float chance, MinecraftPredicate victimPredicate, ResourceKey<DamageType>... types) {
				addDamage(layer, 100, format, FloatRange.ALL, chance, victimPredicate, BuiltinPredicates.alwaysTrue(), BuiltinPredicates.alwaysTrue(), types);
			};
			default void addDamage(int layer, String format, float chance, MinecraftPredicate victimPredicate, TagKey<DamageType> tag) {
				addDamage(layer, 200, format, FloatRange.ALL, chance,  victimPredicate, BuiltinPredicates.alwaysTrue(), BuiltinPredicates.alwaysTrue(), tag);
			};

			void addDamage(int layer, int priority, String format, FloatRange range, float chance, MinecraftPredicate victimPredicate, MinecraftPredicate sourcePredicate, MinecraftPredicate attackerPredicate, ResourceKey<DamageType>... types);
			void addDamage(int layer, int priority, String format, FloatRange range, float chance, MinecraftPredicate victimPredicate, MinecraftPredicate sourcePredicate, MinecraftPredicate attackerPredicate, TagKey<DamageType> tag);

			default void addDeath(String format, ResourceKey<DamageType>... types) {
				addDeath(format, 1, types);
			};
			default void addDeath(int layer, String format, ResourceKey<DamageType>... types) {
				addDeath(layer, format, 1, types);
			}
			default void addDeath(String format, TagKey<DamageType> tag) {
				addDeath(format, 1, tag);
			};;
			default void addDeath(int layer, String format, TagKey<DamageType> tag) {
				addDeath(layer, format, 1, tag);
			}

			default void addDeath(String format, float chance, ResourceKey<DamageType>... types) {
				addDeath(0, format, chance, types);
			}
			default void addDeath(String format, float chance, TagKey<DamageType> tag) {
				addDeath(0, format, chance, tag);
			}
			default void addDeath(int layer, String format, float chance, ResourceKey<DamageType>... types) {
				addDeath(0, format, chance, BuiltinPredicates.alwaysTrue(), types);
			};
			default void addDeath(int layer, String format, float chance, TagKey<DamageType> tag) {
				addDeath(0, format, chance, BuiltinPredicates.alwaysTrue(), tag);
			}

			default void addDeath(int layer, String format, float chance, MinecraftPredicate victimPredicate, ResourceKey<DamageType>... types) {
				addDeath(layer, 100, format, FloatRange.ALL, chance, victimPredicate, BuiltinPredicates.alwaysTrue(), BuiltinPredicates.alwaysTrue(), types);
			};
			default void addDeath(int layer, String format, float chance, MinecraftPredicate victimPredicate, TagKey<DamageType> tag) {
				addDeath(layer, 200, format, FloatRange.ALL, chance, victimPredicate, BuiltinPredicates.alwaysTrue(), BuiltinPredicates.alwaysTrue(), tag);
			};

			void addDeath(int layer, int priority, String format, FloatRange range, float chance, MinecraftPredicate victimPredicate, MinecraftPredicate sourcePredicate, MinecraftPredicate attackerPredicate, ResourceKey<DamageType>... types);
			void addDeath(int layer, int priority, String format, FloatRange range, float chance, MinecraftPredicate victimPredicate, MinecraftPredicate sourcePredicate, MinecraftPredicate attackerPredicate, TagKey<DamageType> tag);

			void addHealing(int layer, int priority, String format, FloatRange range, float chance, MinecraftPredicate entityPredicate);
		}
	}
}
