package ping.otmsapp.iothread;


import ping.otmsapp.log.LLog;

public class IOProxy implements IOInterface {

    private IOInterface imps;

    public IOProxy(IOInterface imps) {
        this.imps = imps;
    }

    public void post(Runnable runnable){
        imps.post(runnable);
    }

    @Override
    public void close(){
        imps.close();
    }
}
