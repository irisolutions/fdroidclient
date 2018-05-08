package fdroidclient.iris.com.fdroidtablet;/*
 * Copyright (CHANGE_CONTROLLER_APP_STATUS) 2010-12  Ciaran Gultnieks, ciaran@ciarang.com
 * Copyright (CHANGE_CONTROLLER_APP_STATUS) 2009  Roberto Jacinto, roberto.jacinto@caixamagica.pt
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */


import android.app.Activity;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import org.fdroid.fdroid.AppDetails;
import org.fdroid.fdroid.FDroidApp;
import org.fdroid.fdroid.NfcHelper;
import org.fdroid.fdroid.Preferences;
import org.fdroid.fdroid.PreferencesActivity;
import org.fdroid.fdroid.UpdateService;
import org.fdroid.fdroid.Utils;
import org.fdroid.fdroid.compat.TabManager;
import org.fdroid.fdroid.compat.UriCompat;
import org.fdroid.fdroid.data.AppProvider;
import org.fdroid.fdroid.data.NewRepoConfig;
import org.fdroid.fdroid.iris.CheckUpdatesService;
import org.fdroid.fdroid.iris.UpdatesScheduler;
import org.fdroid.fdroid.iris.net.ConstantURLs;
import org.fdroid.fdroid.iris.net.PerformNetworkRequest;
import org.fdroid.fdroid.iris.net.PushDownloadNotification;
import org.fdroid.fdroid.views.AppListFragmentPagerAdapter;
import org.fdroid.fdroid.views.IrisLogin;
import org.fdroid.fdroid.views.ManageReposActivity;
import org.fdroid.fdroid.views.swap.SwapWorkflowActivity;

import java.util.HashMap;


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

    private SearchView searchView;

    private ViewPager viewPager;

    @Nullable
    private TabManager tabManager;

    private AppListFragmentPagerAdapter adapter;

    @Nullable
    private MenuItem searchMenuItem;

    @Nullable
    private String pendingSearchQuery;

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

        swipe = (SwipeRefreshLayout)findViewById(R.id.swipe);

        initWebView();

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                myWebView.loadUrl(lastUrl);
                swipe.setRefreshing(true);
            }
        });
        Preferences.get().setPrefDeviceType(TABLET);

//        createViews();

//        getTabManager().createTabs();

        // Start a search by just typin     g
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

        Intent intent = getIntent();
        handleSearchOrAppViewIntent(intent);

//        if (intent.hasExtra(EXTRA_TAB_UPDATE)) {
//            boolean showUpdateTab = intent.getBooleanExtra(EXTRA_TAB_UPDATE, false);
//            if (showUpdateTab) {
//                getTabManager().selectTab(2);
//            }
//        }


        Uri uri = AppProvider.getContentUri();
        getContentResolver().registerContentObserver(uri, true, new AppObserver());

        String token = Preferences.get().getPrefFCMToken();
        String type = Preferences.get().getPrefDeviceType();
        Toast.makeText(FDroidTablet.this, token + "" + "\n" + type,
                Toast.LENGTH_LONG).show();


        updatesScheduler = new UpdatesScheduler();
        updatesScheduler.scheduleUpdates(getApplicationContext());
//        This is just for testing use above comment instead
        Intent i = new Intent(getApplicationContext(), CheckUpdatesService.class);
        getApplicationContext().startService(i);


        // Re-enable once it can be disabled via a setting
        // See https://gitlab.com/fdroid/fdroidclient/issues/435
        //
//         if (UpdateService.isNetworkAvailableForUpdate(this)) {
//             UpdateService.updateNow(this);
//         }
//        requireRootAccess();
    }


    private void performSearch(String query) {
        if (searchMenuItem == null) {
            // Store this for later when we do actually have a search menu ready to use.
            pendingSearchQuery = query;
            return;
        }

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        MenuItemCompat.expandActionView(searchMenuItem);
        searchView.setQuery(query, true);
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
        handleSearchOrAppViewIntent(intent);

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

    private void handleSearchOrAppViewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            performSearch(query);
            return;
        }

        final Uri data = intent.getData();
        if (data == null) {
            return;
        }

        final String scheme = data.getScheme();
        final String path = data.getPath();
        String packageName = null;
        String query = null;
        if (data.isHierarchical()) {
            final String host = data.getHost();
            if (host == null) {
                return;
            }
            switch (host) {
                case "f-droid.org":
                    if (path.startsWith("/repository/browse")) {
                        // http://f-droid.org/repository/browse?fdfilter=search+query
                        query = UriCompat.getQueryParameter(data, "fdfilter");

                        // http://f-droid.org/repository/browse?fdid=packageName
                        packageName = UriCompat.getQueryParameter(data, "fdid");
                    } else if (path.startsWith("/app")) {
                        // http://f-droid.org/app/packageName
                        packageName = data.getLastPathSegment();
                        if ("app".equals(packageName)) {
                            packageName = null;
                        }
                    }
                    break;
                case "details":
                    // market://details?id=app.id
                    packageName = UriCompat.getQueryParameter(data, "id");
                    break;
                case "search":
                    // market://search?q=query
                    query = UriCompat.getQueryParameter(data, "q");
                    break;
                case "play.google.com":
                    if (path.startsWith("/store/apps/details")) {
                        // http://play.google.com/store/apps/details?id=app.id
                        packageName = UriCompat.getQueryParameter(data, "id");
                    } else if (path.startsWith("/store/search")) {
                        // http://play.google.com/store/search?q=foo
                        query = UriCompat.getQueryParameter(data, "q");
                    }
                    break;
                case "apps":
                case "amazon.com":
                case "www.amazon.com":
                    // amzn://apps/android?p=app.id
                    // http://amazon.com/gp/mas/dl/android?s=app.id
                    packageName = UriCompat.getQueryParameter(data, "p");
                    query = UriCompat.getQueryParameter(data, "s");
                    break;
            }
        } else if ("fdroid.app".equals(scheme)) {
            // fdroid.app:app.id
            packageName = data.getSchemeSpecificPart();
        } else if ("fdroid.search".equals(scheme)) {
            // fdroid.search:query
            query = data.getSchemeSpecificPart();
        }

        if (!TextUtils.isEmpty(query)) {
            // an old format for querying via packageName
            if (query.startsWith("pname:")) {
                packageName = query.split(":")[1];
            }

            // sometimes, search URLs include pub: or other things before the query string
            if (query.contains(":")) {
                query = query.split(":")[1];
            }
        }

        if (!TextUtils.isEmpty(packageName)) {
            Utils.debugLog(TAG, "FDroid launched via app link for '" + packageName + "'");
            Intent intentToInvoke = new Intent(this, AppDetails.class);
            intentToInvoke.putExtra(AppDetails.EXTRA_APPID, packageName);
            startActivity(intentToInvoke);
            finish();
        } else if (!TextUtils.isEmpty(query)) {
            Utils.debugLog(TAG, "FDroid launched via search link for '" + query + "'");
            performSearch(query);
        }
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
//        getTabManager().onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (fdroidApp.bluetoothAdapter == null) {
            // ignore on devices without Bluetooth
            MenuItem btItem = menu.findItem(R.id.action_bluetooth_apk);
            btItem.setVisible(false);
        }

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        // LayoutParams.MATCH_PARENT does not work, use a big value instead
        searchView.setMaxWidth(1000000);
        searchView.setOnQueryTextListener(this);

        // If we were asked to execute a search before getting around to building the options
        // menu, then we should deal with that now that the options menu is all sorted out.
        if (pendingSearchQuery != null) {
            performSearch(pendingSearchQuery);
            pendingSearchQuery = null;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if (i == R.id.action_iris_login) {
            startActivity(new Intent(this, IrisLogin.class));
            UpdateService.updateNow(this);
            return true;
        } else if (i == R.id.action_update_repo) {
            UpdateService.updateNow(this);
            return true;
        } else if (i == R.id.action_manage_repos) {
            startActivity(new Intent(this, ManageReposActivity.class));
            return true;
        } else if (i == R.id.action_settings) {
            Intent prefs = new Intent(getBaseContext(), PreferencesActivity.class);
            startActivityForResult(prefs, REQUEST_PREFS);
            return true;
        } else if (i == R.id.action_swap) {
            startActivity(new Intent(this, SwapWorkflowActivity.class));
            return true;
        } else if (i == R.id.action_bluetooth_apk) {/*
                 * If Bluetooth has not been enabled/turned on, then enabling
                 * device discoverability will automatically enable Bluetooth
                 */
            Intent discoverBt = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverBt.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 121);
            startActivityForResult(discoverBt, REQUEST_ENABLE_BLUETOOTH);
            // if this is successful, the Bluetooth transfer is started
            return true;
        } else if (i == R.id.action_about) {
            View view = LayoutInflater.from(this).inflate(R.layout.about, null);

            String versionName = Utils.getVersionName(this);
            if (versionName != null) {
                ((TextView) view.findViewById(R.id.version)).setText(versionName);
            }

            AlertDialog alrt = new AlertDialog.Builder(this).setView(view).create();
            alrt.setTitle(R.string.about_title);
            alrt.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    });
            alrt.show();
            return true;
        }
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

//    private void createViews() {
//        viewPager = (ViewPager) findViewById(R.id.main_pager);
//        adapter = new AppListFragmentPagerAdapter(this);
//        viewPager.setAdapter(adapter);
//        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
//            @Override
//            public void onPageSelected(int position) {
//                getTabManager().selectTab(position);
//            }
//        });
//    }
//
//    @NonNull
//    private TabManager getTabManager() {
//        if (tabManager == null) {
//            tabManager = new TabManager(this, viewPager);
//        }
//        return tabManager;
//    }

//    private void refreshUpdateTabLabel() {
//        getTabManager().refreshTabLabel(TabManager.INDEX_CAN_UPDATE);
//        getTabManager().refreshTabLabel(TabManager.INDEX_INSTALLED);
//    }

    public void removeNotification(int id) {
        NotificationManager nMgr = (NotificationManager) getBaseContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(id);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchView.clearFocus();
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

        myWebView.loadUrl(ConstantURLs.IrisStoreUrl);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.setWebViewClient(new MyWebViewClient());
        myWebView.addJavascriptInterface(new UserJavaScriptInterface(), "UserJavaScriptInterface");
        myWebView.getSettings().setAppCacheEnabled(false);
        myWebView.getSettings().setAppCacheMaxSize(1);
        myWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
//        myWebView.getSettings().setUserAgentString("Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)");
//        myWebView.getSettings().setUserAgentString("Android");

//        if (Build.VERSION.SDK_INT  < Build.VERSION_CODES.KITKAT) {
//            myWebView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; U; Android 4.1.1; en-gb; Build/KLP) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30");
//        } else if (Build.VERSION.SDK_INT  >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT  < Build.VERSION_CODES.M) {
//            myWebView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36");
//        }else if (Build.VERSION.SDK_INT  >= Build.VERSION_CODES.M) {
//            myWebView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 5.1.1; Nexus 5 Build/LMY48B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/43.0.2357.65 Mobile Safari/537.36");
//        }

//        else{
//            myWebView.getSettings().setUserAgentString("Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)");
//        }

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

    private class VideoWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
            view.loadUrl(url);
            return false;
        }
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
            lastUrl = url;
            view.loadUrl(url);
            Log.d(TAG, "shouldOverrideUrlLoading: view url" + url);
            return false;
        }

        @Override
        public void onPageFinished(android.webkit.WebView view, String url) {
            super.onPageFinished(view, url);
            swipe.setRefreshing(false);
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
        PushDownloadNotification.pushAppIDNotification("com.uberspot.a2048");
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
