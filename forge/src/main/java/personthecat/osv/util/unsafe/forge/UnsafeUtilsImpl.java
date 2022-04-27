package personthecat.osv.util.unsafe.forge;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Log4j2
public class UnsafeUtilsImpl {

    private static final Unsafe UNSAFE = getUnsafe();

    private static Unsafe getUnsafe() {
        try {
            final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return (Unsafe) unsafeField.get(null);
        } catch (final Exception e) {
            throw new RuntimeException("Missing availability check: ", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T allocate(final Class<T> clazz) {
        try {
            return (T) UNSAFE.allocateInstance(clazz);
        } catch (final InstantiationException e) {
            throw new RuntimeException("Generating interceptor", e);
        }
    }

    public static <T1, T2 extends T1> void copyFields(final T1 source, final T2 dest) {
        Class<?> clazz = source.getClass();
        do {
            for (final Field rx : clazz.getDeclaredFields()) {
                final int modifiers = rx.getModifiers();
                if (!Modifier.isStatic(modifiers)) {
                    final Field tx = getMatchingField(dest.getClass(), rx);
                    if (tx == null) continue;
                    try {
                        // Use tx and rx in case the offset changes
                        if (Modifier.isFinal(tx.getModifiers())) {
                            copyUnsafe(source, dest, tx, rx);
                        } else {
                            tx.setAccessible(true);
                            rx.setAccessible(true);
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

    private static void copyUnsafe(final Object source, final Object dest, final Field tx, final Field rx) {
        final long txOffset = UNSAFE.objectFieldOffset(tx);
        final long rxOffset = UNSAFE.objectFieldOffset(rx);

        // Avoid platform-dependent issues with Unsafe#putObject and primitive values
        if (tx.getType().isAssignableFrom(int.class)) {
            UNSAFE.putInt(dest, txOffset, UNSAFE.getInt(source, rxOffset));
        } else if (tx.getType().isAssignableFrom(long.class)) {
            UNSAFE.putLong(dest, txOffset, UNSAFE.getLong(source, rxOffset));
        } else if (tx.getType().isAssignableFrom(float.class)) {
            UNSAFE.putFloat(dest, txOffset, UNSAFE.getFloat(source, rxOffset));
        } else if (tx.getType().isAssignableFrom(byte.class)) {
            UNSAFE.putByte(dest, txOffset, UNSAFE.getByte(source, rxOffset));
        } else if (tx.getType().isAssignableFrom(short.class)) {
            UNSAFE.putShort(dest, txOffset, UNSAFE.getShort(source, rxOffset));
        } else if (tx.getType().isAssignableFrom(char.class)) {
            UNSAFE.putChar(dest, txOffset, UNSAFE.getChar(source, rxOffset));
        } else if (tx.getType().isAssignableFrom(boolean.class)) {
            UNSAFE.putBoolean(dest, txOffset, UNSAFE.getBoolean(source, rxOffset));
        } else {
            UNSAFE.putObject(dest, txOffset, UNSAFE.getObject(source, rxOffset));
        }
    }
}
