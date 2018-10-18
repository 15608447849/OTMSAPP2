package otmsapp.ping.tools;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import otmsapp.ping.log.LLog;


/**
 * Created by Leeping on 2018/6/27.
 * email: 793065165@qq.com
 */

public abstract class LeeApplicationAbs extends Application implements Application.ActivityLifecycleCallbacks {

    /** 是否注册activity声明周期的回调管理 */
    private boolean isRegisterActivityLifecycleCallbacks = true;

    protected void setRegisterActivityLifecycleCallbacks(boolean flag) {
        this.isRegisterActivityLifecycleCallbacks =  flag;
    }

    private boolean isPrintLifeLog = false;

    public void setPrintLifeLog(boolean flag) {
        isPrintLifeLog = flag;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        String progressName = AppUtil.getCurrentProcessName(getApplicationContext());
        onCreateByAllProgress(progressName);
        if ( isRegisterActivityLifecycleCallbacks ) registerActivityLifecycleCallbacks(this);//注册 activity 生命周期管理
        if (AppUtil.checkCurrentIsMainProgress(getApplicationContext(),progressName)){
            onCreateByApplicationMainProgress(progressName);
        }else{
            onCreateByApplicationOtherProgress(progressName);
        }
    }

    /**
     * 所有进程需要的初始化操作
     */
    protected void onCreateByAllProgress(String processName) {
                //日志参数
                LLog
                .getBuild()
                .setLevel(Log.ASSERT)
                .setDateFormat(TimeUtil.getSimpleDateFormat("[MM/dd HH:mm]"))
                .setLogFileName(processName+"_"+ TimeUtil.formatUTCByCurrent("MMdd"))
                .setWriteFile(true);
                //存储应用进程号
                storeProcessPidToFile(processName,android.os.Process.myPid());
    }

    private void storeProcessPidToFile(String processName,int pid) {
        try {
            File dirs = new File(getCacheDir().getPath()+"/pids");
            if (!dirs.exists()) dirs.mkdirs();
            File file  = new File(dirs,processName);
            if (!file.exists()) file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(pid+"\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void killAllProcess(boolean containSelf){
        try {
            File dirs = new File(getCacheDir().getPath()+"/pids");
            if (dirs.exists()) {

                for (File file : dirs.listFiles()){
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String sPid = reader.readLine();
                    reader.close();
                    file.delete();
                    int pid = Integer.parseInt(sPid);
                    if (pid == android.os.Process.myPid()) continue;
                    android.os.Process.killProcess(pid);

                }
            }
           if (containSelf) android.os.Process.killProcess(android.os.Process.myPid());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 主包名进程 初始化创建
     */
    protected void onCreateByApplicationMainProgress(String processName){

    }

    /**
     * 其他包名进程 初始化创建
     */
    protected void onCreateByApplicationOtherProgress(String processName){

    }


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (isPrintLifeLog) LLog.format("%s :: %s",activity,"onCreated");
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (isPrintLifeLog) LLog.format("%s :: %s",activity,"onStarted");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (isPrintLifeLog) LLog.format("%s :: %s",activity,"onResumed");
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (isPrintLifeLog) LLog.format("%s :: %s",activity,"onPaused");
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        if (isPrintLifeLog) LLog.format("%s :: %s",activity,"onSaveInstanceState");
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (isPrintLifeLog) LLog.format("%s :: %s",activity,"onStopped");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (isPrintLifeLog) LLog.format("%s :: %s",activity,"onDestroyed");
    }
}
