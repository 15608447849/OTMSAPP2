package ping.otmsapp.mvp.presenter;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import ping.otmsapp.entitys.dispatch.Box;
import ping.otmsapp.entitys.dispatch.Dispatch;
import ping.otmsapp.entitys.dispatch.Store;
import ping.otmsapp.entitys.dispatch.VehicleInfo;
import ping.otmsapp.entitys.except.AbnormalList;
import ping.otmsapp.entitys.map.Trace;
import ping.otmsapp.entitys.recycler.RecyclerBoxList;
import ping.otmsapp.entitys.recycler.RecyclerBoxListSync;
import ping.otmsapp.entitys.tuples.Tuple2;
import ping.otmsapp.entitys.upload.FileUploadItem;
import ping.otmsapp.entitys.upload.FileUploadItemList;
import ping.otmsapp.log.LLog;
import ping.otmsapp.mvp.basics.PresenterViewBind;
import ping.otmsapp.mvp.contract.DispatchContract;
import ping.otmsapp.mvp.model.DispatchModel;
import ping.otmsapp.tools.JsonUtil;

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
        public void onScanLoadRepeat(Box box) {
            if (isBindView()) {
                view.toast("箱号:"+box.barCode+"\n装货时重复扫码");
                view.playScanFailMusic();
            }
        }

        @Override
        public void onScanUnloadRepeat(Box box) {
            if (isBindView()) {
                view.toast("箱号:"+box.barCode+"\n卸货时重复扫码");
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
        LLog.print("处理二维码: " + codeBar,"操作类型: "+allowDispatchState);
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
            view.toast("调度单数据异常,无法进行操作");
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
            LLog.print("点击启程");
            view.dialog("准备出发", "确定将记录您的行驶轨迹,请确保开启GPS定位", new Callback() {
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
        dispatch.changeTakeOutTime = System.currentTimeMillis();
        dispatch.state = Dispatch.STATE.UNLOAD;
        dispatch.save();
        Trace trace = new Trace().fetch();
        trace.state = Trace.STATE.RECODE_ING;
        trace.save();
        view.resetListIndex();
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
            LLog.print("点击返程");
            //判断是否存在回收箱需要同步
            if (!checkRecycleBox()) {
                view.toast("终端后台正在同步回收箱信息,请稍后再试");
                return;
            }
            view.dialog("返回仓库", "注意:请确认已上传回单\n如果您已成功返回仓库,将结束行程记录", new Callback() {
                @Override
                public void onCallback() {
                    backSure();
                }
            });
        }else{
            view.toast("操作失败\n请先卸货完成在进行操作");
        }
    }

    //检测是否存在未同步的回收箱
    private boolean checkRecycleBox() {
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

    @Override
    public void backSure() {
        Dispatch dispatch = new Dispatch().fetch();
        if (dispatch==null) return;
        dispatch.state = Dispatch.STATE.COMPLETE;
        dispatch.save();
        Trace trace = new Trace().fetch();
        trace.state = Trace.STATE.RECODE_FINISH;
        trace.save();
        view.resetListIndex();
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

    //货差
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

    @Override
    public void uploadBillImage(@Nullable FileUploadItem billImage) {
        LLog.print("上传回单: "+ JsonUtil.javaBeanToJson(billImage));
        VehicleInfo vehicleInfo = new VehicleInfo().fetch();
        if (vehicleInfo==null) return;
        assert billImage != null;
        //车次号
        billImage.param.put("dispatchId",String.valueOf(vehicleInfo.carNumber));
        //远程路径
        billImage.serverPath = "/sched/img/" + vehicleInfo.carNumber + "/";

        FileUploadItemList list = new FileUploadItemList().fetch();
        if (list == null) list = new FileUploadItemList();
        list.list.add(billImage);
        list.save();
        if (isBindView()) view.toast("门店单据已提交,稍后将自动上传");
    }

    @Override
    public void setLoadCache(boolean checked) {
        model.setLoadCancel(checked);
    }


}
