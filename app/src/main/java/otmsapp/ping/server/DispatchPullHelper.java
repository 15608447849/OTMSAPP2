package otmsapp.ping.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.hy.otms.rpcproxy.appInterface.DispatchInfo;
import cn.hy.otms.rpcproxy.appInterface.DispatchOrder;
import cn.hy.otms.rpcproxy.appInterface.DispatchRpc;
import cn.hy.otms.rpcproxy.appInterface.DispatchSchedvech;
import otmsapp.ping.entitys.UserInfo;
import otmsapp.ping.entitys.dispatch.Box;
import otmsapp.ping.entitys.dispatch.Dispatch;
import otmsapp.ping.entitys.dispatch.DispatchSync;
import otmsapp.ping.entitys.dispatch.Store;
import otmsapp.ping.entitys.dispatch.VehicleInfo;
import otmsapp.ping.entitys.except.AbnormalList;
import otmsapp.ping.entitys.except.AbnormalListSync;
import otmsapp.ping.entitys.map.Trace;
import otmsapp.ping.entitys.map.TraceSync;
import otmsapp.ping.entitys.recycler.RecyclerBox;
import otmsapp.ping.entitys.recycler.RecyclerBoxList;
import otmsapp.ping.entitys.recycler.RecyclerBoxListSync;
import otmsapp.ping.log.LLog;
import otmsapp.ping.mvp.model.AppInterfaceModel;
import otmsapp.ping.tools.AppUtil;
import otmsapp.ping.tools.JsonUti;

/**
 * 获取调度单
 */
public class DispatchPullHelper extends DispatchOperation {


    public void pull(UserInfo userInfo,VehicleInfo vehicleInfo){
        //获取用户信息
        if (userInfo == null )  return;
        String traceNo = "";
        //获取本地调度信息
        Dispatch dispatch = new Dispatch().fetch();
//        LLog.print("调度信息: "+ JsonUti.javaBeanToJson(dispatch));
        if (dispatch!=null ){
            if (vehicleInfo!=null && dispatch.state <= Dispatch.STATE.TAKEOUT){
                //装货中或者等待启程->获取本地调度任务车次号
                traceNo = String.valueOf(vehicleInfo.carNumber);
            }else{
                return;
            }
        }
        //获取远程调度信息 如果没有车次,获取新车次任务;
        // 如果有车次,通知后台收到任务
        boolean flag = dispatchInfoPause(server.dispatchInfoSync(userInfo.userId,traceNo));
        if (!flag) pull(userInfo,vehicleInfo);

    }

    private boolean dispatchInfoPause(DispatchInfo result) {
        if (result==null) return true;
        if (result.flag == -1) return true; //-1 没有修改
        if (result.flag==-10) {  //-10 强制删除当前任务
            forceDelete();
            if (callback!=null) callback.updateDispatch();
            return false;
        }
        if (result.dispatchSchedvech.driverc == 0) return true;
//        LLog.print(JsonUti.javaBeanToJson(result));
        transfer(result);
        return true;
    }

    private void transfer(DispatchInfo result) {
        long time = System.currentTimeMillis();
        DispatchRpc[] pathList = result.dispatchRpc;

        DispatchSchedvech dispatchSchedvech = result.dispatchSchedvech;
//        LLog.print("收到车次号: "+dispatchSchedvech.schedtn);
        //司机信息
        VehicleInfo vehicleInfo = new VehicleInfo();
            vehicleInfo.carNumber = dispatchSchedvech.schedtn;//车次号
            vehicleInfo.driverCode = dispatchSchedvech.driverc + "";//司机用户码
            vehicleInfo.phoneNo = dispatchSchedvech.drivercp + "";//手机号码
            vehicleInfo.vehicleCode = dispatchSchedvech.vechid;//车牌号
            vehicleInfo.carrierName = dispatchSchedvech.carrname;//承运商机构名
            vehicleInfo.driverName = dispatchSchedvech.drivern;//司机姓名

        Store storeTemp;
        Box boxTemp;
        List<Box> boxList;
        int pos = 0;
        List<Store> storeList = new ArrayList<>();
        HashMap<String, Integer> storeRemoteStateMap = new HashMap<>();
        HashMap<String, Integer> boxRemoteStateMap = new HashMap<>();

        for (DispatchRpc dispatchRpc : pathList) {
            storeTemp = new Store();
            storeTemp.state = Store.STATE.LOAD;//21 等待装货 22等待卸货 23卸货完成

            storeTemp.storeName = dispatchRpc.cusabbname;//门店名
            storeTemp.detailedAddress = dispatchRpc.addr;//详细地址
            storeTemp.customerAgency = dispatchRpc.cusid;//门店机构码
            storeTemp.specifiedOrder = dispatchRpc.disrp;//装卸货顺序

            boxList = new ArrayList<>();

            //集装箱
            for (DispatchOrder dispatchOrder : dispatchRpc.dispatchOrder) {
                boxTemp = new Box();
                boxTemp.state = Box.STATE.LOAD+dispatchOrder.ostatus;//30 带装箱扫码 31 待卸货扫码 32回收 <-> 服务器: 0 没有, 1装 2 卸
                boxTemp.barCode = dispatchOrder.lpn; //二维码识别
//                LLog.print(boxTemp.barCode);
                boxRemoteStateMap.put(boxTemp.barCode, boxTemp.state);//箱子远程状态同步状态
                boxList.add(boxTemp);
                pos++;
            }
            if (boxList.size()==0) continue; //没有集装箱,不去此门店


            storeTemp.boxList = boxList;
            storeTemp.boxSum = boxList.size();//此门店总箱数
            storeRemoteStateMap.put(storeTemp.customerAgency, storeTemp.state);//门店远程状态同步状态
            storeList.add(storeTemp);

        }
        //调度单
        Dispatch dispatch = new Dispatch();
        dispatch.state = Dispatch.STATE.LOAD;//1 扫码装货 2 等待启程 3 配送装卸 4 等待返程
        dispatch.storeBoxSum = pos;//设置所有门店所有箱子总数
        dispatch.storeList = storeList;
        //调度单同步
        DispatchSync dispatchSync = new DispatchSync();
        dispatchSync.dispatchRemoteState = dispatch.state;
        dispatchSync.storeRemoteStateMap = storeRemoteStateMap;
        dispatchSync.boxRemoteStateMap = boxRemoteStateMap;

        //本地轨迹记录
        Trace trace = new Trace();
        //本地轨迹同步
        TraceSync traceSync = new TraceSync();
        //本地异常队列
        AbnormalList abnormalList = new AbnormalList();
        //本地异常队列同步
        AbnormalListSync abnormalListSync = new AbnormalListSync();
        //本地回收箱列表
        RecyclerBoxList recyclerBoxList = new RecyclerBoxList();
        //本地回收箱列表同步
        RecyclerBoxListSync recyclerBoxListSync = new RecyclerBoxListSync();

        //保存
        saveToSQLite(vehicleInfo,dispatch,dispatchSync,trace,traceSync,abnormalList,abnormalListSync,recyclerBoxList,recyclerBoxListSync);
//        LLog.print("已重置本地数据,耗时"+(System.currentTimeMillis() - time)+"毫秒");
        if (callback!=null) callback.updateDispatch();
    }


}
