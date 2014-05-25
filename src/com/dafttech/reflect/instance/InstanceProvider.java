package com.dafttech.reflect.instance;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.dafttech.reflect.Reflector;

public class InstanceProvider<ClassType> extends Reflector {
    public InstanceProvider(Class<?> target) {
        super(target);
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
    public final ClassType getInstance(Object... methodArgs) {
        for (Field field : getAnnotatedFields(Instance.class, null)) {
            if (Modifier.isStatic(field.getModifiers())) {
                try {
                    return (ClassType) field.get(null);
                } catch (Exception e) {
                }
            }
        }
        for (Method method : getAnnotatedMethods(Instance.class, null)) {
            if (Modifier.isStatic(method.getModifiers())) {
                try {
                    return (ClassType) method.invoke(null, methodArgs);
                } catch (Exception e) {
                }
            }
        }
        return null;
    }
}
