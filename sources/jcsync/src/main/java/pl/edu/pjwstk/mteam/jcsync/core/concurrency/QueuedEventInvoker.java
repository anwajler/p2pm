package pl.edu.pjwstk.mteam.jcsync.core.concurrency;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import pl.edu.pjwstk.mteam.jcsync.collections.JCSyncAbstractCollection;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncCoreAlgorithm;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncInvokeMethodIndication;

/**
 *
 * @author Piotr Bucior
 * @version 1.0
 */
public class QueuedEventInvoker extends EventInvoker {

    private final PriorityBlockingQueue<JCSyncInvokeMethodIndication> events_queue;
    private static final ThreadGroup eventConsumers_group = new ThreadGroup("Method_Invokers");
    private final EventConsumer invoker;
    private final String loggerName;
    private volatile boolean isWorking = true;
    private volatile boolean isPaused = true;
    private volatile long latestOperationID = 0;

    static {
        eventConsumers_group.setMaxPriority(Thread.MAX_PRIORITY - 1);
    }

    public QueuedEventInvoker(JCSyncAbstractCollection coll, String collID, InvokerType type) {
        super(coll, collID, type);
        this.events_queue = new PriorityBlockingQueue<JCSyncInvokeMethodIndication>(500,new Comparator<JCSyncInvokeMethodIndication>() {
            public int compare(JCSyncInvokeMethodIndication ind1, JCSyncInvokeMethodIndication ind2) {
                if(ind1.getOperationID() == ind2.getOperationID()) return 0;
                else if(ind1.getOperationID() > ind2.getOperationID()) return 1;
                else return 0;
            }
        });
        this.invoker = new EventConsumer("CONSUMER:" + collID);
        this.invoker.start();
        loggerName = getType().toString() + " MI (" + collectionID + ") -";
        log.info(loggerName + " initialised");
    }

    @Override
    public synchronized void onDeliverEvent(JCSyncInvokeMethodIndication indication) {
        if(this.latestOperationID< indication.getOperationID())
            this.latestOperationID = indication.getOperationID();
        this.events_queue.add(indication);
    }

    @Override
    public void stopWorking() {
        this.isWorking = false;
        this.invoker.interrupt();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    @Override
    public void pauseInvoker() {
        try {
            //waits till current invoked method ends
            this.lock.writeLock().lock();
            //pause invoker
            this.isPaused = true;
        } finally {
            //unlock locker in current thread
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public void runInvoker() {
        this.isPaused = false;
    }

    @Override
    public long getLastQueuedOperationID() {
        return this.latestOperationID;
    }

    private class EventConsumer extends Thread implements Runnable {

        JCSyncInvokeMethodIndication ind;

        private EventConsumer(String name) {
            super(name);
        }

        public void run() {
            while (isWorking) {
                while (!isPaused) {
                    try {
                        //log.trace(loggerName + " attempt to access write lock ...");
                        accessWriteLock();
                        //log.trace(loggerName + " waits for indication ...");
                        if(events_queue.isEmpty()){
                            Thread.sleep(100);
                            continue;
                        }
                        ind = events_queue.take();
                        if (ind != null) {
                            log.trace(loggerName + " invoking method :" + ind.toSimpleString());
                            JCSyncCoreAlgorithm.getInstance().invoke(ind);
                            log.trace(loggerName + " unlocking write lock...");                            
                            ind = null;
                        }
                        //Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        if (isWorking) {
                            log.error("Error while taking indication from queue: ", ex);
                        }
                    }
                    finally{
                        freeWriteLock();
                    }

                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                }
            }
        }
    }
}
