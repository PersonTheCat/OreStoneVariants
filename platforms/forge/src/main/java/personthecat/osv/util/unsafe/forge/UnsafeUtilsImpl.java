package personthecat.osv.util.unsafe.forge;

import lombok.extern.log4j.Log4j2;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Log4j2
public class UnsafeUtilsImpl {

    private static final Unsafe UNSAFE;

    static {
        try {
            final Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
        }
        catch (final IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Unexpected environment. Unsafe is unavailable.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T allocate(final Class<T> clazz) {
        try {
            return (T) UNSAFE.allocateInstance(clazz);
        } catch (final InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T1, T2 extends T1> void copyFields(final T1 source, final T2 dest) {
        Class<?> clazz = source.getClass();
        do {
            for (final Field f : clazz.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers())) {
                    final Class<?> type = f.getType();
                    final long offset = UNSAFE.objectFieldOffset(f);
                    if (!type.isPrimitive()) {
                        UNSAFE.putObject(dest, offset, UNSAFE.getObject(source, offset));
                    } else if (type == int.class) {
                        UNSAFE.putInt(dest, offset, UNSAFE.getInt(source, offset));
                    } else if (type == boolean.class) {
                        UNSAFE.putBoolean(dest, offset, UNSAFE.getBoolean(source, offset));
                    } else if (type == long.class) {
                        UNSAFE.putLong(dest, offset, UNSAFE.getLong(source, offset));
                    } else if (type == short.class) {
                        UNSAFE.putShort(dest, offset, UNSAFE.getShort(source, offset));
                    } else if (type == byte.class) {
                        UNSAFE.putByte(dest, offset, UNSAFE.getByte(source, offset));
                    } else if (type == char.class) {
                        UNSAFE.putChar(dest, offset, UNSAFE.getChar(source, offset));
                    } else if (type == float.class) {
                        UNSAFE.putFloat(dest, offset, UNSAFE.getFloat(source, offset));
                    } else if (type == double.class) {
                        UNSAFE.putDouble(dest, offset, UNSAFE.getDouble(source, offset));
                    }
                }
            }
        } while ((clazz = clazz.getSuperclass()) != Object.class);
    }
}
