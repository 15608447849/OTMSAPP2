package otmsapp.ping.server;

import android.content.Intent;

import otmsapp.ping.R;
import otmsapp.ping.entitys.UserInfo;
import otmsapp.ping.entitys.dispatch.Dispatch;
import otmsapp.ping.entitys.dispatch.VehicleInfo;
import otmsapp.ping.entitys.map.GdMapLocation;
import otmsapp.ping.log.LLog;
import otmsapp.ping.mvp.view.DispatchActivity;
import otmsapp.ping.tools.HearServer;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


public class LoopService extends HearServer implements DispatchOperation.Callback {

    private DispatchSyncHelper dispatchSyncHelper = new DispatchSyncHelper();
    private DispatchPullHelper dispatchPullHelper = new DispatchPullHelper();
    private LocationHelper locationHelper = new LocationHelper();
    private GdMapLocation location;

    @Override
    protected void initCreate() {
        dispatchSyncHelper.setCallback(this);
        dispatchPullHelper.setCallback(this);
        locationHelper.start();
        location = new GdMapLocation(this,locationHelper);
    }

    @Override
    protected int getNotificationIcon() {
        return R.drawable.ic_launcher;
    }

    @Override
    protected String getNotificationGroupKey() {
        return getClass().getSimpleName();
    }

    @Override
    protected Class<?> getOpenActivityClass() {
        return null;
    }

    @Override
    protected int getNotificationId() {
        return 100;
    }

    @Override
    protected void executeTask() {

        UserInfo userInfo = new UserInfo().fetch();
        VehicleInfo vehicleInfo = new VehicleInfo().fetch();

        if (userInfo!=null){
            if (!location.isStart()) location.startLocation();
        }else{
            if (location.isStart()) location.stopLocation();
        }

        if (vehicleInfo!=null){
            dispatchSyncHelper.sync(vehicleInfo);
        }

        dispatchPullHelper.pull(userInfo,vehicleInfo);
    }

    @Override
    public void updateDispatch() {
        try {
            Intent intent = new Intent(getApplicationContext(), DispatchActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("message","dispatch");
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
