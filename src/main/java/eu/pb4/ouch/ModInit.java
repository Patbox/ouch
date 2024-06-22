package eu.pb4.ouch;


import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import eu.pb4.predicate.api.PredicateRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class ModInit implements ModInitializer {
	public static final String ID = "ouch";
	public static final String VERSION = FabricLoader.getInstance().getModContainer(ID).get().getMetadata().getVersion().getFriendlyString();
	public static final Logger LOGGER = LoggerFactory.getLogger("Ouch!");
    public static final boolean DEV_ENV = FabricLoader.getInstance().isDevelopmentEnvironment();
    public static final boolean DEV_MODE = VERSION.contains("-dev.") || DEV_ENV;
	@Nullable
	public static Config config;

	public static Identifier id(String path) {
		return Identifier.of(ID, path);
	}

	@Override
	public void onInitialize() {
		if (VERSION.contains("-dev.")) {
			LOGGER.warn("=====================================================");
			LOGGER.warn("You are using development version of Ouch!");
			LOGGER.warn("Support is limited, as features might be unfinished!");
			LOGGER.warn("You are on your own!");
			LOGGER.warn("=====================================================");
		}

		ServerLifecycleEvents.SERVER_STARTED.register(server -> this.setup(server.getRegistryManager()));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> config = null);
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> this.setup(server.getRegistryManager()));
	}

	@SuppressWarnings("ConstantValue")
	private void setup(DynamicRegistryManager.Immutable registryManager) {
		//noinspection PointlessBooleanExpression
		var dev = DEV_ENV && false;
		config = null;
		var configPath = FabricLoader.getInstance().getConfigDir().resolve("ouch.json");

		if (!Files.exists(configPath) || dev) {
			var damage = new ArrayList<DamageDisplayLogic>();
			var damageExtra = new ArrayList<DamageDisplayLogic>();
			var death = new ArrayList<DeathDisplayLogic>();
			var healing = new ArrayList<HealDisplayLogic>();

			DefaultDisplay.createDefault(damage, healing, death, damageExtra, registryManager);
			config = new Config(List.of(damage, damageExtra), List.of(healing), List.of(death));
			if (dev) {
				return;
			}
		} else {
			try {
				config = Config.CODEC.decode(
						registryManager.getOps(JsonOps.INSTANCE), JsonParser.parseString(Files.readString(configPath, StandardCharsets.UTF_8))
				).getOrThrow().getFirst();
			} catch (Throwable e) {
				LOGGER.error("Failed to load config file (ouch.json)", e);
				return;
			}
		}

		try {
			var gson = new GsonBuilder().disableHtmlEscaping().setLenient().setPrettyPrinting().create();

			Files.writeString(configPath, gson.toJson(Config.CODEC.encodeStart(
					registryManager.getOps(JsonOps.INSTANCE), config
			).getOrThrow()), StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
		} catch (Throwable e) {
			LOGGER.error("Failed to write config file (ouch.json)", e);
		}
	}
}
