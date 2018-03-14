package org.fdroid.fdroid.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.fdroid.fdroid.data.Schema.ApplicationTypeTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Khaled on 3/14/2018.
 * Assumptions
 * Descriptions
 */

public class ApplicatioTypeProvider extends FDroidProvider {

    private static final UriMatcher MATCHER = new UriMatcher(-1);
    private static final String PROVIDER_NAME = ApplicatioTypeProvider.class.getSimpleName();

    public static final int PATH_CODE = CODE_SINGLE + 1;

    private static final String PATH_App_NAME = "appName";
    private static final String PATH_ALL_APPS = "all";
    private static final String PATH_APP_ID = "appID";

    static {
        MATCHER.addURI(getAuthority(), PATH_App_NAME + "/*", CODE_SINGLE);
        MATCHER.addURI(getAuthority(), PATH_ALL_APPS, CODE_LIST);
    }

    public static String getAuthority() {
        return AUTHORITY + "." + PROVIDER_NAME;
    }

    private static Uri getContentUri() {
        return Uri.parse("content://" + getAuthority());
    }

    public static Uri getAllApps() {
        return Uri.withAppendedPath(getContentUri(), PATH_ALL_APPS);
    }

    private static Uri getAppIdUri(long appId) {
        return getContentUri()
                .buildUpon()
                .appendPath(PATH_APP_ID)
                .appendPath(Long.toString(appId))
                .build();
    }

    public static Uri getAppUri(String appName) {
        return getContentUri()
                .buildUpon()
                .appendPath(PATH_App_NAME)
                .appendPath(appName)
                .build();
    }

    @Override
    protected String getTableName() {
        return Schema.ApplicationTypeTable.NAME;
    }

    @Override
    protected String getProviderName() {
        return ApplicatioTypeProvider.class.getSimpleName();
    }

    @Override
    protected UriMatcher getMatcher() {
        return MATCHER;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String customSelection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        switch (MATCHER.match(uri)) {

            case CODE_SINGLE:
                customSelection = ApplicationTypeTable.Cols.PACKAGE_NAME + "=?";
                selectionArgs = new String[]{String.valueOf(uri.getLastPathSegment())};

                cursor = db().query(getTableName(), projection, customSelection, selectionArgs,
                        null, null, sortOrder);
                break;

            case CODE_LIST:
                cursor = db().query(getTableName(), projection, customSelection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Invalid URI for content provider: " + uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        long rowId = db().insertOrThrow(getTableName(), null, contentValues);
        getContext().getContentResolver().notifyChange(AppProvider.getCanUpdateUri(), null);
        return getAppIdUri(rowId);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        throw new UnsupportedOperationException("Delete not supported for " + uri + ".");
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        switch (MATCHER.match(uri)) {
            case CODE_LIST:
                return updateApplicationSyncTable(uri, contentValues, selection, selectionArgs);
            case CODE_SINGLE:
                // For the TEST_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ApplicationTypeTable.Cols.PACKAGE_NAME+ "=?";
                selectionArgs = new String[]{String.valueOf(uri.getLastPathSegment())};
                return updateApplicationSyncTable(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateApplicationSyncTable(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        // If the {@link TestCaseEntry#COLUMN_TEST_NAME} key is present,
        // check that the name value is not null.
        if (contentValues.containsKey(ApplicationTypeTable.Cols.PACKAGE_NAME)) {
            String name = contentValues.getAsString(ApplicationTypeTable.Cols.PACKAGE_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Test requires a name");
            }
        }

        // TODO: 3/12/2018 check validation of inputs
        // If there are no values to update, then don't try to update the database
        if (contentValues.size() == 0) {
            return 0;
        }

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = db().update(ApplicationTypeTable.Cols.PACKAGE_NAME, contentValues, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }


    public static final class Helper {
        private Helper() {
        }

        public static long ensureExists(Context context, String app) {
            long id = getAppIdNum(context, app);
            if (id <= 0) {
                ContentValues values = new ContentValues(1);
                values.put(ApplicationTypeTable.Cols.PACKAGE_NAME, app);
                Uri uri = context.getContentResolver().insert(getContentUri(), values);
                id = Long.parseLong(uri.getLastPathSegment());
            }
            return id;
        }

        public static long getAppIdNum(Context context, String app) {
            String[] projection = new String[]{ApplicationTypeTable.Cols.PACKAGE_NAME};
            Cursor cursor = context.getContentResolver().query(getAppUri(app), projection, null, null, null);
            if (cursor == null) {
                return 0;
            }

            try {
                if (cursor.getCount() == 0) {
                    return 0;
                } else {
                    cursor.moveToFirst();
                    return cursor.getLong(cursor.getColumnIndexOrThrow(ApplicationTypeTable.Cols.PACKAGE_NAME));
                }
            } finally {
                cursor.close();
            }
        }

        public static List<String> apps(Context context) {
            final ContentResolver resolver = context.getContentResolver();
            final Uri uri = ApplicationSyncProvider.getAllApps();
            final String[] projection = {ApplicationTypeTable.Cols.PACKAGE_NAME};
            final Cursor cursor = resolver.query(uri, projection, null, null, null);
            List<String> apps = new ArrayList<>(30);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        final String name = cursor.getString(0);
                        apps.add(name);
                        cursor.moveToNext();
                    }
                }
                cursor.close();
            }
            Collections.sort(apps);
            return apps;
        }
    }
}
