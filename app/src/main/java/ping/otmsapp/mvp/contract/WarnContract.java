package ping.otmsapp.mvp.contract;

import java.util.List;

import ping.otmsapp.entitys.warn.WarnItem;
import ping.otmsapp.mvp.basics.IPresenter;
import ping.otmsapp.mvp.basics.IView;

public class WarnContract {
    public interface Model{
        boolean handleWarn(String codeBar,long time);
    }
    public interface View extends IView{
        //刷新列表信息
        void refreshList(List<WarnItem> warnItems);
        //弹出提示框-是否移除
        void dialogHandler(String msg,Presenter.Callback callback);
    }
    public interface Presenter extends IPresenter<View> {
        interface Callback {
            void action();
        }
        //更新信息
        void updateData();
        //移除数据
        void removeData(WarnItem item);
    }


}
