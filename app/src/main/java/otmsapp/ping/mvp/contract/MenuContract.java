package otmsapp.ping.mvp.contract;

import otmsapp.ping.mvp.basics.IPresenter;
import otmsapp.ping.mvp.basics.IView;

/**
 * Created by Leeping on 2018/10/8.
 * email: 793065165@qq.com
 */
public class MenuContract {
    public interface View extends IView {
        void showWindows();
        void dismissWindows();
       void bindPresenter(Presenter presenter);
       void unbindPresenter();
    }
    public interface Presenter extends IPresenter<MenuContract.View> {
        //打开历史任务
        void openHistory();
        //打开预警界面
        void openWarn();
        //打开费用核对
        void openCost();
        //登出
        void logout();
        //退出
        void exit();
    }

}
