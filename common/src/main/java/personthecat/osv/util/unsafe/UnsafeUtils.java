package personthecat.osv.util.unsafe;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class UnsafeUtils {

    @ExpectPlatform
    public static <T> T allocate(final Class<T> clazz) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T1, T2 extends T1> void copyFields(final T1 source, final T2 dest) {
        throw new AssertionError();
    }
}
