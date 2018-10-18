package otmsapp.ping.server.dispatch;

import otmsapp.ping.entitys.dispatch.VehicleInfo;
import otmsapp.ping.entitys.map.MTraceLocation;
import otmsapp.ping.entitys.map.Trace;
import otmsapp.ping.entitys.tuples.Tuple2;

public class LocationRecode {
    public void recode(Tuple2<MTraceLocation,Float> tuple){
        //调度对象
        Trace trace  = new Trace().fetch();
        if (trace!=null && trace.state == Trace.STATE.RECODE_ING){
            trace.path.add(tuple.getValue0());
            trace.mileage+=tuple.getValue1();
            trace.save();
        }
    }
}
