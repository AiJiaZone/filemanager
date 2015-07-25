//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.android.sqlite.models;

import android.content.ContentValues;
import android.database.Cursor;
import com.android.sqlite.SerializationUtils;
import com.android.sqlite.Annotations.TableName;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class Table {
    protected final String name;
    protected final List<Column> columns;
    protected final List<Column> primaryKeys;

    public static Table fromCursor(String tableName, Cursor cursor) {
        Table result = new Table(tableName, new ArrayList(cursor.getCount()));

        while(cursor.moveToNext()) {
            Column column = Column.fromCursor(cursor, result);
            result.columns.add(column);
            if(column.isPRIMARY_KEY()) {
                result.primaryKeys.add(column);
            }
        }

        return result;
    }

    public List<String> upgradeTable(Table upgradeFrom) {
        ArrayList result = new ArrayList();
        Iterator var4 = this.columns.iterator();

        while(var4.hasNext()) {
            Column column = (Column)var4.next();
            if(!upgradeFrom.columns.contains(column)) {
                StringBuilder resultBuilder = (new StringBuilder("Alter table `")).append(this.name).append("` ADD COLUMN ").append(column.getBuilder()).append(';');
                result.add(resultBuilder.toString());
            }
        }

        return result;
    }

    public static List<String> upgradeTable(Table currentTable, Table newTable) {
        return newTable.upgradeTable(currentTable);
    }

    private Table(String name, List<Column> columns) {
        this.name = name;
        this.columns = columns;
        this.primaryKeys = new ArrayList(1);
    }

    public Table(Class<?> type) {
        if(type.isAnnotationPresent(TableName.class)) {
            this.name = ((TableName)type.getAnnotation(TableName.class)).value();
        } else {
            this.name = type.getSimpleName();
        }

        ArrayList fields = new ArrayList();
        getAllFields(type, fields);
        this.columns = new ArrayList(fields.size());
        this.primaryKeys = new ArrayList(1);
        Iterator var4 = fields.iterator();

        while(var4.hasNext()) {
            Field field = (Field)var4.next();
            if((field.getModifiers() & 152) == 0) {
                Column column = new Column(field, this);
                if(column.getDataType() != null) {
                    this.columns.add(column);
                    if(column.PRIMARY_KEY) {
                        this.primaryKeys.add(column);
                    }
                }
            }
        }

    }

    private static void getAllFields(Class<?> type, List<Field> fields) {
        if(type != null) {
            fields.addAll(Arrays.asList(type.getDeclaredFields()));
            getAllFields(type.getSuperclass(), fields);
        }
    }

    public Column getIntegerPrimaryKey() {
        Column primaryKey = this.getPrimaryKey();
        return primaryKey != null && primaryKey.getDataType() == "INTEGER"?primaryKey:null;
    }

    public void setRowID(Object object, long id) {
        Column primaryKey = this.getIntegerPrimaryKey();
        if(primaryKey != null && id != -1L) {
            primaryKey.setIntegerValue(object, id);
        }

    }

    public String getName() {
        return this.name;
    }

    public List<Column> getColumns() {
        return this.columns;
    }

    public int getNumColumns() {
        return this.columns.size();
    }

    public List<Column> getPrimaryKeys() {
        return this.primaryKeys;
    }

    public Column getPrimaryKey() {
        return this.primaryKeys.size() == 1?(Column)this.primaryKeys.get(0):null;
    }

    public ContentValues getContentValues(Object object) {
        return this.getContentValues(object, (Collection)null);
    }

    public ContentValues getContentValues(Object object, Collection<String> colNames) {
        ContentValues values = new ContentValues(this.columns.size());
        Iterator var5 = this.columns.iterator();

        while(true) {
            Column column;
            do {
                if(!var5.hasNext()) {
                    return values;
                }

                column = (Column)var5.next();
            } while(colNames != null && !colNames.contains(column.name));

            try {
                Object value = column.getValue(object);
                if(value == null) {
                    values.putNull(column.name);
                } else if(column.getFieldType() == 103) {
                    values.put(column.name, SerializationUtils.serialize(value));
                } else {
                    values.put(column.name, value.toString());
                }
            } catch (NoSuchFieldException var8) {
                var8.printStackTrace();
                values.putNull(column.name);
            } catch (IOException var9) {
                throw new RuntimeException(var9);
            }
        }
    }

    private String getWhereClause(List<Column> columns) {
        StringBuilder builder = new StringBuilder();
        String glue = "";

        for(Iterator var5 = columns.iterator(); var5.hasNext(); glue = " AND ") {
            Column col = (Column)var5.next();
            builder.append(glue).append('`').append(col.name).append('`').append('=').append('?');
        }

        return builder.toString();
    }

    private String[] getWhereArgs(List<Column> columns, Object object) {
        String[] result = new String[columns.size()];

        for(int i = 0; i < result.length; ++i) {
            try {
                Object e = ((Column)columns.get(i)).getValue(object);
                if(e == null) {
                    result[i] = null;
                } else {
                    result[i] = e.toString();
                }
            } catch (IllegalArgumentException var6) {
                var6.printStackTrace();
            } catch (NoSuchFieldException var7) {
                var7.printStackTrace();
            }
        }

        return result;
    }

    public String getPrimaryWhereClause() {
        return this.getWhereClause(this.primaryKeys);
    }

    public String getFullWhereClause() {
        return this.getWhereClause(this.columns);
    }

    public String[] getPrimaryWhereArgs(Object object) {
        return this.getWhereArgs(this.primaryKeys, object);
    }

    public String[] getFullWhereArgs(Object object) {
        return this.getWhereArgs(this.columns, object);
    }

    public <T> T getRow(Cursor cursor, Class<T> type) {
        Object result = null;

        try {
            result = type.newInstance();
        } catch (IllegalAccessException var7) {
            var7.printStackTrace();
            return null;
        } catch (InstantiationException var8) {
            throw new RuntimeException(var8);
        }

        boolean columnId = true;
        Iterator var6 = this.columns.iterator();

        while(var6.hasNext()) {
            Column column = (Column)var6.next();
            int columnId1;
            if((columnId1 = cursor.getColumnIndex(column.name)) != -1) {
                column.setValue(result, cursor, columnId1);
            }
        }

        return (T)result;
    }

    /** @deprecated */
    @Deprecated
    public Object[] getValues(Object object) {
        Object[] result = new Object[this.columns.size()];
        int n = 0;

        for(Iterator var5 = this.columns.iterator(); var5.hasNext(); ++n) {
            Column column = (Column)var5.next();

            try {
                result[n] = object.getClass().getField(column.getName()).get(object);
            } catch (Exception var7) {
                var7.printStackTrace();
                result[n] = null;
            }
        }

        return null;
    }
}
