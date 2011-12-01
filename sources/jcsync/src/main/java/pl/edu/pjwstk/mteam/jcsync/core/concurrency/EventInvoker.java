package pl.edu.pjwstk.mteam.jcsync.core.concurrency;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.jcsync.collections.JCSyncAbstractCollection;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncInvokeMethodIndication;

/**
 * This mechanism is a development of multi-thread synchronization technology 
 * to access data in an application running on the local machine, 
 * except that the synchronization process takes place in the network, 
 * while nodes are taking the role of threading in the network. 
 *<p>All messages that contain information about making updates on the 
 * collection are passed to this class. This module is responsible for the 
 * invoking methods contained in the messages
 * @author Piotr Bucior
 * @version 1.0
 */
public abstract class EventInvoker {

    
    /**
     * set of the implemented invoker types 
     */
    public static enum InvokerType {

        /**
         * {@link pl.edu.pjwstk.mteam.jcsync.core.concurrency.SimpleInvoker SimpleInvoker}
         */
        SIMPLE,
        /**
         * {@link pl.edu.pjwstk.mteam.jcsync.core.concurrency.QueuedEventInvoker QueuedEventInvoker}
         */
        QUEUED
    };
    /**
     * collection on which the invoker works
     */
    protected final JCSyncAbstractCollection collection;
    /**
     * collection identifier
     */
    protected final String collectionID;
    /**
     * type of the invoker
     */
    protected final InvokerType type;
    /**
     * console logger
     */
    protected static Logger log = Logger.getLogger("pl.edu.pjwstk.mteam.jcsync.core.concurrency.MethodInvoker");
    /**
     * is used to control access to collection
     */
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();


    /**
     * create new instance of the <tt>EventInvoker</tt>.
     * 
     * @param coll collection on which the invoker will works
     * @param collID collection ID
     * @param type <tt>EventInvoker</tt> type
     */
    protected EventInvoker(JCSyncAbstractCollection coll, String collID, InvokerType type) {
        if (type.equals(InvokerType.SIMPLE) || type.equals(InvokerType.QUEUED)) {
            this.type = type;
            this.collection = coll;
            this.collectionID = collID;
        } else {
            throw new IllegalArgumentException("Illegal Invoker type selected!");            
        }
    }

    /**
     * adds new indication to the stack
     * @param indication to put on the stack
     */
    public abstract void onDeliverEvent(JCSyncInvokeMethodIndication indication);
    /**
     * returns ID of the last added operation to stack / queue
     * @return ID as long
     */
    public abstract long getLastQueuedOperationID();
    /**
     * returns collection on which the invoker works
     */
    public JCSyncAbstractCollection getRegisteredCollection() {
        return this.collection;
    }
    /**
     * stop working 
     */
    public abstract void stopWorking();
    /**
     * returns type of invoker
     * @return <tt>InvokerType.SIMPLE</tt> or <tt>InvokerType.QUEUED</tt>
     */
    public InvokerType getType(){
        return this.type;
    }
    /**
     * called everytime when the <tt>EventInvoker</tt> implementation goes 
     * to the collection editing phase
     */
    protected void accessWriteLock(){
        this.lock.readLock().lock();
    }
    /**
     * called everytime when the <tt>EventInvoker</tt> implementation goes out 
     * from the collection editing phase
     */
    protected void freeWriteLock(){
        this.lock.readLock().unlock();
    }
    /**
     * used to pase the <tt>EventInvoker</tt>
     */
    public abstract void pauseInvoker();
    /**
     * used to run the <tt>EventInvoker</tt>
     */
    public abstract void runInvoker();

}
