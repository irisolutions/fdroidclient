package org.fdroid.fdroid.iris;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Khaled on 4/9/2018.
 * Assumptions
 * Descriptions
 */

public class UpdatesScheduler {

    // Setup a recurring alarm every half hour
    public void scheduleUpdates(Context context) {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(context, AlarmUpdateReceiver.class);
        // TODO: 4/9/2018 do interval 5 minutes
        long interval = 1000 * 60 * 5; // 5 minutes
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, AlarmUpdateReceiver.REQUEST_CODE,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
//                PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every every half hour from this point onwards
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                interval, pendingIntent);
    }

    public void cancelUpdatesScheduler(Context context) {
        Intent intent = new Intent(context, AlarmUpdateReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(context, AlarmUpdateReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }
}
