//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.android.sqlite.models;

import java.util.List;

public class Result<T> {
    protected List<T> columns = null;
    protected int affectedRows = 0;
    protected String queryString;

    public Result(List<T> columns) {
        this.columns = columns;
        this.affectedRows = columns.size();
    }

    public Result(int affectedRows) {
        this.affectedRows = affectedRows;
    }

    public Result(String queryString) {
        this.queryString = queryString;
    }

    public String getQueryString() {
        return this.queryString;
    }
}
