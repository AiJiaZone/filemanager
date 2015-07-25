package com.android.sqlite;

/**
 * Created by john on 7/25/15.
 */

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Annotations {
    public Annotations() {
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface ColumnName {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface DataType {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Default {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface NotNull {
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface PrimaryKey {
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface TableName {
        String value();
    }
}
