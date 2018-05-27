package fdroidclient.iris.com.fdroidtablet;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.fdroid.fdroid.FDroidApp;
import org.fdroid.fdroid.NfcHelper;
import org.fdroid.fdroid.Preferences;
import org.fdroid.fdroid.PreferencesActivity;
import org.fdroid.fdroid.UpdateService;
import org.fdroid.fdroid.data.AppProvider;
import org.fdroid.fdroid.data.NewRepoConfig;
import org.fdroid.fdroid.iris.UpdatesScheduler;
import org.fdroid.fdroid.iris.net.ConstantURLs;
import org.fdroid.fdroid.iris.net.PerformNetworkRequest;
import org.fdroid.fdroid.views.AppListFragmentPagerAdapter;
import org.fdroid.fdroid.views.ManageReposActivity;
import org.fdroid.fdroid.views.swap.SwapWorkflowActivity;

import java.util.HashMap;


/**
 * Created by Khaled on 4/25/2018.
 * Assumptions
 * Descriptions
 */

public class FDroidTablet extends Activity implements SearchView.OnQueryTextListener {

    private static final String TAG = FDroidTablet.class.getSimpleName();

    private static final int REQUEST_PREFS = 1;
    private static final int REQUEST_ENABLE_BLUETOOTH = 2;
    private static final int REQUEST_SWAP = 3;

    public static final String EXTRA_TAB_UPDATE = "extraTab";

    private static final String ACTION_ADD_REPO = "org.fdroid.fdroid.FDroid.ACTION_ADD_REPO";

    private static final String ADD_REPO_INTENT_HANDLED = "addRepoIntentHandled";

    private String lastUrl = ConstantURLs.HOST_IP_ADDRESS + "/IrisCentral/web/app_dev.php/store/";

    private FDroidApp fdroidApp;

    private AppListFragmentPagerAdapter adapter;

    private UpdatesScheduler updatesScheduler;
    public static final String TABLET = "tablet";

    SwipeRefreshLayout swipe;
    private WebView myWebView;
    private boolean canGoBack;
    private boolean correctUserName;
    private static final int CODE_POST_REQUEST = 1025;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        fdroidApp = (FDroidApp) getApplication();
        fdroidApp.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.fdroid_tablet_layout);

        swipe = (SwipeRefreshLayout) findViewById(R.id.swipe);

        initWebView();

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                myWebView.loadUrl(lastUrl);
                swipe.setRefreshing(true);
            }
        });
        Preferences.get().setPrefDeviceType(TABLET);

        // Start a search by just typin     g
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

//        Intent intent = getIntent();
//        handleSearchOrAppViewIntent(intent);

        Uri uri = AppProvider.getContentUri();
        getContentResolver().registerContentObserver(uri, true, new AppObserver());

//        String token = Preferences.get().getPrefFCMToken();
//        String type = Preferences.get().getPrefDeviceType();
//        Toast.makeText(FDroidTablet.this, token + "" + "\n" + type,
//                Toast.LENGTH_LONG).show();
        updatesScheduler = new UpdatesScheduler();
        updatesScheduler.scheduleUpdates(getApplicationContext());
//        Intent i = new Intent(getApplicationContext(), CheckUpdatesService.class);
//        getApplicationContext().startService(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FDroidApp.checkStartTor(this);
        // AppDetails and RepoDetailsActivity set different NFC actions, so reset here
        NfcHelper.setAndroidBeam(this, getApplication().getPackageName());
        checkForAddRepoIntent(getIntent());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // This is called here as well as onResume(), because onNewIntent() is not called the first
        // time the activity is created. An alternative option to make sure that the add repo intent
        // is always handled is to call setIntent(intent) here. However, after this good read:
        // http://stackoverflow.com/a/7749347 it seems that adding a repo is not really more
        // important than the original intent which caused the activity to start (even though it
        // could technically have been an add repo intent itself).
        // The end result is that this method will be called twice for one add repo intent. Once
        // here and once in onResume(). However, the method deals with this by ensuring it only
        // handles the same intent once.
        checkForAddRepoIntent(intent);
    }

    private void checkForAddRepoIntent(Intent intent) {
        // Don't handle the intent after coming back to this view (e.g. after hitting the back button)
        // http://stackoverflow.com/a/14820849
        if (!intent.hasExtra(ADD_REPO_INTENT_HANDLED)) {
            intent.putExtra(ADD_REPO_INTENT_HANDLED, true);
            NewRepoConfig parser = new NewRepoConfig(this, intent);
            if (parser.isValidRepo()) {
                if (parser.isFromSwap()) {
                    Intent confirmIntent = new Intent(this, SwapWorkflowActivity.class);
                    confirmIntent.putExtra(SwapWorkflowActivity.EXTRA_CONFIRM, true);
                    confirmIntent.setData(intent.getData());
                    startActivityForResult(confirmIntent, REQUEST_SWAP);
                } else {
                    startActivity(new Intent(ACTION_ADD_REPO, intent.getData(), this, ManageReposActivity.class));
                }
            } else if (parser.getErrorMessage() != null) {
                Toast.makeText(this, parser.getErrorMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_PREFS:
                // The automatic update settings may have changed, so reschedule (or
                // unschedule) the service accordingly. It's cheap, so no need to
                // check if the particular setting has actually been changed.
                UpdateService.schedule(getBaseContext());

                if ((resultCode & PreferencesActivity.RESULT_RESTART) != 0) {
                    ((FDroidApp) getApplication()).reloadTheme();
                    final Intent intent = getIntent();
                    overridePendingTransition(0, 0);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(intent);
                }
                break;
            case REQUEST_ENABLE_BLUETOOTH:
//                fdroidApp.sendViaBluetooth(this, resultCode, "org.fdroid.fdroid");
                break;
        }
    }

    public void removeNotification(int id) {
        NotificationManager nMgr = (NotificationManager) getBaseContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(id);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
//        searchView.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.updateSearchQuery(newText);
        return true;
    }

    private class AppObserver extends ContentObserver {

        AppObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            FDroidTablet.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // refresh web view
                }
            });
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }
    }


    private void initWebView() {
        canGoBack = false;
        correctUserName = false;
        myWebView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.setWebViewClient(new MyWebViewClient());
        myWebView.addJavascriptInterface(new UserJavaScriptInterface(), "UserJavaScriptInterface");
        myWebView.setInitialScale(1);
        myWebView.getSettings().setLoadWithOverviewMode(true);
        myWebView.getSettings().setUseWideViewPort(true);
//        myWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        myWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        myWebView.setScrollbarFadingEnabled(false);
        myWebView.loadUrl(ConstantURLs.IrisStoreUrl);

//        myWebView.setWebChromeClient(new WebChromeClient());

//        myWebView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; <Android Version>; <Build Tag etc.>) AppleWebKit/<WebKit Rev>(KHTML, like Gecko) Chrome/<Chrome Rev> Safari/<WebKit Rev>");
//        To turn cache off
//        myWebView.getSettings().setAppCacheEnabled(false);
        //        myWebView.getSettings().setAppCacheEnabled(false);
//        myWebView.getSettings().setAppCacheMaxSize(1);
//        myWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        myWebView.setOnTouchListener(new View.OnTouchListener() {

            public static final String TAG = "TouchListener";

            public boolean onTouch(View v, MotionEvent event) {
                android.webkit.WebView.HitTestResult hitTestResult = ((android.webkit.WebView) v).getHitTestResult();
                if (hitTestResult == null) {
                    return false;
                }
                if (hitTestResult.getType() == 9) {
                    Log.d(TAG, "onTouch: login edit text");
                }
                if (hitTestResult.getExtra() != null) {
                    if (hitTestResult.getExtra().endsWith("/store/login")) {
                        Log.d(TAG, "onTouch: login action");
                    } else if (hitTestResult.getExtra().endsWith("/store/logout")) {
                        Log.d(TAG, "onTouch: logout action");
                    }
                }

                Log.d(TAG, "getExtra = " + hitTestResult.getExtra() + "\t\t Type=" + hitTestResult.getType());
                return false;
            }
        });
    }


    // Prevent the back-button from closing the app
    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack();
            canGoBack = true;
        } else if (canGoBack) {
            canGoBack = false;
            Toast.makeText(this, "click again to exit", Toast.LENGTH_LONG).show();
        } else {
            super.onBackPressed();
        }
    }

    // Use When the user clicks a link from a web page in your WebViewActivity
    private class MyWebViewClient extends WebViewClient {

        private final String TAG = MyWebViewClient.class.getSimpleName();

        @Override
        public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
            if (url.startsWith("http://iris-store.iris.ps")) {
                lastUrl = url;
                view.loadUrl(url);
                return false;
            } else {
                view.loadUrl("file:///android_asset/error.html");
                return false;
            }
        }

        @Override
        public void onPageFinished(android.webkit.WebView view, String url) {
            swipe.setRefreshing(false);
            super.onPageFinished(view, url);
            Log.d(TAG, "onPageFinished: finished page with url = " + url);
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            view.loadUrl("file:///android_asset/error.html");
        }
    }

    public class UserJavaScriptInterface {
        @JavascriptInterface
        public void signIn(String userName, String password) {
            Log.d(TAG, "signIn: username = " + userName + "password" + password);
            userSignIn(userName, password);
        }

        @JavascriptInterface
        public void signOut() {
            userSignOut();
        }
    }

    private void userSignOut() {
        Preferences.get().setPrefUsername("");
        Preferences.get().setPrefPassword("");
        unRegisterUserToken();
        Log.d(TAG, "userSignOut: sign out");
    }

    private void userSignIn(String userName, String password) {
        // TODO: 4/25/2018 check username and password from server
        Log.d(TAG, "userSignIn: user name = " + userName + "password = " + password);
        Preferences.get().setPrefUsername(userName);
        Preferences.get().setPrefPassword(password);
        registerUserToken();
        UpdateService.updateNow(getBaseContext());
    }

    private void registerUserToken() {
        HandleTokenRegistration(ConstantURLs.ADD_NEW_TOKEN);
    }

    private void unRegisterUserToken() {
        HandleTokenRegistration(ConstantURLs.DELETE_TOKEN);
    }

    private void HandleTokenRegistration(String url) {
        HashMap<String, String> params = new HashMap<>();
        Log.d(TAG, "HandleTokenRegistration: Token" + Preferences.get().getPrefFCMToken());
        Log.d(TAG, "HandleTokenRegistration: UserName" + Preferences.get().getPrefUsername());
        Log.d(TAG, "HandleTokenRegistration: Type" + Preferences.get().getPrefDeviceType());

        params.put("Token", Preferences.get().getPrefFCMToken());
        params.put("UserName", Preferences.get().getPrefUsername());
        params.put("Type", Preferences.get().getPrefDeviceType());
        Log.d(TAG, "HandleTokenRegistration: url = " + url);
        PerformNetworkRequest performNetworkRequest = new PerformNetworkRequest(url, params, CODE_POST_REQUEST);
        performNetworkRequest.execute();
    }

}
