/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.pjwstk.mteam.jcsync.core.concurrency;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.jcsync.collections.JCSyncAbstractCollection;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncInvokeMethodIndication;

/**
 *
 * @author Piotr Bucior
 * @version 1.0
 */
public abstract class EventInvoker {

    public static enum InvokerType {
        SIMPLE, QUEUED
    };
    protected final JCSyncAbstractCollection collection;
    protected final String collectionID;
    protected final InvokerType type;
    protected static Logger log = Logger.getLogger("pl.edu.pjwstk.mteam.jcsync.core.concurrency.MethodInvoker");
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();


    protected EventInvoker(JCSyncAbstractCollection coll, String collID, InvokerType type) {
        if (type.equals(InvokerType.SIMPLE) || type.equals(InvokerType.QUEUED)) {
            this.type = type;
            this.collection = coll;
            this.collectionID = collID;
        } else {
            throw new IllegalArgumentException("Illegal Invoker type selected!");            
        }
    }

    public abstract void onDeliverEvent(JCSyncInvokeMethodIndication indication);
    public abstract long getLastQueuedOperationID();
    public JCSyncAbstractCollection getRegisteredCollection() {
        return this.collection;
    }
    public abstract void stopWorking();
    public InvokerType getType(){
        return this.type;
    }
    protected void accessWriteLock(){
        this.lock.readLock().lock();
    }
    protected void freeWriteLock(){
        this.lock.readLock().unlock();
    }
    public abstract void pauseInvoker();
    public abstract void runInvoker();

}
