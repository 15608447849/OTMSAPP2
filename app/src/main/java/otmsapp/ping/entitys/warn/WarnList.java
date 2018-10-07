package otmsapp.ping.entitys.warn;

import java.util.ArrayList;

import otmsapp.ping.entitys.JsonLocalSqlStorage;

public class WarnList extends JsonLocalSqlStorage {
    //时间戳
    public long timeStamp = System.currentTimeMillis();
    //实际预警信息
    public ArrayList<WarnItem> list = new ArrayList<>();
}
