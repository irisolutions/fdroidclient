package org.fdroid.fdroid.iris;

import android.util.Log;

import org.fdroid.fdroid.Utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Khaled on 4/17/2018.
 * Assumptions
 * Descriptions
 */

public class SudoInstall {


    private static final String TAG = SudoInstall.class.getSimpleName();

    public static void install(String path) {
        SudoInstallRunnable sudoRunnable = new SudoInstallRunnable(path);
        new Thread(sudoRunnable).start();
    }

    static class SudoInstallRunnable implements Runnable {
        String path;

        public SudoInstallRunnable(String path) {
            this.path = path;
        }

        @Override
        public void run() {
            try {
                List<String> command = new ArrayList<>();
                command.add("su");
                command.add(";");
                command.add("pm install " + path);
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                Process process = processBuilder.start();
                BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line = "";
                Utils.debugLog(TAG, "install: start");
                while ((line = input.readLine()) != null) {
                    Utils.debugLog(TAG, "install: " + line);
                }
                System.out.println("Here is the standard error of the command (if any):\n");
                while ((line = stdError.readLine()) != null) {
                    Utils.debugLog(TAG, "install: " + line);
                }
                input.close();
            } catch (Exception e) {
                Log.e(TAG, "unInstall: exception", e);
            }
        }
    }

    public static void unInstall(String packageName) {
        SudoUnInstallRunnable sudoRunnable = new SudoUnInstallRunnable(packageName);
        new Thread(sudoRunnable).start();
    }


    static class SudoUnInstallRunnable implements Runnable {
        String packageName;

        public SudoUnInstallRunnable(String packageName) {
            this.packageName = packageName;
        }

        @Override
        public void run() {
            try {
                List<String> command = new ArrayList<>();
                command.add("su");
                command.add(";");
                command.add("pm uninstall " + packageName);
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                Process process = processBuilder.start();
                BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line = "";
                Utils.debugLog(TAG, "install: start");
                while ((line = input.readLine()) != null) {
                    Utils.debugLog(TAG, "install: " + line);
                }
                while ((line = stdError.readLine()) != null) {
                    Utils.debugLog(TAG, "install: " + line);
                }
                input.close();
            } catch (Exception e) {
                Log.e(TAG, "unInstall: exception", e);
            }
        }
    }

}
