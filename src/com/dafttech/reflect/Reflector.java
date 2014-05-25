package com.dafttech.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dafttech.eventmanager.Instance;
import com.dafttech.primitives.Primitive;

public class Reflector {
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
    public static final Object[] argTypesToArgArray(Class<?>[] argTypes, Object... appliedArgs) {
        Object[] args = new Object[argTypes.length];
        for (int i1 = 0; i1 < args.length; i1++) {
            for (int i2 = 0; i2 + 1 < appliedArgs.length; i2 += 2) {
                if (argTypes[i1] == appliedArgs[i2]) {
                    args[i1] = appliedArgs[i2 + 1];
                    break;
                }
            }
            if (args[i1] == null && argTypes[i1].isPrimitive()) args[i1] = Primitive.get(argTypes[i1]).getNullValue();
        }
        return args;
    }

    /**
     * REQUIRES THE CLASS TO HAVE EITHER A FIELD OR A METHOD WITH THE INSTANCE
     * ANNOTATED WITH THE @INSTANCE ANNOTATION
     * 
     * @param targetClass
     *            Class<T> - The class, you want the instance from
     * @return T - The instance already casted to the targetClass
     */
    @SuppressWarnings("unchecked")
    public static final <T> T getInstance(Class<T> targetClass, Object... methodArgs) {
        for (Field field : getAnnotatedFields(targetClass, Instance.class, false, null)) {
            if (Modifier.isStatic(field.getModifiers())) {
                try {
                    return (T) field.get(null);
                } catch (Exception e) {
                }
            }
        }
        for (Method method : getAnnotatedMethods(targetClass, Instance.class, false, null)) {
            if (Modifier.isStatic(method.getModifiers())) {
                try {
                    return (T) method.invoke(null, methodArgs);
                } catch (Exception e) {
                }
            }
        }
        return null;
    }

    public static final List<Method> getAnnotatedMethods(Class<?> targetClass, Class<? extends Annotation> annotation,
            boolean throwException, Class<?> reqType, Class<?>... reqArgs) {
        List<Method> methods = new ArrayList<Method>();
        for (Method method : getAllDeclaredMethods(targetClass)) {
            if (method.isAnnotationPresent(annotation)) {
                if ((reqType == null || method.getReturnType() == reqType)
                        && (reqArgs == null || reqArgs.length == 1 && reqArgs[0] == null || Arrays.equals(
                                method.getParameterTypes(), reqArgs))) {
                    methods.add(method);
                } else if (throwException) {
                    String errorMessage = "\nat " + targetClass.getName() + " at Annotation " + annotation.getName() + ":";
                    errorMessage += "\nexpected: " + reqType.getName() + " with " + (reqArgs.length == 0 ? "no args" : "args:");
                    for (Class<?> arg : reqArgs)
                        errorMessage += ", " + arg.getName();
                    errorMessage += "\nand got:  " + method.getReturnType() + " with "
                            + (method.getParameterTypes().length == 0 ? "no args" : "args:");
                    for (Class<?> arg : method.getParameterTypes())
                        errorMessage += ", " + arg.getName();
                    errorMessage += ".";
                    throw new IllegalArgumentException(errorMessage);
                }
            }
        }
        return methods;
    }

    public static final List<Field> getAnnotatedFields(Class<?> targetClass, Class<? extends Annotation> annotation,
            boolean throwException, Class<?> reqType) {
        List<Field> fields = new ArrayList<Field>();
        if (reqType == void.class) return fields;
        for (Field field : getAllDeclaredFields(targetClass)) {
            if (field.isAnnotationPresent(annotation)) {
                if (reqType == null || field.getType() == reqType) {
                    fields.add(field);
                } else if (throwException) {
                    String errorMessage = "\nat " + targetClass.getName() + " at Annotation " + annotation.getName() + ":";
                    errorMessage += "\nexpected: " + reqType.getName();
                    errorMessage += "\nand got:  " + field.getType();
                    errorMessage += ".";
                    throw new IllegalArgumentException(errorMessage);
                }
            }
        }
        return fields;
    }

    public static final List<Method> getAllDeclaredMethods(Class<?> targetClass) {
        return getAllDeclaredMethods(targetClass, null);
    }

    public static final List<Field> getAllDeclaredFields(Class<?> targetClass) {
        return getAllDeclaredFields(targetClass, null);
    }

    private static final List<Method> getAllDeclaredMethods(Class<?> targetClass, List<Method> methods) {
        if (methods == null) methods = new ArrayList<Method>();
        try {
            for (Method method : targetClass.getDeclaredMethods())
                if (!methods.contains(method)) {
                    method.setAccessible(true);
                    methods.add(method);
                }
        } catch (NoClassDefFoundError e) {
        }
        if (targetClass.getSuperclass() != null) getAllDeclaredMethods(targetClass.getSuperclass(), methods);
        return methods;
    }

    private static final List<Field> getAllDeclaredFields(Class<?> targetClass, List<Field> fields) {
        if (fields == null) fields = new ArrayList<Field>();
        try {
            for (Field field : targetClass.getDeclaredFields())
                if (!fields.contains(field)) {
                    field.setAccessible(true);
                    fields.add(field);
                }
        } catch (NoClassDefFoundError e) {
        }
        if (targetClass.getSuperclass() != null) getAllDeclaredFields(targetClass.getSuperclass(), fields);
        return fields;
    }
}
