package org.fdroid.fdroid.iris.net;

/**
 * Created by Khaled on 5/6/2018.
 * Assumptions
 * Descriptions
 */

public class ConstantURLs {

    public static final String HOST_IP_ADDRESS = "http://iris-store.iris.ps";

    public static final String PUSH_DOWNLOAD_NOTIFICATION = HOST_IP_ADDRESS + "/IrisCentral/web/app_dev.php/dashboard/command/pushDownloadNotification";
    public static final String CHANGE_CONTROLLER_APP_STATUS = HOST_IP_ADDRESS + "/IrisCentral/web/app_dev.php/dashboard/command/changeControllerAppStatus";
    public static final String CHANGE_Dongle_APP_STATUS = HOST_IP_ADDRESS + "/IrisCentral/web/app_dev.php/dashboard/command/changeDongleAppStatus";
    public static final String GET_CONTROLLER_APPS = HOST_IP_ADDRESS + "/IrisCentral/web/app_dev.php/dashboard/command/getControllerApps";
    public static final String GET_Dongle_APPS = HOST_IP_ADDRESS + "/IrisCentral/web/app_dev.php/dashboard/command/getDongleApps";
    public static final String IrisStoreUrl = HOST_IP_ADDRESS + "/IrisCentral/web/app_dev.php/store/";
    public static final String ADD_NEW_TOKEN = HOST_IP_ADDRESS + "/IrisCentral/web/app_dev.php/dashboard/command/addNewToken";
    public static final String DELETE_TOKEN = HOST_IP_ADDRESS + "/IrisCentral/web/app_dev.php/dashboard/command/deleteToken";
//
// To enable local server
//    public static final String HOST_IP_ADDRESS = "http://192.168.0.107:8000";
//    public static final String PUSH_DOWNLOAD_NOTIFICATION = HOST_IP_ADDRESS + "/dashboard/command/pushDownloadNotification";
//    public static final String CHANGE_CONTROLLER_APP_STATUS = HOST_IP_ADDRESS + "/dashboard/command/changeControllerAppStatus";
//    public static final String CHANGE_Dongle_APP_STATUS = HOST_IP_ADDRESS + "/dashboard/command/changeDongleAppStatus";
//    public static final String GET_CONTROLLER_APPS = HOST_IP_ADDRESS + "/dashboard/command/getControllerApps";
//    public static final String GET_Dongle_APPS = HOST_IP_ADDRESS + "/dashboard/command/getDongleApps";
//    public static final String IrisStoreUrl = HOST_IP_ADDRESS + "/store";
//    public static final String ADD_NEW_TOKEN = HOST_IP_ADDRESS + "/dashboard/command/addNewToken";
//    public static final String DELETE_TOKEN = HOST_IP_ADDRESS + "/dashboard/command/deleteToken";
}
