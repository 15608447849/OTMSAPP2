package ping.otmsapp.server.dispatch;

public abstract class DispatchThreadAbs extends Thread{

    public DispatchThreadAbs() {
        setName("dispatch-t-"+getId());
        setDaemon(true);
        start();
    }

    private volatile boolean isStart = true;

    public void stopRun(){
        isStart = false;
        executeDispatch();
    }

    @Override
    public void run() {
        while (isStart) {
            synchronized (this){
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            execute();
        }
    }

    public void executeDispatch(){
        synchronized (this){
            this.notify();
        }
    }

    abstract void execute();
}
