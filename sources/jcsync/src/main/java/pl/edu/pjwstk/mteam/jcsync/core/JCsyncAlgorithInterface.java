package pl.edu.pjwstk.mteam.jcsync.core;

import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.jcsync.exception.ObjectExistsException;
import pl.edu.pjwstk.mteam.jcsync.exception.ObjectNotExistsException;
import pl.edu.pjwstk.mteam.jcsync.exception.OperationForbiddenException;
import pl.edu.pjwstk.mteam.jcsync.operation.JCsyncAbstractOperation;
import pl.edu.pjwstk.mteam.pubsub.accesscontrol.AccessControlRules;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.message.request.PublishRequest;

/**
 * Interface <tt>JCsyncAlgorithInterface</tt> determines essential operations of 
 * the JCSync mechanism.
 * @author Piotr Bucior
 */
public interface JCsyncAlgorithInterface {
    
    /**
     * Creates new shared object with given name.
     * <br>Object will be created with default <tt>AccessControlRules</tt>.
     * @param name object identifier as String.
     * @param blocking could be used to suspend calling thread until the object 
     * will be created.
     * @return a pub-sub <tt>Topic</tt> as a shared object identifier.
     * @throws ObjectExistsException if the object (or the <tt>Topic</tt>) 
     * already exists.
     * @throws Exception any error occurred during operation.
     * @see AccessControlLists
     */
    public Topic createSharedObject(String name, boolean blocking) 
            throws ObjectExistsException,
            Exception;
    /**
     * Creates new shared object with given name and custom <tt>AccessControlRules</tt>.
     * @param name object identifier as String.
     * @param blocking could be used to suspend calling thread until the object 
     * will be created.
     * @return a pub-sub <tt>Topic</tt> as a shared object identifier.
     * @throws ObjectExistsException if the object (or the <tt>Topic</tt>) 
     * already exists.
     * @throws Exception any error occurred during operation.
     */
    public Topic createSharedObject(String name, boolean blocking, AccessControlRules acRules) 
            throws ObjectExistsException,
            Exception;
    /**
     * Allows to subscribe with existed shared object.
     * @param name object identifier as String.
     * @param blocking could be used to suspend calling thread until the object 
     * will be created.
     * @return a pub-sub <tt>Topic</tt> as a shared object identifier.
     * @throws ObjectNotExistsException if the <tt>Topic</tt> with given name does
     * not exists.
     * @throws OperationForbiddenException if the node can not subscribe to this
     * <tt>Topic</tt>.
     * @throws Exception any other error occurred during operation.
     */
    public Topic subscribeSharedObject(String name, boolean blocking)
            throws ObjectNotExistsException,
            OperationForbiddenException,
            Exception;
    /**
     * Publish unsubscribe information in the pub-sub layer.
     * @param name object identifier
     * @param blocking could be used to suspend calling thread until the object 
     * will be created.
     * @return depends on implementation 
     * @throws ObjectNotExistsException if the <tt>Topic</tt> with given name does
     * not exists.
     * @throws Exception any other error occurred during operation.
     */
    public boolean unsubscribeSharedObject(String name, boolean blocking)
            throws ObjectNotExistsException,
            Exception;
    /**
     * Publish remove information in the pub-sub layer. 
     * @param name object identifier to remove.
     * @param blocking could be used to suspend calling thread until the 
     * operation will end.
     * @return depends on implementation.
     * @throws ObjectNotExistsException if the <tt>Topic</tt> with given name does
     * not exists.
     * @throws OperationForbiddenException if the node can not subscribe to this
     * <tt>Topic</tt>.
     * @throws Exception any other error occurred during operation.
     */
    public boolean removeSharedObject(String name, boolean blocking)
            throws ObjectNotExistsException,
            OperationForbiddenException,
            Exception;
    /**
     * Checking the root status on the shared object with given name. 
     * <br> For more information please refer to the Publish-Subscribe (pub-sub)
     * reference.
     * @param sharedObjectName object identifier.
     * @return <tt>true</tt> if the current node is a <tt>ROOT</tt> for the 
     * shared object with given <tt>sharedObjectName</tt>, otherwise <tt>false</tt>.
     */
    public boolean checkRootStatus(String sharedObjectName);
    /**
     * Informs the core algorithm which implementation of the 
     * {@link AbstractConsistencyManager AbstractConsistencyManager} will holds 
     * shared object with given <tt>objectName</tt>.
     * @param objectName object identifier.
     * @param consistencyManager a class object of <tt>AbstractConsistencyManager</tt>
     * subclass.
     */
    public void registerConsistencyManager(String objectName, Class consistencyManager);
    /**
     * Allows to send message described in <tt>JCsyncAbstractOperation</tt> 
     * through the overlay.
     * @param op operation that will be send.
     * @param blocking could be used to suspend calling thread until the 
     * operation will end.
     * @return depends on implementation.
     * @throws Exception any error occurred during this operation.
     */
    public Object sendMessage(JCsyncAbstractOperation op, boolean blocking) throws Exception;
    /**
     * Allows to send message described in <tt>JCsyncAbstractOperation</tt> as a 
     * reaction for given <tt>PublishRequest req</tt>.
     * @param req the request associated with given operation.
     * @param op an operation to send.
     * @param publishForAll if <tt>true</tt> then given message must be published
     * in all involved nodes in the overlay.
     */
    public void sendMessage(PublishRequest req,JCsyncAbstractOperation op, boolean publishForAll);
    /**
     * Allows to send response for given request.
     * @param req request that the response is associated.
     * @param respCode response code.
     */
    public void sendResponse(PublishRequest req, int respCode);
    /**
     * Returns <tt>Topic</tt> object associated to shared object with given name.
     * @param name shared object identifier.
     * @return associated <tt>Topic</tt> object or <tt>null</tt> if the shared 
     * object with given <tt>name</tt> does not exists.
     */
    public Topic getAssignedTopic(String name);
    /**
     * Informs that new object was just created. Used to store new object.
     * @param createdObject just created object.
     */ 
    public void objectCtreated(JCSyncAbstractSharedObject createdObject);
    /**
     * Returns an implementation-depending <tt>AbstractConsistencyManager</tt> which
     * is manages shared object with given <tt>objectID</tt>.
     * @param objectID object identifier.
     * @return implementation-depending <tt>AbstractConsistencyManager</tt>.
     */
    public AbstractConsistencyManager getConsistencyManager(String objectID);
    /**
     * Returns an instance of implementation-depending <tt>JCSyncAbstractSharedObject</tt>
     * associated with given <tt>objectID</tt>.
     * @param objectID object identifier.
     * @return the instance of shared object.
     */
    public JCSyncAbstractSharedObject getObject(String objectID);
    /**
     * Returns the <tt>NodeInfo</tt> object which represents current node in the 
     * overlay network. Typically used for debugging.
     * @return node representation in the overlay.
     * @see NodeInfo
     */
    public NodeInfo getNodeInfo();
    
}
