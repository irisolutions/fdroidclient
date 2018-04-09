package org.fdroid.fdroid.iris.net;

import org.fdroid.fdroid.Preferences;

import java.util.HashMap;

/**
 * Created by Khaled on 2/21/2018.
 * Assumptions
 * Descriptions
 */

public class PushDownloadNotification {

    private static final int CODE_POST_REQUEST = 1025;
    private static final String TAG = PushDownloadNotification.class.getName();

    public static void pushAppIDNotification(String appID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("AppID", appID);
        // TODO: 2/21/2018 change type here : should depend on app -get it from app-
        params.put("Type", Preferences.get().getPrefDeviceType());
        params.put("UserName", Preferences.get().getPrefUsername());

        String url = "http://192.168.1.39:8000/dashboard/command/pushDownloadNotification";

        PerformNetworkRequest performNetworkRequest = new PerformNetworkRequest(url,params,CODE_POST_REQUEST);
        performNetworkRequest.execute();
    }

}
