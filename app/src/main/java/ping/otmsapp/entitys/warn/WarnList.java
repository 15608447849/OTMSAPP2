package ping.otmsapp.entitys.warn;

import java.util.ArrayList;

import ping.otmsapp.entitys.JsonLocalSqlStorage;

public class WarnList extends JsonLocalSqlStorage {
    //时间戳
    public long timeStamp = System.currentTimeMillis();
    //实际预警信息
    public ArrayList<WarnItem> list = new ArrayList<>();
}
