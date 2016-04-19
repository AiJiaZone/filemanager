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
import android.os.Environment;

import com.android.filemanager.sqlite.SQLiteHelper;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavouritesManager {
    private final List<FavouriteFolder> mFolders;
    private final SQLiteHelper sqLiteHelper;
    private final Set<FavouritesListener> favouritesListeners;
    private Context mContext = null;

    public FavouritesManager(Context context) {
        this.sqLiteHelper = new SQLiteHelper(context);
        this.favouritesListeners = new HashSet<FavouritesManager.FavouritesListener>();
        this.mFolders = sqLiteHelper.selectAll(FavouriteFolder.class);
        mContext = context;
        sort();
        fixFavouritesOrder();
    }

    public void sort() {
        Collections.sort(mFolders);
    }

    private void fixFavouritesOrder() {
        int lastOrder = 0;
        for (FavouriteFolder folder : mFolders) {
            if (folder.hasValidOrder() == false || folder.getOrder() <= lastOrder) {
                folder.setOrder(lastOrder + 1);
            }

            lastOrder = folder.getOrder();
        }
    }

    public void addFavouritesListener(FavouritesListener favouritesListener) {
        favouritesListeners.add(favouritesListener);
    }

    public void removeFavouritesListener(FavouritesListener favouritesListener) {
        favouritesListeners.remove(favouritesListener);
    }

    void notifyListeners() {
        for (FavouritesListener listener : favouritesListeners) {
            listener.onFavouritesChanged(this);
        }
    }

    public List<FavouriteFolder> getFolders() {
        return mFolders;
    }

    public void addFavourite(FavouriteFolder favouriteFolder) throws FolderAlreadyFavouriteException {
        long id = sqLiteHelper.insert(favouriteFolder);
        if (id == -1) throw new FolderAlreadyFavouriteException(favouriteFolder);
        mFolders.add(favouriteFolder);
        notifyListeners();
    }

    public void removeFavourite(File file) {
        for (FavouriteFolder folder : mFolders) {
            if (folder.equals(file) && folder.isRemovable()) {
                removeFavourite(folder);
                break;
            }
        }
    }

    public void removeFavourite(FavouriteFolder favouriteFolder) {
        mFolders.remove(favouriteFolder);
        sqLiteHelper.delete(favouriteFolder);
        notifyListeners();
    }

    public boolean isFolderFavourite(File file) {
        for (FavouriteFolder folder : mFolders) {
            if (folder.equals(file))
                return true;
        }
        return false;
    }

    public boolean canRemoved(File file) {
        if (Environment.getExternalStorageDirectory().equals(file)) {
            return false;
        }
        return true;
    }

    public interface FavouritesListener {
        void onFavouritesChanged(FavouritesManager favouritesManager);
    }

    public static class FolderAlreadyFavouriteException extends Exception {

        public FolderAlreadyFavouriteException(FavouriteFolder folder) {
            super(folder.toString() + " is already bookmarked");
        }
    }

}
