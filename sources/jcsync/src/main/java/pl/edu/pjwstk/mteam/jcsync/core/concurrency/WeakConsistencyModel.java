package pl.edu.pjwstk.mteam.jcsync.core.concurrency;

import java.util.HashMap;
import java.util.concurrent.Semaphore;

/**
 * <tt>ConsistencyModel</tt> implementation
 * @author Piotr Bucior
 * @version 1.0
 */
public class WeakConsistencyModel extends ConsistencyModel {

//    private static final HashMap<Key, Locker> constructorLockers = new HashMap<Key, Locker>(50);
//    private static final HashMap<Key, Locker> topicLockers = new HashMap<Key, Locker>(50);
    private static final HashMap<Key, Locker> writeLockers = new HashMap<Key, Locker>(100);
    private static final HashMap<String, EventInvoker> invokers = new HashMap<String, EventInvoker>(25);

    /**
     * blank constructor
     */
    public WeakConsistencyModel() {
        super();
    }

    public void finalize() throws Throwable {
        super.finalize();
    }

    

//    @Override
//    public synchronized int lockCreateCollection(String collID, int transID) {
//        log.debug("Locking createCollection for: "+ collID + ", tID:" + transID + " , thread:" + Thread.currentThread().getName());
//        Key k;
//        Locker v;
//        try {
//            k = new Key(collID, transID);
//            v = constructorLockers.get(k);
//            v.semaphore.acquire();
//            constructorLockers.remove(k);
//            return v.respCode;
//        } catch (Exception ex) {
//            log.fatal("Fatal error while locking create collection locker: ", ex);
//        } finally {
//            k = null;
//            v = null;
//        }
//        log.debug("Locker createCollection for: " + collID + ", tID:" + transID + " is FREE, thread:" + Thread.currentThread().getName());
//        return -1;
//    }

//    @Override
//    public void unlockCreateCollection(String collID, int transID, int respCode) {
//        log.debug("Unlocking createCollection for: " + collID + ", tID:" + transID + " , thread:" + Thread.currentThread().getName());
//        //TODO make below in all unlock operation
//        Key k = new Key(collID, transID);
//        Locker v = constructorLockers.get(k);
//        if (v != null) {
//            v.setRespCode(respCode);
//            v.semaphore.release();
//        } else {
//            log.fatal("Key: " + k + " does not exist!");
//        }
//    }
//
//    @Override
//    public void lockRead(String collID, int transID) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    @Override
    public Object waitForWriteResults(String collID, int transID) {
        log.debug("Locking write for: " + collID + ", tID:" + transID + " , thread:" + Thread.currentThread().getName());
        Object retVal = null;
        Key k;
        try {
            k = new Key(collID, transID);
            if(!writeLockers.containsKey(k)) return null;
            writeLockers.get(k).semaphore.acquire();
            retVal = writeLockers.get(k).retVal;
            writeLockers.remove(k);
        } catch (Exception ex) {
            log.fatal("Fatal error while locking write locker: ", ex);
            retVal = ex;
        }
        log.debug("Locker write for: " + collID + ", tID:" + transID + " is FREE, thread:" + Thread.currentThread().getName());
        k = null;
        return retVal;
    }

    @Override
    public boolean onDeliverWriteResults(String collID, int transID, int respCode, Object retVal) {
        log.debug("Unlocking write for: " + collID + ", tID:" + transID + " , thread:" + Thread.currentThread().getName());
        Key k = new Key(collID, transID);
        Locker l = writeLockers.get(k);
        if(l!=null){
        writeLockers.get(k).setRespCode(respCode);
        writeLockers.get(k).setRetVal(retVal);
        writeLockers.get(k).semaphore.release();
        return TRUE;
        }return FALSE;
    }

//    @Override
//    public void unlockRead(String collID, int transID, int respCode) {
//    }
//
//    @Override
//    public int lockCreateTopic(String collID, int transID) {
//        log.debug("Locking createTopic for: "+ collID + ", tID:" + transID + " , thread:" + Thread.currentThread().getName());
//        Key k;
//        Locker v;
//        try {
//            k = new Key(collID, transID);
//            v = topicLockers.get(k);
//            v.semaphore.acquire();
//            topicLockers.remove(k);
//            return v.respCode;
//        } catch (Exception ex) {
//            log.fatal("Fatal error while locking create topic locker: ", ex);
//        } finally {
//            k = null;
//            v = null;
//        }
//        log.debug("Locker createTopic for: " + collID + ", tID:" + transID + " is FREE, thread:" + Thread.currentThread().getName());
//        return -1;
//    }
//
//    @Override
//    public void unlockCreateTopic(String collID, int transID, int respCode) {
//        log.debug("Unlocking createTopic for: "+ collID + ", tID:" + transID + " , thread:" + Thread.currentThread().getName());
//        Key k = new Key(collID, transID);
//        topicLockers.get(k).setRespCode(respCode);
//        topicLockers.get(k).semaphore.release();
//    }
//
//    @Override
//    public void initCreateTopicLocker(String collID, int transID) {
//        log.debug("Preparing locker createTopic for: " + collID + ", tID:" + transID + " , thread:" + Thread.currentThread().getName());
//        try {
//            Semaphore z = new Semaphore(1);
//            z.acquire();
//            Key k = new Key(collID, transID);
//            Locker l = new Locker(z, 0);
//            topicLockers.put(k, l);
//        } catch (InterruptedException ex) {
//        }
//    }

    @Override
    public void preInitWaitForWriteResults(String collID, int transID) {
        log.debug("Preparing locker write for: " + collID + ", tID:" + transID + " , thread:" + Thread.currentThread().getName());
        try {
            Semaphore z = new Semaphore(1);
            z.acquire();
            Key k = new Key(collID, transID);
            Locker l = new Locker(z, 0);
            writeLockers.put(k, l);
        } catch (InterruptedException ex) {
        }
    }

    @Override
    public void registerEventInvoker(String collectionID, EventInvoker invoker) {
        log.debug("Registering MethodInvoker: "+invoker.getType().toString()+" fopr collection: "+collectionID);
        invokers.put(collectionID, invoker);
    }

    @Override
    public EventInvoker getEventInvoker(String collID) {
        return invokers.get(collID);
    }

    @Override
    public void removeEventInvoker(String collectionID) {
        EventInvoker inv = invokers.remove(collectionID);
        inv.stopWorking();
        inv = null;
    }

    @Override
    public void pauseEventInvoker(String collID) {
        invokers.get(collID).pauseInvoker();
    }

    @Override
    public void runEventInvoker(String collID) {
        invokers.get(collID).runInvoker();
    }

    @Override
    public void acquireReadAccess(String collID, Object elementIdentifier) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void acquireWriteAccess(String collID, Object elementIdentifier) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void releaseReadAccess(String collID, Object elementIdentifier) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void releaseWriteAccess(String collID, Object elementIdentifier) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

   

//    @Override
//    public void initCreateCollectionLocker(String collID, int transID) {
//        log.debug("Preparing locker createCollection for: " + collID + ", tID:" + transID + " , thread:" + Thread.currentThread().getName());
//        try {
//            Semaphore z = new Semaphore(1);
//            z.acquire();
//            Key k = new Key(collID, transID);
//            Locker l = new Locker(z, 0);
//            constructorLockers.put(k, l);
//        } catch (InterruptedException ex) {
//        }
//    }
}//end StrongEventLocker
