package ping.otmsapp.entitys.map;

import java.util.ArrayList;

import ping.otmsapp.entitys.JsonLocalSqlStorage;

import static ping.otmsapp.entitys.map.Trace.STATE.RECODE_WAIT;

public class Trace extends JsonLocalSqlStorage {

    public interface STATE{
        int RECODE_WAIT = 0;  //等待记录
        int RECODE_ING = RECODE_WAIT+1;    //记录中
        int RECODE_FINISH = RECODE_ING+1; //记录完成
    }

    /**
     *  当前记录状态
     *  1 等待记录 2 记录中 3 记录结束
     */

    public int state = RECODE_WAIT;

    //连续轨迹- 规则过滤后
    public ArrayList<MTraceLocation> path = new ArrayList<>();

    //当前原始里程数 m
    public float mileage = 0.0f;

}
