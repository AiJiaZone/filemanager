/**
 * ****************************************************************************
 * Copyright (c) 2014 Michal Dabski
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * ****************************************************************************
 */
package com.android.filemanager.favourites;

import android.content.Context;

import com.android.filemanager.nav_drawer.NavDrawerShortcut;
import com.android.sqlite.Annotations.ColumnName;
import com.android.sqlite.Annotations.PrimaryKey;

import java.io.File;

public class FavouriteFolder extends NavDrawerShortcut implements Comparable<FavouriteFolder> {
    @ColumnName("item_order")
    Integer order;
    private String label;
    @PrimaryKey
    private String path;
    private boolean mRemovable = false;

    public FavouriteFolder() {

    }

    public FavouriteFolder(File folder, String label, boolean canRemoved) {
        this();
        if (folder.isDirectory() == false)
            throw new RuntimeException(folder.getName() + " is not a directory");
        this.path = folder.getAbsolutePath();
        this.label = label;
        mRemovable = canRemoved;
    }

    public FavouriteFolder(File folder) {
        this(folder, folder.getName(), false);
    }

    public FavouriteFolder(String path, String label) {
        this(new File(path), label, false);
    }

    public File getFile() {
        return new File(path);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FavouriteFolder)
            return ((FavouriteFolder) o).path.equals(path);
        else if (o instanceof File)
            return o.equals(getFile());
        return super.equals(o);
    }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public int hashCode() {
        return getFile().hashCode();
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean hasValidOrder() {
        return order != null;
    }

    public boolean exists() {
        return getFile().exists();
    }

    public void setCanRemoved(boolean canRemoved) {
        mRemovable = canRemoved;
    }

    public boolean isRemovable() {
        return mRemovable;
    }

    @Override
    public CharSequence getTitle(Context context) {
        return label;
    }

    @Override
    public int compareTo(FavouriteFolder another) {
        if (order == null)
            return -1;
        if (another.order == null)
            return 1;
        return order.compareTo(another.order);
    }

}
