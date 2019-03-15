package ping.otmsapp.entitys.recycler;

/**
 * Created by Leeping on 2019/1/17.
 * email: 793065165@qq.com
 */
public class RecyclerCarton {

    //用户码
    public String userCode;
    //回收箱 回收车次
    public long carNumber;
    //回收箱 门店编号
    public String storeId;
    //回收箱 回收时间
    public long time;
    //回退箱数量
    public int backCartonNum = 0;
    //调剂箱数量
    public int adjustCartonNum = 0;

}
