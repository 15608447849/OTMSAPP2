package otmsapp.ping.entitys.dispatch;

import otmsapp.ping.entitys.JsonLocalSqlStorage;
import otmsapp.ping.entitys.UserInfo;

/**
 * 司机信息
 */
public class VehicleInfo extends JsonLocalSqlStorage {
    //司机用户编码
    public String driverCode;
    //车牌号
    public String vehicleCode;
    //车次号
    public long carNumber;
    //司机手机号
    public String phoneNo;
    //司机姓名
    public String driverName;
    //承运商名称
    public String carrierName;


}
