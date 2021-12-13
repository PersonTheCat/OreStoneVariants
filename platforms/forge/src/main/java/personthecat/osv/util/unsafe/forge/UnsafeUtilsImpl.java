package personthecat.osv.util.unsafe.forge;

import net.minecraftforge.fml.unsafe.UnsafeHacks;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class UnsafeUtilsImpl {
    public static <T> T allocate(final Class<T> clazz) {
        return UnsafeHacks.newInstance(clazz);
    }

    public static <T1, T2 extends T1> void copyFields(final T1 source, final T2 dest) {
        Class<?> clazz = source.getClass();
        do {
            for (final Field f : clazz.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers())) {
                    UnsafeHacks.setField(f, dest, UnsafeHacks.getField(f, source));
                }
            }
        } while ((clazz = clazz.getSuperclass()) != Object.class);
    }
}
