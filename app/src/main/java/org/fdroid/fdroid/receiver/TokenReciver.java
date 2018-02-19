package org.fdroid.fdroid.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.fdroid.fdroid.Preferences;

/**
 * Created by Khaled on 2/19/2018.
 * Assumptions
 * Descriptions
 */

public class TokenReciver extends BroadcastReceiver {
    private static final String TAG = TokenReciver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String token = bundle.getString("token");
            String type = bundle.getString("type");

            Preferences.get().setPrefFCMToken(token);
            Preferences.get().setPrefDeviceType(type);
        }
    }
}
