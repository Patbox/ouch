package eu.pb4.ouch;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Optional;

public class FloatingText extends ElementHolder {
    private final LivingEntity entity;
    private final TextDisplayElement display;
    private final DisplaySettings settings;
    private Vec3d velocity;
    private int timer;

    private FloatingText(LivingEntity entity, DisplaySettings settings, Text text, Vec3d initialVelocity) {
        this.entity = entity;
        this.settings = settings;
        this.timer = settings.stayingTime;
        this.display = new TextDisplayElement(text);
        this.display.setViewRange(0.3f);
        this.display.setShadow(true);
        this.display.setBackground(0);
        this.display.setBrightness(new Brightness(15, 15));
        this.display.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
        this.display.setScale(new Vector3f(settings.scale));
        var box = entity.getBoundingBox();
        var random = entity.getRandom();
        this.display.setOverridePos(box.getCenter()
                .add((random.nextFloat() - 0.5) * box.getLengthX() * 0.8, (random.nextFloat() - 0.35) * box.getLengthY() * 0.6, (random.nextFloat() - 0.5) * box.getLengthZ() * 0.8));
        this.display.setTeleportDuration(2);
        this.velocity = initialVelocity;
        this.addElement(display);
    }

    @Override
    protected void onTick() {
        if (!this.entity.getEntityWorld().getTickManager().shouldTick()) {
            return;
        }
        super.onTick();

        if (this.timer-- == 0) {
            this.destroy();
            return;
        } else if (this.timer == 5) {
            this.display.setScale(new Vector3f(0));
            this.display.setInterpolationDuration(5);
            this.display.startInterpolation();
        }
        var next = this.display.getCurrentPos().add(this.velocity);

        this.display.setOverridePos(next);
        this.velocity = this.velocity.multiply(this.settings.perTickVelocityMultiplier).add(0, -this.settings.gravity, 0);
    }

    @Override
    public boolean startWatching(ServerPlayNetworkHandler player) {
        if (player.player != this.entity) {
            return super.startWatching(player);
        } else {
            return false;
        }
    }

    public static void createDamage(LivingEntity entity, DamageSource source, float amount) {
        Preset.get().selectDamage(entity, source, amount, (text, displaySettings) -> createText(entity, source.getPosition(), text, displaySettings));
    }

    public static void createDeath(LivingEntity entity, DamageSource source) {
        Preset.get().selectDeath(entity, source, (text, displaySettings) -> createText(entity, source.getPosition(), text, displaySettings));
    }

    public static void createHealing(LivingEntity entity, float amount) {
        Preset.get().selectHealing(entity, amount, (text, displaySettings) -> createText(entity, null, text, displaySettings));
    }

    private static void createText(LivingEntity entity, @Nullable Vec3d source, Text text, DisplaySettings displaySettings) {
        var random = entity.getRandom();
        var velocity = displaySettings.velocityOverride().isPresent() ? displaySettings.velocityOverride().get() : (source != null ? source.subtract(entity.getEntityPos())
                .withAxis(Direction.Axis.Y, 0)
                : new Vec3d(random.nextFloat() - 0.5, 0, random.nextFloat() - 0.5)
        ).normalize().rotateY((random.nextFloat() - 0.5f) * 80 * MathHelper.RADIANS_PER_DEGREE).multiply(0.35).add(0, 0.2, 0) ;

        var model = new FloatingText(entity, displaySettings, text, velocity);
        ChunkAttachment.ofTicking(model, (ServerWorld) entity.getEntityWorld(), entity.getEntityPos());
    }

    public record DisplaySettings(float perTickVelocityMultiplier, float gravity, int stayingTime, float scale, Optional<Vec3d> velocityOverride) {
        public static final DisplaySettings GENERAL = new DisplaySettings(0.7f, 0.05f, 20,  0.8f, Optional.empty());
        public static final DisplaySettings DEATH = new DisplaySettings(0.7f, 0f,30, 0.8f, Optional.of(new Vec3d(0, 0.2, 0)));
        public static final MapCodec<DisplaySettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.floatRange(0, 1).optionalFieldOf("per_tick_velocity_multiplier", 0.7f).forGetter(DisplaySettings::perTickVelocityMultiplier),
                Codec.floatRange(-1, 1).optionalFieldOf("gravity", 0.05f).forGetter(DisplaySettings::gravity),
                Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("staying_time", 20).forGetter(DisplaySettings::stayingTime),
                Codec.floatRange(0, 5).optionalFieldOf("text_scale", 0.8f).forGetter(DisplaySettings::scale),
                Vec3d.CODEC.optionalFieldOf("velocity_override").forGetter(DisplaySettings::velocityOverride)
        ).apply(instance, DisplaySettings::new));
    }
}
