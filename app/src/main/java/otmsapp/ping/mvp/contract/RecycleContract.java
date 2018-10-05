package otmsapp.ping.mvp.contract;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import otmsapp.ping.entitys.recycler.RecyclerBox;
import otmsapp.ping.mvp.basics.IPresenter;
import otmsapp.ping.mvp.basics.IView;

public class RecycleContract {
    public interface Model{

    }
    public interface View extends IView{
        //更新当前门店
        void updateStoreName(String storeName);
        //更新box信息
        void updateBoxInfo(String info);
        //刷新列表信息
        void refreshList(List<RecyclerBox> recyclerBoxes);

        void openStoreList(CharSequence[] storeNames);
    }
    public interface Presenter extends IPresenter<View> {
        boolean init();
        void setCurrentStoreIndex(int curIndex);
        void updateData();
        //选择门店
        void selectStore();
        //扫码处理
        void scanHandle(String codeBar,int type);
        //添加纸箱
        void addCartonNumber(int number,int type);
    }


}
