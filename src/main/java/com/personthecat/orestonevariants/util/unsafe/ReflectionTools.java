package com.personthecat.orestonevariants.util.unsafe;

import cpw.mods.modlauncher.api.INameMappingService.Domain;
import lombok.extern.log4j.Log4j2;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import personthecat.fresult.Result;

import java.lang.reflect.Method;

/** A convenient wrapper for ObfuscationReflectionHelper using Result. */
@Log4j2
public class ReflectionTools {

    public static <T> Method getMethod(Class<T> clazz, String name, Class<?>... params) {
        final String mapped = ObfuscationReflectionHelper.remapName(Domain.METHOD, name);
        final Method m = (Method) Result.of(() -> ObfuscationReflectionHelper.findMethod(clazz, mapped, params))
            .expect("Build error: invalid method name used in reflection.");
        m.setAccessible(true);
        return m;
    }

    public static <T> void invoke(Method m, T instance, Object... args) {
        Result.of(() -> { m.invoke(instance, args); })
            .expect("Build error: invalid arguments to reflected method.");
    }

    @SuppressWarnings("unchecked")
    public static <T, R> R get(Method m, T instance, Object... args) {
        return (R) Result.of(() -> m.invoke(instance, args))
            .expect("Build error: invalid arguments to reflected method.");
    }
}