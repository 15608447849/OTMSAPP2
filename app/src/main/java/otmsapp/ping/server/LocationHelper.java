package otmsapp.ping.server;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;

import java.util.concurrent.ConcurrentLinkedQueue;

import otmsapp.ping.entitys.map.MTraceLocation;
import otmsapp.ping.entitys.tuples.Tuple2;
import otmsapp.ping.log.LLog;

public class LocationHelper extends Thread implements AMapLocationListener {

    volatile boolean isRun = true;
    LocationFilter locationFilter = new LocationFilter();
    LocationRecode locationRecode = new LocationRecode();
    //并发无界限线程安全队列
    private final ConcurrentLinkedQueue<AMapLocation> queue = new ConcurrentLinkedQueue<>();
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        //定位点回调
        if (aMapLocation!=null && aMapLocation.getErrorCode()== 0){
            boolean isAdd = queue.offer(aMapLocation);
            if (isAdd){
                synchronized (this){
                    this.notify();
                }
            }

        }
    }

    @Override
    public void run() {
        while (isRun){
            try{
                AMapLocation aMapLocation = queue.poll();
                if (aMapLocation == null) {
                    synchronized (this){
                        try { this.wait(); } catch (InterruptedException ignored) {}
                    }
                    continue;
                }
                onCollection(aMapLocation);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void onCollection(AMapLocation aMapLocation) {
        //轨迹过滤
        try {
            Tuple2<MTraceLocation,Float> tuple = locationFilter.convert(aMapLocation);
            if (tuple!=null){
//                LLog.print(tuple.getValue0());
                //保存
                locationRecode.recode(tuple);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
