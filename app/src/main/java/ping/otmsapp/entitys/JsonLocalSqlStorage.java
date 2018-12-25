package ping.otmsapp.entitys;


import ping.otmsapp.storege.db.SQLiteStore;
import ping.otmsapp.storege.inf.ICacheMap;
import ping.otmsapp.storege.obs.IDataObjectAbs;
import ping.otmsapp.tools.JsonUtil;

/**
 * Created by Leeping on 2018/6/28.
 * email: 793065165@qq.com
 */

public class JsonLocalSqlStorage extends IDataObjectAbs {

    @Override
    protected ICacheMap<String, String> getStorage() {
        return SQLiteStore.get().getSql();
    }

    @Override
    protected String convert(Object object) {
        return JsonUtil.javaBeanToJson(object);
    }

    @Override
    protected Object reverse(String data) {
        return JsonUtil.jsonToJavaBean(data,this.getClass());
    }


    @Override
    protected String getK() { return getClass().getSimpleName(); }


}
