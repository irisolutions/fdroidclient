package org.fdroid.fdroid.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.fdroid.fdroid.Preferences;

/**
 * Created by Khaled on 2/19/2018.
 * Assumptions
 * Descriptions
 */

public class TokenReceiver extends BroadcastReceiver {
    private static final String TAG = TokenReceiver.class.getName();
    private static final int MSG_CODE = 1251;
    private static final int TOKEN_CODE = 1250;
    private static final String RESULT_CODE = "RESULT_CODE";
    private static final String TITLE = "title";
    private static final String MESSAGE = "message";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            int resultCode = bundle.getInt(RESULT_CODE);
            if (resultCode == TOKEN_CODE) {
                String token = bundle.getString("token");
                String type = bundle.getString("type");
                Preferences.get().setPrefFCMToken(token);
                Preferences.get().setPrefDeviceType(type);
                Log.d(TAG, "onReceive: Token received" + token);
            } else if (resultCode == MSG_CODE) {
                String message = bundle.getString(MESSAGE);
                String title = bundle.getString(TITLE);
                Log.d(TAG, "onReceive: message received" + message);
                Log.d(TAG, "onReceive: title received" + title);
            } else {
                Log.d(TAG, "onReceive: no bundles");
            }
        }
    }
}
