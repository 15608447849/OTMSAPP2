package ping.otmsapp.mvp.contract;

import cn.hy.otms.rpcproxy.sysmanage.UserGlobalInfo;
import ping.otmsapp.mvp.basics.IPresenter;
import ping.otmsapp.mvp.basics.IView;

public class LoginContract {
    public interface Model{
        //登录
        UserGlobalInfo login(String phone,String password);
    }
    public interface View extends IView{
        void onLogin(long role);
    }
    public interface Presenter extends IPresenter<View>{
        void tryLogin();
        void login(String username, String password);
    }
}
