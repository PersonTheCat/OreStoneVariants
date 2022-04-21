package personthecat.osv.util.unsafe.forge;

import lombok.extern.log4j.Log4j2;
import net.minecraftforge.fml.unsafe.UnsafeHacks;
import org.jetbrains.annotations.Nullable;
import personthecat.fresult.Result;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Log4j2
public class UnsafeUtilsImpl {

    public static <T> T allocate(final Class<T> clazz) {
        return UnsafeHacks.newInstance(clazz);
    }

    private static final Field MODIFIERS =
        Result.suppress(() -> Field.class.getDeclaredField("modifiers"))
            .ifOk(modifiers -> modifiers.setAccessible(true))
            .unwrap();

    public static <T1, T2 extends T1> void copyFields(final T1 source, final T2 dest) {
        Class<?> clazz = source.getClass();
        do {
            for (final Field rx : clazz.getDeclaredFields()) {
                final int modifiers = rx.getModifiers();
                if (!Modifier.isStatic(modifiers)) {
                    final Field tx = getMatchingField(dest.getClass(), rx);
                    if (tx == null) continue;
                    rx.setAccessible(true);
                    tx.setAccessible(true);
                    try {
                        if (Modifier.isFinal(modifiers)) {
                            MODIFIERS.setInt(rx, modifiers & ~Modifier.FINAL);
                            tx.set(dest, rx.get(source));
                            MODIFIERS.setInt(rx, modifiers);
                        } else {
                            tx.set(dest, rx.get(source));
                        }
                    } catch (final IllegalAccessException e) {
                        log.error("Unable to copy {} {}", rx.getType(), rx.getName());
                    }
                }
            }
        } while ((clazz = clazz.getSuperclass()) != Object.class);
    }

    @Nullable
    private static Field getMatchingField(Class<?> clazz, final Field source) {
        final String name = source.getName();
        final Class<?> type = source.getType();
        do {
            try {
                final Field matching = clazz.getDeclaredField(name);
                if (matching.getType().equals(type)) {
                    return matching;
                }
            } catch (final NoSuchFieldException ignored) {}
        } while ((clazz = clazz.getSuperclass()) != Object.class);
        return null;
    }
}
