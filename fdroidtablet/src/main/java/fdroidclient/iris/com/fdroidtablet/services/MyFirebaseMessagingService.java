package fdroidclient.iris.com.fdroidtablet.services;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import fdroidclient.iris.com.fdroidtablet.FDroidTablet;
import fdroidclient.iris.com.fdroidtablet.objects.NotificationObject;
import fdroidclient.iris.com.fdroidtablet.utiles.NotificationUtils;

/**
 * Created by Khaled on 2/19/2018.
 * Assumptions
 * Descriptions
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgingService";
    private static final String TITLE = "title";
    private static final String EMPTY = "";
    private static final String MESSAGE = "message";
    private static final String IMAGE = "image";
    private static final String ACTION = "action";
    private static final String DATA = "data";
    private static final String ACTION_DESTINATION = "action_destination";
    private static final String NOTIFICATION = "fdroidclient.iris.com.fdroidtablet.services";
    private static final String RESULT_CODE = "RESULT_CODE";
    private static final int MSG_CODE = 1251;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            Map<String, String> data = remoteMessage.getData();
            String title = data.get(TITLE);
            String message = data.get(MESSAGE);
            sendTokenToBroadcast(message, title);

//            handleData(data);

        } else if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            String message = remoteMessage.getNotification().getBody();
            String title = remoteMessage.getNotification().getTitle();

//            start update service to check updates for this user
//            startUpdateService();

            sendTokenToBroadcast(message, title);
//            handleNotification(remoteMessage.getNotification());
        }// Check if message contains a notification payload.

    }

//    private void startUpdateService() {
//        Intent i = new Intent(getApplicationContext(), CheckUpdatesService.class);
//        getApplicationContext().startService(i);
//    }

    private void sendTokenToBroadcast(String message, String title) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(RESULT_CODE, MSG_CODE);
        intent.putExtra(MESSAGE, message);
        intent.putExtra(TITLE, title);
        sendBroadcast(intent);
    }

    private void handleNotification(RemoteMessage.Notification RemoteMsgNotification) {
        String message = RemoteMsgNotification.getBody();
        String title = RemoteMsgNotification.getTitle();
        NotificationObject notificationObject = new NotificationObject();
        notificationObject.setTitle(title);
        notificationObject.setMessage(message);

        Intent resultIntent = new Intent(getApplicationContext(), FDroidTablet.class);
        NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
        notificationUtils.displayNotification(notificationObject, resultIntent);
        notificationUtils.playNotificationSound();
    }

    private void handleData(Map<String, String> data) {
        String title = data.get(TITLE);
        String message = data.get(MESSAGE);
        String iconUrl = data.get(IMAGE);
        String action = data.get(ACTION);
        String actionDestination = data.get(ACTION_DESTINATION);
        NotificationObject notificationObject = new NotificationObject();
        notificationObject.setTitle(title);
        notificationObject.setMessage(message);
        notificationObject.setIconUrl(iconUrl);
        notificationObject.setAction(action);
        notificationObject.setActionDestination(actionDestination);


        Intent resultIntent = new Intent(getApplicationContext(), FDroidTablet.class);

        NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
        notificationUtils.displayNotification(notificationObject, resultIntent);
        notificationUtils.playNotificationSound();
    }
}