package otmsapp.ping.server.dispatch;

import android.app.Notification;
import android.content.Intent;

import otmsapp.ping.R;
import otmsapp.ping.entitys.UserInfo;
import otmsapp.ping.entitys.dispatch.VehicleInfo;
import otmsapp.ping.entitys.map.GdMapLocation;
import otmsapp.ping.mvp.view.DispatchActivity;
import otmsapp.ping.mvp.view.WarnActivity;
import otmsapp.ping.tools.AppUtil;
import otmsapp.ping.tools.FrontNotification;
import otmsapp.ping.tools.HearServer;
import otmsapp.ping.tools.MediaUse;
import otmsapp.ping.tools.PowerUse;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


public class LoopService extends HearServer implements DispatchOperation.Callback {

    private DispatchSyncHelper dispatchSyncHelper = new DispatchSyncHelper();
    private DispatchPullHelper dispatchPullHelper = new DispatchPullHelper();
    private LocationHelper locationHelper = new LocationHelper();
    private GdMapLocation location;

    private FrontNotification gpsNotify;
    private FrontNotification warnNotify;
    private MediaUse mediaUse;

    @Override
    protected void initialize() {
        powerUse = new PowerUse(getApplicationContext(),"LoopPowerLock");
        mediaUse = new MediaUse(getApplicationContext());
        gpsNotify = createGpsNotify();
        warnNotify = createWarnNotify();
        dispatchSyncHelper.setCallback(this);
        dispatchPullHelper.setCallback(this);
        locationHelper.start();
        location = new GdMapLocation(this,locationHelper);
    }

    @Override
    public void onDestroy() {
        location.destroy();
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    protected FrontNotification createForeNotification(FrontNotification.Build build) {
        return  build
                .setId(1000)
                .setGroup("心跳")
//                .setActivityIntent(openActivityClass)
//                .setServiceIntent(getClass())
                .autoGenerateNotification(
                        "老百姓大药房TMS服务",
                        "正在服务中",
                        "请勿关闭",
                        R.drawable.ic_launcher);
    }

    /** 检查GPS */
    private void checkGps() {
        if (!AppUtil.isOenGPS(getApplicationContext())){
            gpsNotify.showNotification();
        }else{
            gpsNotify.cancelNotification();
        }
    }

    @Override
    protected void executeTask() {

        UserInfo userInfo = new UserInfo().fetch();
        VehicleInfo vehicleInfo = new VehicleInfo().fetch();

        if (userInfo!=null && vehicleInfo!=null){
            if (!location.isStart()) location.startLocation();
        }else{
            if (location.isStart()) location.stopLocation();
        }

        if (location.isStart()) checkGps();

        if (vehicleInfo!=null){
            dispatchSyncHelper.sync(vehicleInfo);
        }
        if (userInfo!=null){
            dispatchPullHelper.pull(userInfo,vehicleInfo);
        }

    }

    @Override
    public void updateDispatch() {
        try {
            mediaUse.play(R.raw.msg); //通知调度信息到来
            Intent intent = new Intent(getApplicationContext(), DispatchActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("message","dispatch");
            //亮屏解锁
            powerUse.startPowerWakeLockByScreen();
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PowerUse powerUse;
    @Override
    public void notifyWarn() {
        //亮屏解锁
        powerUse.startPowerWakeLockByScreen();
        //显示预警通知
        warnNotify.showNotification();
    }


    //创建GPS 通知栏
    private FrontNotification createGpsNotify() {
        FrontNotification.Build build = new FrontNotification.Build(getApplicationContext()).setId(400);
        Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        // 打开GPS设置界面
        build.setActivityIntent(intent);
        build.setFlags(new int[]{Notification.FLAG_INSISTENT,Notification.FLAG_AUTO_CANCEL});
        build.setDefaults(Notification.DEFAULT_ALL);
        return build.autoGenerateNotification(
                "通知",
                "您好,请在设置中打开定位功能,避免影响您的行程录入",
                "请及时处理",
                R.drawable.ic_launcher);
    }

    //创建预警 通知栏
    private FrontNotification createWarnNotify() {
        FrontNotification.Build build = new FrontNotification.Build(getApplicationContext()).setId(401);
        build.setActivityIntent(WarnActivity.class);
        build.setFlags(new int[]{Notification.FLAG_INSISTENT,Notification.FLAG_AUTO_CANCEL});
        build.setDefaults(Notification.DEFAULT_ALL);
        return build.autoGenerateNotification(
                "预警",
                "注意: 请检查冷藏箱,温度异常!!!",
                "请及时处理",
                R.drawable.ic_launcher);
    }


}
