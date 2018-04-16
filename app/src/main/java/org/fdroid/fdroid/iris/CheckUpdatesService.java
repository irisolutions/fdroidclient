package org.fdroid.fdroid.iris;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.fdroid.fdroid.Preferences;
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

    private static final String TAG = CheckUpdatesService.class.getName();
    private static final int CODE_POST_REQUEST = 1025;
    public static final String OPERATION = "operation";
    public static final String PKG_NAME = "pkgName";
    public static final String VERSION = "version";
    public static final String UNINSTALL_OPERATION = "uninstall";
    public static final String DOWNLOAD_OPERATION = "download";
    public static final String INSTALL_OPERATION = "install";

    private String activeDownloadUrlString;
    private LocalBroadcastManager localBroadcastManager;

    public CheckUpdatesService() {
        super(CheckUpdatesService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent: just handling intent");
        Handler mHandler = new Handler(getMainLooper());
        getApps();
    }

    public void getApps() {
        String url;
        HashMap<String, String> params = new HashMap<>();
//        params.put("UserName", Preferences.get().getPrefUsername());

//        for testing
        params.put("UserName", "najah_child");
        url = Preferences.get().getHostIp() + "/dashboard/command/getControllerApps";

        Log.d(TAG, "getApps: device = " + Preferences.get().getPrefDeviceType());
//        if (Preferences.get().getPrefDeviceType().equalsIgnoreCase("tablet")) {
//            url = "http://192.168.1.2:8080/dashboard/command/getControllerApps";
//        } else {
//            url = "http://192.168.1.2:8080/dashboard/command/getDongleApps";
//        }
        Preferences.get().getAllowedAppsURL();
        GetAppsFromServerDB performNetworkRequest = new GetAppsFromServerDB(this, url, params, CODE_POST_REQUEST);
        performNetworkRequest.execute();
    }

    @Override
    public void onResult(String result) {
//        ApplicationStatus applicationStatus = new ApplicationStatus();
        ArrayList<ApplicationStatus> applicationStatusArrayList = new ArrayList<>();

        Log.d(TAG, "onResult: ___________________________");
//        Log.d(TAG, result);

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
                case "1"://website_downloaded
                    if (Float.parseFloat(applicationStatus.getVersion()) > 0) {
                        downloadApp(applicationStatus);
                        Log.d(TAG, "handleApps: download app "+applicationStatus.getApplicationId());
                    }
                    break;
                case "2"://device_downloaded
                    if (Float.parseFloat(applicationStatus.getVersion()) > 0) {
                        installApp(applicationStatus);
                    }
                    break;
                case "3"://device_installed
//                  doNothing
                    break;
                case "4"://need_update
                    if (Float.parseFloat(applicationStatus.getVersion()) > 0) {
                        updateApp(applicationStatus);
                    }
                    break;
                case "5"://uninstall
                    unInstallApp(applicationStatus);
                    //do nothing
                    break;
                case "6"://none
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

    @NonNull
    private Intent getIntent() {
        Intent installIntent;
        if (Preferences.get().getPrefDeviceType().equalsIgnoreCase("dongle")) {
            installIntent = new Intent(getApplicationContext(), DongleInstallationService.class);
        } else if (Preferences.get().getPrefDeviceType().equalsIgnoreCase("tablet")) {
            installIntent = new Intent(getApplicationContext(), ControllerInstallationService.class);
        } else {
            installIntent = new Intent(getApplicationContext(), ControllerInstallationService.class);
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
