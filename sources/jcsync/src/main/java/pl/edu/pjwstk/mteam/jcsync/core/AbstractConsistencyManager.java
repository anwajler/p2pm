package pl.edu.pjwstk.mteam.jcsync.core;

import java.lang.reflect.InvocationTargetException;
import pl.edu.pjwstk.mteam.jcsync.core.pubsub.MessageDeliveryObserver;
import pl.edu.pjwstk.mteam.jcsync.core.pubsub.PubSubCustomisableAlgorithm;
import pl.edu.pjwstk.mteam.jcsync.operation.JCsyncAbstractOperation;
import pl.edu.pjwstk.mteam.jcsync.operation.MethodCarrier;
import pl.edu.pjwstk.mteam.jcsync.operation.RegisteredOperations;
import pl.edu.pjwstk.mteam.pubsub.algorithm.implementation.DefaultCustomizableAlgorithm;
import pl.edu.pjwstk.mteam.pubsub.message.request.PublishRequest;

/**
 *class represents an abstract mechanism for data integrity management. 
 * <p>Every action Performed on collections (after initialisation) is passed 
 * through this mechanism. 
 * <p>Through Appropriate implementations have the ability programmer to set 
 * priorities for specific action (for example, When We decide to set priority 
 * to invoke events for the selected node), to filter out certain requests or 
 * queuing of events to their calling in the order given.
 * <p>Class methods are divided into 3 categories:
 * <p>- first category provide a set of  communication method between 
 * CoreAlgorith and ConsistencyManager (they are used primarily to transmit 
 * received messages), they include:
 * <p>{@link AbstractConsistencyManager#indicationReceived(JCsyncAbstractOperation) indicationReceived} 
 * <br>{@link AbstractConsistencyManager#requestReceived(pl.edu.pjwstk.mteam.pubsub.message.request.PublishRequest, pl.edu.pjwstk.mteam.jcsync.operation.JCsyncAbstractOperation) requestReceived} 
 * <br>{@link AbstractConsistencyManager#responseReceived(pl.edu.pjwstk.mteam.jcsync.operation.JCsyncAbstractOperation, short) responseReceived}
 * <br>{@link AbstractConsistencyManager#registerObject(java.lang.String) registerObject}
 * <br>{@link AbstractConsistencyManager#setCoreAlgorith(pl.edu.pjwstk.mteam.jcsync.core.JCsyncAlgorithInterface) setCoreAlgorith}
 * <br><p>- second provide a mechanism for communication between 
 * JCSyncAbstractSharedObject (and its derivatives) and ConsistencyManager. 
 * That can be used for example to suspend the worker threads until a response 
 * and/or indication is received . These are:
 * <p>{@link AbstractConsistencyManager#beforeRequestSend(pl.edu.pjwstk.mteam.jcsync.operation.JCsyncAbstractOperation, boolean) beforeRequestSend}
 * <br>{@link AbstractConsistencyManager#afterRequestSend(pl.edu.pjwstk.mteam.jcsync.operation.JCsyncAbstractOperation, boolean) afterRequestSend}
 * <p>- the third category is a set of internal methods for better transparency 
 * of the implementation:
 * <p>{@link AbstractConsistencyManager#invoke(pl.edu.pjwstk.mteam.jcsync.core.JCSyncAbstractSharedObject, pl.edu.pjwstk.mteam.jcsync.operation.MethodCarrier) invoke}
 * 
 * 
 * @author Piotr Bucior
 * @version 2.0
 */
public abstract class AbstractConsistencyManager {
    /**
     * Called when the response is received from the layer.
     * @param op operation for which the answer is received
     * @param respCode response code contained in {@link JCSyncConstans JCSyncConstans}
     * Every 'pub-sub' codes is also converted to JCSync codes, take a look at {@link MessageDeliveryObserver#onDeliverPubSubResponse(java.lang.String, short, short, long)  onDeliverPubSubResponse}
     */
    public abstract void responseReceived(JCsyncAbstractOperation op, short respCode);
    /**
     * Called when the request is received from the layer. The {@link PubSubCustomisableAlgorithm PubSubCustomisableAlgorith} 
     * (extension of {@link DefaultCustomizableAlgorithm DefaultCustomisableAlgorith}) 
     * checks whether the request is one of the jcsync type, and then is passed to 
     * the jcsync core and to the consistency manager.
     * @param req received request
     * @param op operation which contains a request
     * @see JCsyncAbstractOperation
     * @see RegisteredOperations
     */
    public abstract void requestReceived(PublishRequest req, JCsyncAbstractOperation op);
    /**
     * Called when the indication is received from the layer.
     * The {@link PubSubCustomisableAlgorithm PubSubCustomisableAlgorith} 
     * (extension of {@link DefaultCustomizableAlgorithm DefaultCustomisableAlgorith}) 
     * checks whether the indication is one of the jcsync type, and then is passed to 
     * the jcsync core and to the consistency manager.
     * @param op delivered operation which include all information about the 
     * method which will be invoked.
     * @see JCsyncAbstractOperation
     * @see RegisteredOperations
     * @see MethodCarrier
     */
    public abstract void indicationReceived(JCsyncAbstractOperation op);
    
    /**
     * Called each time before sending the request. It is used to detect responses related to the request.
     * @param op operation which is sending 
     * @param bloking if true then will be initialised locker to suspend worker thread until the response is received (depends on implementation)
     * @throws Exception when something goes wrong during locker initialisation
     */
    public abstract void beforeRequestSend(JCsyncAbstractOperation op, boolean bloking)throws Exception;
//    /**
//     * Called each time before sending the response. Not used at all.
//     * @param op operation which is sending 
//     * @param bloking if true then will be used a locker to suspend worker thread until the response is received (depends on implementation)
//     * @throws Exception when something goes wrong during locker initialisation
//     */
//    public abstract void beforeResponseSend(JCsyncAbstractOperation op, boolean bloking)throws Exception;
//    /**
//     * Called each time before sending the indication. Not used at all.
//     * @param op operation which is sending 
//     * @param bloking if true then will be used a locker to suspend worker thread until the response is received (depends on implementation)
//     * @throws Exception when something goes wrong during locker initialisation
//     */
//    public abstract void beforeIndicationSend(JCsyncAbstractOperation op, boolean bloking)throws Exception;
    
//    /**
//     * 
//     * @param op
//     * @param bloking
//     * @return
//     * @throws Exception
//     */
//    public abstract Object afterIndicationSend(JCsyncAbstractOperation op, boolean bloking)throws Exception;
//    /**
//     * 
//     * @param op
//     * @param bloking
//     * @return
//     * @throws Exception
//     */
//    public abstract Object afterResponseSend(JCsyncAbstractOperation op, boolean bloking)throws Exception;
    
    /**
     * Called each time after sending the request. It is used to detect responses related to the request. 
     * @param op operation which is sending 
     * @param bloking if true then will be used a locker to suspend worker thread until the response is received (depends on implementation)
     * @return the results of invoked method or null or exception (if it throws)
     * @throws Exception when something goes wrong during locker initialisation
     */
    public abstract Object afterRequestSend(JCsyncAbstractOperation op, boolean bloking)throws Exception;
    /**
     * Called to inform about new shared object, which will be hold by this consistency model.
     * @param id identifier of shared object (typically the topic ID)
     */
    public abstract void registerObject(String id);    
    /**
     * Just set core algorithm. 
     * @param core an instance of core algorithm
     */
    public abstract void setCoreAlgorith(JCsyncAlgorithInterface core);
    /**
     * Called internally when the indication is received (or request in the topic root stage) to reflective invoke method on shared object.
     * May throws one of below exceptions related with reflective method invoking.
     * @param o the operation related with this action
     * @param m all necessary data to invoke method (e.q. method name, arguments, ...)
     * @return the results of invoked method or null or exception (if it throws)
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    protected Object invoke(JCSyncAbstractSharedObject o,MethodCarrier m) throws SecurityException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{        
        return o.invokeWriteOperation(m.getGenericMethodName(), m.getArgTypes(),m.getArgValues());
    }
}
