package ping.otmsapp.mvp.presenter;

import cn.hy.otms.rpcproxy.sysmanage.UserGlobalInfo;
import ping.otmsapp.entitys.UserInfo;
import ping.otmsapp.mvp.basics.PresenterViewBind;
import ping.otmsapp.mvp.contract.LoginContract;
import ping.otmsapp.mvp.model.SysModel;
import ping.otmsapp.tools.MD5Util;
import ping.otmsapp.tools.StrUtil;

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
                userInfo.id = info.userid;
                userInfo.phone = phone;
                userInfo.name = info.realname;
                userInfo.compName = info.compname;
            userInfo.save();
            tryLogin();
        }else{
            view.toast("请输入正确的手机号码和密码");
        }
    }



}