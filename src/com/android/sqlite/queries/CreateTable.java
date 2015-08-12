//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.android.sqlite.queries;

import android.util.Log;

import com.android.sqlite.models.Column;
import com.android.sqlite.models.Table;

import java.util.Iterator;
import java.util.List;

public class CreateTable extends QueryBuilder {
    private boolean IF_NOT_EXISTS = false;

    public CreateTable(Class<?> type) {
        super(type);
    }

    public CreateTable(Table table) {
        super(table);
    }

    public CreateTable setIF_NOT_EXIST(boolean iF_NOT_EXIST) {
        this.IF_NOT_EXISTS = iF_NOT_EXIST;
        return this;
    }

    public String build() {
        Table table = this.getTable();
        StringBuilder builder = new StringBuilder("CREATE TABLE ");
        if (this.IF_NOT_EXISTS) {
            builder.append(" IF NOT EXISTS");
        }

        builder.append('`').append(table.getName()).append('`').append(" (");
        int n = 0;

        Column primaryKeys;
        for (Iterator column = table.getColumns().iterator(); column.hasNext(); builder.append('\n').append(primaryKeys.getBuilder())) {
            primaryKeys = (Column) column.next();
            if (n++ > 0) {
                builder.append(',');
            }
        }

        n = 0;
        List var7 = table.getPrimaryKeys();
        if (!var7.isEmpty()) {
            builder.append(",\nPRIMARY KEY (");

            Column var8;
            for (Iterator var6 = var7.iterator(); var6.hasNext(); builder.append('`').append(var8.getName()).append('`')) {
                var8 = (Column) var6.next();
                if (n++ > 0) {
                    builder.append(", ");
                }
            }

            builder.append(")");
        }

        builder.append("\n);");
        Log.d("QueryBuilder", builder.toString());
        return builder.toString();
    }
}