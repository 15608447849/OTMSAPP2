package ping.otmsapp.entitys.dispatch;

import java.util.List;

import ping.otmsapp.entitys.JsonLocalSqlStorage;


/**
 * 调度单
 */
public class Dispatch extends JsonLocalSqlStorage {


    public interface STATE{
        int LOAD = 10;
        int TAKEOUT = LOAD+1;
        int UNLOAD = TAKEOUT+1;
        int BACK = UNLOAD + 1;
        int COMPLETE = BACK+1;
    }
    //门店信息
    public List<Store> storeList;
    /**
     * 10 扫码装载任务(未启程)
     * 11 启程
     * 12 配送装卸任务(途中)
     * 13 返程
     * 14 完成
     */
    public int state;
    //所有门店箱子总数
    public int storeBoxSum;
    //当前 装货扫描 箱子总数
    public int loadScanBoxIndex;
    //当前 卸货扫描 箱子总数
    public int unloadScanBoxIndex;
    //变更 启程 状态的时间
    public long changeTakeOutTime;
    //变更 签收 状态的时间
    public long changeUnloadStateTime;


}
