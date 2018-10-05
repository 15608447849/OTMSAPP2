package otmsapp.ping.mvp.presenter;

import cn.hy.otms.rpcproxy.sysmanage.UserGlobalInfo;
import otmsapp.ping.entitys.UserInfo;
import otmsapp.ping.mvp.basics.PresenterViewBind;
import otmsapp.ping.mvp.contract.LoginContract;
import otmsapp.ping.mvp.model.SysModel;
import otmsapp.ping.tools.MD5Util;
import otmsapp.ping.tools.StrUtil;

public class LoginPresenter extends PresenterViewBind<LoginContract.View> implements LoginContract.Presenter{

    private UserInfo userInfo = new UserInfo().fetch();
    private LoginContract.Model model = new SysModel();

    @Override
    public void tryLogin() {
        if (isBindView() && userInfo!=null) {
            view.onLogin();
        }
    }

    @Override
    public void login(String phone, String password) {
        if (!isBindView()) return;

        if (StrUtil.validate(phone) && StrUtil.validate(password)){
            if (phone.length()!=11){
                view.toast("手机号码格式不正确");
                return;
            }
            if (password.length()<6){
                view.toast("密码长度不正确");
                return;
            }
           view.showProgressBar();
            UserGlobalInfo info = model.login(phone, MD5Util.encryption(password));
            view.hindProgressBar();
            if (info == null){
                view.toast("登录失败,请检查手机号码或密码是否正确");
                return;
            }
            userInfo = new UserInfo();
            userInfo.userId = info.userid;
            userInfo.save();
            tryLogin();
        }else{
            view.toast("请输入正确的手机号码和密码");
        }
    }



}
