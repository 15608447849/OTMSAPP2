package ping.otmsapp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import ping.otmsapp.entitys.scanner.ScannerApiThread;
import ping.otmsapp.entitys.scanner.ScannerApi_UROVO;
import ping.otmsapp.entitys.scanner.ScannerCallback;
import ping.otmsapp.entitys.scanner.ScannerApi_SEUIC;
import ping.otmsapp.server.dispatch.LoopService;
import ping.otmsapp.storege.db.SQLiteStore;
import ping.otmsapp.tools.AppUtil;
import ping.otmsapp.tools.LeeApplicationAbs;
import ping.otmsapp.zerocice.IceHelper;


public class ApplicationInitialization extends LeeApplicationAbs{

    //活跃的activity
    private int activeCount= 0;
    private ScannerApiThread scannerApiThread = null;

    @Override
    protected void onCreateByAllProgress(String processName) {
        if (processName.contains(":location")) return;
        super.onCreateByAllProgress(processName);
        SQLiteStore.get().init(getApplicationContext());
        //添加网络状态过滤器
        IceHelper.get().addFilter(new IceHelper.IFilter() {
            @Override
            public void filter() throws Exception {
                if (!AppUtil.isNetworkAvailable(getApplicationContext()))
                    throw new IllegalStateException("网络无效.");
            }
        });

        IceHelper.get().initBySharedPreference(getApplicationContext(),"LBXTMS", "192.168.1.120", 4061);
    }


    @Override
    protected void onCreateByApplicationMainProgress(String processName) {
        startService(new Intent(getApplicationContext(), LoopService.class)); //打开服务
        AppUtil.addShortcut(getApplicationContext(),R.drawable.ic_launcher,true);//创建快捷方式
    }

    private void initScannerApi() {
        if (android.os.Build.VERSION.SDK_INT == 22){
            scannerApiThread = new ScannerApi_SEUIC(this);
        }else if (android.os.Build.VERSION.SDK_INT == 18){
            scannerApiThread = new ScannerApi_UROVO(this);
        }
    }

    /**
     * activity启动设置
     */
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        super.onActivityCreated(activity, savedInstanceState);
        //竖屏锁定
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //标题栏隐藏
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //应用运行时，保持屏幕高亮,不锁屏
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //设定软键盘的输入法模式: 确保当前输入焦点是可见的
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        //扫码设备
        if (activity instanceof ScannerCallback){
            if (scannerApiThread == null) initScannerApi();
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        super.onActivityResumed(activity);
        //注册扫码监听
        if (activity instanceof ScannerCallback){
           if (scannerApiThread!=null) {
               scannerApiThread.setScanCallback((ScannerCallback)activity);
               scannerApiThread.enable();
           }
        }
        activeCount++;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        super.onActivityPaused(activity);
        if (activity instanceof ScannerCallback){
            if (scannerApiThread!=null){
                scannerApiThread.setScanCallback(null);
                scannerApiThread.disable();
            }
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        super.onActivityDestroyed(activity);
        activeCount--;
        if (activeCount==0 && scannerApiThread!=null){
            scannerApiThread.stopScan();
            scannerApiThread = null;
        }
    }
}
