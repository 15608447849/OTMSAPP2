package ping.otmsapp.mvp.contract;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import cn.hy.otms.rpcproxy.appInterface.AppSchedvech;
import ping.otmsapp.entitys.history.DispatchDetail;
import ping.otmsapp.mvp.basics.IPresenter;
import ping.otmsapp.mvp.basics.IView;

public class HistoryContract {
    public interface Model{
        AppSchedvech[] getHistoryTask(int userId, String y_m_d);
    }
    public interface View extends IView {
        void updateDataText(@NotNull String text);
        void updateList(List<DispatchDetail> data);
        void refreshList();
    }
    public interface Presenter extends IPresenter<View> {
        void query(int year,int month,int day);
        void convert(AppSchedvech[] array, List<DispatchDetail> list);
    }
}
