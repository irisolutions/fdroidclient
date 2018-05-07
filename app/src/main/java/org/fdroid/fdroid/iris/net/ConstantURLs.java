package org.fdroid.fdroid.iris.net;

import org.fdroid.fdroid.Preferences;

/**
 * Created by Khaled on 5/6/2018.
 * Assumptions
 * Descriptions
 */

public class ConstantURLs {

    public static final String HOST_IP_ADDRESS = "http://34.217.120.206";


    public static final String PUSH_DOWNLOAD_NOTIFICATION = Preferences.get().getHostIp() + "/IrisCentral/web/app_dev.php/dashboard/command/pushDownloadNotification";

    public static final String CHANGE_CONTROLLER_APP_STATUS = Preferences.get().getHostIp() + "/IrisCentral/web/app_dev.php/dashboard/command/changeControllerAppStatus";
    public static final String CHANGE_Dongle_APP_STATUS = Preferences.get().getHostIp() + "/IrisCentral/web/app_dev.php/dashboard/command/changeDongleAppStatus";

    public static final String GET_CONTROLLER_APPS = Preferences.get().getHostIp() + "/IrisCentral/web/app_dev.php/dashboard/command/getControllerApps";
    public static final String GET_Dongle_APPS = Preferences.get().getHostIp() + "/IrisCentral/web/app_dev.php/dashboard/command/getDongleApps";

    public static final String IrisStoreUrl = "http://34.217.120.206" + "/IrisCentral/web/app_dev.php/store/";

    public static final String ADD_NEW_TOKEN = Preferences.get().getHostIp() + "/IrisCentral/web/app_dev.php/dashboard/command/addNewToken";
    public static final String DELETE_TOKEN = Preferences.get().getHostIp() + "/IrisCentral/web/app_dev.php/dashboard/command/deleteToken";

}
