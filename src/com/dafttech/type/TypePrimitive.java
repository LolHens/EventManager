package com.dafttech.type;

import java.lang.reflect.Field;

public abstract class TypePrimitive<ClassType> extends Type<ClassType> {
    protected TypePrimitive(boolean prototype) {
        super(prototype);
    }

    public abstract int getSize();

    public int getBytes() {
        return getSize() / Byte.SIZE;
    }

    public abstract long toLong();

    public abstract ClassType fromLong(long val);

    public void setValue(ClassType val) {
        try {
            getValueField().set(value, val);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    protected abstract Field getValueField();

    public abstract Class<?> getPrimitiveClass();

    @Override
    public byte[] toByteArray() {
        long value = toLong();
        byte[] array = new byte[getBytes()];
        for (int i = 0; i < array.length; i++)
            array[i] = (byte) (value >> (array.length - 1 - i) * 8);
        return array;
    }

    @Override
    public boolean isClass(Class<?> targetClass) {
        return targetClass != null
                && (getTypeClass().isAssignableFrom(targetClass) || getPrimitiveClass().isAssignableFrom(targetClass));
    }

    @Override
    public Type<ClassType> fromByteArray(byte... array) {
        long value = 0;
        int size = getBytes();
        for (int i = 0; i < size; i++)
            value = value | (array[i] & 0xFF) << (size - 1 - i) * 8;
        return create(fromLong(value));
    }

    public static TypePrimitive<?> forObject(Object obj) {
        for (Type<?> type : types) {
            if (!type.isType(obj)) continue;
            if (!(type instanceof TypePrimitive)) continue;
            return (TypePrimitive<?>) type.create(obj);
        }
        return VOID.<TypeVoid> create(null);
    }

    public static TypePrimitive<?> forClass(Class<?> targetClass) {
        for (Type<?> type : types) {
            if (!type.isClass(targetClass)) continue;
            if (!(type instanceof TypePrimitive)) continue;
            return (TypePrimitive<?>) type.create(null);
        }
        return VOID.<TypeVoid> create(null);
    }
}
