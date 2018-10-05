package otmsapp.ping.entitys;


import otmsapp.ping.storege.db.SQLiteStore;
import otmsapp.ping.storege.inf.ICacheMap;
import otmsapp.ping.storege.obs.IDataObjectAbs;
import otmsapp.ping.tools.JsonUti;

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
        return JsonUti.javaBeanToJson(object);
    }

    @Override
    protected Object reverse(String data) {
        return JsonUti.jsonToJavaBean(data,this.getClass());
    }


    @Override
    protected String getK() { return getClass().getSimpleName(); }


}
