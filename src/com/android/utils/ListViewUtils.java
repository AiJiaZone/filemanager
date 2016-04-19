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
package com.android.utils;

import android.app.Activity;
import android.view.View;
import android.widget.AbsListView;

import com.readystatesoftware.systembartint.SystemBarTintManager;

public class ListViewUtils {
    /**
     * Add header to listview to compensate for translucent navbar and system bar
     */
    public static void addListViewHeader(AbsListView listView, Activity activity) {
        addListViewPadding(listView, activity, false);
    }

    /**
     * Add padding to listview to compensate for translucent navbar and system bar
     */
    public static void addListViewPadding(AbsListView listView, Activity activity, boolean ignoreRightInset) {
        SystemBarTintManager systemBarTintManager = new SystemBarTintManager(activity);
        int headerHeight = systemBarTintManager.getConfig().getPixelInsetTop(true);
        int footerHeight = systemBarTintManager.getConfig().getPixelInsetBottom();
        int paddingRight = systemBarTintManager.getConfig().getPixelInsetRight();
        listView.setPadding(listView.getPaddingLeft(), headerHeight, ignoreRightInset ? listView.getPaddingRight() : paddingRight, footerHeight);
    }

    public static void addListViewPadding(View listView, Activity activity, boolean ignoreRightInset) {
        SystemBarTintManager systemBarTintManager = new SystemBarTintManager(activity);
        int headerHeight = systemBarTintManager.getConfig().getPixelInsetTop(true);
        int footerHeight = systemBarTintManager.getConfig().getPixelInsetBottom();
        int paddingRight = systemBarTintManager.getConfig().getPixelInsetRight();
        listView.setPadding(listView.getPaddingLeft(), headerHeight, ignoreRightInset ? listView.getPaddingRight() : paddingRight, footerHeight);
    }
}
