package ping.otmsapp.server.dispatch;

import android.app.Notification;
import android.content.Intent;

import ping.otmsapp.R;
import ping.otmsapp.entitys.UserInfo;
import ping.otmsapp.entitys.dispatch.VehicleInfo;
import ping.otmsapp.entitys.map.GdMapLocation;
import ping.otmsapp.mvp.view.DispatchActivity;
import ping.otmsapp.mvp.view.WarnActivity;
import ping.otmsapp.tools.AppUtil;
import ping.otmsapp.tools.FrontNotification;
import ping.otmsapp.tools.HearServer;
import ping.otmsapp.tools.MediaUse;
import ping.otmsapp.tools.PowerUse;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


public class LoopService extends HearServer implements DispatchOperation.Callback {

    private FileUploader fileUploader = new FileUploader();
    private DispatchSyncHelper dispatchSyncHelper = new DispatchSyncHelper();
    private DispatchPullHelper dispatchPullHelper = new DispatchPullHelper();
    private LocationHelper locationHelper = new LocationHelper();
    private GdMapLocation location;

    private FrontNotification gpsNotify;
    private FrontNotification warnNotify;
    private MediaUse mediaUse;

    private DispatchNotifyView dispatchNotifyView;

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
        fileUploader.stopRun();
        location.destroy();
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    protected FrontNotification createForeNotification(FrontNotification.Build build) {
        dispatchNotifyView = new DispatchNotifyView(getApplication());
        return  build
                .setId(1000)
                .setGroup("心跳")
                .setSmallIcon(R.drawable.ic_warn)
//                .setBigIcon(R.drawable.ic_launcher)
//                .setActivityIntent(openActivityClass)
//                .setServiceIntent(getClass())
                .setView(dispatchNotifyView.remoteViews)
                .autoGenerateNotification(
                        "老百姓大药房TMS服务",
                        "正在服务中",
                        "请勿关闭");
    }

    /** 检查GPS */
    private void checkGps() {
        if (!AppUtil.isOenGPS(getApplicationContext())){
            gpsNotify.showNotification();
        }else{
            gpsNotify.cancelNotification();
        }
    }

    private boolean checkNetwork() {
        return AppUtil.isNetworkAvailable(getApplicationContext());
    }

    @Override
    protected void executeTask() {
      try{
          if (!checkNetwork()){
              //如果网络不可用
              dispatchNotifyView.refreshView("请连接网络");
              return;
          }
          fileUploader.executeDispatch();
          UserInfo userInfo = new UserInfo().fetch();
          VehicleInfo vehicleInfo = new VehicleInfo().fetch();

          if (userInfo!=null && vehicleInfo!=null){
              if (!location.isStart()) location.startLocation();
          }else{
              if (location.isStart()) location.stopLocation();
          }

          //if (location.isStart()) checkGps();//是否检测GPS开启

          dispatchSyncHelper.sync(userInfo,vehicleInfo);
          dispatchPullHelper.pull(userInfo,vehicleInfo);
          dispatchNotifyView.refreshView(userInfo,vehicleInfo,getNextTime());
      }catch (Exception e){
          e.printStackTrace();
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
        build.setIcon(R.drawable.ic_launcher);
        return build.autoGenerateNotification(
                "通知",
                "您好,请在设置中打开定位功能,避免影响您的行程录入",
                "点击设置");
    }

    //创建预警 通知栏
    private FrontNotification createWarnNotify() {
        FrontNotification.Build build = new FrontNotification.Build(getApplicationContext()).setId(401);
        build.setActivityIntent(WarnActivity.class);
        build.setFlags(new int[]{Notification.FLAG_INSISTENT,Notification.FLAG_AUTO_CANCEL});
        build.setDefaults(Notification.DEFAULT_ALL);
        build.setIcon(R.drawable.ic_launcher);
        return build.autoGenerateNotification(
                "预警",
                "您好,请检查冷藏箱,温度异常",
                "点击查看");
    }
}
