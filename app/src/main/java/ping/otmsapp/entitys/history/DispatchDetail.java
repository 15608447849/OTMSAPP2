package ping.otmsapp.entitys.history;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询的订单详细信息
 */
public class DispatchDetail {
    //车次号
    public long trainNo;
    //总费用
    public double totalFee;
    //初始运费
    public double initialFee;
    //异动运费
    public double abnormalFee;
    //门店列表
    public List<StoreDetail> storeDetails = new ArrayList<>();

}
