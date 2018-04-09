package org.fdroid.fdroid.iris;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Khaled on 4/9/2018.
 * Assumptions
 * Descriptions
 */

public class AlarmUpdateReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 1253;
    public static final String ACTION = "example.khaled.com.schedulerapp";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, CheckUpdatesService.class);
        context.startService(i);
    }
}
