package otmsapp.ping.server.dispatch;

import otmsapp.ping.entitys.JsonLocalSqlStorage;
import otmsapp.ping.entitys.dispatch.Dispatch;
import otmsapp.ping.entitys.dispatch.DispatchSync;
import otmsapp.ping.entitys.dispatch.VehicleInfo;
import otmsapp.ping.entitys.except.AbnormalList;
import otmsapp.ping.entitys.except.AbnormalListSync;
import otmsapp.ping.entitys.map.Trace;
import otmsapp.ping.entitys.map.TraceSync;
import otmsapp.ping.entitys.recycler.RecyclerBoxList;
import otmsapp.ping.entitys.recycler.RecyclerBoxListSync;
import otmsapp.ping.entitys.warn.WarnList;
import otmsapp.ping.mvp.model.AppInterfaceModel;

public class DispatchOperation{

    public interface Callback{
        void updateDispatch();
        void notifyWarn();
    }

    Callback callback ;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    AppInterfaceModel server = new AppInterfaceModel();

    void saveToSQLite(JsonLocalSqlStorage... arrays){
        for (JsonLocalSqlStorage obj : arrays){
            obj.save();
        }
    }
    void forceDelete(){
        //清理异常队列
        new AbnormalList().remove();
        new AbnormalListSync().remove();

        //清理回收队列
        new RecyclerBoxList().remove();
        new RecyclerBoxListSync().remove();

        //清理轨迹
        new Trace().remove();
        new TraceSync().remove();

        //清理调度信息
        new Dispatch().remove();
        new DispatchSync().remove();

        //清理预警信息
        new WarnList().remove();

        //清理司机
        new VehicleInfo().remove();
    }

}
