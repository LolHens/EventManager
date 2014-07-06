package com.dafttech.util;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ReflectionUtil {

    public static final Set<Field> getAnnotatedFields(Class<?> target, Class<? extends Annotation> annotation, Class<?> reqType) {
        Set<Field> fields = new HashSet<Field>();
        if (reqType == void.class) return fields;
        for (Field field : getAllDeclaredFields(target)) {
            if (!field.isAnnotationPresent(annotation)) continue;
            if (reqType == null || field.getType() == reqType) {
                fields.add(field);
            }
        }
        return fields;
    }

    public static final Set<Method> getAnnotatedMethods(Class<?> target, Class<? extends Annotation> annotation,
            Class<?> reqType, Class<?>... reqArgs) {
        Set<Method> methods = new HashSet<Method>();
        for (Method method : getAllDeclaredMethods(target)) {
            if (!method.isAnnotationPresent(annotation)) continue;
            if ((reqType == null || method.getReturnType() == reqType)
                    && (reqArgs == null || reqArgs.length == 1 && reqArgs[0] == null || Arrays.equals(method.getParameterTypes(),
                            reqArgs))) {
                methods.add(method);
            }
        }
        return methods;
    }

    public static final Set<Constructor<?>> getAnnotatedConstructors(Class<?> target, Class<? extends Annotation> annotation,
            Class<?>... reqArgs) {
        Set<Constructor<?>> constructors = new HashSet<Constructor<?>>();
        for (Constructor<?> constructor : getAllDeclaredConstructors(target)) {
            if (!constructor.isAnnotationPresent(annotation)) continue;
            if (reqArgs == null || reqArgs.length == 1 && reqArgs[0] == null
                    || Arrays.equals(constructor.getParameterTypes(), reqArgs)) {
                constructors.add(constructor);
            }
        }
        return constructors;
    }

    public static final Set<Field> getAllDeclaredFields(Class<?> target) {
        Set<Field> fields = new HashSet<Field>();
        getAllDeclaredFields(target, fields);
        return fields;
    }

    private static final void getAllDeclaredFields(Class<?> target, Set<Field> fields) {
        try {
            for (Field field : target.getDeclaredFields()) {
                if (!field.isAccessible()) field.setAccessible(true);
                fields.add(field);
            }
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
        }
        Class<?> superclass = target.getSuperclass();
        if (superclass != null) getAllDeclaredFields(superclass, fields);
    }

    public static final Set<Method> getAllDeclaredMethods(Class<?> target) {
        Set<Method> methods = new HashSet<Method>();
        getAllDeclaredMethods(target, methods);
        return methods;
    }

    private static final void getAllDeclaredMethods(Class<?> target, Set<Method> methods) {
        try {
            for (Method method : target.getDeclaredMethods()) {
                if (!method.isAccessible()) method.setAccessible(true);
                methods.add(method);
            }
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
        }
        Class<?> superclass = target.getSuperclass();
        if (superclass != null) getAllDeclaredMethods(superclass, methods);
    }

    public static final Set<Constructor<?>> getAllDeclaredConstructors(Class<?> target) {
        Set<Constructor<?>> constructors = new HashSet<Constructor<?>>();
        getAllDeclaredConstructors(target, constructors);
        return constructors;
    }

    private static final void getAllDeclaredConstructors(Class<?> target, Set<Constructor<?>> constructors) {
        try {
            for (Constructor<?> constructor : target.getDeclaredConstructors()) {
                if (!constructor.isAccessible()) constructor.setAccessible(true);
                constructors.add(constructor);
            }
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
        }
        Class<?> superclass = target.getSuperclass();
        if (superclass != null) getAllDeclaredConstructors(superclass, constructors);
    }

    /**
     * Used to get an array of null objects (or false for boolean, 0 for
     * numbers, etc.) for dynamic instantiation. You can also insert objects for
     * specific types instead of null.
     * 
     * @param argTypes
     *            Class<?>[] - the argument Types
     * @param appliedArgs
     *            Object... - The arg types that are replaced with objects
     *            Example: ([Class<?>[] argTypes], String.class, "test")
     * @return Object[] - Array with null or objects (look above)
     */
    public static final Object[] buildArgumentArray(Class<?>[] argTypes, Object... appliedArgs) {
        Object[] args = new Object[argTypes.length];
        for (int i1 = 0; i1 < args.length; i1++) {
            for (int i2 = 0; i2 + 1 < appliedArgs.length; i2 += 2) {
                if (argTypes[i1] == appliedArgs[i2]) {
                    args[i1] = appliedArgs[i2 + 1];
                    break;
                }
            }
            if (args[i1] == null && argTypes[i1].isPrimitive()) args[i1] = PrimitiveUtil.get(argTypes[i1]).nullObj;

        }
        return args;
    }

    public Constructor<?> getConstructor(Class<?> target) {
        try {
            Constructor<?> constructor = target.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Target({ METHOD, FIELD })
    @Retention(RUNTIME)
    @Documented
    public static @interface SingletonInstance {
    }

    @SuppressWarnings("unchecked")
    public static final <ClassType> ClassType getSingletonInstance(Class<?> target, Object... appliedArgs) {
        for (Field field : getAnnotatedFields(target, SingletonInstance.class, null)) {
            if (!Modifier.isStatic(field.getModifiers())) continue;
            try {
                return (ClassType) field.get(null);
            } catch (Exception e) {
            }
        }
        for (Method method : getAnnotatedMethods(target, SingletonInstance.class, null, (Class<?>[]) null)) {
            if (!Modifier.isStatic(method.getModifiers())) continue;
            try {
                return (ClassType) method.invoke(null, buildArgumentArray(method.getParameterTypes(), appliedArgs));
            } catch (Exception e) {
            }
        }
        return null;
    }
}
