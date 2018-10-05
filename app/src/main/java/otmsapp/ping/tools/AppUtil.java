package otmsapp.ping.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by Leeping on 2018/4/16.
 * email: 793065165@qq.com
 */

public class AppUtil {

    /**
     * 隐藏软键盘
     * @param activity
     */
    public static void hideSoftInputFromWindow(@NonNull Activity activity){
        try {
            View v = activity.getCurrentFocus();
            if (v!=null && v.getWindowToken()!=null){
                InputMethodManager inputMethodManager =  ((InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE));
                if (inputMethodManager!=null)  inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     *是否打开无线模块
     * @param context
     * @return
     */
    public static boolean isOpenWifi(@NonNull Context context){
        WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return mWifiManager.isWifiEnabled();
    }

    /**
     * @param context 上下文
     * @return 仅仅是用来判断网络连接
     */
    @SuppressLint("MissingPermission")
    public static boolean isNetworkAvailable(@NonNull Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            try {
                return cm.getActiveNetworkInfo().isAvailable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     * @param context
     * @return true 表示开启
     */
    public static boolean isOenGPS(@NonNull Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    //打开GPS设置界面
    public static void openGPS(@NonNull Context context){
        Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        // 打开GPS设置界面
        context.startActivity(intent);
    }

    //检查UI线程
    public static boolean checkUIThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    /**
     * 获取当前进程名
     */
    public static String getCurrentProcessName(@NonNull Context context) {
        int pid = android.os.Process.myPid();
        String processName = "";
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager!=null){
            for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
                if (process.pid == pid) {
                    processName = process.processName;
                }
            }
        }
        return processName;
    }

    /**
     * 判断当前进程是否是主进程
     */
    public static boolean checkCurrentIsMainProgress(@NonNull Context context){
        return checkCurrentIsMainProgress(context,AppUtil.getCurrentProcessName(context));
    }

    public static boolean checkCurrentIsMainProgress(@NonNull Context context, @NonNull String currentProgressName){
        return context.getPackageName().equals(currentProgressName);
    }

    //获取应用版本号
    public static int getVersionCode(@NonNull Context ctx) {
        // 获取packagemanager的实例
        int version = 0;
        try {
            PackageManager packageManager = ctx.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(ctx.getPackageName(), 0);
            version = packInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    //获取应用版本名
    public static String getVersionName(@NonNull Context ctx) {
        // 获取package manager的实例
        String version = "";
        try {
            PackageManager packageManager = ctx.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(ctx.getPackageName(), 0);
            version = packInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    public static void toast(@NonNull Context context, @NonNull String message){
        if (!checkUIThread() ) return;
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }




    //获取CPU型号
    public static String getCpuType(@NonNull Context context){
        if (Build.VERSION.SDK_INT>21){
                return Arrays.toString(Build.SUPPORTED_ABIS);
        }else{
            try {
                FileReader fr = new FileReader("/proc/cpuinfo");
                BufferedReader br = new BufferedReader(fr);
                String text = br.readLine();
                fr.close();
                br.close();
                String[] array = text.split(":\\s+", 2);
                return array[1];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "unknown";
    }



}
