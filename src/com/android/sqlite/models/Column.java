//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.android.sqlite.models;

import android.database.Cursor;
import android.util.Log;
import com.android.sqlite.DataTypes;
import com.android.sqlite.SerializationUtils;
import com.android.sqlite.Annotations.ColumnName;
import com.android.sqlite.Annotations.DataType;
import com.android.sqlite.Annotations.Default;
import com.android.sqlite.Annotations.NotNull;
import com.android.sqlite.Annotations.PrimaryKey;
import java.lang.reflect.Field;

public class Column {
    private final Field field;
    protected boolean NOT_NULL;
    protected boolean PRIMARY_KEY;
    protected final String name;
    protected final String dataType;
    protected final String uniqueName;
    private final int fieldType;
    private final String defultValue;

    public static Column fromCursor(Cursor cursor, Table table) {
        boolean FIELD_NAME = true;
        boolean FIELD_TYPE = true;
        boolean FIELD_NOT_NULL = true;
        boolean FIELD_DEF_VALUE = true;
        boolean FIELD_PK = true;
        String name = cursor.getString(1);
        Column column = new Column((Field)null, cursor.getInt(3) == 1, cursor.getInt(5) == 1, name, cursor.getString(2), table.getName() + "." + name, -1, cursor.getString(4));
        return column;
    }

    private Column(Field field, boolean nOT_NULL, boolean pRIMARY_KEY, String name, String dataType, String uniqueName, int fieldType, String defaultValue) {
        this.field = field;
        this.NOT_NULL = nOT_NULL;
        this.PRIMARY_KEY = pRIMARY_KEY;
        this.name = name;
        this.dataType = dataType;
        this.uniqueName = uniqueName;
        this.fieldType = fieldType;
        this.defultValue = defaultValue;
    }

    public Column(Field field, Table table) {
        this.field = field;
        if(field.isAnnotationPresent(ColumnName.class)) {
            this.name = ((ColumnName)field.getAnnotation(ColumnName.class)).value();
        } else {
            this.name = field.getName();
        }

        if(field.isAnnotationPresent(Default.class)) {
            this.defultValue = ((Default)field.getAnnotation(Default.class)).value();
        } else {
            this.defultValue = null;
        }

        this.uniqueName = String.format("%s.%s", new Object[]{table.getName(), this.name});
        this.fieldType = DataTypes.getFieldType(field.getType());
        if(this.fieldType == 100) {
            Log.w("MSQLite", field.getType().getSimpleName() + " is not supported as a table field yet. Consider making this field transient.");
            this.dataType = null;
        } else {
            if(field.isAnnotationPresent(DataType.class)) {
                this.dataType = ((DataType)field.getAnnotation(DataType.class)).value();
            } else {
                this.dataType = DataTypes.getDataType(this.fieldType);
            }

            this.PRIMARY_KEY = field.isAnnotationPresent(PrimaryKey.class);
            this.NOT_NULL = field.isAnnotationPresent(NotNull.class);
        }
    }

    public String getDataType() {
        return this.dataType;
    }

    public CharSequence getBuilder() {
        StringBuilder builder = (new StringBuilder()).append('`').append(this.name).append('`').append(' ').append(this.dataType);
        if(this.defultValue != null) {
            builder.append(" DEFAULT \'").append(this.defultValue).append("\'");
        }

        if(this.NOT_NULL) {
            builder.append(" NOT");
        }

        builder.append(" NULL");
        return builder;
    }

    public String toString() {
        return this.uniqueName;
    }

    public String getName() {
        return this.name;
    }

    public int hashCode() {
        return this.uniqueName.hashCode();
    }

    public boolean equals(Object obj) {
        return obj instanceof Column?this.uniqueName.equals(((Column)obj).uniqueName):super.equals(obj);
    }

    public Object getValue(Object object) throws IllegalArgumentException, NoSuchFieldException {
        try {
            Object e = this.field.get(object);
            return e instanceof Boolean?Integer.valueOf(((Boolean)e).booleanValue()?1:0):e;
        } catch (IllegalAccessException var4) {
            this.field.setAccessible(true);
            Object value = this.getValue(object);
            this.field.setAccessible(false);
            return value;
        }
    }

    public void setValue(Object object, Object value) {
        try {
            this.field.set(object, value);
        } catch (IllegalAccessException var4) {
            this.field.setAccessible(true);
            this.setValue(object, value);
            this.field.setAccessible(false);
        }

    }

    public int getFieldType() {
        return this.fieldType;
    }

    public boolean isPRIMARY_KEY() {
        return this.PRIMARY_KEY;
    }

    public void setValue(Object object, Cursor cursor, int columnId) {
        if(cursor.isNull(columnId)) {
            this.setValue(object, (Object)null);
        }

        switch(this.fieldType) {
            case 101:
                this.setValue(object, cursor.getString(columnId));
                break;
            case 103:
                byte[] bytes = cursor.getBlob(columnId);

                try {
                    this.setValue(object, SerializationUtils.deserialize(bytes));
                    break;
                } catch (Exception var6) {
                    throw new RuntimeException(var6);
                }
            case 201:
                this.setValue(object, Integer.valueOf(cursor.getInt(columnId)));
                break;
            case 202:
                this.setValue(object, Long.valueOf(cursor.getLong(columnId)));
                break;
            case 203:
                this.setValue(object, Short.valueOf(cursor.getShort(columnId)));
                break;
            case 204:
                this.setValue(object, Byte.valueOf((byte)cursor.getShort(columnId)));
                break;
            case 205:
                this.setValue(object, Character.valueOf((char)cursor.getShort(columnId)));
                break;
            case 206:
                this.setValue(object, Boolean.valueOf(cursor.getShort(columnId) != 0));
                break;
            case 306:
                this.setValue(object, Float.valueOf((float)cursor.getDouble(columnId)));
                break;
            case 307:
                this.setValue(object, Double.valueOf(cursor.getDouble(columnId)));
        }

    }

    protected void setIntegerValue(Object instance, long number) {
        switch(this.fieldType) {
            case 201:
                this.setValue(instance, Integer.valueOf((int)number));
                break;
            case 202:
                this.setValue(instance, Long.valueOf(number));
                break;
            case 203:
                this.setValue(instance, Short.valueOf((short)((int)number)));
                break;
            case 204:
                this.setValue(instance, Byte.valueOf((byte)((int)number)));
                break;
            case 205:
                this.setValue(instance, Character.valueOf((char)((int)number)));
                break;
            default:
                throw new IllegalArgumentException("Invalid integer field type");
        }

    }
}
