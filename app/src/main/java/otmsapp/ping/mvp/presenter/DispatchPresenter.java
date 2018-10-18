package otmsapp.ping.mvp.presenter;

import java.util.ArrayList;
import java.util.Iterator;

import otmsapp.ping.entitys.dispatch.Box;
import otmsapp.ping.entitys.dispatch.Dispatch;
import otmsapp.ping.entitys.dispatch.Store;
import otmsapp.ping.entitys.dispatch.VehicleInfo;
import otmsapp.ping.entitys.except.AbnormalList;
import otmsapp.ping.entitys.map.Trace;
import otmsapp.ping.entitys.tuples.Tuple2;
import otmsapp.ping.log.LLog;
import otmsapp.ping.mvp.basics.PresenterViewBind;
import otmsapp.ping.mvp.contract.DispatchContract;
import otmsapp.ping.mvp.model.DispatchModel;

public class DispatchPresenter extends PresenterViewBind<DispatchContract.View> implements DispatchContract.Presenter {


    private DispatchContract.model model = new DispatchModel();


    private DispatchContract.model.Callback callback = new DispatchContract.model.Callback() {
        @Override
        public void onStoreStateChanged(Store store) {

        }

        @Override
        public void onDispatchStateChanged(Dispatch dispatch) {

        }

        @Override
        public void onScanBoxSuccess(Box box) {
            if (isBindView()){
                view.playScanSuccessMusic();
            }
        }

        @Override
        public void onScanFail(String codeBar) {
            if (isBindView()) {
                view.playScanFailMusic();
            }
        }

        @Override
        public void onScanUnloadRepeat(Box box) {
            if (isBindView()) {
                view.playScanFailMusic();
            }
        }
    };

    public DispatchPresenter() {
        model.setCallback(callback);
    }

    @Override
    public void validateDispatch() {
        Dispatch dispatch = new Dispatch().fetch();
        if (dispatch!=null){
            model.tryCorrectDispatchByLoadState(dispatch);
            dispatch.save();
        }
        if (isBindView()) view.updateDispatch();
    }

    @Override
    public void codeBarHandle(String codeBar,int allowDispatchState , int selectIndex) {

        LLog.print("二维码: "+codeBar+" ,当前选择的状态: "+ allowDispatchState+" ,当前选择的下标:"+selectIndex);
        if (!isBindView()) return;

        if (allowDispatchState==-1)  return;

        Dispatch dispatch = new Dispatch().fetch();
        if (dispatch==null) {
            view.toast("暂无调度任务");
            return;
        }

        if (selectIndex==-1) {
            view.toast("请选择门店");
            return;
        }
        if (selectIndex>dispatch.storeList.size()){
            return;
        }
        Store store = dispatch.storeList.get(selectIndex);

        //打开进度条
        boolean flag = false;

        if (allowDispatchState == 1){

            //装货中或者等待期程 - 可以进行装货扫码
            if (dispatch.state == Dispatch.STATE.LOAD || dispatch.state == Dispatch.STATE.TAKEOUT){
                //装货扫码
                flag = model.scannerByLoad(codeBar,dispatch,store);
            }
        }
        else if (allowDispatchState == 2){
            if (dispatch.state == Dispatch.STATE.UNLOAD){
                //卸货扫码
                 flag = model.scannerByUnLoad(codeBar,dispatch,store);
            }
        }

        //关闭进度条
        if (flag) {
            dispatch.save();
            view.updateDispatch();
        }
    }

    @Override
    public void take() {
        Dispatch dispatch = new Dispatch().fetch();
        if (dispatch==null){
            view.toast("暂无调度任务");
            return;
        }
        if (dispatch.state == Dispatch.STATE.TAKEOUT){
            view.dialog("准备发出", "确定将记录您的行驶轨迹,请确保开启GPS定位", new Callback() {
                @Override
                public void onCallback() {
                    takeSure();
                }
            });
        }else{
            view.toast("操作失败");
        }
    }

    @Override
    public void takeSure() {
        Dispatch dispatch = new Dispatch().fetch();
        if (dispatch==null) return;
        dispatch.state = Dispatch.STATE.UNLOAD;
        dispatch.save();
        Trace trace = new Trace().fetch();
        trace.state = Trace.STATE.RECODE_ING;
        trace.save();
        view.updateDispatch();
        view.toast("祝您一路平安");

    }

    @Override
    public void back() {
        Dispatch dispatch = new Dispatch().fetch();
        if (dispatch==null){
            view.toast("暂无调度任务");
            return;
        }
        if (dispatch.state == Dispatch.STATE.BACK){
            view.dialog("返回仓库", "如果您已成功返回仓库,将结束行程记录", new Callback() {
                @Override
                public void onCallback() {
                    backSure();
                }
            });
        }else{
            view.toast("操作失败");
        }
    }

    @Override
    public void backSure() {
        Dispatch dispatch = new Dispatch().fetch();
        if (dispatch==null) return;
        dispatch.state = Dispatch.STATE.COMPLETE;
        dispatch.save();
        Trace trace = new Trace().fetch();
        trace.state = Trace.STATE.RECODE_FINISH;
        trace.save();
        view.updateDispatch();
        view.toast("您辛苦了,调度单将自动完成数据同步");

    }

    @Override
    public void loadALL() {
        //装载全部
        Dispatch dispatch = new Dispatch().fetch();
        if (dispatch==null){
            view.toast("暂无调度任务");
            return;
        }

        if (dispatch.state == Dispatch.STATE.LOAD){
            //获取剩余待扫码箱号
            ArrayList<Tuple2<String,Store>> remains = new ArrayList<>();

            for (Store store : dispatch.storeList){
                if (store.state == Store.STATE.LOAD){
                    for (Box box : store.boxList){
                        if (box.state == Box.STATE.LOAD){
                            remains.add(new Tuple2<>(box.barCode,store));
                        }
                    }
                }
            }

            for (Tuple2<String,Store> tuple2 : remains){
                model.scannerByLoad(tuple2.getValue0(),dispatch,tuple2.getValue1());
            }
            dispatch.save();
            view.updateDispatch();
        }else{
            view.toast("操作失败");
        }

    }

    //提交货差
    @Override
    public void unloadAbnormal(final int index) {
        //装载全部
        Dispatch dispatch = new Dispatch().fetch();
        if (dispatch==null){
            view.toast("暂无调度任务");
            return;
        }
        if (dispatch.state == Dispatch.STATE.UNLOAD){
            if (index==-1){
                view.toast("请选择门店");
                return;
            }
            Store store = dispatch.storeList.get(index);
            if (store.state == Store.STATE.UNLOAD){

                view.dialog("签收失败", "选中门店:" + store.storeName + "\n剩余货物无法正常签收交接,确定将产生货差异常", new Callback() {
                    @Override
                    public void onCallback() {
                        unloadAbnormalSure(index);
                    }
                });

            }else{
                view.toast("门店全部签收完成");
            }

        }else{
            view.toast("操作失败");
        }
    }

    @Override
    public void unloadAbnormalSure(int index) {
        Dispatch dispatch = new Dispatch().fetch();
        AbnormalList abnormalList = new AbnormalList().fetch();
        VehicleInfo vehicleInfo  = new VehicleInfo().fetch();

        Store store = dispatch.storeList.get(index);
        for (Box box : store.boxList){
            if (box.state == Box.STATE.UNLOAD){
                model.unloadAbnormal(dispatch,store,box,abnormalList,vehicleInfo);
            }
        }
        abnormalList.save();
        dispatch.save();
        view.updateDispatch();
    }





}
