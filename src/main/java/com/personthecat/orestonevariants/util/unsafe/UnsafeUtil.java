package com.personthecat.orestonevariants.util.unsafe;

import lombok.extern.log4j.Log4j2;
import personthecat.fresult.Result;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

@Log4j2
public class UnsafeUtil {

    public static Unsafe UNSAFE = locateUnsafe();

    private static Unsafe locateUnsafe() {
        final Field f = ReflectionTools.getField(Unsafe.class, "theUnsafe");
        return ReflectionTools.getValue(f, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T allocate(Class<T> clazz) {
        return (T) Result.of(() -> UNSAFE.allocateInstance(clazz))
            .ifErr(log::warn)
            .unwrap();
    }

}
