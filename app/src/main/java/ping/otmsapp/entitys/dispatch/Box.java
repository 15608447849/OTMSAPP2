package ping.otmsapp.entitys.dispatch;


public class Box{
    public interface STATE{
        int LOAD = 30;
        int UNLOAD = LOAD+1;
        int RECYCLE = UNLOAD+1;
    }

    //二维码
    public String barCode;

    /**
     * 30 待装箱扫码
     * 31 待卸货扫码
     * 32 已卸货待回收
     */
    public int state;

    //是否异常
    public boolean isAbnormal = false;

    //变更为可卸货状态的时间
    public long changeToUnloadStateTime;

    //变更为可回收状态的时间==签收时间 卸货时间
    public long changeToRecycleStateTime;

}
