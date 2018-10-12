package otmsapp.ping.entitys.history;

/**
 * 查询的订单详细信息
 */
public class DispatchDetail {
    //门店数
    public int storeNum;
    //总箱数
    public int boxNum;
    //调度单号
    public String dispatchNo;
    //车牌号
    public String plateNo;
    //签收时间
    public long time;
    //里程数
    public double mileage;
    //初始运费
    public double initialFee;
    //异动运费
    public double abnormalFee;
    //总费用
    public double totalFee;
    //应结运费
    public double settle;
    //实结运费
    public double realSettle;
}
