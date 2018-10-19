package ping.otmsapp.server.dispatch;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;

import ping.otmsapp.entitys.map.MTraceLocation;
import ping.otmsapp.entitys.tuples.Tuple2;
import ping.otmsapp.log.LLog;

public class LocationFilter {

    //当前GPS定位点信息
    private AMapLocation curtLoc;
    //上一次被记录的GPS定位点信息
    private AMapLocation prevLoc;
    /**
     * @return 上一个位置经纬度
     */
    private LatLng getPrevLatLng() {
        if (prevLoc!=null) return new LatLng(prevLoc.getLatitude(), prevLoc.getLatitude());
        return null;
    }

    /**
     * @return 上一个位置角度
     */
    private float getPrevBearing() {
        if (prevLoc!=null) return prevLoc.getBearing();
        return 0;
    }

    /**
     *
     * @return 获取当前位置的经纬度
     */
    private LatLng getCurLatLng(){
        if (curtLoc!=null) return  new LatLng(curtLoc.getLatitude(), curtLoc.getLatitude());
        return null;
    }

    /**
     * 获取当前位置角度
     * @return
     */
    private float getCurBearing(){
        if (curtLoc!=null) return  curtLoc.getBearing();
        return 0;
    }

    //转换过滤
    public Tuple2<MTraceLocation,Float> convert(AMapLocation aMapLocation) throws Exception {
        curtLoc = aMapLocation;
        //如果当前 类型不符,精度过大,卫星数过低 不记录
        if ( curtLoc.getLocationType() != AMapLocation.LOCATION_TYPE_GPS //类型不符
                || curtLoc.getAccuracy() > 50 //精度过大
                || (curtLoc.getBearing() == 0 && curtLoc.getSpeed() == 0) //不存在角度偏移 且静止
                || curtLoc.getSatellites() < 4){ //卫星数过低
            return null;
        }

        if (prevLoc == null) {
            prevLoc = curtLoc;
            return null;
        }

        final float cDistance = AMapUtils.calculateLineDistance(getCurLatLng(),getPrevLatLng());//距离改变量,单位米

        final float cBearing = Math.abs(getCurBearing() - getPrevBearing());//角度改变量

        //如果角度改变量过小 并且 速度改变量过小
        if ( (cDistance > curtLoc.getAccuracy()  && cBearing < 45 ) || (cDistance <curtLoc.getAccuracy() && (cBearing > 30 && cBearing < 60))  ){
            MTraceLocation mTraceLocation = new MTraceLocation(
                    curtLoc.getLatitude(),//径度
                    curtLoc.getLongitude(),//维度
                    curtLoc.getSpeed(),//速度
                    curtLoc.getBearing(),//角度
                    curtLoc.getTime());
            prevLoc = curtLoc;

            return new Tuple2<>(mTraceLocation, cDistance);
        }
        return null;
    }
}
