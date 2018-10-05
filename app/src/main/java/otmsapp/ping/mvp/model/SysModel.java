package otmsapp.ping.mvp.model;


import cn.hy.otms.rpcproxy.sysmanage.SysManageServicePrx;
import cn.hy.otms.rpcproxy.sysmanage.UserGlobalInfo;
import otmsapp.ping.mvp.contract.LoginContract;
import otmsapp.ping.zerocice.IceServerAbs;

public class SysModel extends IceServerAbs<SysManageServicePrx> implements LoginContract.Model {

    /**
     *  用户登陆
     */
    @Override
    public UserGlobalInfo login(String phone,String password){
        try {
            printParam("用户登陆",phone,password);
            UserGlobalInfo userGlobalInfo = getProxy().loginCS(phone,password);
            if (userGlobalInfo.roleid == 2){
                return userGlobalInfo;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }


}
