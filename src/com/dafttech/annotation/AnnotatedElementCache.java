package com.dafttech.annotation;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;

public abstract class AnnotatedElementCache<R, T extends AnnotatedElement> {
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

    protected final T element;
    protected final Class<?> type;
    protected final boolean elementStatic;
    protected final Class<?> accessClass;
    protected final Object accessInstance;

    public AnnotatedElementCache(Class<?> type, T element, boolean isStatic, Object access) {
        if (type == null || element == null || access == null) throw new NullPointerException();
        this.type = type;
        this.element = element;
        this.elementStatic = isStatic;

        if (access.getClass() == Class.class) {
            if (isStatic) {
                accessClass = (Class<?>) access;
                accessInstance = null;
            } else {
                throw new IllegalArgumentException("Instance of non-static AccessObject cannot be null!");
            }
        } else {
            accessClass = access.getClass();
            if (isStatic)
                accessInstance = null;
            else
                accessInstance = access;
        }
    }

    public T getElement() {
        return element;
    }

    public Class<?> getType() {
        return type;
    }

    public boolean isStatic() {
        return elementStatic;
    }

    public Class<?> getAccessClass() {
        return accessClass;
    }

    public Object getAccessInstance() {
        return accessInstance;
    }

    public Object[] getNullArgArray() {
        return EMPTY_OBJECT_ARRAY;
    }

    public abstract Class<?> getReturnType();

    public Class<?>[] getParameterTypes() {
        return EMPTY_CLASS_ARRAY;
    }

    public abstract R get(Object... args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
            InstantiationException;
}