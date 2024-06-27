package eu.pb4.ouch;


import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ModInit implements ModInitializer {
	public static final String ID = "ouch";
	public static final String VERSION = FabricLoader.getInstance().getModContainer(ID).get().getMetadata().getVersion().getFriendlyString();
	public static final Logger LOGGER = LoggerFactory.getLogger("Ouch!");
    public static final boolean DEV_ENV = FabricLoader.getInstance().isDevelopmentEnvironment();
    public static final boolean DEV_MODE = VERSION.contains("-dev.") || DEV_ENV;
	public static Preset config = Preset.EMPTY;
	private static Either<String, Preset> configValue = null;

	public static Identifier id(String path) {
		return Identifier.of(ID, path);
	}

	public static Map<String, Preset> PRESETS = new HashMap<>();

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
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			config = Preset.EMPTY;
			configValue = null;
			PRESETS.clear();
		});
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> this.setup(server.getRegistryManager()));
	}

	@SuppressWarnings("ConstantValue")
	private void setup(DynamicRegistryManager.Immutable registryManager) {
		PRESETS.clear();
		Presets.setupPresets(PRESETS::put, registryManager);
		var gson = new GsonBuilder().disableHtmlEscaping().setLenient().setPrettyPrinting().create();

		if (DEV_MODE) {
			var path = FabricLoader.getInstance().getGameDir().resolve("../preset");
            try {
				Files.createDirectories(path);
				for (var preset : PRESETS.entrySet()) {
					Files.writeString(path.resolve(preset.getKey() + ".json"), gson.toJson(Preset.SELF_CODEC.encodeStart(
							registryManager.getOps(JsonOps.INSTANCE), preset.getValue()
					).getOrThrow()), StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
				}
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

		//noinspection PointlessBooleanExpression
		config = null;
		var configPath = FabricLoader.getInstance().getConfigDir().resolve("ouch.json");

		if (!Files.exists(configPath)) {
			configValue = Either.left(Presets.DEFAULT);
		} else {
			try {
				configValue = Preset.CODEC.decode(
						registryManager.getOps(JsonOps.INSTANCE), JsonParser.parseString(Files.readString(configPath, StandardCharsets.UTF_8))
				).getOrThrow().getFirst();
			} catch (Throwable e) {
				LOGGER.error("Failed to load config file (ouch.json)", e);
				return;
			}
		}

		config = configValue.map(x -> PRESETS.getOrDefault(x, Preset.EMPTY), Function.identity());

		try {
			Files.writeString(configPath, gson.toJson(Preset.CODEC.encodeStart(
					registryManager.getOps(JsonOps.INSTANCE), configValue
			).getOrThrow()), StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
		} catch (Throwable e) {
			LOGGER.error("Failed to write config file (ouch.json)", e);
		}
	}
}
