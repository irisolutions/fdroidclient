package fdroidclient.iris.com.fdroiddongle.services;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by Khaled on 2/19/2018.
 * Assumptions
 * Descriptions
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFirebaseIdService";
    private static final String TOPIC_GLOBAL = "global";
    public static final String TOKEN = "token";
    public static final String NOTIFICATION = "fdroidclient.iris.com.fdroiddongle.services";
    private static final String TYPE = "type";
    public static final String DONGLE = "dongle";
    private static final String RESULT_CODE = "RESULT_CODE";
    private static final int TOKEN_CODE = 1250;

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // now subscribe to `global` topic to receive app wide notifications
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_GLOBAL);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        sendTokenToBroadcast(refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }


    private void sendTokenToBroadcast(String token) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(RESULT_CODE, TOKEN_CODE);
        intent.putExtra(TOKEN, token);
        intent.putExtra(TYPE, DONGLE);
        sendBroadcast(intent);
    }

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.

//        Subscribe the user to “global” topic so that you can send notifications to all the apps
//        FirebaseMessaging.getInstance().subscribeToTopic("global");


//        In order to unsubscribe from a topic you can use:
//        FirebaseMessaging.getInstance().unsubscribeFromTopic("global");
    }

}
