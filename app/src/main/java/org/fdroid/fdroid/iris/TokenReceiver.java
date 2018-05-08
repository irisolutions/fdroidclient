package org.fdroid.fdroid.iris;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.fdroid.fdroid.Preferences;
import org.fdroid.fdroid.iris.net.ConstantURLs;
import org.fdroid.fdroid.iris.net.PerformNetworkRequest;

import java.util.HashMap;

/**
 * Created by Khaled on 2/19/2018.
 * Assumptions
 * Descriptions
 */

public class TokenReceiver extends BroadcastReceiver {
    private static final String TAG = TokenReceiver.class.getName();
    private static final int CODE_POST_REQUEST = 1025;
    private static final int MSG_CODE = 1251;
    private static final int TOKEN_CODE = 1250;
    private static final String RESULT_CODE = "RESULT_CODE";
    private static final String TITLE = "title";
    private static final String MESSAGE = "message";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Log.d(TAG, "onReceive: receive message from FCM");
        if(intent.getAction().equalsIgnoreCase("fdroidclient.iris.com.fdroidtablet.services")||
                intent.getAction().equalsIgnoreCase("fdroidclient.iris.com.fdroiddongle.services")) {
            if (bundle != null) {
                int resultCode = bundle.getInt(RESULT_CODE);
                if (resultCode == TOKEN_CODE) {
                    String token = bundle.getString("token");
                    String type = bundle.getString("type");
                    Preferences.get().setPrefFCMToken(token);
                    Preferences.get().setPrefDeviceType(type);
                    if (!Preferences.get().getPrefUsername().equalsIgnoreCase("")) {
                        registerUserToken();
                    }
                    Log.d(TAG, "onReceive: Token received" + token);
                    Toast.makeText(context, "Token received" + token, Toast.LENGTH_SHORT).show();
                } else if (resultCode == MSG_CODE) {
                    String message = bundle.getString(MESSAGE);
                    String title = bundle.getString(TITLE);
                    Log.d(TAG, "onReceive: message received" + message);
                    Log.d(TAG, "onReceive: title received" + title);
                    Toast.makeText(context, "Message received" + title + "\n" + message, Toast.LENGTH_SHORT).show();
                    DoUpdate(context);
//                startService(context, message, title);
                } else {
                    Log.d(TAG, "onReceive: no bundles");
                }
            }
        }
    }

    private void DoUpdate(Context context) {
//        This is just for testing use above comment instead
        Intent i = new Intent(context, CheckUpdatesService.class);
        context.startService(i);
    }

    private void registerUserToken() {
        HashMap<String, String> params = new HashMap<>();
        Log.d(TAG, "HandleTokenRegistration: Token" + Preferences.get().getPrefFCMToken());
        Log.d(TAG, "HandleTokenRegistration: UserName" + Preferences.get().getPrefUsername());
        Log.d(TAG, "HandleTokenRegistration: Type" + Preferences.get().getPrefDeviceType());

        params.put("Token", Preferences.get().getPrefFCMToken());
        params.put("UserName", Preferences.get().getPrefUsername());
        params.put("Type", Preferences.get().getPrefDeviceType());
        String url = ConstantURLs.ADD_NEW_TOKEN;
        Log.d(TAG, "HandleTokenRegistration: url = "+url);
        PerformNetworkRequest performNetworkRequest = new PerformNetworkRequest(url, params, CODE_POST_REQUEST);
        performNetworkRequest.execute();
    }

}
