/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.edu.pjwstk.mteam.jcsync.core.concurrency;

import pl.edu.pjwstk.mteam.jcsync.collections.JCSyncAbstractCollection;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncCoreAlgorithm;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncInvokeMethodIndication;

/**
 *
 * @author Piotr Bucior
 * @version 1.0
 */
public class SimpleInvoker extends EventInvoker{

    public SimpleInvoker(JCSyncAbstractCollection coll, String collID, InvokerType type){
        super(coll, collID, type);
    }
    
    @Override
    public void onDeliverEvent(JCSyncInvokeMethodIndication indication) {
        //wait for synchronization process if necessary
        accessWriteLock();
        //just forward to core
        JCSyncCoreAlgorithm.getInstance().invoke(indication);
        freeWriteLock();
    }

    @Override
    public void stopWorking() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void pauseInvoker() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void runInvoker() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getLastQueuedOperationID() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
