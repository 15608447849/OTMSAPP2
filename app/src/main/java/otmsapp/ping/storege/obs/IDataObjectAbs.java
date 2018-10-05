package otmsapp.ping.storege.obs;


import otmsapp.ping.log.LLog;
import otmsapp.ping.storege.inf.ICacheMap;

/**
 * Created by Leeping on 2018/6/28.
 * email: 793065165@qq.com
 */

public abstract class IDataObjectAbs{

    protected abstract String getK();
    protected abstract String convert(Object object);
    protected abstract Object reverse(String data);
    protected  abstract ICacheMap<String,String> getStorage();


    public String getV() {
            return convert(this);
    }

    public void save() {
        getStorage().putValue(getK(),getV());
    }

    public <T extends IDataObjectAbs> T  fetch() {
        try {
            String data = getStorage().getValue(getK());
            return (T) reverse(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void remove() {
        getStorage().removeKey(getK());
    }
}
