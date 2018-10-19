package ping.otmsapp.entitys.map;

import android.content.Context;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import ping.otmsapp.log.LLog;

/**
 * Created by lzp on 2018/2/25.
 * 高德地图
 * GPS定位采集
 * 只在后台服务中创建
 */
public class GdMapLocation {

    private boolean isStart = false;
    //声明locationClient对象
    private AMapLocationClient mLocationClient;
    //创建 初始化定位客户端
    public GdMapLocation(Context context, AMapLocationListener listener) {

        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
//            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);//定位模式,gps
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//定位模式,gps
            mLocationOption.setInterval(1000);//间隔
            mLocationOption.setSensorEnable(false);//不使用手机传感器定位角度
            mLocationOption.setLocationCacheEnable(false);//不使用定位缓存
            mLocationOption.setNeedAddress(false);//不用返回地理信息
            mLocationOption.setWifiScan(false);//降低耗电不自动刷新wifi
            mLocationOption.setLocationPurpose(null);//不需要场景
        mLocationClient = new AMapLocationClient(context);//定位客户端
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.setLocationListener(listener);
    }

    //关闭
    public void destroy()  {
        stopLocation();//停止定位
        LLog.print(mLocationClient.isStarted());
        mLocationClient.onDestroy(); // 高德定位客户端销毁
        LLog.print("-"+mLocationClient.isStarted());
    }

    //开始定位
    public void startLocation() {
        mLocationClient.startLocation();
        isStart = true;
        LLog.print("打开定位");
    }

    //停止定位
    public void stopLocation() {
        mLocationClient.stopLocation();
        isStart = false;
        LLog.print("关闭定位");
    }

    public boolean isStart() {
        return isStart;
    }
}



