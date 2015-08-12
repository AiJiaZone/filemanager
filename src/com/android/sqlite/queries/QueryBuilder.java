//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.android.sqlite.queries;

import com.android.sqlite.models.Table;

public abstract class QueryBuilder {
    protected Table table;
    protected String condition = "1";

    /**
     * @deprecated
     */
    @Deprecated
    public QueryBuilder(Class<?> type) {
        this.table = new Table(type);
    }

    public QueryBuilder(Table table) {
        this.table = table;
    }

    public Table getTable() {
        return this.table;
    }

    public abstract String build();

    public QueryBuilder setCondition(String conditions) {
        this.condition = conditions;
        return this;
    }
}
