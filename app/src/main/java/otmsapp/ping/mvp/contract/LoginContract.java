package otmsapp.ping.mvp.contract;

import cn.hy.otms.rpcproxy.sysmanage.UserGlobalInfo;
import otmsapp.ping.mvp.basics.IPresenter;
import otmsapp.ping.mvp.basics.IView;

public class LoginContract {
    public interface Model{
        //登录
        UserGlobalInfo login(String phone,String password);
    }
    public interface View extends IView{
        void onLogin();
    }
    public interface Presenter extends IPresenter<View>{
        void tryLogin();
        void login(String phone, String password);
    }
}
