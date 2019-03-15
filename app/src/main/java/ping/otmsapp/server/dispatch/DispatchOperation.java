package ping.otmsapp.server.dispatch;

import ping.otmsapp.entitys.JsonLocalSqlStorage;
import ping.otmsapp.entitys.dispatch.Dispatch;
import ping.otmsapp.entitys.dispatch.DispatchSync;
import ping.otmsapp.entitys.dispatch.VehicleInfo;
import ping.otmsapp.entitys.except.AbnormalList;
import ping.otmsapp.entitys.except.AbnormalListSync;
import ping.otmsapp.entitys.map.Trace;
import ping.otmsapp.entitys.map.TraceSync;
import ping.otmsapp.entitys.recycler.RecyclerBoxList;
import ping.otmsapp.entitys.recycler.RecyclerBoxListSync;
import ping.otmsapp.entitys.warn.WarnList;
import ping.otmsapp.log.LLog;
import ping.otmsapp.mvp.model.AppInterfaceModel;

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

   public void forceDelete(){
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

        LLog.print("已清理本地调度数据");
    }

}
