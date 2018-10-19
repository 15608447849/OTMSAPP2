package ping.otmsapp.entitys;

import ping.otmsapp.tools.AppUtil;
import ping.otmsapp.zerocice.IOThreadPool;

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
