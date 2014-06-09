package com.dafttech.type;

public class TypeArray<ArrayType extends Type<?>> extends Type<Object[]> {
    ArrayType arrayType;

    public TypeArray(ArrayType arrayType) {
        super(null);
        this.arrayType = arrayType;
    }
}
