/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.edu.pjwstk.mteam.jcsync.core;

import pl.edu.pjwstk.mteam.jcsync.core.concurrency.ConsistencyModel;
import pl.edu.pjwstk.mteam.jcsync.core.concurrency.EventInvoker;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncCreateCollectionIndication;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncInvokeMethodIndication;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncMessage;
import pl.edu.pjwstk.mteam.jcsync.exception.CollectionExistException;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncCreateCollectionMethod;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncMethod;

/**
 *
 * @author pb
 */
public abstract class AbstractCoreAlgorithm{

    protected abstract boolean onDeliverCreateCollectionIndication(JCSyncCreateCollectionIndication request);

    protected abstract boolean onDeliverOperation(JCSyncMessage operation);

    protected abstract boolean onDeliverNotify(JCSyncMessage notify);

    protected abstract boolean onDeliverCreateCollectionResponse(JCSyncMessage response);

    protected abstract boolean onDeliverOperationResponse(JCSyncMessage response);

    protected abstract boolean requestReadOperation(JCSyncMessage operation);

    protected abstract boolean requestWriteOperation(JCSyncMessage operation);

    protected abstract boolean onDeliverResponse(JCSyncMessage msg);

    public abstract int requestCreateCollection(JCSyncCreateCollectionMethod op,boolean subscribe_if_exist,EventInvoker.InvokerType type, ConsistencyModel model) throws CollectionExistException;
    public abstract Object publishWriteOperation(JCSyncMethod operation);
    public abstract void invoke(JCSyncInvokeMethodIndication indication);


}
