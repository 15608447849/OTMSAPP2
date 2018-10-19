package ping.otmsapp.entitys.except;

public class Abnormal {
    //调度车次
    public long carNumber;
    //异常发生客户机构码
    public String abnormalCustomerAgency;
    //异常箱号
    public String abnormalBoxNumber;
    //异常发生采集时间
    public long abnormalTime;
    //异常类型
    public int abnormalType;
    //异常发生备注
    public String abnormalRemakes;
    //异常发生人用户码
    public String abnormalUserCode;
    //异常处理客户机构
    public String handleCustomerAgency;
    //异常处理人用户码
    public String handlerUserCode;
    //异常处理时间
    public long handlerTime;
    //异常处理备注
    public String handlerRemakes;
    //同步表示
    public int syncFlag;
}
