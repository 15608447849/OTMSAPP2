package ping.otmsapp.server.dispatch;

import android.content.Context;
import android.widget.RemoteViews;

import ping.otmsapp.R;
import ping.otmsapp.entitys.UserInfo;
import ping.otmsapp.entitys.dispatch.VehicleInfo;
import ping.otmsapp.entitys.map.Trace;
import ping.otmsapp.tools.TimeUtil;

public class DispatchNotifyView {
    public RemoteViews remoteViews;

    public DispatchNotifyView(Context context) {
        remoteViews =  new RemoteViews(context.getPackageName(), R.layout.notify_loop);
    }

    public void refreshView(UserInfo userInfo, VehicleInfo vehicleInfo,long nextTime){
        if (userInfo != null){
            Trace trace = new Trace().fetch();
            if (vehicleInfo!=null && trace!=null){
                remoteViews.setTextViewText(R.id.tv_notify_content, vehicleInfo.driverName+" - "+vehicleInfo.vehicleCode +(trace.state >= Trace.STATE.RECODE_ING ? "\n已行驶里程数:"+trace.mileage+"米":"\n暂未启程"));

            }else{
                remoteViews.setTextViewText(R.id.tv_notify_content, "等待调度任务,下次获取时间: "+ TimeUtil.formatUTC(nextTime,"HH:mm:ss"));
            }
        }else{
            remoteViews.setTextViewText(R.id.tv_notify_content, "请登录运输系统");
        }
    }

}
