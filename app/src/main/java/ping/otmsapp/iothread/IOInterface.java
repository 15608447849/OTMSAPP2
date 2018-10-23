package ping.otmsapp.iothread;

public interface IOInterface {
        void post(Runnable runnable);
        void close();
}
