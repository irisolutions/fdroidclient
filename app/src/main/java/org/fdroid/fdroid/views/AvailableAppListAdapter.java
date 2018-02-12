package org.fdroid.fdroid.views;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import org.fdroid.fdroid.views.fragments.AppListFragment;

public class AvailableAppListAdapter extends AppListAdapter {

    public static AvailableAppListAdapter create(Context context, Cursor cursor, int flags) {
        if (Build.VERSION.SDK_INT >= 11) {
            return new AvailableAppListAdapter(context, cursor, flags);
        }
        return new AvailableAppListAdapter(context, cursor);
    }

    private AvailableAppListAdapter(Context context, Cursor c) {
        super(context, c);
    }

    public AvailableAppListAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    private AvailableAppListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    protected boolean showStatusUpdate() {
        return true;
    }

    @Override
    protected boolean showStatusInstalled() {
        return true;
    }
}
