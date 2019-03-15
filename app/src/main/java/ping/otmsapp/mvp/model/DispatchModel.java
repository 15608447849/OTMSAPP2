package ping.otmsapp.mvp.model;

import java.util.Iterator;

import ping.otmsapp.entitys.dispatch.Box;
import ping.otmsapp.entitys.dispatch.Dispatch;
import ping.otmsapp.entitys.dispatch.Store;
import ping.otmsapp.entitys.dispatch.VehicleInfo;
import ping.otmsapp.entitys.except.Abnormal;
import ping.otmsapp.entitys.except.AbnormalList;
import ping.otmsapp.mvp.contract.DispatchContract;

public class DispatchModel implements DispatchContract.model{

    private boolean isCancel; //装货时 是否在进行取消操作

    private DispatchContract.model.Callback callback;

    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void tryCorrectDispatchByLoadState(Dispatch dispatch) {
        if (dispatch.state != Dispatch.STATE.LOAD){
            return ;
        }

        int tempPos = 0;
        for (Store store : dispatch.storeList){
            int tempStoreBoxIndex = 0;
            for (Box box : store.boxList){
                //如果箱子为等待卸货
                if (box.state == Box.STATE.UNLOAD){
                    tempStoreBoxIndex++;
                    tempPos++;
                }else if (box.state == Box.STATE.RECYCLE){
                    //箱子为回收 (错误状态,重置)
                    box.state = Box.STATE.LOAD;
                }
            }
            store.loadScanIndex = tempStoreBoxIndex;
            checkStoreIsToUnLoadState(store);
        }
        dispatch.loadScanBoxIndex = tempPos;
        checkDispatchIsToTakeoutState(dispatch);
    }

    @Override
    public boolean checkStoreIsToCompleteState(Store store) {
        //如果当前门店 总卸货数量 = 此门店总数量 >> 改变状态为 完成状态
        if (store.unloadScanIndex == store.boxSum){
            //修改门店状态
            store.state = Store.STATE.COMPLETE;
            if (callback!=null){
                //通知改变门店状态
                callback.onStoreStateChanged(store);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean checkStoreIsToUnLoadState(Store store) {
        //如果当前门店已扫码数量 = 此门店总数量 >> 改变状态为 等待卸货
        if (store.loadScanIndex == store.boxSum){
            //此门店进入卸货状态
            store.state = Store.STATE.UNLOAD;
            if (callback!=null){
                //通知改变门店状态
                callback.onStoreStateChanged(store);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean checkStoreIsToLoadState(Store store) {
        //如果当前门店已扫码数量 < 此门店总数量 ->> 改变状态为 等待装货
        if (store.loadScanIndex < store.boxSum){
            //此门店进入卸货状态
            store.state = Store.STATE.LOAD;
            if (callback!=null){
                //通知改变门店状态
                callback.onStoreStateChanged(store);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean checkDispatchIsToBackState(Dispatch dispatch) {
        //如果当前卸货箱数=调度单全部箱数 >> 改变调度信息
        if (dispatch.unloadScanBoxIndex == dispatch.storeBoxSum){
            //设置调度单签收状态时间
            dispatch.changeUnloadStateTime = System.currentTimeMillis();
            //转为等待回程状态
            dispatch.state = Dispatch.STATE.BACK;
            if (callback!=null){
                //通知改变调度单状态(调度单)
                callback.onDispatchStateChanged(dispatch);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean checkDispatchIsToTakeoutState(Dispatch dispatch) {
        if (dispatch.loadScanBoxIndex == dispatch.storeBoxSum){
            //调度单 转为 等待启程
            dispatch.state = Dispatch.STATE.TAKEOUT;
            if (callback!=null){
                //通知改变调度单状态(调度单)
                callback.onDispatchStateChanged(dispatch);
            }

        }
        return false;
    }

    @Override
    public boolean checkDispatchIsToLoadState(Dispatch dispatch) {
        if (dispatch.loadScanBoxIndex < dispatch.storeBoxSum){
            //调度单 转为 等待启程
            dispatch.state = Dispatch.STATE.LOAD;
            if (callback!=null){
                //通知改变调度单状态(调度单)
                callback.onDispatchStateChanged(dispatch);
            }
            return true;
        }
        return false;
    }

    //装货扫码
    @Override
    public boolean scannerByLoad(String codeBar, Dispatch dispatch,Store store) {

        int curPos = dispatch.loadScanBoxIndex; //当前已扫描总箱数
        int curStoreBoxIndex = store.loadScanIndex;//当前门店已扫描总箱数
        for (Box box : store.boxList) {
            if (box.barCode.equals(codeBar) ) {
                int state = box.state;
                if (state == Box.STATE.LOAD && !isCancel) {//等待装货
                    //箱子改为卸货状态的时间
                    box.changeToUnloadStateTime = System.currentTimeMillis();
                    //箱子 转为卸货状态
                    box.state = Box.STATE.UNLOAD;
                    //改变当前门店已扫码数+1
                    curStoreBoxIndex++;
                    store.loadScanIndex = curStoreBoxIndex;
                    //改变调度单总扫码数+1
                    curPos++;
                    dispatch.loadScanBoxIndex = curPos;
                    //检测门店是否进入卸货状态
                    checkStoreIsToUnLoadState(store);
                    //检测调度单是否进入等待启程状态
                    checkDispatchIsToTakeoutState(dispatch);
                } else if (state == Box.STATE.UNLOAD){//等待卸货

                    if (isCancel){
                        //箱子改为卸货状态的时间
                        box.changeToUnloadStateTime = 0;
                        //箱子 转为装货状态
                        box.state = Box.STATE.LOAD;
                        //改变当前门店已扫码数-1
                        curStoreBoxIndex--;
                        store.loadScanIndex =curStoreBoxIndex;
                        //改变调度单总扫码数-1
                        curPos--;
                        dispatch.loadScanBoxIndex = curPos;
                        //检测门店是否返回装货状态
                        checkStoreIsToLoadState(store);
                        //检测调度单是否返回装货状态
                        checkDispatchIsToLoadState(dispatch);
                        //检测是否存在可以处理的异常
                        checkUnloadScanException(dispatch,store,box);
                    }else{
                        if (callback!=null) callback.onScanLoadRepeat(box);
                    }
                }
                if (callback!=null) callback.onScanBoxSuccess(box);
                return true;
            }
        }
        if (callback!=null) callback.onScanFail(codeBar);
        return false;
    }

    //卸货扫码
    @Override
    public boolean scannerByUnLoad(String codeBar, Dispatch dispatch,Store store) {
        int curPos = dispatch.unloadScanBoxIndex;
        int curStoreBoxIndex = store.unloadScanIndex;
        for (Box box : store.boxList){

            if (box.barCode.equals(codeBar)){
                //发现一个可扫码得箱子
                if (box.state == Box.STATE.UNLOAD) {
                    //设置改变成回收状态的时间
                    box.changeToRecycleStateTime = System.currentTimeMillis();
                    //箱子转为回收状态
                    box.state = Box.STATE.RECYCLE;
                    //改变当前门店卸货总数+1
                    curStoreBoxIndex++;
                    store.unloadScanIndex = curStoreBoxIndex;
                    //改变调度单卸货总数+1
                    curPos++;
                    dispatch.unloadScanBoxIndex = curPos;
                    //检测门店是否进入完成状态
                    checkStoreIsToCompleteState(store);
                    //检测调度单是否进入等待回程状态
                    checkDispatchIsToBackState(dispatch);
                    //检测是否存在可以处理的异常
                    checkUnloadScanException(dispatch,store,box);
                }else if (box.state == Box.STATE.RECYCLE){
                    //等待回收的箱子 -> 重复扫码通知
                    if (callback!=null) callback.onScanUnloadRepeat(box);
                }
                if (callback!=null) callback.onScanBoxSuccess(box);
                return true;
            }
        }
        //记录异常信息
        generateUnloadScanException(codeBar,dispatch, store);
        if (callback!=null) callback.onScanFail(codeBar);
        return false;
    }

    //记录异常信息
    @Override
    public void generateUnloadScanException(String codeBar, Dispatch dispatch, Store store) {
        AbnormalList abnormalList = new AbnormalList().fetch();
        if (abnormalList==null) return;
        Iterator<Abnormal> iterator = abnormalList.list.iterator();
        Abnormal abnormal;
        while (iterator.hasNext()){
            abnormal = iterator.next();
            if (abnormal.abnormalBoxNumber.equals(codeBar)
                    &&abnormal.abnormalCustomerAgency.equals(store.customerAgency)
                    && abnormal.abnormalType == 1){
                //已存在的记录
                return;
            }
        }
        abnormal = new Abnormal();
        VehicleInfo vehicleInfo = new VehicleInfo().fetch();
        abnormal.abnormalUserCode = vehicleInfo.driverCode;
        abnormal.carNumber = vehicleInfo.carNumber;
        abnormal.abnormalCustomerAgency = store.customerAgency;
        abnormal.abnormalBoxNumber = codeBar;
        abnormal.abnormalTime = System.currentTimeMillis();
        abnormal.abnormalType = 1;
        abnormal.abnormalRemakes = "扫码卸货错误";
        abnormal.syncFlag++; //同步标识
        abnormalList.list.add(abnormal);

        abnormalList.save();
    }

    //尝试处理异常记录
    @Override
    public void checkUnloadScanException(Dispatch dispatch, Store store, Box box) {
        AbnormalList abnormalList = new AbnormalList().fetch();
        if (abnormalList==null) return;
        for (Abnormal abnormal : abnormalList.list){
            if (abnormal.abnormalBoxNumber.equals(box.barCode) && abnormal.abnormalType == 1){
                //已存在的扫码错误-添加处理信息
                VehicleInfo vehicleInfo = new VehicleInfo().fetch();
                abnormal.handleCustomerAgency = store.customerAgency;
                abnormal.handlerUserCode = vehicleInfo.driverCode;
                abnormal.handlerTime = System.currentTimeMillis();
                abnormal.handlerRemakes = "扫码卸货异常纠正";
                abnormal.syncFlag++;
                return;
            }
        }


    }
    //货差异常
    @Override
    public void unloadAbnormal(Dispatch dispatch, Store store,Box box,AbnormalList abnormalList,VehicleInfo vehicleInfo) {

        Abnormal abnormal = new Abnormal();
        abnormal.carNumber = vehicleInfo.carNumber; //车次号
        abnormal.abnormalUserCode = vehicleInfo.driverCode;
        abnormal.abnormalCustomerAgency = store.customerAgency;//异常门店
        abnormal.abnormalBoxNumber = box.barCode;//异常箱号
        abnormal.abnormalTime = System.currentTimeMillis();//异常发生时间
        abnormal.abnormalType = 3;//异常类型 - 货差异常
        abnormal.abnormalRemakes = "手动签收,未正确扫码,无法获取异常详情"; //异常标注
        abnormal.syncFlag++;
        abnormalList.list.add(abnormal);

        box.isAbnormal = true;
        box.state = Box.STATE.RECYCLE;
        store.unloadScanIndex++;
        dispatch.unloadScanBoxIndex++;
        //检测门店是否进入完成状态
        checkStoreIsToCompleteState(store);
        //检测调度单是否进入等待回程状态
        checkDispatchIsToBackState(dispatch);
    }

    @Override
    public void setLoadCancel(boolean isChecked) {
        isCancel = isChecked;
    }
}
