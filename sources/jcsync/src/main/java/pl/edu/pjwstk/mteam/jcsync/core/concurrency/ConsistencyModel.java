package pl.edu.pjwstk.mteam.jcsync.core.concurrency;

import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;

/**
 * Class represents the logics to manage the consistency of collection.
 * @author Piotr Bucior
 * @version 1.0
 */
public abstract class ConsistencyModel {

    /**
     * console logger
     */
    protected static final Logger log = Logger.getLogger("pl.edu.pjwstk.mteam.jcsync.core.EventLocker");
    /**
     * static logic TRUE value
     */
    public static final boolean TRUE = true;
    /**
     * static logic FALSE value
     */
    public static final boolean FALSE = false;

    /**
     * blank protected constructor, to create new instance of 
     * <tt>ConsistencyModel</tt> take a look at the subclass, called 
     * {@link pl.edu.pjwstk.mteam.jcsync.core.concurrency.WeakConsistencyModel WeakConsistencyModel} 
     */
    protected ConsistencyModel() {
    }

    @Override
    public void finalize() throws Throwable {
    }

    /**
     * 
     * @param operation
     */
    //public abstract void lockRead(String collID, int transID);
    
    /**
     * called when the method is send through overlay to publish update operation 
     * and it must wait for the return value of the invoked method
     * @param collID collection identifier
     * @param transID the operation identifier associated with the methon which 
     * the return value will be returned 
     * @return return value of invoked method
     */
    public abstract Object waitForWriteResults(String collID, int transID);

    /**
     * called when the method was invoked and the return value is already known. 
     * @param collID collection identifier
     * @param transID the operation identifier associated with the methon which 
     * the return value will be returned 
     * @param respCode response code of the invoked method
     * @param retVal returned value of the invoked method
     * @return depend of the implementation
     */
    public abstract boolean onDeliverWriteResults(String collID, int transID, int respCode, Object retVal);

    /**
     * inform <tt>ConsistencyModel</tt> which type of the <tt>EventInvoker</tt> will handle given collection
     * @param collectionID collection identifier
     * @param invoker <tt>EventInvoker</tt> instance
     */
    public abstract void registerEventInvoker(String collectionID, EventInvoker invoker);

    /**
     * removes <tt>EventInvoker</tt> frome the list
     * @param collectionID collection identifier
     */
    public abstract void removeEventInvoker(String collectionID);

    /**
     * called to pause <tt>EventInvoker</tt>
     * @param collID collection identifier
     */
    public abstract void pauseEventInvoker(String collID);

    /**
     * called to run <tt>EventInvoker</tt>
     * @param collID collection identifier
     */
    public abstract void runEventInvoker(String collID);

    /**
     * returns <tt>EventInvoker</tt> which holds given collection
     * @param collID collectiuon identifier
     */
    public abstract EventInvoker getEventInvoker(String collID);

    /**
     * called when the specific consistency model needs to acquire acces to the collection 
     * in the 'READ' mode
     * @param collID collection identifier
     * @param elementIdentifier collection the item to which application need to acquire access
     */
    public abstract void acquireReadAccess(String collID, Object elementIdentifier);

    /**
     * called when the specific consistency model needs to acquire acces to the collection 
     * in the 'WRITE' mode
     * @param collID collection identifier
     * @param elementIdentifier collection the item to which application need to acquire access
     */
    public abstract void acquireWriteAccess(String collID, Object elementIdentifier);

    /**
     * called when the specific consistency model is releasing access in the 'READ' mode
     * @param collID collection identifier
     * @param elementIdentifier collection the item to which application need to acquire access
     */
    public abstract void releaseReadAccess(String collID, Object elementIdentifier);

    /**
     * called when the specific consistency model is releasing access in the 'WRITE' mode
     * @param collID collection identifier
     * @param elementIdentifier collection the item to which application need to acquire access
     */
    public abstract void releaseWriteAccess(String collID, Object elementIdentifier);

    /**
     * called to initialise mechanism to waits for the write results
     * @param collID collection identifier
     * @param transID the operation identifier associated with the methon which 
     * the return value will be returned 
     */
    public abstract void preInitWaitForWriteResults(String collID, int transID);
    //public abstract ConsistencyModel newInstance();
    //public abstract void unlockRead(String collID, int transID, int respCode);
    //public abstract int lockCreateCollection(String collID,int transID);
    //public abstract void unlockCreateCollection(String collID,int transID,int respCode);
    //public abstract int lockCreateTopic(String collID, int transID);
    //public abstract void unlockCreateTopic(String collID,int transID, int respCode);
    //public abstract void initCreateTopicLocker(String id,int transID);
    // public abstract void initCreateCollectionLocker(String id,int transID);

    /**
     * class Key represents the relationship of the collection name to transaction (operation) identifier 
     */
    protected class Key {

        /**
         * collection identifier
         */
        public String id;
        /**
         * the operation identifier associated with the methon which 
     * the return value will be returned
         */
        public int transID;

        /**
         * create new key
         * @param id collection identifier
         * @param transID operation identifier
         */
        public Key(String id, int transID) {
            this.id = id;
            this.transID = transID;
        }

        @Override
        public boolean equals(Object obj) {
            Key b = (Key) obj;
            if (this.id.equals(b.id)) {
                if (this.transID == b.transID) {
                    return true;
                }
            }
            return false;
        }

        @Override
        protected void finalize() throws Throwable {
            this.id = null;
            super.finalize();
        }

        @Override
        public int hashCode() {
            return ((String) (id + transID)).hashCode();
        }

        @Override
        public String toString() {
            return "collID: " + this.id + ", transID: " + this.transID;
        }
    }

    /**
     * class represents the relationship between semaphore (uses as a locker) 
     * and invoked method returned value and the response code. 
     */
    protected class Locker {

        /**
         * uses to lock process (e.g. <tt>waitForWriteResults(...)</tt> ) 
         */
        public Semaphore semaphore;
        /**
         * response code of invoked method
         */
        public int respCode;
        /**
         * returned value
         */
        public Object retVal = null;

        /**
         * create new insatnce
         * @param sem
         * @param resp
         */
        public Locker(Semaphore sem, int resp) {
            this.semaphore = sem;
            this.respCode = resp;
        }

        /**
         * set up the response code
         */
        public void setRespCode(int resp) {
            this.respCode = resp;
        }

        /**
         * set up the returned value
         */
        public void setRetVal(Object ret) {
            this.retVal = ret;
        }

        @Override
        protected void finalize() throws Throwable {
            this.semaphore = null;
            this.retVal = null;
            super.finalize();
        }
    }
}//end EventLocker