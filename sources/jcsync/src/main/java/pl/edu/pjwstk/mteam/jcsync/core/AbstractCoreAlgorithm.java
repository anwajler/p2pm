
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
 * <tt>AbstractCoreAlgortihm</tt> is a set of methods and components of the 
 * core JCSync algorithm.
 * @author Piotr Bucior
 */
public abstract class AbstractCoreAlgorithm{

    /**
     * called when the delivered PUB-SUB message is a indication to create collection.
     * @param cci delivered indication with all of details to create new collection
     * @return <tt>true</tt> if the indication was properly made
     */
    protected abstract boolean onDeliverCreateCollectionIndication(JCSyncCreateCollectionIndication cci);

    /**
     * called when delivered PUB-SUB message is a indication to invoke method.
     * @param imi delivered indication with all of details to invoke method
     * @return <tt>true</tt> if the indication was passed to invoke on collection,
     * <tt>false</tt> if not, e.g. the collection is not exist's on this machine.
     */
    protected abstract boolean onDeliverInvokeMethodIndication(JCSyncInvokeMethodIndication imi);
//    /**
//     * called when 
//     * @param operation
//     * @return
//     */
//    protected abstract boolean onDeliverOperation(JCSyncMessage operation);
//
//    /**
//     * 
//     * @param notify
//     * @return
//     */
//    protected abstract boolean onDeliverNotify(JCSyncMessage notify);

    /**
     * called when the reponse for request of create collection was delivered. 
     * @param response details
     */
    protected abstract boolean onDeliverCreateCollectionResponse(JCSyncMessage response);

    /**
     * called when the reponse for request of make operation was delivered. 
     * @param response details
     */
    protected abstract boolean onDeliverOperationResponse(JCSyncMessage response);

    /**
     * called to send read operation request through PUB-SUB layer
     * @param operation read operation details
     */
    protected abstract boolean requestReadOperation(JCSyncMessage operation);

    /**
     * called to send write operation request through PUB-SUB layer
     * @param operation write operation details
     */
    protected abstract boolean requestWriteOperation(JCSyncMessage operation);

    /**
     * called when the encapsuled message is a <tt>response</tt>type
     * @param msg - response details
     */
    protected abstract boolean onDeliverResponse(JCSyncMessage msg);

    /**
     * Called by {@link pl.edu.pjwstk.mteam.jcsync.collections.implementation.AbstractCollectionsManager AbstractCollectionsManager}
     * to send <tt> 'create collection request'</tt> through overlay.
     * @param ccm all details required to create collection
     * @param subscribe_if_exist if <tt>true</tt> then user will be subscribed 
     * when collection exists in the layer. 
     * @param type the type of mechanism for calling received methods (updates)
     * @param model consistency model of the new collection <b>now is only WeakConsistencyModel</b>
     * @return 0 if the operation ends succesfull, otherwise <tt>CollectionExistsException</tt> will be invoked.
     * @throws CollectionExistException throws if the collection exists and <tt>subscribe_if_exists = false </tt>
     */
    public abstract int requestCreateCollection(JCSyncCreateCollectionMethod ccm,boolean subscribe_if_exist,EventInvoker.InvokerType type, ConsistencyModel model) throws CollectionExistException;
    /**
     * Called by {@link pl.edu.pjwstk.mteam.jcsync.collections.implementation.AbstractCollectionsManager AbstractCollectionsManager}
     * to send <tt> 'write operation request'</tt> through overlay.
     * @param operation details of the update
     * @return results of the invoked method
     */
    public abstract Object publishWriteOperation(JCSyncMethod operation);
    /**
     * Called by {@link pl.edu.pjwstk.mteam.jcsync.core.concurrency.EventInvoker EventInvoker} implementation
     * to invoke delivered (and queued if the <tt>EventInvoker.getType() = InvokerType.QUEUED</tt>) method from overlay.
     * @param indication details
     */
    public abstract void invoke(JCSyncInvokeMethodIndication indication);


}
