package ping.otmsapp.mvp.presenter;

import java.util.ArrayList;
import java.util.List;

import ping.otmsapp.entitys.dispatch.Box;
import ping.otmsapp.entitys.dispatch.Dispatch;
import ping.otmsapp.entitys.dispatch.Store;
import ping.otmsapp.entitys.dispatch.VehicleInfo;
import ping.otmsapp.entitys.recycler.RecyclerBox;
import ping.otmsapp.entitys.recycler.RecyclerBoxList;
import ping.otmsapp.mvp.basics.PresenterViewBind;
import ping.otmsapp.mvp.contract.RecycleContract;
import ping.otmsapp.tools.StrUtil;

public class RecyclePresenter extends PresenterViewBind<RecycleContract.View> implements RecycleContract.Presenter {

    private VehicleInfo vehicleInfo = new VehicleInfo().fetch();
    private Dispatch dispatch = new Dispatch().fetch();
    private int cIndex = 0;
    private Store cStore;
    private CharSequence[] storeNames;
    private ArrayList<String> notAllowList = new ArrayList<>();

    @Override
    public boolean init() {
        if (dispatch==null) return false;
        storeNames = new CharSequence[dispatch.storeList.size()];
        for (int i = 0;  i<dispatch.storeList.size() ;i++ ){
            storeNames[i] =  dispatch.storeList.get(i).storeName;
            for (Box box : dispatch.storeList.get(i).boxList){
                if ( !(box.state == Box.STATE.RECYCLE && !box.isAbnormal) ){
                    //不允许回收的
                    notAllowList.add(box.barCode);
                }
            }
        }

        return true;
    }

    @Override
    public void setCurrentStoreIndex(int curIndex) {
        if (curIndex>=0){
            cIndex = curIndex;
        }
        cStore = dispatch.storeList.get(cIndex);
        if (isBindView()) view.updateStoreName(storeNames[cIndex].toString());
    }

    @Override
    public void updateData() {
        RecyclerBoxList list = new RecyclerBoxList().fetch();
        int sumCount = 0;
        int cartonCount = 0;
        List<RecyclerBox> recyclerBoxes = new ArrayList<>();
        for (RecyclerBox recyclerBox : list.list){
            if (recyclerBox.boxNo.equals("")){
                cartonCount++;
            }else{
                recyclerBoxes.add(recyclerBox);
            }
            sumCount++;
        }

        if (isBindView()){
            view.updateBoxInfo(StrUtil.format("当前总回收数量: %d,其中纸箱数量: %d",sumCount,cartonCount));
            view.refreshList(recyclerBoxes);
        }
    }

    @Override
    public void selectStore() {
        view.openStoreList(storeNames);
    }

    @Override
    public void scanHandle(String codeBar,int type) {
        if (codeBar==null || codeBar.equals("") || type == -1 || !isBindView()) return;
        RecyclerBoxList list = new RecyclerBoxList().fetch();
        if (list == null) return;
        view.showProgressBar();
        if (validRecycleBox(codeBar,type)){
            RecyclerBox recyclerBox = createRecycleBox(codeBar,type);
            list.list.add(recyclerBox);
            list.save();
        }
        view.hindProgressBar();
        updateData();
    }

    @Override
    public void addCartonNumber(int number,int type) {
        view.showProgressBar();

        RecyclerBoxList list = new RecyclerBoxList().fetch();

        if (list.list.size() + number > 2000) {
            view.toast("回收箱总数超过上限2000");
        }else{
            RecyclerBox recyclerBox;
            for (int i = 0; i < number; i++){
                recyclerBox = createRecycleBox("",type);
                list.list.add(recyclerBox);
            }
            list.save();
        }
        view.hindProgressBar();
        updateData();
    }

    private boolean validRecycleBox(String codeBar,int type) {
        //回收箱
        if (notAllowList.contains(codeBar)) {
            view.toast("编号: ["+codeBar+"]\n不可进行回收操作");
            return false;
        }
        RecyclerBoxList list = new RecyclerBoxList().fetch();
        //判断重复
        for (RecyclerBox recyclerBox : list.list){
            if (recyclerBox.boxNo.equals(codeBar)){
                boolean update = false;
                if (recyclerBox.type!= type){
                    //更新
                    recyclerBox.type = type;
                    update= true;
                }
                if (!recyclerBox.storeId .equals(cStore.customerAgency)){
                    recyclerBox.storeId = cStore.customerAgency;
                    update = true;
                }
                if (update) {
                    recyclerBox.syncFlag++;
                    view.toast("编号: ["+codeBar+"]\n已更新信息");
                }
                list.save();
                return false;
            }
        }

        return true;
    }

    private RecyclerBox createRecycleBox(String codeBar, int type) {
        RecyclerBox recyclerBox = new RecyclerBox();
            recyclerBox.boxNo = codeBar;
            recyclerBox.type = type;
            recyclerBox.userCode = vehicleInfo.driverCode;
            recyclerBox.carNumber = vehicleInfo.carNumber;
            recyclerBox.storeId = cStore.customerAgency;
            recyclerBox.time = System.currentTimeMillis();
            recyclerBox.syncFlag++;
        return recyclerBox;

    }


}
