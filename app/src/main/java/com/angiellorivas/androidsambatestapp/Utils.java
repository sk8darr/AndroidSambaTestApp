package com.angiellorivas.androidsambatestapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

/**
 * @author sk8 on 18/10/17.
 */

public class Utils {

    private static final String PREFERENCES = "prefs";
    private static final String KEY_WORKGROUP = "workgroup";
    private static final String KEY_CHECKED = "checked";
    private static final String KEY_IP = "ip";
    private static final String KEY_USER = "user";
    private static final String KEY_PASS = "pass";
    private static final String KEY_FOLDER = "folder";
    private static final String KEY_FILENAME = "filename";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    static void setWorkgroup(String workgroup, Context context){
        SharedPreferences preferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_WORKGROUP, workgroup);
        editor.apply();
    }

    static String getWorkgroup(Context mContext){
        return getSharedPreferences(mContext).getString(KEY_WORKGROUP, "");
    }

    static void setCheck(boolean isChecked, Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_CHECKED, isChecked);
        editor.apply();
    }

    static boolean getCheck(Context mContext) {
        return getSharedPreferences(mContext).getBoolean(KEY_CHECKED, false);
    }

    static void setIp(String value, Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_IP, value);
        editor.apply();
    }

    static String getIp(Context mContext) {
        return getSharedPreferences(mContext).getString(KEY_IP, "");
    }

    static void setUser(String value, Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USER, value);
        editor.apply();
    }

    static String getUser(Context mContext) {
        return getSharedPreferences(mContext).getString(KEY_USER, "");
    }

    static void setPass(String value, Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_PASS, value);
        editor.apply();
    }

    static String getPass(Context mContext) {
        return getSharedPreferences(mContext).getString(KEY_PASS, "");
    }

    static void setFolder(String value, Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_FOLDER, value);
        editor.apply();
    }

    static String getFolder(Context mContext) {
        return getSharedPreferences(mContext).getString(KEY_FOLDER, "");
    }

    static void setFileName(String value, Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_FILENAME, value);
        editor.apply();
    }

    static String getFileName(Context mContext) {
        return getSharedPreferences(mContext).getString(KEY_FILENAME, "");
    }



    public static String getLocalPath(){
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    }
}
