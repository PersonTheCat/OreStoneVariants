package personthecat.osv.preset.resolver;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.jetbrains.annotations.Nullable;
import personthecat.osv.mixin.MobEffectInstanceAccessor;

import static personthecat.catlib.serialization.CodecUtils.dynamic;
import static personthecat.catlib.serialization.DynamicField.field;

public class MobEffectResolver {

    public static final Codec<Pair<MobEffectInstance, Float>> CODEC = dynamic(MobEffectBuilder::new, MobEffectBuilder::build).create(
        field(Registry.MOB_EFFECT, "effect", p -> p.getFirst().getEffect(), (b, e) -> b.effect = e),
        field(Codec.INT, "duration", p -> p.getFirst().getDuration(), (b, d) -> b.duration = d),
        field(Codec.INT, "amplifier", p -> p.getFirst().getAmplifier(), (b, a) -> b.amplifier = a),
        field(Codec.BOOL, "splash", p -> ((MobEffectInstanceAccessor) p.getFirst()).getSplash(), (b, s) -> b.splash = s),
        field(Codec.BOOL, "ambient", p -> p.getFirst().isAmbient(), (b, a) -> b.ambient = a),
        field(Codec.BOOL, "visible", p -> p.getFirst().isVisible(), (b, v) -> b.visible = v),
        field(Codec.BOOL, "showIcon", p -> p.getFirst().showIcon(), (b, s) -> b.showIcon = s),
        field(Codec.FLOAT, "unknown", Pair::getSecond, (b, u) -> b.unknown = u),
        field(Registry.MOB_EFFECT, "hiddenEffect", p -> ((MobEffectInstanceAccessor) p.getFirst()).getHiddenEffect().getEffect(),
            (b, h) -> b.hiddenEffect = new MobEffectInstance(h, b.duration))
    );

    private static class MobEffectBuilder {
        private MobEffect effect = MobEffects.BAD_OMEN;
        private int duration = 8;
        private int amplifier = 0;
        private boolean splash = false;
        private boolean ambient = false;
        private boolean visible = true;
        private boolean showIcon = true;
        private float unknown = 1F;
        @Nullable private MobEffectInstance hiddenEffect;

        Pair<MobEffectInstance, Float> build() {
            final MobEffectInstance instance =
                new MobEffectInstance(effect, duration, amplifier, ambient, visible, showIcon, hiddenEffect);
            ((MobEffectInstanceAccessor) instance).setSplash(splash);
            return Pair.of(instance, unknown);
        }
    }
}
