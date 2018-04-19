package org.fdroid.fdroid.iris;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.fdroid.fdroid.Preferences;
import org.fdroid.fdroid.Utils;
import org.fdroid.fdroid.iris.net.GetAppsFromServerDB;
import org.fdroid.fdroid.iris.net.IOnUpdateResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Khaled on 4/9/2018.
 * Assumptions
 * Descriptions
 */

public class CheckUpdatesService extends IntentService implements IOnUpdateResult {

    private static final String TAG = CheckUpdatesService.class.getSimpleName();
    private static final int CODE_POST_REQUEST = 1025;
    public static final String OPERATION = "operation";
    public static final String PKG_NAME = "pkgName";
    public static final String VERSION = "version";
    public static final String UNINSTALL_OPERATION = "uninstall";
    public static final String DOWNLOAD_OPERATION = "download";
    public static final String INSTALL_OPERATION = "install";
    public static final String WEBSITE_DOWNLOADED = "1";
    public static final String DEVICE_DOWNLOADED = "2";
    public static final String DEVICE_INSTALLED = "3";
    public static final String NEED_UPDATE = "4";
    public static final String UNINSTALL = "5";
    public static final String NONE = "6";

    private String activeDownloadUrlString;
    private LocalBroadcastManager localBroadcastManager;

    public CheckUpdatesService() {
        super(CheckUpdatesService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent: just handling intent");
        PackageManager pk = getApplicationContext().getPackageManager();
        getApps();
    }

    public void getApps() {
        String url;
        HashMap<String, String> params = new HashMap<>();
        params.put("UserName", Preferences.get().getPrefUsername());
        Utils.debugLog(TAG, "getApps: device = " + Preferences.get().getPrefDeviceType());
        if (Preferences.get().getPrefDeviceType().equalsIgnoreCase("tablet")) {
            url = Preferences.get().getHostIp() + "/dashboard/command/getControllerApps";
        } else if (Preferences.get().getPrefDeviceType().equalsIgnoreCase("dongle")) {
            url = Preferences.get().getHostIp() + "/dashboard/command/getDongleApps";
        } else {
            url = Preferences.get().getHostIp() + "/dashboard/command/getControllerApps";
        }
        Preferences.get().getAllowedAppsURL();
        GetAppsFromServerDB performNetworkRequest = new GetAppsFromServerDB(this, url, params, CODE_POST_REQUEST);
        performNetworkRequest.execute();
    }

    @Override
    public void onResult(String result) {
//        ApplicationStatus applicationStatus = new ApplicationStatus();
        ArrayList<ApplicationStatus> applicationStatusArrayList = new ArrayList<>();

        Log.d(TAG, "onResult: ");

        try {
            JSONArray jsonArray = new JSONArray(result);
            JSONObject jsonObject;
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = (JSONObject) jsonArray.get(i);
                applicationStatusArrayList.add(getApplicationStatusFromJsonObject(jsonObject));
                printJsonObject(jsonObject);
            }
            handleApps(applicationStatusArrayList);

        } catch (JSONException e) {
            Log.e(TAG, "onResult: jsonException", e);
        }
    }

    @NonNull
    private ApplicationStatus getApplicationStatusFromJsonObject(JSONObject jsonObject) throws JSONException {
        return new ApplicationStatus(
                jsonObject.get("ClientID").toString(),
                jsonObject.get("ApplicationID").toString(),
                jsonObject.get("Version").toString(),
                jsonObject.get("WebDownloadDate").toString(),
                jsonObject.get("DeviceDownloadDate").toString(),
                jsonObject.get("InstallationDate").toString(),
                jsonObject.get("Status").toString()
        );
    }

    private void printJsonObject(JSONObject jsonObject) throws JSONException {
        Log.d(TAG, "json object ===" + jsonObject.toString());
        Log.d(TAG, "onResult: " + jsonObject.get("ClientID") +
                jsonObject.get("ApplicationID") +
                jsonObject.get("Version") +
                jsonObject.get("WebDownloadDate") +
                jsonObject.get("DeviceDownloadDate") +
                jsonObject.get("InstallationDate") +
                jsonObject.get("Status"));
    }

    private void handleApps(ArrayList<ApplicationStatus> applicationStatusArrayList) {

        for (ApplicationStatus applicationStatus :
                applicationStatusArrayList) {
            switch (applicationStatus.getStatus()) {
                case WEBSITE_DOWNLOADED:
                    if (Float.parseFloat(applicationStatus.getVersion()) > 0) {
                        downloadApp(applicationStatus);
                        Log.d(TAG, "handleApps: download app " + applicationStatus.getApplicationId());
                    }
                    break;
                case DEVICE_DOWNLOADED:
                    if (Float.parseFloat(applicationStatus.getVersion()) > 0) {
                        installApp(applicationStatus);
                    }
                    break;
                case DEVICE_INSTALLED:
//                  doNothing
                    break;
                case NEED_UPDATE:
                    if (Float.parseFloat(applicationStatus.getVersion()) > 0) {
//                        updateApp(applicationStatus);
                        // we can use downloadApp in update status
                        downloadApp(applicationStatus);
                        Log.d(TAG, "handleApps: download app " + applicationStatus.getApplicationId());
                    }
                    break;
                case UNINSTALL:
                    unInstallApp(applicationStatus);
                    //do nothing
                    break;
                case NONE://none
                    // do nothing
                    break;
                default:
                    break;

            }
        }
    }

    private void updateApp(ApplicationStatus applicationStatus) {
        // TODO: 4/12/2018 handle update App
        startInstallService(applicationStatus, DOWNLOAD_OPERATION);
    }

    private void installApp(ApplicationStatus applicationStatus) {
        startInstallService(applicationStatus, INSTALL_OPERATION);
    }


    private void downloadApp(ApplicationStatus applicationStatus) {
        startInstallService(applicationStatus, DOWNLOAD_OPERATION);
    }

    public void unInstallApp(ApplicationStatus applicationStatus) {
        startInstallService(applicationStatus, UNINSTALL_OPERATION);
    }

    // TODO: 4/18/2018 create InstallIntentFactory instead of this method
    @NonNull
    private Intent getIntent() {
        Intent installIntent;
//        installIntent = new Intent(getApplicationContext(), ControllerInstallationService.class);
        if (Preferences.get().getPrefDeviceType().equalsIgnoreCase("dongle")) {
            installIntent = new Intent(getApplicationContext(), DongleInstallationService.class);
        } else if (Preferences.get().getPrefDeviceType().equalsIgnoreCase("tablet")) {
            installIntent = new Intent(getApplicationContext(), ControllerInstallationService.class);
        } else {
            installIntent = new Intent(getApplicationContext(), DongleInstallationService.class);
        }
        return installIntent;
    }

    private void startInstallService(ApplicationStatus applicationStatus, String operation) {
        Intent installIntent = getIntent();
        installIntent.putExtra(OPERATION, operation);
        installIntent.putExtra(PKG_NAME, applicationStatus.getApplicationId());
        installIntent.putExtra(VERSION, applicationStatus.getVersion());
        getApplicationContext().startService(installIntent);
    }

}
