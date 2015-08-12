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
package com.android.filemanager.folders;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.android.filemanager.AppPreferences;
import com.android.filemanager.FileManagerApplication;
import com.android.filemanager.R;
import com.android.filemanager.clipboard.Clipboard;
import com.android.filemanager.clipboard.FileOperationListener;
import com.android.filemanager.favourites.FavouriteFolder;
import com.android.filemanager.favourites.FavouritesManager;
import com.android.utils.AsyncResult;
import com.android.utils.FilePreviewCache;
import com.android.utils.FileUtils;
import com.android.utils.FontApplicator;
import com.android.utils.IntentUtils;
import com.android.utils.ListViewUtils;
import com.android.utils.OnResultListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class FolderFragment extends Fragment implements
        OnItemClickListener, OnScrollListener,
        OnItemLongClickListener, MultiChoiceModeListener,
        FileAdapter.OnFileSelectedListener {
    public static final String EXTRA_DIR = "directory";
    public static final String EXTRA_SELECTED_FILES = "selected_files";
    public static final String EXTRA_SCROLL_POSITION = "scroll_position";
    private static final String LOG_TAG = "FolderFragment";
    private final int DISTANCE_TO_HIDE_ACTIONBAR = 0;
    private final HashSet<File> mSelectedLists = new HashSet<File>();
    File mCurrentDir = null;
    File mNextDir = null;
    int mTopVisItem = 0;
    List<File> mFiles = null;
    @SuppressWarnings("rawtypes")
    AsyncTask mLoadTask = null;
    AbsListView mListView = null;
    FileAdapter mAdapter;
    // set to true when selection shouldn't be cleared from switching out fragments
    boolean mPreserveSelection = false;
    FilePreviewCache mThumbCache;
    private ActionMode mActionMode = null;
    private ShareActionProvider mShareProvider;
    private AppPreferences mAppPreferences = null;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (savedInstanceState != null) {
            mTopVisItem = savedInstanceState.getInt(EXTRA_SCROLL_POSITION, 0);
            mSelectedLists.addAll((HashSet<File>) savedInstanceState.getSerializable(EXTRA_SELECTED_FILES));
        }

        mAppPreferences = getPreferences();
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(EXTRA_DIR)) {
            mCurrentDir = new File(arguments.getString(EXTRA_DIR));
        } else {
            mCurrentDir = mAppPreferences.getStartFolder();
        }

        loadFileList();
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    public AbsListView getListView() {
        return mListView;
    }

    private void setListAdapter(FileAdapter fileAdapter) {
        this.mAdapter = fileAdapter;
        if (mListView != null) {
            mListView.setAdapter(fileAdapter);
            mListView.setSelection(mTopVisItem);

            getView().findViewById(R.id.layoutMessage).setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        }
    }

    FontApplicator getFontApplicator() {
        FolderActivity folderActivity = (FolderActivity) getActivity();
        return folderActivity.getFontApplicator();
    }

    void showProgress() {
        if (getView() != null) {
            mListView.setVisibility(View.GONE);
            getView().findViewById(R.id.layoutMessage).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.text_view).setVisibility(View.GONE);
        }
    }

    FileManagerApplication getApplication() {
        if (getActivity() == null) {
            return null;
        }
        return (FileManagerApplication) getActivity().getApplication();
    }

    AppPreferences getPreferences() {
        if (getApplication() == null) {
            return null;
        }
        return getApplication().getAppPreferences();
    }

    void showMessage(CharSequence message) {
        View view = getView();
        if (view != null) {
            getListView().setVisibility(View.GONE);
            view.findViewById(R.id.layoutMessage).setVisibility(View.VISIBLE);
            view.findViewById(R.id.progress).setVisibility(View.GONE);
            TextView tvMessage = (TextView) view.findViewById(R.id.text_view);
            tvMessage.setText(message);

        }
    }

    void showMessage(int message) {
        showMessage(getString(message));
    }

    void showList() {
        getListView().setVisibility(View.VISIBLE);
        getView().findViewById(R.id.layoutMessage).setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        this.mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((GridView) mListView).setNumColumns(mAppPreferences.getShowType());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mListView.setFastScrollAlwaysVisible(true);
        }
        return view;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mThumbCache != null) {
            if (getView() == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mThumbCache.evictAll();
            } else {
                mThumbCache.trimToSize(1024 * 1024);
            }
        }
    }

    void loadFileList() {
        if (mLoadTask != null) {
            return;
        }

        mLoadTask = new AsyncTask<File, Void, AsyncResult<File[]>>() {
            @Override
            protected AsyncResult<File[]> doInBackground(File... params) {
                try {
                    File[] files = params[0].listFiles(FileUtils.DEFAULT_FILE_FILTER);
                    if (files == null) {
                        throw new NullPointerException(
                                getString(R.string.cannot_read_directory, params[0].getName()));
                    }
                    if (isCancelled()) {
                        throw new Exception("Task cancelled");
                    }
                    Arrays.sort(files, mAppPreferences.getFileSortingComparator());
                    return new AsyncResult<File[]>(files);
                } catch (Exception e) {
                    return new AsyncResult<File[]>(e);
                }
            }

            @Override
            protected void onCancelled(AsyncResult<File[]> result) {
                mLoadTask = null;
            }

            @Override
            protected void onPostExecute(AsyncResult<File[]> result) {
                Log.d("folder fragment", "Task finished");
                mLoadTask = null;

                try {
                    mFiles = Arrays.asList(result.getResult());

                    if (mFiles.isEmpty()) {
                        showMessage(R.string.folder_empty);
                        return;
                    }
                    FileAdapter adapter;
                    final int cardPreference = mAppPreferences.getCardLayout();
                    if (cardPreference == AppPreferences.CARD_LAYOUT_ALWAYS ||
                            (cardPreference == AppPreferences.CARD_LAYOUT_MEDIA &&
                                    FileUtils.isMediaDirectory(mCurrentDir))) {
                        if (mThumbCache == null) {
                            mThumbCache = new FilePreviewCache();
                        }
                        adapter = new FileCardAdapter(getActivity(), mFiles, mThumbCache,
                                getApplication().getFileIconResolver());
                    } else {
                        adapter = new FileAdapter(getActivity(), mFiles,
                                getApplication().getFileIconResolver());
                    }
                    adapter.setSelectedFiles(mSelectedLists);
                    adapter.setOnFileSelectedListener(FolderFragment.this);
                    adapter.setFontApplicator(getFontApplicator());
                    setListAdapter(adapter);

                } catch (Exception e) {
                    // exception was thrown while loading files
                    showMessage(e.getMessage());
                    //adapter = new FileAdapter(getActivity(), getApplication().getFileIconResolver());
                }

                getActivity().invalidateOptionsMenu();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mCurrentDir);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.folder_browser, menu);

        menu.findItem(R.id.menu_selectAll).setVisible(!(mFiles == null || mFiles.isEmpty()));

        FavouritesManager fm = getApplication().getFavouritesManager();
        if (fm.isFolderFavourite(mCurrentDir)) {
            menu.findItem(R.id.menu_unfavourite).setVisible(fm.canRemoved(mCurrentDir) ? true : false);
            menu.findItem(R.id.menu_favourite).setVisible(false);
        } else {
            menu.findItem(R.id.menu_unfavourite).setVisible(false);
            menu.findItem(R.id.menu_favourite).setVisible(true);
        }

        int showType = mAppPreferences.getShowType();
        if (showType == AppPreferences.TYPE_GRID) {
            menu.findItem(R.id.menu_gridview).setVisible(false);
            menu.findItem(R.id.menu_listview).setVisible(true);
        } else {
            menu.findItem(R.id.menu_gridview).setVisible(true);
            menu.findItem(R.id.menu_listview).setVisible(false);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_paste).setVisible(Clipboard.getInstance().isEmpty() == false);
        menu.findItem(R.id.menu_navigate_up).setVisible(mCurrentDir.getParentFile() != null);
    }

    void showEditTextDialog(int title, int okButtonText, final OnResultListener<CharSequence> enteredTextResult,
                            CharSequence hint, CharSequence defaultValue) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_edittext,
                (ViewGroup) getActivity().getWindow().getDecorView(), false);
        final EditText editText = (EditText) view.findViewById(android.R.id.edit);
        editText.setHint(hint);
        editText.setText(defaultValue);

        if (TextUtils.isEmpty(defaultValue) == false) {
            int end = defaultValue.toString().indexOf('.');
            if (end > 0) {
                editText.setSelection(0, end);
            }
        }

        final Dialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(view)
                .setPositiveButton(okButtonText, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        enteredTextResult.onResult(new AsyncResult<CharSequence>(editText.getText()));
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.show();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_selectAll:
                selectFiles(this.mFiles);
                return true;

            case R.id.menu_navigate_up:
                String newFolder = mCurrentDir.getParent();
                if (newFolder != null) {
                    Bundle args = new Bundle(1);
                    args.putString(EXTRA_DIR, newFolder);
                    FolderFragment fragment = new FolderFragment();
                    fragment.setArguments(args);
                    FolderActivity activity = (FolderActivity) getActivity();
                    activity.showFragment(fragment);

                }
                return true;

            case R.id.menu_favourite:
                try {
                    final String directoryName = FileUtils.getFolderDisplayName(mCurrentDir);

                    FavouritesManager favouritesManager = getApplication().getFavouritesManager();
                    favouritesManager.addFavourite(new FavouriteFolder(mCurrentDir, directoryName, true));
                    getActivity().invalidateOptionsMenu();
                } catch (FavouritesManager.FolderAlreadyFavouriteException e1) {
                    e1.printStackTrace();
                }
                return true;

            case R.id.menu_unfavourite:
                FavouritesManager favouritesManager = getApplication().getFavouritesManager();
                favouritesManager.removeFavourite(mCurrentDir);
                getActivity().invalidateOptionsMenu();
                return true;

            case R.id.menu_create_folder:
                showEditTextDialog(R.string.create_folder, R.string.create, new OnResultListener<CharSequence>() {

                    @Override
                    public void onResult(AsyncResult<CharSequence> result) {
                        try {
                            String name = result.getResult().toString();
                            File newFolder = new File(mCurrentDir, name);
                            if (newFolder.mkdirs()) {
                                refreshFolder();
                                Toast.makeText(getActivity(),
                                        R.string.folder_created_successfully,
                                        Toast.LENGTH_SHORT)
                                        .show();
                                navigateTo(newFolder);
                            } else {
                                Toast.makeText(getActivity(),
                                        R.string.folder_created_failed,
                                        Toast.LENGTH_SHORT)
                                        .show();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, "", "");
                return true;

            case R.id.menu_paste:
                pasteFiles();
                return true;

            case R.id.menu_refresh:
                refreshFolder();
                return true;
            case R.id.menu_gridview:
                updateViewType(AppPreferences.TYPE_GRID);
                return true;
            case R.id.menu_listview:
                updateViewType(AppPreferences.TYPE_LIST);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateViewType(int type) {
        ((GridView) mListView).setNumColumns(type);
        mAppPreferences.setShowType(type);
        getActivity().invalidateOptionsMenu();
        mListView.invalidate();
    }

    public void pasteFiles() {
        new AsyncTask<Clipboard, Float, Exception>() {

            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle(getActivity().getString(R.string.pasting_files_));
                progressDialog.setIndeterminate(false);
                progressDialog.setCancelable(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.show();
            }

            @Override
            protected void onProgressUpdate(Float... values) {
                float progress = values[0];
                progressDialog.setMax(100);
                progressDialog.setProgress((int) (progress * 100));
            }

            @Override
            protected Exception doInBackground(Clipboard... params) {
                try {
                    final int total = FileUtils.countFilesIn(params[0].getFiles());
                    final int[] progress = {0};
                    params[0].paste(mCurrentDir, new FileOperationListener() {
                        @Override
                        public void onFileProcessed(String filename) {
                            progress[0]++;
                            publishProgress((float) progress[0] / (float) total);
                        }

                        @Override
                        public boolean isOperationCancelled() {
                            return isCancelled();
                        }
                    });
                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                    return e;
                }
            }

            @Override
            protected void onCancelled() {
                progressDialog.dismiss();
                refreshFolder();
            }

            @Override
            protected void onPostExecute(Exception result) {
                progressDialog.dismiss();
                refreshFolder();
                if (result == null) {
                    Clipboard.getInstance().clear();
                    Toast.makeText(getActivity(), R.string.files_pasted, Toast.LENGTH_SHORT).show();
                } else {
                    new AlertDialog.Builder(getActivity())
                            .setMessage(result.getMessage())
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Clipboard.getInstance());
    }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getFontApplicator().applyFont(view);

        loadFileList();

        if (mSelectedLists.isEmpty() == false) {
            selectFiles(mSelectedLists);
        }

        final String directoryName = FileUtils.getFolderDisplayName(mCurrentDir);
        getActivity().setTitle(directoryName);
        mListView.setOnItemClickListener(FolderFragment.this);
        mListView.setOnScrollListener(this);
        mListView.setOnItemLongClickListener(this);
        mListView.setMultiChoiceModeListener(this);
        getActivity().getActionBar().setSubtitle(FileUtils.getUserFriendlySdcardPath(mCurrentDir));

        if (mTopVisItem <= DISTANCE_TO_HIDE_ACTIONBAR) {
            setActionbarVisibility(true);
        }

        // add listview header to push items below the actionbar
        ListViewUtils.addListViewHeader(getListView(), getActivity());

        if (mAdapter != null) {
            setListAdapter(mAdapter);
        }

        FolderActivity activity = (FolderActivity) getActivity();
        activity.setLastFolder(mCurrentDir);
    }

    @Override
    public void onDestroyView() {
        finishActionMode(true);
        mListView = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (mLoadTask != null) {
            mLoadTask.cancel(true);
        }
        if (mThumbCache != null) {
            mThumbCache.evictAll();
        }
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_SCROLL_POSITION, mTopVisItem);
        outState.putSerializable(EXTRA_SELECTED_FILES, mSelectedLists);
    }

    void navigateTo(File folder) {
        mNextDir = folder;
        FolderActivity activity = (FolderActivity) getActivity();
        FolderFragment fragment = new FolderFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_DIR, folder.getAbsolutePath());
        fragment.setArguments(args);
        activity.showFragment(fragment);
    }

    void openFile(File file) {
        if (file.isDirectory()) {
            throw new IllegalArgumentException("File cannot be a directory!");
        }

        Intent intent = IntentUtils.createFileOpenIntent(file);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            startActivity(Intent.createChooser(intent, getString(R.string.open_file_with_, file.getName())));
        } catch (Exception e) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(e.getMessage())
                    .setTitle(R.string.error)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View arg1, int position, long arg3) {
        Object selectedObject = adapterView.getItemAtPosition(position);
        if (selectedObject instanceof File) {
            if (mActionMode == null) {
                File selectedFile = (File) selectedObject;
                if (selectedFile.isDirectory()) {
                    navigateTo(selectedFile);
                } else {
                    openFile(selectedFile);
                }
            } else {
                toggleFileSelected((File) selectedObject);
            }
        }
    }

    void setActionbarVisibility(boolean visible) {
        if (mActionMode == null || visible == true) // cannot hide CAB
        {
            ((FolderActivity) getActivity()).setActionbarVisible(visible);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem < this.mTopVisItem - DISTANCE_TO_HIDE_ACTIONBAR) {
            setActionbarVisibility(true);
            this.mTopVisItem = firstVisibleItem;
        } else if (firstVisibleItem > this.mTopVisItem + DISTANCE_TO_HIDE_ACTIONBAR) {
            setActionbarVisibility(false);
            this.mTopVisItem = firstVisibleItem;
        }

        ListAdapter adapter = view.getAdapter();
        if (adapter instanceof HeaderViewListAdapter) {
            HeaderViewListAdapter headerViewListAdapter = (HeaderViewListAdapter) adapter;
            if (headerViewListAdapter.getWrappedAdapter() instanceof FileCardAdapter) {
                int startPrefetch = firstVisibleItem + visibleItemCount - headerViewListAdapter.getHeadersCount();
                ((FileCardAdapter) headerViewListAdapter.getWrappedAdapter())
                        .prefetchImages(startPrefetch, visibleItemCount);
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
        setFileSelected((File) arg0.getItemAtPosition(arg2), true);
        return true;
    }

    void showFileInfo(Collection<File> files) {
        final CharSequence title;
        final StringBuilder message = new StringBuilder();
        if (files.size() == 1) {
            title = ((File) files.toArray()[0]).getName();
        } else {
            title = getString(R.string.multi_objects, files.size());
        }

        if (files.size() > 1) {
            message.append(FileUtils.combineFileNames(files)).append("\n\n");
        }
        message.append(getString(R.string.size_s, FileUtils.formatFileSize(files))).append('\n');
        message.append(getString(R.string.mime_type_s, FileUtils.getCollectiveMimeType(files)));

        new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                new AlertDialog.Builder(getActivity())
                        .setMessage(getString(R.string.delete_d_items_, mSelectedLists.size()))
                        .setPositiveButton(R.string.delete, new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int n = FileUtils.deleteFiles(mSelectedLists);
                                Toast.makeText(getActivity(), getString(R.string.multi_files_deleted, n),
                                        Toast.LENGTH_SHORT).show();
                                refreshFolder();
                                finishActionMode(false);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;

            case R.id.action_selectAll:
                if (isEverythingSelected()) {
                    clearFileSelection();
                } else {
                    selectFiles(mFiles);
                }
                return true;

            case R.id.action_info:
                if (mSelectedLists.isEmpty()) {
                    return true;
                }
                showFileInfo(mSelectedLists);
                return true;

            case R.id.action_copy:
                Clipboard.getInstance().addFiles(mSelectedLists, Clipboard.FileAction.Copy);
                Toast.makeText(getActivity(), R.string.objects_copied_to_clipboard, Toast.LENGTH_SHORT).show();
                finishActionMode(false);
                return true;

            case R.id.action_cut:
                Clipboard clipboard = Clipboard.getInstance();
                clipboard.addFiles(mSelectedLists, Clipboard.FileAction.Cut);
                Toast.makeText(getActivity(), R.string.objects_cut_to_clipboard, Toast.LENGTH_SHORT).show();
                finishActionMode(false);
                return true;

            case R.id.action_rename:
                final File fileToRename = (File) mSelectedLists.toArray()[0];
                showEditTextDialog(fileToRename.isDirectory() ? R.string.rename_folder : R.string.rename_file,
                        R.string.rename, new OnResultListener<CharSequence>() {

                            @Override
                            public void onResult(AsyncResult<CharSequence> result) {
                                try {
                                    String newName = result.getResult().toString();
                                    if (fileToRename.renameTo(new File(fileToRename.getParentFile(), newName))) {
                                        finishActionMode(false);
                                        refreshFolder();
                                        Toast.makeText(getActivity(), R.string.file_renamed, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getActivity(), getActivity()
                                                        .getString(R.string.file_could_not_be_renamed_to_s, newName),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        }, fileToRename.getName(), fileToRename.getName());
                return true;

            case R.id.menu_add_homescreen_icon:

                for (File file : mSelectedLists) {
                    IntentUtils.createShortcut(getActivity(), file);
                }
                Toast.makeText(getActivity(), R.string.shortcut_created, Toast.LENGTH_SHORT).show();
                mActionMode.finish();
                return true;
        }
        return false;
    }

    protected void refreshFolder() {
        showProgress();
        loadFileList();
    }

    void updateActionMode() {
        if (mActionMode != null) {
            mActionMode.invalidate();
            int count = mSelectedLists.size();
            mActionMode.setTitle(getString(R.string.multi_objects, count));

            mActionMode.setSubtitle(FileUtils.combineFileNames(mSelectedLists));

            if (mShareProvider != null) {
                final Intent shareIntent;
                if (mSelectedLists.isEmpty()) {
                    shareIntent = null;
                } else if (mSelectedLists.size() == 1) {
                    File file = (File) mSelectedLists.toArray()[0];
                    shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType(FileUtils.getFileMimeType(file));
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                } else {
                    ArrayList<Uri> fileUris = new ArrayList<Uri>(mSelectedLists.size());

                    for (File file : mSelectedLists) {
                        if (file.isDirectory() == false) {
                            fileUris.add(Uri.fromFile(file));
                        }
                    }
                    shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris);
                    shareIntent.setType(FileUtils.getCollectiveMimeType(mSelectedLists));
                }

                mShareProvider.setShareIntent(shareIntent);
            }
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        setActionbarVisibility(true);
        getActivity().getMenuInflater().inflate(R.menu.action_file, menu);
        getActivity().getMenuInflater().inflate(R.menu.action_file_single, menu);

        MenuItem shareMenuItem = menu.findItem(R.id.action_share);
        mShareProvider = (ShareActionProvider) shareMenuItem.getActionProvider();
        this.mPreserveSelection = false;
        return true;
    }

    void finishSelection() {
        if (mListView != null) {
            mListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
        }
        clearFileSelection();
    }

    void finishActionMode(boolean preserveSelection) {
        this.mPreserveSelection = preserveSelection;
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
        mShareProvider = null;
        if (mPreserveSelection == false) {
            finishSelection();
        }
        Log.d(LOG_TAG, "Action mode destroyed");
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        int count = mSelectedLists.size();
        if (count == 1) {
            menu.findItem(R.id.action_rename).setVisible(true);
            menu.findItem(R.id.menu_add_homescreen_icon).setTitle(R.string.add_to_homescreen);
        } else {
            menu.findItem(R.id.action_rename).setVisible(false);
            menu.findItem(R.id.menu_add_homescreen_icon).setTitle(R.string.add_to_homescreen_multiple);
        }

        // show Share button if no folder was selected
        boolean allowShare = (count > 0);
        if (allowShare) {
            for (File file : mSelectedLists) {
                if (file.isDirectory()) {
                    allowShare = false;
                    break;
                }
            }
        }
        menu.findItem(R.id.action_share).setVisible(allowShare);

        return true;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position,
                                          long id, boolean checked) {
    }

    void toggleFileSelected(File file) {
        setFileSelected(file, !mSelectedLists.contains(file));
    }

    void clearFileSelection() {
        if (mListView != null) {
            mListView.clearChoices();
        }
        mSelectedLists.clear();
        updateActionMode();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        Log.d(LOG_TAG, "Selection cleared");
    }

    boolean isEverythingSelected() {
        return mSelectedLists.size() == mFiles.size();
    }

    void selectFiles(Collection<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        if (mActionMode == null) {
            mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            mActionMode = getActivity().startActionMode(this);
        }

        mSelectedLists.addAll(files);
        updateActionMode();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    void setFileSelected(File file, boolean selected) {
        if (mActionMode == null) {
            mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            mActionMode = getActivity().startActionMode(this);
        }

        if (selected) {
            mSelectedLists.add(file);
        } else {
            mSelectedLists.remove(file);
        }
        updateActionMode();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }

        if (mSelectedLists.isEmpty()) {
            finishActionMode(false);
        }
    }

    @Override
    public void onFileSelected(File file) {
        toggleFileSelected(file);
    }
}
