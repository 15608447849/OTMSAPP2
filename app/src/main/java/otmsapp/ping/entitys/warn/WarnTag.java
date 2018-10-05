package otmsapp.ping.entitys.warn;

import java.util.ArrayList;
import java.util.List;

public class WarnTag {
    //时间戳
    public long current;
    public long remote;
    //实际预警信息
    public ArrayList<WarnState> warnStateList = new ArrayList<>();
}
