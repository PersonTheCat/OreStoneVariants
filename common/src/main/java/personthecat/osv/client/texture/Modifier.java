package personthecat.osv.client.texture;

import lombok.AllArgsConstructor;
import personthecat.osv.util.StateMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@AllArgsConstructor
public enum Modifier {
    DENSE(() -> DenseOverlayModifier.INSTANCE),
    CLASSIC_DENSE(() -> ClassicDenseOverlayModifier.INSTANCE),
    SHADE(() -> ShadedOverlayModifier.INSTANCE),
    SHADOW(() -> ShadowOverlayModifier.INSTANCE),
    DARK(() -> DarkenedOverlayModifier.INSTANCE),
    BRIGHT(() -> BrightenedOverlayModifier.INSTANCE);

    private final Supplier<OverlayModifier> modifier;

    public static String format(final Collection<Modifier> modifiers) {
        if (modifiers.isEmpty()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        Modifier last = null;
        int count = 0;
        for (final Modifier modifier : modifiers) {
            if (last == modifier) {
                count++;
            } else {
                if (sb.length() != 0) {
                    sb.append('_');
                }
                sb.append(modifier.name().toLowerCase());
                if (count > 1) {
                    sb.append(count);
                }
                count = 1;
            }
            last = modifier;
        }
        return sb.toString();
    }

    public static StateMap<List<Modifier>> createDefault(final boolean dense, final boolean shade) {
        final StateMap<List<Modifier>> map = new StateMap<>();
        if (dense) {
            map.put("dense=true", shade ? Arrays.asList(DENSE, SHADOW) : Collections.singletonList(DENSE));
            map.put("dense=false", shade ? Collections.singletonList(SHADE) : Collections.emptyList());
        } else {
            map.put("", shade ? Collections.singletonList(SHADE) : Collections.emptyList());
        }
        return map;
    }

    public OverlayModifier get() {
        return this.modifier.get();
    }
}
