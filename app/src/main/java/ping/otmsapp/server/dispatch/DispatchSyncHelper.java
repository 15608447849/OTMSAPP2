package ping.otmsapp.server.dispatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;

import cn.hy.otms.rpcproxy.appInterface.WarnsDetailInfo;
import cn.hy.otms.rpcproxy.appInterface.WarnsInfo;
import cn.hy.otms.rpcproxy.comm.cstruct.BoolMessage;
import ping.otmsapp.entitys.IO;
import ping.otmsapp.entitys.UserInfo;
import ping.otmsapp.entitys.dispatch.Box;
import ping.otmsapp.entitys.dispatch.Dispatch;
import ping.otmsapp.entitys.dispatch.DispatchSync;
import ping.otmsapp.entitys.dispatch.Store;
import ping.otmsapp.entitys.dispatch.VehicleInfo;
import ping.otmsapp.entitys.except.Abnormal;
import ping.otmsapp.entitys.except.AbnormalList;
import ping.otmsapp.entitys.except.AbnormalListSync;
import ping.otmsapp.entitys.map.Trace;
import ping.otmsapp.entitys.map.TraceSync;
import ping.otmsapp.entitys.recycler.RecyclerBox;
import ping.otmsapp.entitys.recycler.RecyclerBoxList;
import ping.otmsapp.entitys.recycler.RecyclerBoxListSync;
import ping.otmsapp.entitys.recycler.RecyclerCarton;
import ping.otmsapp.entitys.warn.WarnItem;
import ping.otmsapp.entitys.warn.WarnList;
import ping.otmsapp.tools.JsonUtil;
import ping.otmsapp.tools.StrUtil;

public class DispatchSyncHelper extends DispatchOperation{

    private  int remoteState;
    private int localState;
    private BoolMessage boolMessage;
    //预警访问网络中
    private volatile boolean isWarning;

    //检测ice后台返回的对象有效性
    private boolean checkBoolMessage() throws IllegalStateException {
        return boolMessage != null && boolMessage.flag;
    }

    public void sync(UserInfo userInfo, final VehicleInfo vehicleInfo){
        if (userInfo == null || vehicleInfo == null) return;
        try {

            //轨迹同步
            syncTraceRecode(vehicleInfo);
            //异常信息同步
            syncAbnormal();
            //回收信息同步
            syncRecycle();
            //调度信息同步
            executeDispatchSync(vehicleInfo);

            IO.queue(new Runnable() {
                @Override
                public void run() {
                    //预警信息同步
                    syncWarn(vehicleInfo);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    //预警信息同步
    private void syncWarn(VehicleInfo vehicleInfo) {
        if (isWarning) return;
        isWarning = true;

        Dispatch dispatch = new Dispatch().fetch();

        if(dispatch == null) return;

        if (dispatch.state <= Dispatch.STATE.TAKEOUT || dispatch.state >= Dispatch.STATE.BACK) return;

        WarnList warnTag = new WarnList().fetch();

        if (warnTag==null) return;

        WarnsInfo warnsInfo = server.queryTimeLaterWarnInfoByDriver(vehicleInfo.carNumber,warnTag.timeStamp);

        warnTag = warnTag.fetch(); //再次判断避免当前已不存在预警

        if (warnTag==null) return;

        if (warnsInfo==null || warnsInfo.pendingWarnsNum == 0) return;

            WarnItem state = null;
            Iterator<WarnItem> it;
            boolean isAdd;
            boolean isNotify = false;

            tag:for (WarnsDetailInfo warnsDetailInfo : warnsInfo.detailList){
                isAdd = true;
                it = warnTag.list.iterator();
                while (it.hasNext()){
                    state = it.next();
                    if (state.code.equals(warnsDetailInfo.lpn)){ //相同箱子的预警信息

                        if (warnsDetailInfo.ostatus == 1){  //已处理,移除
                            it.remove();
                            break tag;
                        }else{
                            isAdd = false;
                            break;
                        }
                    }
                }

                if (warnsDetailInfo.ostatus==1) continue;

                if (isAdd) {
                    state = new WarnItem();
                    state.code = warnsDetailInfo.lpn;
                }

                state.type = "("+warnsDetailInfo.mtype+")";
                state.value =
                        (Double.parseDouble(warnsDetailInfo.wval) < Double.parseDouble(warnsDetailInfo.minval) ? "温度过低" : "温度超高")
                                +"( "+warnsDetailInfo.wval+" )";
                state.range = StrUtil.format("正常范围:%s - %s",warnsDetailInfo.minval,warnsDetailInfo.maxval);
                state.time = warnsDetailInfo.timestamp;

               if (isAdd) warnTag.list.add(state);

               isNotify = true;
            }

            //按时间排序
            Collections.sort(warnTag.list, new Comparator<WarnItem>() {
                @Override
                public int compare(WarnItem o1, WarnItem o2) {
                    return (int) (o2.time - o1.time); //降序
                }
            });

            if (warnTag.list.size()>0) warnTag.timeStamp = warnTag.list.get(0).time;

            if (isNotify) {
                warnTag.save();
                if (callback!=null) callback.notifyWarn();
            }
        try { Thread.sleep( 30 * 1000 );  } catch (InterruptedException ignored) { }
        isWarning = false;
    };

    //执行调度单同步
    private void executeDispatchSync(VehicleInfo vehicleInfo) {

        Dispatch dispatch = new Dispatch().fetch();
        DispatchSync dispatchSync = new DispatchSync().fetch();
        if (vehicleInfo!=null && dispatch!=null && dispatchSync!=null){
            boolean isChangeState = syncDispatch(vehicleInfo,dispatch,dispatchSync);
            //再次执行
            if (isChangeState) syncDispatch(vehicleInfo,dispatch,dispatchSync);
        }
    }

    //同步调度单
    private boolean syncDispatch(VehicleInfo vehicleInfo,Dispatch dispatch, DispatchSync dispatchSync){
        //检查门店同步是否存在改变
        boolean isChangeState = syncStore(vehicleInfo,dispatch,dispatchSync);
        remoteState = dispatchSync.dispatchRemoteState;
        localState = dispatch.state;
        //发现调度信息存在改变
        if (remoteState<localState ) {
            if (remoteState == Dispatch.STATE.LOAD){
                //改变调度状态 -> 等待启程
                dispatchSync.dispatchRemoteState = Dispatch.STATE.TAKEOUT;

            }else if (remoteState == Dispatch.STATE.TAKEOUT){
                //改变调度状态 -> 在途
                boolMessage = server.changeDispatchStateSync(
                        vehicleInfo.carNumber,
                        vehicleInfo.driverCode,
                        3,
                        dispatch.changeTakeOutTime);
                if (checkBoolMessage()){
                    //改变车辆状态 -> 在途
                    boolMessage = server.changeVehicleStateSync(
                            vehicleInfo.carNumber,
                            vehicleInfo.driverCode,
                            3);
                    if (checkBoolMessage()){
                        dispatchSync.dispatchRemoteState = Dispatch.STATE.UNLOAD;
                        isChangeState=true;
                    }
                }

            }else if (remoteState == Dispatch.STATE.UNLOAD){
                boolMessage =  server.changeDispatchStateSync(
                        vehicleInfo.carNumber,
                        vehicleInfo.driverCode,
                        4,
                        dispatch.changeUnloadStateTime
                );
                if (checkBoolMessage()){
                    dispatchSync.dispatchRemoteState = Dispatch.STATE.BACK;
                    isChangeState=true;
                }

            }else if (remoteState== Dispatch.STATE.BACK){
                boolMessage = server.changeVehicleStateSync(
                        vehicleInfo.carNumber,
                        vehicleInfo.driverCode,
                        0);
                if (checkBoolMessage()){
                    dispatchSync.dispatchRemoteState = Dispatch.STATE.COMPLETE;
                    isChangeState=true;
                }
            }


            }
        if (remoteState>localState){
            dispatchSync.dispatchRemoteState = localState;//本地状态回滚
            isChangeState = true;
        }
        if (isChangeState){
            //保存更新后的远程状态
            dispatchSync.save();
        }
        if (remoteState == Dispatch.STATE.COMPLETE){ //调度单已完成
            //检测轨迹是否传输完毕;异常状态同步情况;回收箱同步情况
            if (checkTraceFinish(vehicleInfo)
                    && checkAbnormal()
                    && checkRecycleBox()){
                forceDelete();
                isChangeState = false;
                if (callback!=null) callback.updateDispatch();
            }
        }
        return isChangeState;
        }

    //同步门店
    private boolean syncStore(VehicleInfo vehicleInfo,Dispatch dispatch, DispatchSync dispatchSync){
        boolean isChangeState = false;//默认没有一个改变
        boolean boxFlag;
        for (Store store : dispatch.storeList) {
            boxFlag = syncBox(vehicleInfo,dispatch, store, dispatchSync);// 检测箱子是否存在改变
            if (boxFlag) isChangeState = true;
            localState = store.state;
            remoteState = dispatchSync.storeRemoteStateMap.get(store.customerAgency);
            if (remoteState<localState){
                if (remoteState == Store.STATE.LOAD){
                    dispatchSync.storeRemoteStateMap.put(store.customerAgency,Store.STATE.UNLOAD);
                    isChangeState = true;
                }else if (remoteState == Store.STATE.UNLOAD){
                    dispatchSync.storeRemoteStateMap.put(store.customerAgency,Store.STATE.COMPLETE);
                    isChangeState = true;
                }
            }
            if (remoteState>localState) {//门店状态回退
                if (remoteState == Store.STATE.UNLOAD){
                    // 等待卸货->等待装货
                    dispatchSync.storeRemoteStateMap.put(store.customerAgency,Store.STATE.LOAD);
                    isChangeState = true;
                }
            }
        }
        return  isChangeState;
    }

   //同步箱子
   private boolean syncBox(VehicleInfo vehicleInfo,Dispatch dispatch,Store store, DispatchSync dispatchSync){
            boolean isChangeState = false;//默认没有一个改变

            //循环门店箱子
            for (Box box : store.boxList) {
                remoteState = dispatchSync.boxRemoteStateMap.get(box.barCode);
                localState = box.state;

                //发现一个本地状态与远程状态不同步的箱子
                if (remoteState < localState) {

                    if (remoteState == Box.STATE.LOAD) {
                        //通知后台改变箱子状态  装货->卸货
                        boolMessage = server.changeBoxStateSync(
                                vehicleInfo.carNumber,
                                store.customerAgency,
                                box.barCode,
                                1,
                                box.changeToUnloadStateTime
                        );
                        if (checkBoolMessage()) {
                            dispatchSync.boxRemoteStateMap.put(box.barCode, Box.STATE.UNLOAD);
                            isChangeState = true;//改变成功
                        }

                    } else if (remoteState == Box.STATE.UNLOAD) {
                        //修改远程状态 等待卸货->可回收
                        if (box.isAbnormal) {
                            //如果箱子存在异常, 直接改变远程状态
                            dispatchSync.boxRemoteStateMap.put(box.barCode, Box.STATE.RECYCLE);
                            isChangeState = true;
                        } else {
                            //通知后台改变箱子状态->卸货完成
                            boolMessage = server.changeBoxStateSync(
                                    vehicleInfo.carNumber,
                                    store.customerAgency,
                                    box.barCode,
                                    2,
                                    box.changeToUnloadStateTime);
                            if (checkBoolMessage()) {
                                dispatchSync.boxRemoteStateMap.put(box.barCode, Box.STATE.RECYCLE);
                                isChangeState = true;
                            }
                        }
                    }
                }

                //状态回退
                if (remoteState > localState) {
                    if (remoteState == Box.STATE.UNLOAD) {
                        //等待卸货->等待装货
                        boolMessage = server.changeBoxStateSync(
                                vehicleInfo.carNumber,
                                store.customerAgency,
                                box.barCode,
                                0,
                                0
                        );
                        if (checkBoolMessage()) {
                            dispatchSync.boxRemoteStateMap.put(box.barCode, Box.STATE.LOAD);
                            isChangeState = true;//改变成功
                        }
                    }
                }

            }
            return isChangeState;
        }

    //同步本地轨迹与远程轨迹
    private void syncTraceRecode(VehicleInfo vehicleInfo) {
        Trace trace = new Trace().fetch();
        TraceSync traceSync = new TraceSync().fetch();
        if (trace!=null && traceSync!=null && trace.state == Trace.STATE.RECODE_ING){
            if (trace.path == null || trace.path.size() == 0) return;

            if (traceSync.flag < trace.path.size()){
                int i = server.addTrail(
                        vehicleInfo.carNumber,
                        vehicleInfo.driverCode,
                        JsonUtil.javaBeanToJson(trace.path),
                        trace.path.size(),
                        2
                );
                if (i>0){
                    //更新本地轨迹远程状态对象
                    traceSync.flag = i;
                    traceSync.save();
                }
            }
        }
    }

    //检查本地轨迹与远程轨迹是否完成同步
    private boolean checkTraceFinish(VehicleInfo vehicleInfo){
        Trace trace = new Trace().fetch();
        if (trace!=null){
            if (trace.state == Trace.STATE.RECODE_WAIT){
                return true;
            }
            if (trace.state == Trace.STATE.RECODE_ING){
                return false;
            }
            if (trace.path == null || trace.path.size() == 0){
                return true;
            }
            int i = server.addTrail(
                    vehicleInfo.carNumber,
                    vehicleInfo.driverCode,
                    JsonUtil.javaBeanToJson(trace.path),
                    trace.path.size(),
                    4
                    );
            return i == trace.path.size();

        }
        return false;
    }

   //同步异常信息
    private void syncAbnormal() {
        AbnormalList abnormalList = new AbnormalList().fetch();
        AbnormalListSync abnormalListSync = new AbnormalListSync().fetch();

        if (abnormalList!=null && abnormalList.list!=null && abnormalList.list.size() > 0 && abnormalListSync!=null){
            boolean isSave =false;

            if (abnormalListSync.list==null) abnormalListSync.list = new ArrayList<>();

            //新增
            for (int i = abnormalListSync.list.size(); i< abnormalList.list.size() ;i++){
                isSave = true;
                abnormalListSync.list.add(0);
            }
            //同步
            ListIterator<Integer> it = abnormalListSync.list.listIterator();
            int index = 0;
            int temp;
            Abnormal abnormal;
            while (it.hasNext()){
                temp = it.next();
                abnormal = abnormalList.list.get(index);
                if (abnormal.syncFlag != temp){
                    boolMessage = server.addAbnormal(abnormal);
                    if (checkBoolMessage()){
                        it.set(abnormal.syncFlag);
                        isSave = true;
                    }
                }
                index++;
            }
           if (isSave) abnormalListSync.save();

        }
    }

    //检查异常信息
    private boolean checkAbnormal() {
        AbnormalList abnormalList = new AbnormalList().fetch();
        AbnormalListSync abnormalListSync = new AbnormalListSync().fetch();
        if (abnormalList!=null && abnormalListSync!=null && abnormalList.list!=null && abnormalListSync.list!=null && abnormalList.list.size() == abnormalListSync.list.size()){
            int size = abnormalList.list.size();
            for (int i = 0 ; i < size ; i++){
                if (abnormalList.list.get(i).syncFlag!=abnormalListSync.list.get(i)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    //同步回收箱
    private void syncRecycle() {
        RecyclerBoxList recyclerBoxList = new RecyclerBoxList().fetch();
        RecyclerBoxListSync recyclerBoxListSync = new RecyclerBoxListSync().fetch();
        if(recyclerBoxList!=null && recyclerBoxList.list2!=null){
            //同步纸箱
            for (RecyclerCarton carton : recyclerBoxList.list2){
                server.updateRecycleBoxNumberSync(
                        carton.carNumber,
                        carton.storeId,
                        carton.backCartonNum,
                        carton.adjustCartonNum,
                        carton.time);
            }
        }

        if (recyclerBoxList!=null && recyclerBoxList.list!=null && recyclerBoxList.list.size() > 0 && recyclerBoxListSync!=null){
            boolean isSave =false;
            if (recyclerBoxListSync.list==null) recyclerBoxListSync.list = new ArrayList<>();
            //新增
            for (int i = recyclerBoxListSync.list.size(); i< recyclerBoxList.list.size() ;i++){
                isSave = true;
                recyclerBoxListSync.list.add(0);
            }
            //同步
            ListIterator<Integer> it = recyclerBoxListSync.list.listIterator();
            int index = 0;
            int temp;
            RecyclerBox recyclerBox;
            while (it.hasNext()){
                temp = it.next();
                recyclerBox = recyclerBoxList.list.get(index);
                if (recyclerBox.syncFlag != temp){
                    if (recyclerBox.boxNo.equals("")) continue;
                        //确定同步
                        boolMessage = server.updateRecycleBoxSync(recyclerBox);
                        if (checkBoolMessage()){
                            it.set(recyclerBox.syncFlag);
                            isSave = true;
                        }
                }
                index++;
            }
            if (isSave) recyclerBoxListSync.save();
        }

    }

    //检查回收列表是否存在未同步
    private boolean checkRecycleBox(){
        RecyclerBoxList recyclerBoxList = new RecyclerBoxList().fetch();
        RecyclerBoxListSync recyclerBoxListSync = new RecyclerBoxListSync().fetch();
        if (recyclerBoxList!=null && recyclerBoxListSync!=null && recyclerBoxList.list!=null && recyclerBoxListSync.list!=null && recyclerBoxList.list.size() == recyclerBoxListSync.list.size()){
            int size = recyclerBoxList.list.size();
            for (int i = 0 ; i < size ; i++){
                if (recyclerBoxList.list.get(i).syncFlag!=recyclerBoxListSync.list.get(i)){
                   return false;
                }
            }
           return true;
        }
        return false;
    }
}
