package ping.otmsapp.zerocice;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by Leeping on 2018/7/13.
 * email: 793065165@qq.com
 */

public abstract class IceServerAbs<P extends Ice.ObjectPrx> {

    private Class<P> cls;

    public IceServerAbs() {
        ParameterizedType parameterizedType = (ParameterizedType)this.getClass().getGenericSuperclass();
        Type[] typeArr = parameterizedType.getActualTypeArguments();
        this.cls = (Class) typeArr[0];
    }

    private IceClient getIce(){
        return IceHelper.get().getIceClient();
    }

    protected P getProxy() throws Exception{
        IceHelper.get().executeFilter();
        return getIce().getServicePrx(cls);
    }

    protected void printParam(Object... params){
        IceHelper.get().println(params);
    }

    /**ice string数组的参数传递 */
    public String[] convert(Object... objects){
        String[] strings = new String[objects.length];
        String temp;
        for (int i = 0;i<objects.length;i++){
            temp = String.valueOf(objects[i]) ;
            strings[i] = temp==null || temp.equals("null")?"":temp;
        }
        return strings;
    }



    protected String getParam(String k){
        return IceHelper.get().getParams(k);
    }
}
