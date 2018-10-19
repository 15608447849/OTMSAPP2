package ping.otmsapp.entitys.dispatch;

import java.util.HashMap;

import ping.otmsapp.entitys.JsonLocalSqlStorage;

public class DispatchSync extends JsonLocalSqlStorage {

    /**
     * 调度信息远程状态码
     */
    public Integer dispatchRemoteState;
    /**
     * 门店远程状态码 门店机构码-门店远程状态
     */
    public HashMap<String,Integer> storeRemoteStateMap;
    /**
     * 箱子远程状态码 箱子二维码-箱子远程状态码
     */
    public HashMap<String,Integer> boxRemoteStateMap;

}
