package ping.otmsapp.entitys.dispatch;

import java.util.List;


public class Store {
    public interface STATE {
        int LOAD = 20;
        int UNLOAD = LOAD+1;
        int COMPLETE = UNLOAD +1;
    }
    //门店名
    public String storeName;
    //详细地址
    public String detailedAddress;
    //客户机构码
    public String customerAgency;
    //相关扫码箱编号列表
    public List<Box> boxList;
    //指定配送顺序
    public int specifiedOrder;
    //集装箱总数
    public int boxSum;
    //当前装箱已扫描总数
    public int loadScanIndex;
    //当前卸货已扫描总数
    public int unloadScanIndex;
    /**
     * 20 门店等待装货
     * 21 门店等待卸货
     * 22 门店卸货完成
     */
    public int state;
}
