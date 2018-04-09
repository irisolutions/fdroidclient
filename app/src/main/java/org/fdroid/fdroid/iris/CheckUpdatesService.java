package org.fdroid.fdroid.iris;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Khaled on 4/9/2018.
 * Assumptions
 * Descriptions
 */

public class CheckUpdatesService extends IntentService {

    private static final String TAG = CheckUpdatesService.class.getName();

    public CheckUpdatesService() {
        super(CheckUpdatesService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent: just handling intent");
        Handler mHandler = new Handler(getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "test", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
