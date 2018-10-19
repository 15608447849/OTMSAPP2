package otmsapp.ping.entitys;

import otmsapp.ping.tools.AppUtil;
import otmsapp.ping.zerocice.IOThreadPool;

public class IO {
    private static IOThreadPool pool = new IOThreadPool();
    public static void run(Runnable runnable){
        if (AppUtil.checkUIThread()){
             pool.post(runnable);
        }else{
            runnable.run();
        }
    }
}
