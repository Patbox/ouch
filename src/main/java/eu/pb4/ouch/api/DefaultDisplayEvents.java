package eu.pb4.ouch.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public final class DefaultDisplayEvents {

	/**
	 * This event allows to append a format applied on one or more {@link DamageType} for the default configuration.
	 * Example:
	 * <pre>
	 * {@code
	 *  DefaultDisplayEvents.APPEND_DISPLAY_LOGIC.register((lookup, logics) -> {
	 *      logics.add(firstDamageFormat, firstDamageTypes...);
	 *      logics.add(secondDamageFormat, secondDamageTypes...);
	 *      logics.add(thirdDamageFormat, thirdDamageTypes...);
	 *      return logics;
	 *  });
	 * }
	 * </pre>
	 */
	public static final Event<AppendDisplayLogic> APPEND_DISPLAY_LOGIC = EventFactory.createArrayBacked(AppendDisplayLogic.class, callbacks -> (lookup, _logics) -> {
		AppendDisplayLogic.Logics logics = _logics;
		for (AppendDisplayLogic callback : callbacks) {
			logics = callback.append(lookup, logics);
		}
		return logics;
	});

	/**
	 * This event allows to modify a format applied on one or more {@link DamageType} for the default configuration.
	 * Example:
	 * <pre>
	 * {@code
	 *  DefaultDisplayEvents.MODIFY_DISPLAY_LOGIC.register((lookup, types, format) -> {
	 *      if (types.contains(DamageTypes.OUT_OF_WORLD)) {
	 *          return yourModifiedFormat;
	 *      }
	 *      return format;
	 *  });
	 * }
	 * </pre>
	 */
	public static final Event<ModifyDisplayLogic> MODIFY_DISPLAY_LOGIC = EventFactory.createArrayBacked(ModifyDisplayLogic.class, callbacks -> (lookup, types, _format) -> {
		String format = _format;
		for (ModifyDisplayLogic callback : callbacks) {
			format = callback.modify(lookup, types, format);
		}
		return format;
	});

	@FunctionalInterface
	public interface AppendDisplayLogic {

		Logics append(RegistryWrapper.WrapperLookup lookup, Logics logics);

		@ApiStatus.NonExtendable
		interface Logics {

			void add(String format, RegistryKey<DamageType>... types);
		}
	}

	@FunctionalInterface
	public interface ModifyDisplayLogic {

		String modify(RegistryWrapper.WrapperLookup lookup, List<RegistryKey<DamageType>> types, String format);
	}
}
