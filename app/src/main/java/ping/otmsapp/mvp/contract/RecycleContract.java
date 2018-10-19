package ping.otmsapp.mvp.contract;

import java.util.List;

import ping.otmsapp.entitys.recycler.RecyclerBox;
import ping.otmsapp.mvp.basics.IPresenter;
import ping.otmsapp.mvp.basics.IView;

public class RecycleContract {

    public interface View extends IView{
        //更新当前门店
        void updateStoreName(String storeName);
        //更新box信息
        void updateBoxInfo(String info);
        //刷新列表信息
        void refreshList(List<RecyclerBox> recyclerBoxes);
        //打开门店列表
        void openStoreList(CharSequence[] storeNames);
    }
    public interface Presenter extends IPresenter<View> {
        //初始化
        boolean init();
        //设置当前选中门店下标
        void setCurrentStoreIndex(int curIndex);
        //更新数据
        void updateData();
        //选择门店
        void selectStore();
        //扫码处理
        void scanHandle(String codeBar,int type);
        //添加纸箱
        void addCartonNumber(int number,int type);
    }


}
