package ping.otmsapp.mvp.contract;

import org.jetbrains.annotations.Nullable;

import ping.otmsapp.entitys.dispatch.Box;
import ping.otmsapp.entitys.dispatch.Dispatch;
import ping.otmsapp.entitys.dispatch.Store;
import ping.otmsapp.entitys.dispatch.VehicleInfo;
import ping.otmsapp.entitys.except.AbnormalList;
import ping.otmsapp.entitys.upload.BillImage;
import ping.otmsapp.mvp.basics.IPresenter;
import ping.otmsapp.mvp.basics.IView;

public class DispatchContract {

    public interface model{
        void setCallback(Callback callback);
        //尝试纠正装货状态调度单
        void tryCorrectDispatchByLoadState(Dispatch dispatch);
        //检测门店是否进入完成状态
        boolean checkStoreIsToCompleteState(Store store);
        //检查门店是否进入卸货状态
        boolean checkStoreIsToUnLoadState(Store store);
        //检测门店是否返回装货状态
        boolean checkStoreIsToLoadState(Store store);
        //检测调度单是否进入完成状态
        boolean checkDispatchIsToBackState(Dispatch dispatch);
        //检查调度单是否进入等待启程的状态
        boolean checkDispatchIsToTakeoutState(Dispatch dispatch);
        //检测调度单是否返回装货状态
        boolean checkDispatchIsToLoadState(Dispatch dispatch);
        //扫码处理 二维码,调度单,选择的门店
        boolean scannerByLoad(String codeBar, Dispatch dispatch,Store store);
        boolean scannerByUnLoad(String codeBar, Dispatch dispatch,Store store);
        //生成卸货扫码异常
        void generateUnloadScanException(String codeBar,Dispatch dispatch, Store store);
        //成功卸货-检查是否存在可处理的扫码异常
        void checkUnloadScanException(Dispatch dispatch, Store store, Box box);
        //卸货异常签收-货差异常
        void unloadAbnormal(Dispatch dispatch, Store store, Box box, AbnormalList abnormalList,VehicleInfo vehicleInfo);

        interface Callback{
            //通知门店状态已改变
            void onStoreStateChanged(Store store);
            //通知调度单状态已改变
            void onDispatchStateChanged(Dispatch dispatch);
            //箱子扫码成功
            void onScanBoxSuccess(Box box);
            //扫码失败
            void onScanFail(String codeBar);
            //卸载-重复扫码
            void onScanUnloadRepeat(Box box);
        }
    }

    public interface View extends IView {
        void dialog(String btnName,String message,Presenter.Callback callback);

        void updateDispatch();

        void playScanSuccessMusic();

        void playScanFailMusic();

        void resetListIndex();
    }

    public interface Presenter extends IPresenter<DispatchContract.View>{



        interface Callback{
            void onCallback();
        }

        //验证数据完整性
        void validateDispatch();
        //处理条形码
        void codeBarHandle(String codeBar,int allowDispatchState , int selectIndex);
        //开启行程
        void take();
        //开启行程确定
        void takeSure();
        //回仓
        void back();
        //回仓确定
        void backSure();
        //装载全部
        void loadALL();
        //卸货提交货差
        void unloadAbnormal(int index);

        void unloadAbnormalSure(int index);
        //上传门店签收单据
        void uploadBillImage(@Nullable BillImage billImage);
    }

}
