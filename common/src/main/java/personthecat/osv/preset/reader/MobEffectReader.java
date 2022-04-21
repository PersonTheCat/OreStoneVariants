package personthecat.osv.preset.reader;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.jetbrains.annotations.Nullable;
import personthecat.osv.mixin.MobEffectInstanceAccessor;

import java.util.function.Function;

import static personthecat.catlib.serialization.codec.CodecUtils.dynamic;
import static personthecat.catlib.serialization.codec.DynamicField.field;

public class MobEffectReader {

    private static final Codec<Pair<MobEffectInstance, Float>> OBJECT = dynamic(MobEffectBuilder::new, MobEffectBuilder::build).create(
        field(Registry.MOB_EFFECT.byNameCodec(), "effect", p -> p.getFirst().getEffect(), (b, e) -> b.effect = e),
        field(Codec.INT, "duration", p -> p.getFirst().getDuration(), (b, d) -> b.duration = d),
        field(Codec.INT, "amplifier", p -> p.getFirst().getAmplifier(), (b, a) -> b.amplifier = a),
        field(Codec.BOOL, "ambient", p -> p.getFirst().isAmbient(), (b, a) -> b.ambient = a),
        field(Codec.BOOL, "visible", p -> p.getFirst().isVisible(), (b, v) -> b.visible = v),
        field(Codec.BOOL, "showIcon", p -> p.getFirst().showIcon(), (b, s) -> b.showIcon = s),
        field(Codec.FLOAT, "chance", Pair::getSecond, (b, u) -> b.chance = u),
        field(Registry.MOB_EFFECT.byNameCodec(), "hiddenEffect",
            p -> ((MobEffectInstanceAccessor) p.getFirst()).getHiddenEffect().getEffect(),
            (b, h) -> b.hiddenEffect = new MobEffectInstance(h, b.duration))
    );

    public static Codec<Pair<MobEffectInstance, Float>> CODEC = Codec.either(Registry.MOB_EFFECT.byNameCodec(), OBJECT).xmap(
        either -> either.map(effect -> Pair.of(new MobEffectInstance(effect), 1.0F), Function.identity()),
        Either::right
    );

    private static class MobEffectBuilder {
        private MobEffect effect = MobEffects.BAD_OMEN;
        private int duration = 160;
        private int amplifier = 0;
        private boolean ambient = false;
        private boolean visible = true;
        private boolean showIcon = true;
        private float chance = 1F;
        @Nullable private MobEffectInstance hiddenEffect;

        Pair<MobEffectInstance, Float> build() {
            final MobEffectInstance instance =
                new MobEffectInstance(effect, duration, amplifier, ambient, visible, showIcon, hiddenEffect);
            return Pair.of(instance, chance);
        }
    }
}
