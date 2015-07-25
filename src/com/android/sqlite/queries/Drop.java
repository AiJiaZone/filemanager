//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.android.sqlite.queries;

import com.android.sqlite.queries.QueryBuilder;

public class Drop extends QueryBuilder {
    private boolean ifExists = false;

    public Drop(Class<?> type) {
        super(type);
    }

    public String build() {
        StringBuilder builder = new StringBuilder("DROP TABLE ");
        if(this.ifExists) {
            builder.append("IF EXISTS ");
        }

        builder.append('`').append(this.getTable().getName()).append('`').append(';');
        return builder.toString();
    }

    public Drop setIfExists(boolean ifNotExists) {
        this.ifExists = ifNotExists;
        return this;
    }
}
