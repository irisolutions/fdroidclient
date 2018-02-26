package org.fdroid.fdroid;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Khaled on 2/22/2018.
 * Assumptions
 * Descriptions
 */

public class InstallService extends IntentService {

    private static final String TAG = InstallService.class.getName();
    private Handler toastHandler;

    public InstallService() {
        super(InstallService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
                String message = bundle.getString("message");
                String title= bundle.getString("title");
                Log.d(TAG, "onReceive: Token received" + message);
                showToast(message,title);
            }
        }


    private void showToast(final String message, final String title) {
        if (toastHandler == null) {
            toastHandler = new Handler(Looper.getMainLooper());
        }
        toastHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        "Install services" +message+"\n" +title, Toast.LENGTH_SHORT).show();
            }
        });
    }
    }

