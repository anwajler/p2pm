package pl.edu.pjwstk.mteam.jcsync.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.core.NodeError;
import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.jcsync.core.consistencyManager.DefaultConsistencyManager;
import pl.edu.pjwstk.mteam.jcsync.core.pubsub.MessageDeliveryObserver;
import pl.edu.pjwstk.mteam.jcsync.core.pubsub.PubSubWrapper;
import pl.edu.pjwstk.mteam.jcsync.exception.ObjectExistsException;
import pl.edu.pjwstk.mteam.jcsync.exception.ObjectNotExistsException;
import pl.edu.pjwstk.mteam.jcsync.exception.OperationForbiddenException;
import pl.edu.pjwstk.mteam.jcsync.operation.JCsyncAbstractOperation;
import pl.edu.pjwstk.mteam.jcsync.operation.RegisteredOperations;
import static pl.edu.pjwstk.mteam.jcsync.operation.RegisteredOperations.*;
import pl.edu.pjwstk.mteam.p2p.P2PNode;
import pl.edu.pjwstk.mteam.pubsub.accesscontrol.AccessControlRules;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.core.Transaction;
import pl.edu.pjwstk.mteam.pubsub.core.User;
import pl.edu.pjwstk.mteam.pubsub.message.indication.NotifyIndication;
import pl.edu.pjwstk.mteam.pubsub.message.request.PublishRequest;
import pl.edu.pjwstk.mteam.pubsub.message.response.PubSubResponse;
import pl.edu.pjwstk.mteam.pubsub.transport.PubSubTransport;
import pl.edu.pjwstk.p2pp.util.Arrays;

/**
 * Basic implementation of JCsync mechanism.
 * @author Piotr Bucior
 */
public class JCSyncCore implements MessageDeliveryObserver, JCsyncAlgorithInterface {

    private static final Logger log = Logger.getLogger("pl.edu.pjwstk.mteam.jcsync.core.JCSyncCore");

    /**
     * Used to determine protocol type
     */
    public static enum ProtocolType {

        /**
         * 
         */
        UDP,
        /**
         * 
         */
        TCP
    }
    private final P2PNode node;
    private final PubSubWrapper pubsubLayer;
    private final HashMap<String, JCSyncAbstractSharedObject> sharedObjects;
    //lockers for base pubsub operations : createtopic, subscribe,unsubscribe, remove
    private final HashMap<String, Class> registeredConsistencyManagers;
    private final HashMap<Class, AbstractConsistencyManager> usedConsistensyManagers;
    private final DefaultConsistencyManager dcManager;
    private volatile ReentrantReadWriteLock request_response_locker = new ReentrantReadWriteLock();
    private volatile ArrayBlockingQueue<MessageToSend> messagesToSend = new ArrayBlockingQueue<MessageToSend>(200, true);
    private final MessageSender mSender;

    /**
     * Construct new instance of class.
     * @param node the node used by this instance
     * @param port the port number for the PubSub layer
     * @throws IllegalArgumentException if the node is not connected
     */
    public JCSyncCore(P2PNode node, int port) {
        if (!node.isConnected()) {
            throw new IllegalArgumentException("Node must be connected.");
        }
        this.node = node;
        this.pubsubLayer = new PubSubWrapper(port, node, this);
        this.sharedObjects = new HashMap<String, JCSyncAbstractSharedObject>(50);
        this.registeredConsistencyManagers = new HashMap<String, Class>(50);
        this.usedConsistensyManagers = new HashMap<Class, AbstractConsistencyManager>(50);        
        this.dcManager = new DefaultConsistencyManager();
        this.dcManager.setCoreAlgorith(this);
        this.usedConsistensyManagers.put(this.dcManager.getClass(), (AbstractConsistencyManager) this.dcManager);
        this.mSender = new MessageSender();
    }

    /**
     * Pre-configuration, must be done before any usage of this class.
     * @throws Exception if occurred
     */
    public void init() throws Exception {
        this.pubsubLayer.initialize();
        this.mSender.start();
    }

    /**
     * Returns NodeInfo object related with this class instance.     * 
     * @return node information about the node as NodeInfo
     * @see NodeInfo
     */
    @Override
    public NodeInfo getNodeInfo() {
        return this.pubsubLayer.getNodeInfo();
    }

    /**
     * Returns the SharedObject identifier defined in the Pub-Sub layer as a 
     * Topic by given name.
     * @param name shared object name
     * @return shared object identifier as Topic
     * @see Topic
     */
    @Override
    public Topic getAssignedTopic(String name) {
        return this.pubsubLayer.getTopic(name);
    }

    /**
     * Called to inform the algorithm about the fact that new object is created. 
     * Typically used by {@link JCSyncAbstractSharedObject JCsyncAbstractSharedObject}.
     * at the end of the process of creating new object
     * @param so the object that was already created
     * @see JCSyncAbstractSharedObject
     */
    @Override
    public void objectCtreated(JCSyncAbstractSharedObject so) {
        log.info(this.pubsubLayer.getNodeInfo().getName() + " - object successfully created: " + so);
        this.sharedObjects.put(so.getID(), so);
    }

    /**
     * Returns already known object (the object that was already created in the past).
     * @param ID object identifier as String
     * @return shared object related with given identifier
     */
    @Override
    public JCSyncAbstractSharedObject getObject(String ID) {
        return this.sharedObjects.get(ID);
    }

    /**
     * Called to inform algorithm which implementation if 
     * {@link AbstractConsistencyManager AbstractConsistencyManager} 
     * will hold and manage shared object related with given objectName.<br>
     * Only one instance of AbstractConsistencyManager subclass is created to 
     * manage all selected shared object and that way the argument is a class 
     * not particular class instance.
     * @param objectName shared object identifier
     * @param consistencyManager a subclass of {@link AbstractConsistencyManager AbstractConsistencyManager} selected to manage indicated object
     */
    @Override
    public void registerConsistencyManager(String objectName, Class consistencyManager) {
        //throw an exception if the object has already defined consistency manager
        if (this.registeredConsistencyManagers.containsKey(objectName)) {
            throw new IllegalArgumentException("Consistency manager is already defined for this object");
        } else {
            if (!this.usedConsistensyManagers.containsKey(consistencyManager)) {
                //try to create new instance of AbstractConsistencyManager subclass
                try {
                    this.usedConsistensyManagers.put(consistencyManager, (AbstractConsistencyManager) consistencyManager.newInstance());
                } catch (Exception ex) {
                    log.error("An error occured while creating new consistency manager", ex);
                }
            }
            this.registeredConsistencyManagers.put(objectName, consistencyManager);
        }

        getConsistencyManager(objectName).setCoreAlgorith(this);
        getConsistencyManager(objectName).registerObject(objectName);
    }

    /**
     * Sends operation as a pub-sub message thought overlay. If the <tt>blocking</tt>
     * value is <tt>true</tt> then working thread will be suspended until the response 
     * and indication (if required) will be received from the overlay, if 
     * <tt>blocking</tt> value is <tt>false</tt> then working thread will not be 
     * suspended and method returns <tt>null</tt>.<br>
     * Typically used to sends requests.
     * @param op operation that will be send
     * @param blocking determines whenever the calling thread will be suspended 
     * until the results of given operation will be received. 
     * @return the results of given operation or <tt>null</tt> if the blocking 
     * argument value is set to <tt>false</tt> 
     * @throws Exception any error which was occurred during this operation
     */
    @Override
    public Object sendMessage(JCsyncAbstractOperation op, boolean blocking) throws Exception {
        // if the topic for this object is unknown then throw new exception        
        try {
            this.request_response_locker.writeLock().lock();
            if (getAssignedTopic(op.getObjectID()) == null) {
                throw ObjectNotExistsException.instance();
            }
            // try to get the consistency manager if there is already selected
            AbstractConsistencyManager acm = getConsistencyManager(op.getObjectID());
            if (acm == null) {
                acm = this.dcManager;
            }
            short type = op.getOperationType();
            if ((type & OP_REQ_GENERIC) == OP_REQ_GENERIC) {
                acm.beforeRequestSend(op, blocking);
                this.pubsubLayer.getCustomAlgorith().registerSharedObjectName(op.getObjectID());
                Transaction t = this.pubsubLayer.getCustomAlgorith().nextPublishTransaction(op.getObjectID(), op.getOperationType());
                op.setReqestID(t.getID());
                log.trace("(sendMessage) - operation sent (blocking=[" + blocking + "]) operation: " + op.toString());
                this.messagesToSend.put(new MessageToSend(op, t));
                this.request_response_locker.writeLock().unlock();
                Object result = acm.afterRequestSend(op, blocking);
                return result;

            } else if ((type & OP_INDICATION_GENERIC) == OP_INDICATION_GENERIC) {

                acm.beforeRequestSend(op, blocking);
                this.pubsubLayer.getCustomAlgorith().registerSharedObjectName(op.getObjectID());
                Transaction t = this.pubsubLayer.getCustomAlgorith().nextPublishTransaction(op.getObjectID(), op.getOperationType());
                op.setReqestID(t.getID());
                log.trace("(sendMessage) - operation sent (blocking=[" + blocking + "]) operation: " + op.toString());
                this.messagesToSend.put(new MessageToSend(op, t));
                this.request_response_locker.writeLock().unlock();
                Object result = acm.afterRequestSend(op, blocking);
                return result;

            } else {
                log.error("Unhandled operation type: " + op.toString());
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (this.request_response_locker.writeLock().getHoldCount() > 0 && this.request_response_locker.writeLock().isHeldByCurrentThread()) {
                this.request_response_locker.writeLock().unlock();
            }
        }

        return null;
    }

    /**
     * Sends operation as a pub-sub message thought overlay related with given 
     * request. Typically used to send indication as a response for given request.
     * Operation may be send to all object subscribers or only to one, selected node 
     * (request publisher) - for example when the operation is a response for 
     * {@link RegisteredOperations#OP_REQ_TRANSFER_OBJECT transfer object request}.
     * @param req request that the given operation is related with it
     * @param op operation to sent
     * @param publishForAll if <tt>true</tt> then given operation will be 
     * published for all subscribers, if <tt>false</tt> then given operation will
     * be sent only to publisher of given request
     */
    @Override
    public void sendMessage(PublishRequest req, JCsyncAbstractOperation op, boolean publishForAll) {
        try {
            this.messagesToSend.put(new MessageToSend(op, req, publishForAll));
        } catch (InterruptedException ex) {
            log.error("An error occurred while trying to put message to the sendr queue", ex);
        }
    }

    @Override
    public void sendResponse(PublishRequest req, int respCode) {
        this.pubsubLayer.sendResponse(respCode, req, getAssignedTopic(req.getTopicID()));
    }
    private class Key implements Comparable{
        
        private final String topicID;
        private final Integer tID;
        private Key(String topicID, int tID){
            this.topicID = topicID;
            this.tID = tID;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Key other = (Key) obj;
            if ((this.topicID == null) ? (other.topicID != null) : !this.topicID.equals(other.topicID)) {
                return false;
            }
            if (this.tID != other.tID && (this.tID == null || !this.tID.equals(other.tID))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 23 * hash + (this.topicID != null ? this.topicID.hashCode() : 0);
            hash = 23 * hash + (this.tID != null ? this.tID.hashCode() : 0);
            return hash;
        }

        @Override
        public int compareTo(Object o) {
            Key other = (Key) o;
            int retVal = this.topicID.compareToIgnoreCase(other.topicID);
            if(retVal==0){
                if(this.tID < other.tID )retVal = -1;
                else if (this.tID > other.tID )retVal = 1;
            }
            return retVal;
        }
    }
    private ConcurrentHashMap<Key, PublishRequest> alreadyDeliveredRequests 
            = new ConcurrentHashMap<Key, PublishRequest>();
    
    private boolean checkIfAlreadyReceived(PublishRequest req){
        boolean retVal = false;
        Integer tid = req.getTransactionID();
        if(this.alreadyDeliveredRequests.containsKey(tid)){
            PublishRequest oldReq = this.alreadyDeliveredRequests.get(tid);
            if(oldReq.getPublisher().compareTo(req.getPublisher())==0 && Arrays.equals(oldReq.getMessage(), req.getMessage())){
                retVal = true;
            }
            retVal = true;            
            JCsyncAbstractOperation op = JCsyncAbstractOperation.encode(req.getMessage());
            JCsyncAbstractOperation op_ = JCsyncAbstractOperation.encode(oldReq.getMessage());
            log.warn("request already received: "+req+", with operation:"+op);
            log.warn("\toldReq in buffer: "+oldReq+", with operation:"+op_);
        }else{
                retVal = false;
                log.debug("request added to received requests buffer: "+req);
                this.alreadyDeliveredRequests.put(new Key(req.getTopicID(), tid), req);
                
                //log.trace(retVal);
            }
        PublishRequest req_;
        if (this.alreadyDeliveredRequests.size() > 200) {
            List<Key> list = new ArrayList<Key>();
            list.addAll(this.alreadyDeliveredRequests.keySet());
            log.trace("Clrearing received request buffer, actual size:"+this.alreadyDeliveredRequests.size());
            Collections.sort(list);
            for (int i = 0; i < list.size() - 100; i++) {
                req_ = this.alreadyDeliveredRequests.remove(list.get(i));
                req_ = null;
            }            
            list = null;
            System.gc();
        }
        return retVal;
    }
    @Override
    public void onDeliverRequest(PublishRequest req) {
        try{
//        // TODO remove 4 lines below - just for debug
//        byte[] details = req.getMessage();
//        if (req.getEventType() == 1413) {
//            System.out.println("zzz");
//        }
        JCsyncAbstractOperation op = JCsyncAbstractOperation.encode(req.getMessage());
        op.setReqestID(req.getTransactionID());
        
        log.trace(this.pubsubLayer.getNodeInfo().getName() + " (onDeliverRequest) - [publisher]:"+req.getPublisher()+", [node relay]:"+req.getSourceInfo()+": " + op);
        if(this.checkIfAlreadyReceived(req)) {
            log.trace(this.pubsubLayer.getNodeInfo().getName() + 
                    " (onDeliverRequest): - received request that was aready received, skipping ... : " + op);
            return;
        }
        // check that this node can hold this request
        JCSyncAbstractSharedObject so = this.sharedObjects.get(op.getObjectID());
        if (so != null) {
            if ((op.getOperationType() & OP_REQ_GENERIC) == OP_REQ_GENERIC) {
                if (op.getOperationType() == OP_REQ_TRANSFER_OBJECT) {
                    // there is no need to inform consistency manager about this kind of request, just send response and indication
                    sendResponse(req, PubSubConstants.RESP_SUCCESS);
                    synchronized (so.operationIdIncrementLocker) {
                        log.trace(getNodeInfo().getName()
                                + ": transferring shared object with current operation id: "
                                + so.getCurrentOperationID() + ", to node: " + req.getPublisher());
                        JCsyncAbstractOperation op_ = JCsyncAbstractOperation.get_OP_IND_TRANSFER_OBJECT(op.getObjectID(), so.encode(),this.getNodeInfo().getName());
                        op_.setReqestID(op.getReqestID());
                        sendMessage(req, op_, false);
                    }



                } else {
                    //if this node is a root 
                    if (getAssignedTopic(req.getTopicID()).isTopicRoot(this.pubsubLayer.getNodeInfo().getID())) {
                        AbstractConsistencyManager acm = getConsistencyManager(req.getTopicID());
                        if (acm != null) {
                            log.trace("(onDeliverRequest) - passing operation to consistency model: " + op.toString());
                            acm.requestReceived(req, op);
                        } else {
                            log.fatal("(onDeliverRequest) - there is no registered consistency manager for this operation: " + op.toString());
                        }
                    } else {
                        log.trace("(onDeliverRequest) - forwarding operation request to the parent node: " + op.toString());
                        this.pubsubLayer.forwardToParent(req, getAssignedTopic(req.getTopicID()));
                    }
                }
            } else {
                log.fatal("(onDeliverRequest) - unhandled operation type: " + op.toString());
            }
        } else {
            if (op.getOperationType() == OP_REQ_TRANSFER_OBJECT) {
                log.trace(getNodeInfo().getName() + ": (onDeliverRequest) - shared object not found!: " + op.toString() + ", forwarding request to the topic owner: " + op.toString());
                this.pubsubLayer.forwardToOtherNode(req, this.pubsubLayer.getTopic(op.getObjectID()).getOwner().getNodeInfo());
            } else {
                log.fatal(getNodeInfo().getName() + ": (onDeliverRequest) - shared object not found!: " + op.toString());
                sendResponse(req, PubSubConstants.RESP_DOESNOTEXIST);
            }
        }
        }catch(Exception e){
            log.error("An error occurred:",e);
        }
    }

    @Override
    public AbstractConsistencyManager getConsistencyManager(String objectID) {
        if (this.sharedObjects.get(objectID) != null) {
            return this.usedConsistensyManagers.get(this.registeredConsistencyManagers.get(objectID));
        } else {
            return null;
        }
    }

    @Override
    public void onDeliverResponse(PubSubResponse op) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onDeliverIndication(JCsyncAbstractOperation op) {
        if (getConsistencyManager(op.getObjectID()) != null) {
            // if the consistency manager is already defined for the object related with this operation
            getConsistencyManager(op.getObjectID()).indicationReceived(op.changeTypeToRequest());

        } else {
            this.dcManager.indicationReceived(op.changeTypeToRequest());
        }
    }

    @Override
    public void onDeliverPubSubResponse(String name, short operationType, short respCode, long reqID) {

        log.trace(this.pubsubLayer.getNodeInfo().getName() + ": (onDeliverPubSubResponse) , topicName: " + name + ", operationType: " + operationType + ", respCode: " + respCode + ",reqID: " + reqID);

        short rCode = respCode;
        //mapping respCode - in the PubSub sometimes calls respCodes from PubSUbConstants and sometimes from NodeError
        if (respCode == NodeError.AUTHERR) {
            rCode = PubSubConstants.RESP_FORBIDDEN;
        } else if (respCode == NodeError.NOSUCHTOPICERR) {
            rCode = PubSubConstants.RESP_DOESNOTEXIST;
        }
        respCode = rCode;
        //mapping pubsub respCodes to jcsync resp codes
        if (respCode == PubSubConstants.RESP_SUCCESS) {
            rCode = JCSyncConstans.J_RESP_GENERAL_SUCCESS;
        } else if (respCode == PubSubConstants.RESP_ALREADYEXISTS) {
            rCode = JCSyncConstans.J_ERR_COLLECTION_EXISTS;
        } else if (respCode == PubSubConstants.RESP_DOESNOTEXIST) {
            rCode = JCSyncConstans.J_ERR_OBJECT_NOT_EXISTS;
        } else if (respCode == PubSubConstants.RESP_FORBIDDEN) {
            rCode = JCSyncConstans.J_ERR_COLLECTION_AUTH_ERROR;
        }
        respCode = rCode;
        try {
            /* sometimes response is already received, but the locker is not 
             * properly initialized in the consistency manager - if 
             * the request is sending then wait for the end of it.             * 
             */
            this.request_response_locker.readLock().lock();
            if ((operationType & OP_GENERIC_JCSYNC_OPERATION) == OP_GENERIC_JCSYNC_OPERATION) {
                JCsyncAbstractOperation o = null;//JCsyncAbstractOperation.getByType(operationType, name);
                o = JCsyncAbstractOperation.getByType(operationType, name,this.getNodeInfo().getName());
                o.setReqestID(reqID);
                if (getConsistencyManager(name) != null) {
                    getConsistencyManager(name).responseReceived(o, respCode);
                } else {
                    this.dcManager.responseReceived(o, respCode);
                }
                return;
            }
            switch (operationType) {
                case PubSubConstants.MSG_CREATETOPIC: {
                    JCsyncAbstractOperation o = null;
                    o = JCsyncAbstractOperation.get_OP_REQ_CREATE_SHARED_OBJECT(name,this.getNodeInfo().getName());
                    this.dcManager.responseReceived(o, respCode);
                    return;
                }
                case PubSubConstants.MSG_SUBSCRIBE: {
                    JCsyncAbstractOperation o = null;
                    o = JCsyncAbstractOperation.get_OP_REQ_SUBSCRIBE(name,this.getNodeInfo().getName());
                    this.dcManager.responseReceived(o, respCode);
                    return;
                }
                case PubSubConstants.MSG_UNSUBSCRIBE: {
                    JCsyncAbstractOperation o = null;
                    o = JCsyncAbstractOperation.get_OP_REQ_UNSUBSCRIBE(name,this.getNodeInfo().getName());
                    this.dcManager.responseReceived(o, respCode);
                    return;
                }
                case PubSubConstants.EVENT_REMOVETOPIC: {
                    JCsyncAbstractOperation o = null;
                    o = JCsyncAbstractOperation.get_OP_REQ_REMOVE(name,this.getNodeInfo().getName());
                    this.dcManager.responseReceived(o, respCode);
                    return;
                }
                default: {
                    log.fatal("Received unsuported response code: " + respCode
                            + ", for operation: " + operationType
                            + ", for shared object name: " + name);
                }
            }
        } catch (Exception ex) {
            log.error("An error while processing (onDeliverPubSubResponse)", ex);
        } finally {
            this.request_response_locker.readLock().unlock();
        }
    }

    @Override
    public synchronized Topic createSharedObject(String name, boolean blocking)
            throws ObjectExistsException,
            Exception {
        if (this.sharedObjects.containsKey(name)) {
            throw ObjectExistsException.instance();
        } else {
            // preparing locker to wait for response
            try {
                JCsyncAbstractOperation operation = JCsyncAbstractOperation.get_OP_REQ_CREATE_SHARED_OBJECT(name,this.getNodeInfo().getName());
                // inform Consistency Manager before sending request
                this.dcManager.beforeRequestSend(operation, blocking);
                this.pubsubLayer.getCustomAlgorith().registerSharedObjectName(name);
                this.pubsubLayer.getCustomAlgorith().createTopic(name, false);
                Short respCode = (Short) this.dcManager.afterRequestSend(operation, blocking);
                if (respCode != null) {
                    // if the topic was created 
                    if (respCode.shortValue() == JCSyncConstans.J_RESP_GENERAL_SUCCESS) {
                        return this.pubsubLayer.getTopic(name);
                        //if node can't subscribe to this topic
                    } else if (respCode == JCSyncConstans.J_ERR_COLLECTION_EXISTS) {
                        throw ObjectExistsException.instance();
                    } else {
                        log.fatal("received UNHANLED respCode for operation: "
                                + operation.getOperationType() + ", objectID:" + operation.getObjectID());
                    }
                } else {
                    log.fatal("received [null] respCode for operation: " + operation.getOperationType() + ", objectID:" + operation.getObjectID());
                }
            } catch (InterruptedException e) {
                throw new Exception("An error while creating new shared object");
            }
        }
        log.error("(createSharedObject) returns Topic = null from unknow reason!");
        return null;
    }

    @Override
    public synchronized Topic createSharedObject(String name, boolean blocking, AccessControlRules acRules)
            throws ObjectExistsException,
            Exception {
        if (this.sharedObjects.containsKey(name)) {
            throw ObjectExistsException.instance();
        } else {
            // preparing locker to wait for response
            try {
                JCsyncAbstractOperation operation = JCsyncAbstractOperation.get_OP_REQ_CREATE_SHARED_OBJECT(name,this.getNodeInfo().getName());
                // inform Consistency Manager before sending request
                this.dcManager.inititBuffer(name);
                this.dcManager.beforeRequestSend(operation, blocking);
                this.pubsubLayer.getCustomAlgorith().registerSharedObjectName(name);
                this.pubsubLayer.getCustomAlgorith().createTopic(name, false, acRules);
                Short respCode = (Short) this.dcManager.afterRequestSend(operation, blocking);
                if (respCode != null) {
                    // if the topic was created 
                    if (respCode.shortValue() == JCSyncConstans.J_RESP_GENERAL_SUCCESS) {
                        return this.pubsubLayer.getTopic(name);
                        //if node can't subscribe to this topic
                    } else if (respCode == JCSyncConstans.J_ERR_COLLECTION_EXISTS) {
                        throw ObjectExistsException.instance();
                    } else {
                        log.fatal("received UNHANLED respCode for operation: "
                                + operation.getOperationType() + ", objectID:" + operation.getObjectID());
                    }
                } else {
                    log.fatal("received [null] respCode for operation: " + operation.getOperationType() + ", objectID:" + operation.getObjectID());
                }
            } catch (InterruptedException e) {
                throw new Exception("An error while creating new shared object");
            }
        }
        log.error("(createSharedObject) returns Topic = null from unknow reason!");
        return null;
    }

    @Override
    public synchronized Topic subscribeSharedObject(String name, boolean blocking)
            throws ObjectNotExistsException,
            OperationForbiddenException,
            Exception {
        // preparing locker to wait for response
        try {
            JCsyncAbstractOperation operation = JCsyncAbstractOperation.get_OP_REQ_SUBSCRIBE(name,this.getNodeInfo().getName());
            // inform Consistency Manager before sending request
            this.dcManager.inititBuffer(name);
            this.dcManager.beforeRequestSend(operation, blocking);
            this.pubsubLayer.getCustomAlgorith().registerSharedObjectName(name);
            this.pubsubLayer.getCustomAlgorith().networkSubscribe(name);
            Short respCode = (Short) this.dcManager.afterRequestSend(operation, blocking);
            if (respCode != null) {
                // if the topic was created 
                if (respCode == JCSyncConstans.J_RESP_GENERAL_SUCCESS) {
                    return this.pubsubLayer.getTopic(name);
                    //if node can't subscribe to this topic
                } else if (respCode == JCSyncConstans.J_ERR_COLLECTION_AUTH_ERROR) {
                    throw OperationForbiddenException.instance();
                    //if the topic not exists
                } else if (respCode == JCSyncConstans.J_ERR_OBJECT_NOT_EXISTS) {
                    throw ObjectNotExistsException.instance();
                } else {
                    log.fatal("received UNHANLED respCode for operation: "
                            + operation.getOperationType() + ", objectID:" + operation.getObjectID());
                }
            } else {
                log.fatal("received [null] respCode for operation: "
                        + operation.getOperationType() + ", objectID:" + operation.getObjectID());
            }

        } catch (InterruptedException e) {
            throw new Exception("An error on (subscribeSharedObject)");
        }
        log.error("(subscribeSharedObject) returns Topic = null from unknow reason!");
        return null;
    }

    @Override
    public synchronized boolean unsubscribeSharedObject(String name, boolean blocking)
            throws ObjectNotExistsException,
            Exception {
        // preparing locker to wait for response
        try {
            JCsyncAbstractOperation operation = JCsyncAbstractOperation.get_OP_REQ_UNSUBSCRIBE(name,this.getNodeInfo().getName());
            // inform Consistency Manager before sending request
            this.dcManager.beforeRequestSend(operation, blocking);
            this.pubsubLayer.getCustomAlgorith().registerSharedObjectName(name);
            this.pubsubLayer.getCustomAlgorith().networkUnsubscribe(name);
            Short respCode = (Short) this.dcManager.afterRequestSend(operation, blocking);
            if (respCode != null) {
                if (respCode == JCSyncConstans.J_RESP_GENERAL_SUCCESS) {
                    return true;
                    //if the node is not a subscriber 
                } else if (respCode == JCSyncConstans.J_ERR_OBJECT_NOT_EXISTS) {
                    throw ObjectNotExistsException.instance();
                } else {
                    log.fatal("received UNHANLED respCode for operation: "
                            + operation.getOperationType() + ", objectID:" + operation.getObjectID());
                }
            } else {
                log.fatal("received [null] respCode for operation: "
                        + operation.getOperationType() + ", objectID:" + operation.getObjectID());
            }

        } catch (InterruptedException e) {
            throw new Exception("An error on (unsubscribeSharedObject)");
        }
        log.error("(unsubscribeSharedObject) returns false from unknown reason.");
        return false;
    }

    @Override
    public synchronized boolean removeSharedObject(String name, boolean blocking)
            throws ObjectNotExistsException,
            OperationForbiddenException,
            Exception {
        // preparing locker to wait for response
        try {
            JCsyncAbstractOperation operation = JCsyncAbstractOperation.get_OP_REQ_REMOVE(name,this.getNodeInfo().getName());
            // inform Consistency Manager before sending request
            this.dcManager.beforeRequestSend(operation, blocking);
            this.pubsubLayer.getCustomAlgorith().registerSharedObjectName(name);
            this.pubsubLayer.getCustomAlgorith().removeTopic(name);
            Short respCode = (Short) this.dcManager.afterRequestSend(operation, blocking);
            if (respCode != null) {
                if (respCode == JCSyncConstans.J_RESP_GENERAL_SUCCESS) {
                    return true;
                    //if the object not exists
                } else if (respCode == JCSyncConstans.J_ERR_OBJECT_NOT_EXISTS) {
                    throw ObjectNotExistsException.instance();
                } else if (respCode == JCSyncConstans.J_ERR_COLLECTION_AUTH_ERROR) {
                    throw OperationForbiddenException.instance();
                } else {
                    log.fatal("received UNHANLED respCode for operation: "
                            + operation.getOperationType() + ", objectID:" + operation.getObjectID());
                }
            } else {
                log.fatal("received [null] respCode for operation: "
                        + operation.getOperationType() + ", objectID:" + operation.getObjectID());
            }

        } catch (InterruptedException e) {
            throw new Exception("An error on (removeSharedObject) ");
        }
        log.error("(removeSharedObject) returns false from unknown reason.");
        return false;
    }

    @Override
    public boolean checkRootStatus(String sharedObjectName) {
        return this.pubsubLayer.getTopic(sharedObjectName).isTopicRoot(this.pubsubLayer.getNodeInfo().getID());
    }

    /**
     * Simply container which contains the necessary information to send a
     * message.
     */
    class MessageToSend {

        private JCsyncAbstractOperation operation = null;
        private PublishRequest request = null;
        private boolean forAll = false;
        private Transaction t = null;

        private MessageToSend(JCsyncAbstractOperation o, Transaction t) {
            this.operation = o;
            this.t = t;
        }

        private MessageToSend(JCsyncAbstractOperation o, PublishRequest req, boolean forAll) {
            this.operation = o;
            this.request = req;
            this.forAll = forAll;
        }
    }

    class MessageSender extends Thread implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    MessageToSend ms = messagesToSend.take();
                    if (ms.request == null) {
                        pubsubLayer.getCustomAlgorith().networkPublish_(ms.operation.getObjectID(), ms.operation.toByteArray(), ms.operation.getOperationType(), ms.t);
                    } else {
                        NotifyIndication ind = new NotifyIndication(
                                pubsubLayer.getNodeInfo(),
                                new NodeInfo(""), ms.request.getTopicID(),
                                ms.operation.getOperationType(), ms.operation.toByteArray(), false,
                                new User(ms.request.getPublisher()), -1);
                        if (ms.forAll) {
                            ind.setOperationID(getAssignedTopic(ind.getTopicID()).increaseCurrentOperation(pubsubLayer.getNodeInfo().getName(), -10));
                            pubsubLayer.storeNotifyIndication(ind);
                            log.trace(getNodeInfo().getName() + " - Sending notifyIndications with operationID: " + ((NotifyIndication) ind).getOperationID()+", that contains operation: "+ms.operation);
                            pubsubLayer.forwardToChildren(ind, getAssignedTopic(ind.getTopicID()));
                        } else {
//                            Subscriber child = getAssignedTopic(ms.request.getTopicID()).getChild(ms.request.getSourceInfo().getID());
//                            if(child == null) 
//                                child = getAssignedTopic(ms.request.getTopicID()).getParent();
//                            if()
                            NodeInfo thisNode = pubsubLayer.getNodeInfo();
                            ind.getSourceInfo().setID(thisNode.getID());
                            ind.getSourceInfo().setIP(thisNode.getIP());
                            ind.getSourceInfo().setName(thisNode.getName());
                            ind.getSourceInfo().setPort(thisNode.getPort());
                            ind.getDestinationInfo().setID(ms.request.getSourceInfo().getID());
                            ind.getDestinationInfo().setIP(ms.request.getSourceInfo().getIP());
                            ind.getDestinationInfo().setName(ms.request.getSourceInfo().getName());
                            ind.getDestinationInfo().setPort(ms.request.getSourceInfo().getPort());
                            
                            ind.setDirect(true);
                            log.trace(getNodeInfo().getName() + " - Sending notifyIndications with operationID: " + ((NotifyIndication) ind).getOperationID()+" direcltry to: "+ms.request.getSourceInfo().getName()+", that contains operation: "+ms.operation);
                            pubsubLayer.sendMessage(ind, PubSubTransport.ROUTING_DIRECT, null);
                        }

                    }
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    private static class ComparatorImpl implements Comparator<JCSyncAbstractSharedObject> {

        public ComparatorImpl() {
        }

        @Override
        public int compare(JCSyncAbstractSharedObject o1, JCSyncAbstractSharedObject o2) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
