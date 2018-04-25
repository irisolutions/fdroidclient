package fdroidclient.iris.com.fdroidtablet;

/**
 * Created by Khaled on 4/25/2018.
 * Assumptions
 * Descriptions
 */

public class WebViewActivity {
}

//
//public class FDroidTablet extends AppCompatActivity {
//    public static final String TABLET = "tablet";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        setContentView(R.layout.fdroid_tablet_layout);
//        Preferences.get().setPrefDeviceType(TABLET);
//
////        Intent intent = new Intent(this, FDroid.class);
////        startActivity(intent);
//    }
//}



//
//    private static final String TAG = MainActivity.class.getSimpleName();
//    private WebViewActivity myWebView;
//    private boolean canGoBack;
//    private static final String userName = "najah_child";
//    private static final String userPassword = "najah_child";
//    private boolean correctUserName;
//
//    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//
//        super.onCreate(savedInstanceState);
//
//        setContentView(R.layout.fdroid_tablet_layout);
//
//        canGoBack = false;
//        correctUserName = false;
//
//        myWebView = findViewById(R.id.webView);
//
////        myWebView.loadUrl("http://54.89.24.164/IrisCentral/web/app_dev.php/store/");
//        myWebView.loadUrl("http://192.168.1.104:8080/store/");
//        WebSettings webSettings = myWebView.getSettings();
//        webSettings.setJavaScriptEnabled(true);
//        myWebView.setWebViewClient(new MyWebViewClient());
//        myWebView.addJavascriptInterface(new UserJavaScriptInterface(),"UserJavaScriptInterface");
////        myWebView.getSettings().setUserAgentString("Android");
//
//        if (Build.VERSION.SDK_INT  < Build.VERSION_CODES.KITKAT) {
//            myWebView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; U; Android 4.1.1; en-gb; Build/KLP) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30");
//        } else if (Build.VERSION.SDK_INT  >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT  < Build.VERSION_CODES.M) {
//            myWebView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36");
//        }else if (Build.VERSION.SDK_INT  >= Build.VERSION_CODES.M) {
//            myWebView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 5.1.1; Nexus 5 Build/LMY48B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/43.0.2357.65 Mobile Safari/537.36");
//        }
//
////        else{
////            myWebView.getSettings().setUserAgentString("Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)");
////        }
//
//
//        myWebView.setOnTouchListener(new View.OnTouchListener() {
//
//            public static final String TAG = "TouchListener";
//
//            public boolean onTouch(View v, MotionEvent event) {
//                WebViewActivity.HitTestResult hitTestResult = ((WebViewActivity) v).getHitTestResult();
//                if (hitTestResult == null) {
//                    return false;
//                }
//                if (hitTestResult.getType() == 9) {
//                    Log.d(TAG, "onTouch: login edit text");
//                }
//                if (hitTestResult.getExtra() != null) {
//                    if (hitTestResult.getExtra().endsWith("/store/login")) {
//                        Log.d(TAG, "onTouch: login action");
//                    } else if (hitTestResult.getExtra().endsWith("/store/logout")) {
//                        Log.d(TAG, "onTouch: logout action");
//                    }
//                }
//
//
//                Log.d(TAG, "getExtra = " + hitTestResult.getExtra() + "\t\t Type=" + hitTestResult.getType());
//                return false;
//            }
//        });
//    }
//
//private class VideoWebViewClient extends WebViewClient {
//    @Override
//    public boolean shouldOverrideUrlLoading(WebViewActivity view, String url) {
//        view.loadUrl(url);
//        return false;
//    }
//
//}
//
//    // Prevent the back-button from closing the app
//    @Override
//    public void onBackPressed() {
//        if (myWebView.canGoBack()) {
//            myWebView.goBack();
//            canGoBack = true;
//        } else if (canGoBack) {
//            canGoBack = false;
//            Toast.makeText(this, "click again to exit", Toast.LENGTH_LONG).show();
//        } else {
//            super.onBackPressed();
//        }
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//
//// Use When the user clicks a link from a web page in your WebViewActivity
//private class MyWebViewClient extends WebViewClient {
//
//    private final String TAG = MyWebViewClient.class.getSimpleName();
//
//    @Override
//    public boolean shouldOverrideUrlLoading(WebViewActivity view, String url) {
//        view.loadUrl(url);
//        Log.d(TAG, "shouldOverrideUrlLoading: view url" + url);
//        return false;
//    }
//
//    @Override
//    public void onPageFinished(WebViewActivity view, String url) {
//        super.onPageFinished(view, url);
//
//        Log.d(TAG, "onPageFinished: finished page with url = " + url);
///*
//            if (Build.VERSION.SDK_INT >= 19) {
//                Log.d(TAG, "onPageFinished: start evaluate js code");
//                view.evaluateJavascript(jsGetByClass, new ValueCallback<String>() {
//                    @Override
//                    public void onReceiveValue(String s) {
//                        Log.d(TAG, "onReceiveValue" + s);
//                    }
//                });
//            }
//            view.loadUrl("javascript:(function() { " +
//                    jsInjectCode + "})()");*/
//    }
//}
//
//public class UserJavaScriptInterface{
//
//    @JavascriptInterface
//    public void signIn(String userName, String password) {
//        userSignIn(userName,password);
//    }
//
//    @JavascriptInterface
//    public void signOut() {
//        userSignOut();
//    }
//}
//
//    private void userSignOut() {
//        Log.d(TAG, "userSignOut: sign out");
//    }
//
//    private void userSignIn(String userName, String password) {
//        Log.d(TAG, "userSignIn: user name = "+userName+"password = "+password);
//    }