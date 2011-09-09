package pl.edu.pjwstk.mteam.jcsync.core;

import pl.edu.pjwstk.mteam.jcsync.core.concurrency.ConsistencyModel;
import pl.edu.pjwstk.mteam.jcsync.core.concurrency.WeakConsistencyModel;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.jcsync.collections.JCSyncAbstractCollection;
import pl.edu.pjwstk.mteam.jcsync.collections.JCSyncCollectionStateListener;
import pl.edu.pjwstk.mteam.jcsync.collections.implementation.AbstractCollectionsManager;
import pl.edu.pjwstk.mteam.jcsync.core.concurrency.EventInvoker;
import pl.edu.pjwstk.mteam.jcsync.core.concurrency.QueuedEventInvoker;
import pl.edu.pjwstk.mteam.jcsync.core.concurrency.SimpleInvoker;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncCreateCollectionIndication;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncCreateCollectionRequest;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncInvokeMethodIndication;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncInvokeMethodRequest;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncMessage;
import pl.edu.pjwstk.mteam.jcsync.core.pubsubwrapper.JCSyncMessageCarrier;
import pl.edu.pjwstk.mteam.jcsync.core.pubsubwrapper.PubSubWrapper;
import pl.edu.pjwstk.mteam.jcsync.exception.CollectionExistException;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncCreateCollectionMethod;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncMethod;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncWriteMethod;
import pl.edu.pjwstk.mteam.p2p.P2PNode;
import pl.edu.pjwstk.mteam.pubsub.core.Transaction;

/**
 * @author Piotr Bucior
 * @version 1.0
 */
public final class JCSyncCoreAlgorithm extends AbstractCoreAlgorithm implements AlgorithmInterface {

    private static Logger log = Logger.getLogger("jcsync.core.JCSyncCoreAlgorithm");
    private CustomisableAlgorithm customAlg = null;
    private static PubSubWrapper networkLayer;
    private final P2PNode node;
    private static JCSyncCoreAlgorithm instance;
    private final HashMap<String, JCSyncAbstractCollection> collections = new HashMap<String, JCSyncAbstractCollection>(5);
    private final ConsistencyModel eventLocker;

    private JCSyncCoreAlgorithm(String nodeName, int tcpPort, int udpPort,
            String reflexiveServerAddress, int reflexiveServerPort,
            String bootIP, int bootPort, int pubSubPort) {
        log.trace("initialising network layer: \n"
                + "-> NODE_NAME: " + nodeName
                + "\n-> TCP_PORT: " + tcpPort
                + "\n-> UDP_PORT: " + udpPort
                + "\n-> RS_IP: " + reflexiveServerAddress
                + "\n-> PS_PORT: " + reflexiveServerPort
                + "\n-> BOOT IP: " + bootIP
                + "\n-> BOOT_PORT: " + bootPort
                + "\n-> PUB-SUB_PORT: " + pubSubPort);
        node = new P2PNode(PubSubWrapper.getNodeCallback(), P2PNode.RoutingAlgorithm.SUPERPEER);
        node.setUserName(nodeName);
        node.setTcpPort(tcpPort);
        node.setUdpPort(udpPort);
        ((P2PNode) node).setServerReflexiveAddress(reflexiveServerAddress);
        ((P2PNode) node).setServerReflexivePort(reflexiveServerPort);
        node.setBootIP(bootIP);
        node.setBootPort(bootPort);
        networkLayer = new PubSubWrapper(pubSubPort, node, this);
        this.networkLayer.setCustomizableAlgorithm();
        this.eventLocker = new WeakConsistencyModel();
    }

    public static JCSyncCoreAlgorithm getInstance() {
        if(!isInitialised) return null;
        //if (networkLayer.isConnected()) {
            instance = networkLayer.getJCSyncCore();
        //}
        if (JCSyncCoreAlgorithm.instance == null) {
            throw new IllegalArgumentException("Network layer are not initialized yet!");
        } else {
            return JCSyncCoreAlgorithm.instance;
        }
    }
    private static boolean isInitialised = false;
    
    private JCSyncCoreAlgorithm(P2PNode node, int pubSubPort) {
        this.node = node;                
        networkLayer = new PubSubWrapper(pubSubPort, node, this);
        this.node.addCallback(networkLayer.getNodeCallback());
        this.networkLayer.setCustomizableAlgorithm();
        this.eventLocker = new WeakConsistencyModel();
    }
    public static void init(P2PNode node, int pubSubPort){
        instance = new JCSyncCoreAlgorithm(node, pubSubPort);
        isInitialised = true;
    }

    public void joinToNetwork() {
        log.debug("Joining to network...");
        this.node.networkJoin();
    }

    private void setCustomAlgorithm(CustomisableAlgorithm alg) {
        this.customAlg = alg;
    }

    public void finalize() throws Throwable {
    }

    /**
     *
     * @param request
     */
    protected boolean onDeliverCreateCollectionIndication(JCSyncCreateCollectionIndication req) {
        this.collections.put(req.getCollectionID(), AbstractCollectionsManager.getInstance().makeCollection((JCSyncCreateCollectionMethod) req.getDetailedMethod()));
        this.networkLayer.collectionCreated(req.getCollectionID(), req.getTransactionID_(), JCSyncConstans.J_RESP_GENERAL_SUCCESS);        
        this.eventLocker.runEventInvoker(req.getCollectionID());
        return true;
    }

    /**
     *
     * @param operation
     */
    protected boolean onDeliverOperation(JCSyncMessage operation) {
        return false;
    }

    /**
     *
     * @param notify
     */
    protected boolean onDeliverNotify(JCSyncMessage notify) {
        return false;
    }

    /**
     *
     * @param response
     */
    protected boolean onDeliverCreateCollectionResponse(JCSyncMessage response) {
        return false;
    }

    /**
     *
     * @param response
     */
    protected boolean onDeliverOperationResponse(JCSyncMessage response) {
        return false;
    }

    /**
     *
     * @param operation
     */
    protected boolean requestReadOperation(JCSyncMessage operation) {
        return false;
    }

    /**
     *
     * @param operation
     */
    protected boolean requestWriteOperation(JCSyncMessage operation) {
        return false;
    }

    @Override
    protected boolean onDeliverResponse(JCSyncMessage msg) {
        return false;
    }

    @Override
    public int requestCreateCollection(JCSyncCreateCollectionMethod op, boolean subscribe_if_exists, EventInvoker.InvokerType type, ConsistencyModel model) throws CollectionExistException {
        if (!this.collections.containsKey(op.getCollectionID())) {
            this.collections.put((String) op.getCollectionID(), null);
        } else if (this.collections.get(op.getCollectionID()) != null) {
            
           throw CollectionExistException.getInstance();
            //throw new IllegalArgumentException("Collection with id: " + op.getCollectionID() + " already exist");
        }
        this.eventLocker.registerEventInvoker(op.getCollectionID(), createMethodInvoker(op.getCollectionID(),getCollection(op.getCollectionID()), type));
        //all incoming event will wait till synchronisation end
        this.eventLocker.pauseEventInvoker(op.getCollectionID());
        int ret_code = this.networkLayer.createCollection(op);        
        switch(ret_code){
            case (JCSyncConstans.J_ERR_COLLECTION_NAME_RESERVED):
            {
                // subscribe if nesessary
                if(subscribe_if_exists){
                    log.debug("Collection: ["+op.getCollectionID()+" ] exists, subscribing ...");
                    ret_code = subscribeCollection(op);
                    if(ret_code== JCSyncConstans.J_RESP_GENERAL_SUCCESS){
                        //this.eventLocker.registerMethodInvoker(op.getCollectionID(), createMethodInvoker(op.getCollectionID(),getCollection(op.getCollectionID()), type));
                        return 0;
                    }                    
                    else{
                        this.eventLocker.removeEventInvoker(op.getCollectionID());
                        throw CollectionExistException.getInstance();
                    }
                }else{
                    this.eventLocker.removeEventInvoker(op.getCollectionID());
                    throw CollectionExistException.getInstance();
                }
            }
            case (JCSyncConstans.J_RESP_GENERAL_SUCCESS):{
                //this.eventLocker.registerMethodInvoker(op.getCollectionID(), createMethodInvoker(op.getCollectionID(),getCollection(op.getCollectionID()), type));
                return 0;
            }
            default:
                return 0;
        }
    }
    private EventInvoker createMethodInvoker(String collectionID, JCSyncAbstractCollection collection, EventInvoker.InvokerType type){
        EventInvoker invoker = null;
        if(type == EventInvoker.InvokerType.SIMPLE){
            invoker = new SimpleInvoker(collection, collectionID, type);
        }
        else if(type == EventInvoker.InvokerType.QUEUED){
            invoker = new QueuedEventInvoker(collection, collectionID, type);
        }
        return invoker;
    }
    private int subscribeCollection(JCSyncCreateCollectionMethod op) {
        int retCode = this.networkLayer.subscribeCollection(op.getCollectionID());
        if(retCode == JCSyncConstans.J_RESP_GENERAL_SUCCESS){
            return retCode;
        }
        return 0;
    }


    @Override
    public Object publishWriteOperation(JCSyncMethod operation) {
        Transaction trans = new Transaction(null, this.networkLayer.getTopic(operation.getCollectionID()));
        this.eventLocker.preInitWaitForWriteResults(operation.getCollectionID(), trans.getID());
        int respCode = this.networkLayer.publishWriteOperation(operation, trans);
        if (respCode >= 0) {
            return this.eventLocker.waitForWriteResults(operation.getCollectionID(), trans.getID());
        } else {
            throw new IllegalArgumentException("An error while publishing operation, errCode: " + respCode);
        }
    }

    public JCSyncAbstractCollection getCollection(String id) {
        return this.collections.get(id);
    }

    public Object getCollectionIdentifier(String id) {
        return this.networkLayer.getTopic(id);
    }



    private boolean onDeliverInvokeMethodIndication(JCSyncInvokeMethodIndication jcs) {
        //only sent to MethodInvoker        
        JCSyncWriteMethod wm = (JCSyncWriteMethod) jcs.getDetailedMethod();
        Object retVal;
        Method m;
        JCSyncAbstractCollection coll = this.collections.get(jcs.getCollectionID());
        try {
            if (coll != null) {
                this.eventLocker.getEventInvoker(jcs.getCollectionID()).onDeliverEvent(jcs);
                return true;
            }
        }
        finally {
            wm = null;
            m = null;
            jcs = null;
        }
        return false;
    }

    @Override
    public void invoke(JCSyncInvokeMethodIndication jcs) {
        JCSyncWriteMethod wm = (JCSyncWriteMethod) jcs.getDetailedMethod();
        Object retVal;
        Method m;
        JCSyncAbstractCollection coll = this.collections.get(jcs.getCollectionID());
        try {
            log.debug("Invoking method: " + wm.getGenericMethodName() + ", collection: " + jcs.getCollectionID());
            m = getCollection(jcs.getCollectionID()).getDeclaredClass().getDeclaredMethod(wm.getGenericMethodName(), wm.getParamTypes());
            retVal = getCollection(jcs.getCollectionID()).invokeMethod(m,jcs.getOperationID().longValue(), false, wm.getParamValues());

            //if (coll != null) {
                ArrayList<JCSyncCollectionStateListener> listeners = coll.getListeners();
                if (this.eventLocker.onDeliverWriteResults(jcs.getCollectionID(), jcs.getTransactionID_(), 0, retVal)) {
                    for (int i = 0; i < listeners.size(); i++) {
                        listeners.get(i).onLocalStateUpdated(coll, wm);
                    }
                } else {
                    for (int i = 0; i < listeners.size(); i++) {
                        listeners.get(i).onRemoteStateUpdated(coll, wm);
                    }
                }
            //}
        } catch (Exception e) {
            retVal = e;
        } finally {
            wm = null;
            m = null;
            jcs = null;
        }
    }
    

    public boolean iAmRoot(String collectionID) {
        return this.networkLayer.iAmRoot(collectionID);
    }

    public void onDeliverJCSyncMessage(JCSyncMessageCarrier req) {
        short msgType = req.getInternalMessage().getMessageType();
        boolean iAmRoot = iAmRoot(req.getCollectionID());
        log.debug("On deliver message: " + JCSyncConstans.messages.get(msgType));
        //if the message is a sub-message of REQUEST
        if ((msgType & JCSyncConstans.JCSYNC_GENERIC_REQUEST) == JCSyncConstans.JCSYNC_GENERIC_REQUEST) {
            //if receive getConstructorRequest from node who wants to subscribe collection
            if(msgType == JCSyncConstans.JCSYNC_GET_CONSTRUCTOR_REQUEST){
                //check if this node contais collection from request
                if(getCollection(req.getCollectionID())!=null){
                    this.networkLayer.sendResponse(JCSyncConstans.J_RESP_GENERAL_SUCCESS, JCSyncConstans.JCSYNC_GET_CONSTRUCTOR_REQUEST, req);
                    this.eventLocker.pauseEventInvoker(req.getCollectionID());
                    JCSyncCreateCollectionMethod m = getCollection(req.getCollectionID()).getConstructorDetails();               
                    m.setAdditionalData(getCollection(req.getCollectionID()).serialize());
                    JCSyncCreateCollectionIndication notifyIndication = new JCSyncCreateCollectionIndication(m, 0, "...", req.getTransactionID(),req.getCollectionID(), false);
                    notifyIndication.setDestinationInfo(req.getSourceInfo());
                    this.networkLayer.sendDirect(notifyIndication);
                    this.eventLocker.runEventInvoker(req.getCollectionID());
                }else{
                    //just forward to parent
                    this.networkLayer.forwardToParent(req);
                }
            }
            else if (iAmRoot) {
                if (msgType == JCSyncConstans.JCSYNC_CREATE_COLLECTION_REQUEST) {

                    // collection can be created
                    if (getCollection(req.getCollectionID()) == null) {
                        this.networkLayer.sendResponse(JCSyncConstans.J_RESP_GENERAL_SUCCESS, JCSyncConstans.JCSYNC_CREATE_COLLECTION_REQUEST, req);
                        JCSyncCreateCollectionIndication notifyIndication = new JCSyncCreateCollectionIndication((JCSyncCreateCollectionRequest) req.getInternalMessage(), 0, "...", req.getTransactionID());
                        log.debug("On deliver create collection indication in root stage ...");
                        onDeliverCreateCollectionIndication(notifyIndication);
                        this.networkLayer.forwardToChlidren_(notifyIndication);
                        notifyIndication = null;
                    } else {
                        // if not
                        this.networkLayer.sendResponse(JCSyncConstans.J_ERR_COLLECTION_EXISTS, JCSyncConstans.JCSYNC_CREATE_COLLECTION_REQUEST, req);
                    }

                } else if ((msgType | JCSyncConstans.JCSYNC_INVOKE_WRITE_METHOD_REQ) == JCSyncConstans.JCSYNC_INVOKE_WRITE_METHOD_REQ) {
                    // collection can be created
                    if (getCollection(req.getCollectionID()) != null) {
                        this.networkLayer.sendResponse(JCSyncConstans.J_RESP_GENERAL_SUCCESS, JCSyncConstans.JCSYNC_INVOKE_WRITE_METHOD_REQ, req);
                        JCSyncInvokeMethodIndication notifyIndication = new JCSyncInvokeMethodIndication((JCSyncInvokeMethodRequest) req.getInternalMessage(), 0, "...", req.getTransactionID());
                        notifyIndication.setOperationID(this.eventLocker.getEventInvoker(notifyIndication.getCollectionID()).getLastQueuedOperationID());
                        this.onDeliverInvokeMethodIndication(notifyIndication);
                        this.networkLayer.forwardToChlidren_(notifyIndication);
                        notifyIndication = null;
                    } else {
                        // if not
                        this.networkLayer.sendResponse(JCSyncConstans.J_ERR_GENERAL_ERROR, JCSyncConstans.JCSYNC_INVOKE_WRITE_METHOD_REQ, req);
                    }
                }
            } else {
                log.debug("Forwarding message to parent: " + JCSyncConstans.messages.get(msgType));
                this.networkLayer.forwardToParent(req);
            }
//
////	        NotifyIndication notify = new NotifyIndication(
////	        		                         getCoreAlgorithm().getNodeInfo(),
////					                         new NodeInfo(""), t.getID(),
////					                         req.getEventType(), req.getMessage(), false,
////					                         new User(req.getPublisher()));
////	        getCoreAlgorithm().forwardToChildren(notify, t);
//                }
        //if message is a sub-indication
        } else if ((msgType & JCSyncConstans.JCSYNC_GENERIC_INDICATION) == JCSyncConstans.JCSYNC_GENERIC_INDICATION) {
            if (msgType == JCSyncConstans.JCSYNC_CREATE_COLLECTION_INDICATION) {
                log.debug("On deliver create collection indication ...");
                onDeliverCreateCollectionIndication((JCSyncCreateCollectionIndication) req.getInternalMessage());
                this.networkLayer.sendResponse(JCSyncConstans.J_RESP_GENERAL_SUCCESS, JCSyncConstans.JCSYNC_CREATE_COLLECTION_INDICATION, req);
                //if the indication is prepared for all nodes in tree
                if(((JCSyncCreateCollectionIndication) req.getInternalMessage()).isGlobal())
                this.networkLayer.forwardToChlidren_(req.getInternalMessage());
            }else if(msgType == JCSyncConstans.JCSYNC_INVOWE_WRITE_METHOD_INDICATION){
                log.debug("On deliver write method indication ...");
                onDeliverInvokeMethodIndication((JCSyncInvokeMethodIndication) req.getInternalMessage());
                this.networkLayer.sendResponse(JCSyncConstans.J_RESP_GENERAL_SUCCESS, JCSyncConstans.JCSYNC_INVOWE_WRITE_METHOD_INDICATION, req);
                this.networkLayer.forwardToChlidren_(req.getInternalMessage());
            }
        //if message is a sub-response
        } else if ((msgType & JCSyncConstans.JCSYNC_GENERIC_RESPONSE) == JCSyncConstans.JCSYNC_GENERIC_RESPONSE) {
            JCSyncCoreAlgorithm.networkLayer.terminateTransaction(req.getTransactionID(), Transaction.COMPLETED);
        }
    }

    public void DEBUG_publish(String string, byte[] bytes) {
        this.networkLayer.getCustomizableAlgorithm().networkPublish(string, bytes);

    }

    public void DEBUG_Subscribe(String tID) {
        this.networkLayer.getCustomizableAlgorithm().networkSubscribe(tID);
    }


}//end JCSyncCoreAlgorithm

