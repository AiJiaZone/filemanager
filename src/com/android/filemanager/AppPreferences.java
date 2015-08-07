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
package com.android.filemanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.android.utils.FileManagerConstants;
import com.android.utils.FileUtils;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.Comparator;

public class AppPreferences {
    public static final int
            SORT_BY_NAME = 0,
            SORT_BY_TYPE = 1,
            SORT_BY_SIZE = 2;
    public static final int
            CARD_LAYOUT_MEDIA = 0,
            CARD_LAYOUT_ALWAYS = 1,
            CARD_LAYOUT_NEVER = 2;

    public static final int TYPE_LIST = 1;
    public static final int TYPE_GRID = TYPE_LIST << 1;
    private static final String
            NAME = "FileExplorerPreferences",
            PREF_START_FOLDER = "start_folder",
            PREF_CARD_LAYOUT = "card_layout",
            PREF_SORT_BY = "sort_by",
            PREF_SHOW_TYPE = "show_type";
    private final static int DEFAULT_SORT_BY = SORT_BY_NAME;

    File mStartFolder = null;
    int mSortBy = SORT_BY_NAME;
    int mCardLayout = CARD_LAYOUT_MEDIA;

    int mShowType = TYPE_GRID;

    private static Context mContext = null;

    private AppPreferences() {
    }

    public static AppPreferences loadPreferences(Context context) {
        mContext = context;
        AppPreferences instance = new AppPreferences();
        instance.loadFromSharedPreferences(context.getSharedPreferences(NAME, Context.MODE_PRIVATE));
        return instance;
    }

    private void loadFromSharedPreferences(SharedPreferences sharedPreferences) {
        if (FileManagerConstants.DISABLE_SAVE_LASTFILE) {
            mStartFolder = Environment.getExternalStorageDirectory();
        } else {
            String startPath = sharedPreferences.getString(PREF_START_FOLDER, null);
            if (startPath == null) {
                if (Environment.getExternalStorageDirectory().list() != null) {
                    mStartFolder = Environment.getExternalStorageDirectory();
                } else {
                    mStartFolder = new File("/");
                }
            } else {
                this.mStartFolder = new File(startPath);
            }
        }
        this.mSortBy = sharedPreferences.getInt(PREF_SORT_BY, DEFAULT_SORT_BY);
        this.mCardLayout = sharedPreferences.getInt(PREF_CARD_LAYOUT, CARD_LAYOUT_MEDIA);
        mShowType = sharedPreferences.getInt(PREF_SHOW_TYPE, TYPE_GRID);
    }

    private void saveToSharedPreferences(SharedPreferences sharedPreferences) {
        sharedPreferences.edit()
                .putString(PREF_START_FOLDER, mStartFolder.getAbsolutePath())
                .putInt(PREF_SORT_BY, mSortBy)
                .putInt(PREF_CARD_LAYOUT, mCardLayout)
                .putInt(PREF_SHOW_TYPE, mShowType)
                .apply();
    }

    public void saveChangesAsync(final Context context) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                saveChanges(context);

            }
        }).run();
    }

    public void saveChanges(Context context) {
        saveToSharedPreferences(context.getSharedPreferences(NAME, Context.MODE_PRIVATE));
    }

    public int getCardLayout() {
        return mCardLayout;
    }

    public void setCardLayout(int cardLayout) {
        this.mCardLayout = cardLayout;
    }

    public int getSortBy() {
        return mSortBy;
    }

    public AppPreferences setSortBy(int sortBy) {
        if (sortBy < 0 || sortBy > 2)
            throw new InvalidParameterException(String.valueOf(sortBy) + " is not a valid id of sorting order");

        this.mSortBy = sortBy;
        return this;
    }

    public void setShowType(int showType) {
        mShowType = showType;
        saveChangesAsync(mContext);
    }
    public int getShowType(){
        return mShowType;
    }

    public File getStartFolder() {
        if (mStartFolder.exists() == false)
            mStartFolder = new File("/");
        return mStartFolder;
    }

    public AppPreferences setStartFolder(File mStartFolder) {
        this.mStartFolder = mStartFolder;
        return this;
    }

    public Comparator<File> getFileSortingComparator() {
        switch (mSortBy) {
            case SORT_BY_SIZE:
                return new FileUtils.FileSizeComparator();

            case SORT_BY_TYPE:
                return new FileUtils.FileExtensionComparator();

            default:
                return new FileUtils.FileNameComparator();
        }
    }
}
