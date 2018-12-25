package ping.otmsapp.mvp.model;


import cn.hy.otms.rpcproxy.sysmanage.SysManageServicePrx;
import cn.hy.otms.rpcproxy.sysmanage.UpdateRequestPackage;
import cn.hy.otms.rpcproxy.sysmanage.UpdateResponsePackage;
import cn.hy.otms.rpcproxy.sysmanage.UserGlobalInfo;
import ping.otmsapp.mvp.contract.LoginContract;
import ping.otmsapp.zerocice.IceServerAbs;

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

    /**
     * 获取文件
     */
    public UpdateResponsePackage getFileBytes(String remoteFileName, int index){

        try {
            printParam("获取文件数据", remoteFileName, index+"");
            UpdateRequestPackage updateRequestPackage = new UpdateRequestPackage();
            updateRequestPackage.index = index; //第一次请求写1
            updateRequestPackage.size = 1024*1024;//返回块大小 0为整包返回
            updateRequestPackage.filePath = remoteFileName;//需要获取的服务器文件
            updateRequestPackage.OS = 2;
            return getProxy().getVersionPackage(updateRequestPackage);
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
