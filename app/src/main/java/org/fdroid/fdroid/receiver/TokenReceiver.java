package org.fdroid.fdroid.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.fdroid.fdroid.iris.InstallService;
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
        Log.d(TAG, "onReceive: receive message from FCM");
        if (bundle != null) {
            int resultCode = bundle.getInt(RESULT_CODE);
            if (resultCode == TOKEN_CODE) {
                String token = bundle.getString("token");
                String type = bundle.getString("type");
                Preferences.get().setPrefFCMToken(token);
                Preferences.get().setPrefDeviceType(type);
                Log.d(TAG, "onReceive: Token received" + token);
                Toast.makeText(context, "Token received" + token, Toast.LENGTH_SHORT).show();
            } else if (resultCode == MSG_CODE) {
                String message = bundle.getString(MESSAGE);
                String title = bundle.getString(TITLE);
                Log.d(TAG, "onReceive: message received" + message);
                Log.d(TAG, "onReceive: title received" + title);
                Toast.makeText(context, "Message received" + title + "\n" + message, Toast.LENGTH_SHORT).show();
                startService(context, message, title);
            } else {
                Log.d(TAG, "onReceive: no bundles");
            }
        }
    }

    private void startService(Context context, String message, String title) {
        Intent intent = new Intent(context, InstallService.class);
        intent.putExtra(MESSAGE, message);
        intent.putExtra(TITLE, title);
        context.startService(intent);
    }

}
