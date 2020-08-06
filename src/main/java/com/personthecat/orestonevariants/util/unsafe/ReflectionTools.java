package com.personthecat.orestonevariants.util.unsafe;

import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import personthecat.fresult.Result;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

/** A convenient wrapper for ObfuscationReflectionHelper using Result. */
public class ReflectionTools {
    public static Field getField(Class clazz, String name) {
        final Field f = (Field) Result.of(() -> ObfuscationReflectionHelper.findField(clazz, name))
            .expect("Build error: invalid field name used in reflection.");
        f.setAccessible(true);
        return f;
    }

    public static Field getField(Class clazz, String name, int index) {
        final Field f = (Field) Result.of(() -> ObfuscationReflectionHelper.findField(clazz, name))
            .ifErr(e -> debug("Reflection error: field \"{}\" not found in mappings. Trying index...", name))
            .orElseTry(e -> clazz.getDeclaredFields()[index])
            .expect("Build error: invalid field name / index used in reflection.");
        f.setAccessible(true);
        return f;
    }

    public static Method getMethod(Class clazz, String name, Class... params) {
        final Method method = ObfuscationReflectionHelper.findMethod(clazz, name, params);
        method.setAccessible(true);
        return method;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getValue(Field f, Object instance) {
        return (T) Result.of(() -> f.get(instance))
            .expect("Build error: field not marked as accessible.");
    }

    public static <T> T getValue(Class clazz, String name, int index, Object instance) {
        return getValue(getField(clazz, name, index), instance);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getValue(Class clazz, String name, Object instance) {
        return (T) Result.of(() -> ObfuscationReflectionHelper.getPrivateValue(clazz, instance, name))
            .expect("Build error: invalid field names used in reflection.");
    }

    public static void setValue(Field f, Object instance, Object value) {
        Result.of(() -> f.set(instance, value))
            .expect("Build error: field not marked as accessible.");
    }
}