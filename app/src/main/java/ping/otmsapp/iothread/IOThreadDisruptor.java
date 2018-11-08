package ping.otmsapp.iothread;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.ThreadFactory;

import ping.otmsapp.log.LLog;

public class IOThreadDisruptor implements IOInterface{
    private static class RunnableEvent {
        Runnable runnable;
    }


    private static class RunnableEventHandler implements EventHandler<RunnableEvent> {

        @Override
        public void onEvent(RunnableEvent runnableEvent, long sequence, boolean endOfBatch){

            if (runnableEvent.runnable == null) return;
            LLog.print(this+" - "+sequence);
            try {
                runnableEvent.runnable.run();
            }catch (Exception e){
                e.printStackTrace();
            }

            runnableEvent.runnable = null;
        }
    }

    private static class RunnableEventHandler2 implements WorkHandler<RunnableEvent> {

        @Override
        public void onEvent(RunnableEvent runnableEvent) throws Exception {
            if (runnableEvent.runnable == null) return;
//            LLog.print(this.hashCode() +" - "+runnableEvent);
            try {
                runnableEvent.runnable.run();
            }catch (Exception e){
                e.printStackTrace();
            }

            runnableEvent.runnable = null;
        }
    }

    private static class RunnableEventFactory implements EventFactory<RunnableEvent> {

        @Override
        public RunnableEvent newInstance() {
            return new RunnableEvent();
        }
    }

    private static final int BUFFERSIZE = 1024 * 1024;

    private Disruptor<RunnableEvent> disruptor;

    private RingBuffer<RunnableEvent> ringBuffer;

    public IOThreadDisruptor() {
        disruptor = new Disruptor<>(new RunnableEventFactory(), BUFFERSIZE,
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("io-dispatch-"+thread.getId());
                        thread.setDaemon(true);
                        return thread;
                    }
                },
                ProducerType.MULTI,
                new YieldingWaitStrategy()
        );

//        disruptor.handleEventsWith(new RunnableEventHandler());
//        disruptor.handleEventsWith(new RunnableEventHandler());


        disruptor.handleEventsWithWorkerPool(new RunnableEventHandler2(),new RunnableEventHandler2(),new RunnableEventHandler2());


        ringBuffer = disruptor.start();
    }

    @Override
    public void post(Runnable runnable) {
        long index = ringBuffer.next();
        try {
            RunnableEvent event = ringBuffer.get(index);
            event.runnable = runnable;
        } finally {
            ringBuffer.publish(index);
        }
    }

    @Override
    public void close() {
        if (disruptor!=null){
            disruptor.shutdown();
            disruptor = null;
        }
    }



}
