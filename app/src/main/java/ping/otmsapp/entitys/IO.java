package ping.otmsapp.entitys;

import ping.otmsapp.iothread.IOThreadPool;
import ping.otmsapp.tools.AppUtil;
import ping.otmsapp.iothread.IOProxy;
import ping.otmsapp.iothread.IOThreadDisruptor;

public class IO {

    private final static IOProxy pool = new IOProxy(new IOThreadPool());

    private final static IOProxy queue = new IOProxy(new IOThreadDisruptor());

    public static void pool(Runnable runnable){
        if (AppUtil.checkUIThread()){
             pool.post(runnable);
        }else{
            runnable.run();
        }
    }

    public  static void queue(Runnable runnable){
        queue.post(runnable);
    }

}
