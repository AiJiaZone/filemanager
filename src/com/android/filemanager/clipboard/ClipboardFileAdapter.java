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
package com.android.filemanager.clipboard;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.android.filemanager.BaseFileAdapter;
import com.android.filemanager.R;
import com.android.utils.FileIconResolver;

public class ClipboardFileAdapter extends BaseFileAdapter {
    final Clipboard clipboard;

    public ClipboardFileAdapter(Context context, Clipboard clipboard, FileIconResolver fileIconResolver) {
        super(context, R.layout.list_item_file, clipboard.getFilesList(), fileIconResolver);
        this.clipboard = clipboard;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TOOD: implement paste icon
        return super.getView(position, convertView, parent);
    }

}
