package otmsapp.ping.zerocice;


import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import otmsapp.ping.log.LLog;


/**
 * Created by lzp on 2018/3/7.
 * ICE访问线程池
 */

public class IceIo implements Closeable {

    private static final String TAG = IceIo.class.getSimpleName();

    public interface IFilter{
        void filter() throws Exception;
    }

    private List<IFilter> filterList = new ArrayList<>();

    public void addFilter(IFilter filter){
        filterList.add(filter);
    }


    private IceIo(){}

    private static class Holder{
        private static IceIo INSTANCE = new IceIo();
    }

    private IOThreadPool pool = new IOThreadPool();

    public static IceIo get(){
        return Holder.INSTANCE;
    }

    public void init(String serverName,String host,int port){
            IceClient.getInstance().getBuild().setServerName(serverName).setIp(host).setPort(port).reboot();
    }

    @Override
    public void close() throws IOException {
        filterList.clear();
        IceClient.getInstance().close();
        pool.close();
    }

   //执行过滤
    public void executeFilter() throws Exception {
        for (IFilter f : filterList){
            f.filter();
        }
    }

    public IceClient getIceClient(){
        return IceClient.getInstance();
    }


    /**是否打印信息*/
    private boolean isPrint = true;
    /**
     * 打印访问信息
     */
    public synchronized void println(Object... objects){
        if (isPrint){
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("ICE:"+ IceClient.getInstance().getServerInfo());
            for (Object s: objects){
                stringBuffer.append(" ,").append(s.toString());
            }
            LLog.print(stringBuffer.toString());
        }
    }

    public void setPrintln(boolean f){
        this.isPrint = f;
    }



    private HashMap<String,String> params = new HashMap<>();

    public synchronized void addParams(String k, String v) {
        params.put(k,v);
    }
    public synchronized String getParams(String k,String def){
        String v = params.get(k);
        if (v==null) return def;
        return v;
    }
    public String getParams(String k){
        return getParams(k,"");
    }

}
