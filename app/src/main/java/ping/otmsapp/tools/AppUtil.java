package ping.otmsapp.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import ping.otmsapp.log.LLog;

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

    //检查无线网络有效
    private boolean isWirelessNetworkValid(Context context) {
        return AppUtil.isOpenWifi(context) && AppUtil.isNetworkAvailable(context);
    }

   //判断GPS是否开启
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

    //获取当前进程名
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

    //判断当前进程是否是主进程
    public static boolean checkCurrentIsMainProgress(@NonNull Context context){
        return checkCurrentIsMainProgress(context,AppUtil.getCurrentProcessName(context));
    }
    //判断当前进程是否是主进程
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

    //简单信息弹窗
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

    //把bitmap 转file
    public static boolean bitmap2File(Bitmap bitmap, File file){
        try {
            if (bitmap==null || file==null) return false;
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    //创建快捷方式 ; 权限:  <uses-permission android:name="com.android.launcher.permission.INSTALL_SHORTCUT"/>
    public static void addShortcut(Context context, int appIcon,boolean isCheck) {

        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
            if (isCheck){
                boolean isExist = sharedPreferences.getBoolean("shortcut", false);
                if (isExist) return;
            }
            Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            Intent shortcutIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            final PackageManager pm = context.getPackageManager();
            String title = pm.getApplicationLabel( pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA)).toString();
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
            shortcut.putExtra("duplicate", false);
            Parcelable iconResource = Intent.ShortcutIconResource.fromContext(context,appIcon);
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
            context.sendBroadcast(shortcut);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("shortcut", true);
            editor.apply();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void installApk(Context context, String apkPath) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://"+apkPath),"application/vnd.android.package-archive");
        context.startActivity(intent);
    }

}
