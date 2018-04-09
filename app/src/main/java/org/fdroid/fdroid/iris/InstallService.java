package org.fdroid.fdroid.iris;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.fdroid.fdroid.Preferences;
import org.fdroid.fdroid.R;
import org.fdroid.fdroid.Utils;
import org.fdroid.fdroid.data.Apk;
import org.fdroid.fdroid.data.ApkProvider;
import org.fdroid.fdroid.data.App;
import org.fdroid.fdroid.data.AppProvider;
import org.fdroid.fdroid.data.InstalledApp;
import org.fdroid.fdroid.data.InstalledAppProvider;
import org.fdroid.fdroid.installer.InstallManagerService;
import org.fdroid.fdroid.installer.Installer;
import org.fdroid.fdroid.installer.InstallerFactory;
import org.fdroid.fdroid.installer.InstallerService;

import java.util.List;

/**
 * Created by Khaled on 2/22/2018.
 * Assumptions
 * Descriptions
 */

public class InstallService extends IntentService {

    private static final String TAG = InstallService.class.getName();
    private Handler toastHandler;
    private App app;
    private PackageManager packageManager;

    private static final int REQUEST_ENABLE_BLUETOOTH = 2;
    private static final int REQUEST_PERMISSION_DIALOG = 3;
    private static final int REQUEST_UNINSTALL_DIALOG = 4;

    public static final String EXTRA_APPID = "appid";
    public static final String EXTRA_FROM = "from";
    public static final String EXTRA_HINT_SEARCHING = "searching";

    private LocalBroadcastManager localBroadcastManager;
    public static boolean updateWanted;


    public InstallService() {
        super(InstallService.class.getSimpleName());
        packageManager = getPackageManager();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String message = bundle.getString("message");
            String title = bundle.getString("title");
            Log.d(TAG, "onReceive: Token received" + message);
            String packageName = title;
            String device = message;
//            handleApp(packageName, device);
            showToast(message, title);
        }
    }



    private void handleApp(String packageName, String device) {

        app = AppProvider.Helper.findSpecificApp(getContentResolver(), packageName, 1);

        if (app.canAndWantToUpdate(getApplicationContext())) {
            updateWanted = true;
//            btMain.setText(R.string.menu_upgrade);
        } else {
            updateWanted = false;
            if (packageManager.getLaunchIntentForPackage(app.packageName) != null) {
//                btMain.setText(R.string.menu_launch);
                Log.d(TAG, "handleApp: get lunch");
            } else {
                Log.d(TAG, "handleApp: get uninstall");
//                btMain.setText(R.string.menu_uninstall);
            }
        }

        installApp(app);
        if (app != null) {
            Log.d(TAG, "handleApp: ---------- " + app.packageName);
            Log.d(TAG, "handleApp: ---------- " + app.repoId);
        } else {
            Log.d(TAG, "handleApp: ---------- app = null");
        }
    }

    private void installApp(App app) {

        if (updateWanted && app.suggestedVersionCode > 0) {
            Apk apkToInstall = ApkProvider.Helper.findApkFromAnyRepo(getApplicationContext(), app.packageName, app.suggestedVersionCode);
            install(apkToInstall);
            return;
        }
//        if (installed) {
        if (true) {
            // If installed
            if (packageManager.getLaunchIntentForPackage(app.packageName) != null) {
                // If "launchable", launch
                launchApk(app.packageName);
            } else {
                uninstallApk();
            }
        } else if (app.suggestedVersionCode > 0) {
            // If not installed, install
//            btMain.setEnabled(false);
//            btMain.setText(R.string.system_install_installing);
            final Apk apkToInstall = ApkProvider.Helper.findApkFromAnyRepo(getApplicationContext(), app.packageName, app.suggestedVersionCode);
            install(apkToInstall);
        }
    }


    class ApkListAdapter extends ArrayAdapter<Apk> {

        ApkListAdapter(Context context, App app) {
            super(context, 0);
            final List<Apk> apks = ApkProvider.Helper.findByPackageName(context, app.packageName);
            for (final Apk apk : apks) {
                if (apk.compatible || Preferences.get().showIncompatibleVersions()) {
                    add(apk);
                }
            }
        }

        private String getInstalledStatus(final Apk apk) {
            // Definitely not installed.
            if (apk.versionCode != app.installedVersionCode) {
                return getString(R.string.app_not_installed);
            }
            // Definitely installed this version.
            if (apk.sig != null && apk.sig.equals(app.installedSig)) {
                return getString(R.string.app_installed);
            }
            // Installed the same version, but from someplace else.
            final String installerPkgName;
            try {
                installerPkgName = packageManager.getInstallerPackageName(app.packageName);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Application " + app.packageName + " is not installed anymore");
                return getString(R.string.app_not_installed);
            }
            if (TextUtils.isEmpty(installerPkgName)) {
                return getString(R.string.app_inst_unknown_source);
            }
            final String installerLabel = InstalledAppProvider
                    .getApplicationLabel(getApplicationContext(), installerPkgName);
            return getString(R.string.app_inst_known_source, installerLabel);
        }
    }


    // Install the version of this app denoted by 'app.curApk'.
    private void install(final Apk apk) {
        // TODO: 2/26/2018 handle is finishing
//        if (isFinishing()) {
//            return;
//        }

        if (!apk.compatible) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.installIncompatible);
            builder.setPositiveButton(R.string.yes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            initiateInstall(apk);
                        }
                    });
            builder.setNegativeButton(R.string.no,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            return;
        }
        if (app.installedSig != null && apk.sig != null
                && !apk.sig.equals(app.installedSig)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.SignatureMismatch).setPositiveButton(
                    R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            return;
        }
        initiateInstall(apk);
    }


    private void initiateInstall(Apk apk) {
        Installer installer = InstallerFactory.create(this, apk);
        Intent intent = installer.getPermissionScreen();
        if (intent != null) {
            // permission screen required
            Utils.debugLog(TAG, "permission screen required");
//            startActivityForResult(intent, REQUEST_PERMISSION_DIALOG);
            return;
        }

        startInstall(apk);
    }

    private void startInstall(Apk apk) {
//        activeDownloadUrlString = apk.getUrl();
        registerDownloaderReceiver();
        InstallManagerService.queue(this, app, apk);
    }

    private void registerDownloaderReceiver() {
        // TODO: 2/26/2018 handle register download reciever
//        if (activeDownloadUrlString != null) { // if a download is active
//            String url = activeDownloadUrlString;
//            localBroadcastManager.registerReceiver(downloadReceiver,
//                    DownloaderService.getIntentFilter(url));
//        }
    }

    /**
     * Attempts to find the installed {@link Apk} from the database. If not found, will lookup the
     * {@link InstalledAppProvider} to find the details of the installed app and use that to
     * instantiate an {@link Apk} to be returned.
     * <p>
     * Cases where an {@link Apk} will not be found in the database and for which we fall back to
     * the {@link InstalledAppProvider} include:
     * + System apps which are provided by a repository, but for which the version code bundled
     * with the system is not included in the repository.
     * + Regular apps from a repository, where the installed version is old enough that it is no
     * longer available in the repository.
     *
     * @throws IllegalStateException If neither the {@link PackageManager} or the
     *                               {@link InstalledAppProvider} can't find a reference to the installed apk.
     */
    @NonNull
    private Apk getInstalledApk() {
        try {
            PackageInfo pi = packageManager.getPackageInfo(app.packageName, 0);

            Apk apk = ApkProvider.Helper.findApkFromAnyRepo(this, pi.packageName, pi.versionCode);
            if (apk == null) {
                InstalledApp installedApp = InstalledAppProvider.Helper.findByPackageName(getApplicationContext(), pi.packageName);
                if (installedApp == null) {
                    throw new IllegalStateException("No installed app found when trying to uninstall");
                }

                apk = new Apk(installedApp);
            }
            return apk;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            throw new IllegalStateException("Couldn't find installed apk for " + app.packageName, e);
        }
    }

    /**
     * Queue for uninstall based on the instance variable {@link #app}.
     */
    private void uninstallApk() {
        if (app.installedApk == null) {
            // TODO ideally, app would be refreshed immediately after install, then this
            // workaround would be unnecessary
            app.installedApk = getInstalledApk();
        }

        Installer installer = InstallerFactory.create(this, app.installedApk);
        Intent intent = installer.getUninstallScreen();
        if (intent != null) {
            // uninstall screen required
            Utils.debugLog(TAG, "screen screen required");
            // TODO: 2/26/2018 handle uninstall process
//            startActivityForResult(intent, REQUEST_UNINSTALL_DIALOG);
            return;
        }

        startUninstall();
    }

    private void startUninstall() {
        localBroadcastManager.registerReceiver(uninstallReceiver,
                Installer.getUninstallIntentFilter(app.packageName));
        InstallerService.uninstall(getApplicationContext(), app.installedApk);
    }

    private void launchApk(String packageName) {
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);
        startActivity(intent);
    }

    private final BroadcastReceiver uninstallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Installer.ACTION_UNINSTALL_STARTED:
//                    headerFragment.startProgress(false);
//                    headerFragment.showIndeterminateProgress(getString(R.string.uninstalling));
                    break;
                case Installer.ACTION_UNINSTALL_COMPLETE:
//                    headerFragment.removeProgress();
                    onAppChanged();

                    localBroadcastManager.unregisterReceiver(this);
                    break;
                case Installer.ACTION_UNINSTALL_INTERRUPTED:
//                    headerFragment.removeProgress();

                    String errorMessage =
                            intent.getStringExtra(Installer.EXTRA_ERROR_MESSAGE);

                    if (!TextUtils.isEmpty(errorMessage)) {
                        Log.e(TAG, "uninstall aborted with errorMessage: " + errorMessage);

                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getApplicationContext());
                        alertBuilder.setTitle(R.string.uninstall_error_notify_title);
                        alertBuilder.setMessage(errorMessage);
                        alertBuilder.setNeutralButton(android.R.string.ok, null);
                        alertBuilder.create().show();
                    }

                    localBroadcastManager.unregisterReceiver(this);
                    break;
                case Installer.ACTION_UNINSTALL_USER_INTERACTION:
                    PendingIntent uninstallPendingIntent =
                            intent.getParcelableExtra(Installer.EXTRA_USER_INTERACTION_PI);

                    try {
                        uninstallPendingIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        Log.e(TAG, "PI canceled", e);
                    }

                    break;
                default:
                    throw new RuntimeException("intent action not handled!");
            }
        }
    };


    private void onAppChanged() {
//        if (!reset(app.packageName)) {
//            this.finish();
//            return;
//        }
////// we don't need them in service
//        refreshApkList();
//        refreshHeader();
//        supportInvalidateOptionsMenu();
    }


    private void showToast(final String message, final String title) {
        if (toastHandler == null) {
            toastHandler = new Handler(Looper.getMainLooper());
        }
        toastHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        "Install services" + message + "\n" + title, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

