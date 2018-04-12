package org.fdroid.fdroid.iris.net;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Khaled on 2/20/2018.
 * Assumptions
 * Descriptions
 */

public class PushAppStatusToServer extends AsyncTask<Void, Void, String> {
    private static final int CODE_GET_REQUEST = 1024;
    public static final int CODE_POST_REQUEST = 1025;
    private static final String TAG = PushAppStatusToServer.class.getName();
    //the url where we need to send the request
    String url;
    String networkTask;

    //the parameters
    HashMap<String, String> params;

    //the request code to define whether it is a GET or POST
    int requestCode;

    //constructor to initialize values
    public PushAppStatusToServer(String url, HashMap<String, String> params, int requestCode) {
        this.url = url;
        this.params = params;
        this.requestCode = requestCode;
    }

    //when the task started displaying a progressbar
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
//        progressBar.setVisibility(View.VISIBLE);
    }


    //this method will give the response from the request
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        try {
            JSONObject object = new JSONObject(result);
            if (object.getBoolean("success")) {
                JSONArray apps = object.getJSONArray("apps");
            }
        } catch (JSONException e) {
            Log.e(TAG, "onPostExecute: JSONException", e);
        }
    }

    //the network operation will be performed in background
    @Override
    protected String doInBackground(Void... voids) {
        RequestHandler requestHandler = new RequestHandler();

        if (requestCode == CODE_POST_REQUEST)
            return requestHandler.sendPostRequest(url, params);


        if (requestCode == CODE_GET_REQUEST)
            return requestHandler.sendGetRequest(url);

        return null;
    }
}