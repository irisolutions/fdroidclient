package org.fdroid.fdroid;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.LocalBroadcastManager;

import org.fdroid.fdroid.data.Apk;
import org.fdroid.fdroid.data.ApkProvider;
import org.fdroid.fdroid.data.App;
import org.fdroid.fdroid.data.AppPrefs;

/**
 * Created by Khaled on 4/10/2018.
 * Assumptions
 * Descriptions
 */

public class AppStatusHandler {
    private App app;
    private PackageManager packageManager;
    private String activeDownloadUrlString;
    private LocalBroadcastManager localBroadcastManager;
    private AppPrefs startingPrefs;
    private final Context context;


    public AppStatusHandler(Context context) {
        this.context = context;
    }

    public void downloadApp(String packageName,String version) {
        // TODO: 4/10/2018 handle get version code by version
//        app = AppProvider.Helper.findSpecificApp(context.getContentResolver(),packageName,Preferences.get().getLocalRepoName())
//        Apk apkToInstall = ApkProvider.Helper.findApkFromAnyRepo(context, app.packageName, app.suggestedVersionCode);
        Apk apkToInstall = ApkProvider.Helper.findApkFromAnyRepo(context, packageName, Integer.parseInt(version));
        initiateInstall(apkToInstall);
    }

    public void installApp() {

    }

    public void unInstallApp() {

    }

    private void initiateInstall(Apk apk) {
        // TODO: 4/10/2018 -khaled- : handle permissions
//        Installer installer = InstallerFactory.create(context, apk);
//        Intent intent = installer.getPermissionScreen();
//        if (intent != null) {
//            // permission screen required
//            Utils.debugLog(TAG, "permission screen required");
//            startActivityForResult(intent, REQUEST_PERMISSION_DIALOG);
//            return;
//        }
    }



}
