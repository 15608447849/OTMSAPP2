package otmsapp.ping.mvp.contract;

import android.graphics.Bitmap;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

import cn.hy.otms.rpcproxy.appInterface.SureFeeInfo;
import otmsapp.ping.entitys.cost.FeeDetail;
import otmsapp.ping.mvp.basics.IPresenter;
import otmsapp.ping.mvp.basics.IView;

public class CostContract {
    public interface Model{
        //获取费用账单
        SureFeeInfo[] getCostBill(int userId, String y_m_d);
        //操作费用账单
        boolean optionCostBill(int userId,long train,int opCode);
        //上传图片
        boolean uploadImage(File image,String serverFilePath,String serverFileName);
    }

    public interface View extends IView {
        void updateDataText(@NotNull String text);
        void updateList(List<FeeDetail> data);
        void refreshList();
        void selectPicture(@NotNull File imageFile);
        void previewPictures(@NotNull Bitmap bitmap);

    }

    public interface Presenter extends IPresenter<View> {
        void query(int year,int month,int day);
        void convert(SureFeeInfo[] array, List<FeeDetail> list);
        void rejectCostBill(FeeDetail feeDetail);
        void sureCostBill(FeeDetail feeDetail);
        void preUploadImage(FeeDetail feeDetail);
        void uploadImage(File image);
    }
}
