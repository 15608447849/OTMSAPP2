package otmsapp.ping.mvp.contract;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import cn.hy.otms.rpcproxy.appInterface.AppSchedvech;
import otmsapp.ping.entitys.history.DispatchDetail;
import otmsapp.ping.mvp.basics.IPresenter;
import otmsapp.ping.mvp.basics.IView;

public class HistoryContract {
    public interface Model{
        //登录
        AppSchedvech[] getHistoryTask(int userId, String y_m_d);
    }
    public interface View extends IView {
        void updateDataText(@NotNull String text);
        void updateList(List<DispatchDetail> data);
    }
    public interface Presenter extends IPresenter<View> {
        void query(int year,int month,int day);
    }
}
