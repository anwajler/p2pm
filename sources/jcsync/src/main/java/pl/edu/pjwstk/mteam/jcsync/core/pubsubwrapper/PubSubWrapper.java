package pl.edu.pjwstk.mteam.jcsync.core.pubsubwrapper;

/**
 * ZMIANY W PUB-SUB
 * 1. zmiana typu dla PubSubMessage (byte) na short
 */
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.core.NetworkObject;
import pl.edu.pjwstk.mteam.core.Node;
import pl.edu.pjwstk.mteam.core.NodeCallback;
import pl.edu.pjwstk.mteam.core.NodeError;
import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.jcsync.collections.JCSyncAbstractCollection;
import pl.edu.pjwstk.mteam.jcsync.core.AlgorithmInterface;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncConstans;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncCoreAlgorithm;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncCreateCollectionIndication;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncCreateCollectionRequest;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncInvokeMethodRequest;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncMessage;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncGeneralResponse;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncGetConstructorRequest;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncCreateCollectionMethod;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncMethod;
import pl.edu.pjwstk.mteam.p2p.P2PNode;
import pl.edu.pjwstk.mteam.pubsub.accesscontrol.AccessControlRules;
import pl.edu.pjwstk.mteam.pubsub.core.CoreAlgorithm;
import pl.edu.pjwstk.mteam.pubsub.algorithm.AlgorithmConfigurator;
import pl.edu.pjwstk.mteam.pubsub.core.Event;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.Subscriber;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.core.Transaction;
import pl.edu.pjwstk.mteam.pubsub.core.User;
import pl.edu.pjwstk.mteam.pubsub.interestconditions.InterestConditions;
import pl.edu.pjwstk.mteam.pubsub.message.indication.PubSubIndication;
import pl.edu.pjwstk.mteam.pubsub.message.request.CreateTopicRequest;
import pl.edu.pjwstk.mteam.pubsub.message.request.PubSubRequest;
import pl.edu.pjwstk.mteam.pubsub.message.request.SubscribeRequest;
import pl.edu.pjwstk.mteam.pubsub.message.response.PubSubResponse;
import pl.edu.pjwstk.mteam.pubsub.operation.CreateTopicOperation;
import pl.edu.pjwstk.mteam.pubsub.operation.NotifyOperation;
import pl.edu.pjwstk.mteam.pubsub.operation.PublishOperation;
import pl.edu.pjwstk.mteam.pubsub.operation.SubscribeOperation;
import pl.edu.pjwstk.mteam.pubsub.topology.TopologyManager;
import pl.edu.pjwstk.mteam.pubsub.transport.PubSubTransport;

/**
 * @author Piotr Bucior
 * @version 1.0
 */
public class PubSubWrapper extends CoreAlgorithm{

    private static final Logger log = Logger.getLogger("pubsubwrapper.PubSubWrapper");
    /**
     * JCSync core algorithm instance
     */
    private AlgorithmInterface jcsyncCore = null;
    private static final HashMap<Key, Locker> constructorLockers = new HashMap<Key, Locker>(50);
    private static final HashMap<Key, Locker> topicLockers = new HashMap<Key, Locker>(50);
    private static final NodeCallback_ callback = new NodeCallback_();
    public PubSubWrapper(int port, P2PNode n, AlgorithmInterface jcsyncCore) {
        super(port, n, new pl.edu.pjwstk.mteam.jcsync.core.pubsubwrapper.AlgorithmConfigurator());
        this.jcsyncCore = jcsyncCore;
    }

    public PubSubWrapper(int port, Node n, AlgorithmConfigurator chooser, AlgorithmInterface jcsyncCore) {
        super(port, n, chooser);
        this.jcsyncCore = jcsyncCore;
    }

    public PubSubWrapper(int port, Node n, TopologyManager topology) {
        super(port, n, topology);
    }
    

//    @Override
//    public boolean onDeliverIndication(PubSubIndication psi) {
//        //if (psi instanceof JCSyncIndication) {
//            return listener.onDeliverIndication(getNodeInfo().getName(),psi);
//        //} else {
//        //    return super.onDeliverIndication(psi);
//        //}
//    }

    @Override
    public boolean onDeliverRequest(PubSubRequest psr) {        
        if ((((short) psr.getType()) & JCSyncConstans.JCSYNC_GENERIC_MESSAGE) == JCSyncConstans.JCSYNC_GENERIC_MESSAGE) {
            log.trace("onDeliverJCSyncMessage : " + psr);
            this.jcsyncCore.onDeliverJCSyncMessage(((JCSyncMessageCarrier) psr));
            psr = null;
            return false;
        } else {
            return super.onDeliverRequest(psr);
        }
    }
    public Boolean isConnected(){
        return callback.isInitialized();
    }

    @Override
    public boolean onDeliverResponse(PubSubResponse res) {
        log.trace("onDeliverResponse:\n" + res+", tID:"+res.getTransactionID());
        boolean b = false;
        if(super.transaction_logger.getTransaction(res.getTransactionID()).getOperation().getType() == PubSubConstants.OPERATION_SUBSCRIBE){
            b = super.onDeliverResponse(res);
            if(res.getResponseCode() == PubSubConstants.RESP_SUCCESS){
                unlockCreateTopic(res.getTopicID(), res.getTransactionID(), JCSyncConstans.J_RESP_GENERAL_SUCCESS);
            }else{
                unlockCreateTopic(res.getTopicID(), res.getTransactionID(), JCSyncConstans.J_ERR_GENERAL_ERROR);
            }
        }else{
            b = super.onDeliverResponse(res);
        }
        return b;
    }

    @Override
    public boolean onDeliverIndication(PubSubIndication ind) {
        log.trace("onDeliverResponse:\n" + ind);
        return super.onDeliverIndication(ind);
    }

//    @Override
//    public boolean onDeliverResponse(PubSubResponse psr) {
//        //if (psr instanceof JCSyncGeneralResponse) {
//            return listener.onDeliverResponse(getNodeInfo().getName(),psr);
//        //} else {
//        //    return super.onDeliverResponse(psr);
//        //}
//    }
    
    public void init() {
        super.init();
        super.algorithm.setCoreAlgorithm(this);
    }

    private int createCollection(JCSyncCreateCollectionMethod op, Transaction trans) {
        
        String tid = op.getCollectionID();
        Topic topic = getTopic(tid);
        if (topic == null) {
            //Topic exists locally
            log.trace("Topic (collection) " + tid + " not exists locally, create topic first!");
            return -1;
        } else {
            NodeInfo thisNode = getNodeInfo();
            if (topic.getChild(thisNode.getID()) != null) {
                User parent = topic.getParent();
			if(parent == null){
				//this node is topic root
				parent = new User(thisNode);
			}
                CreateTopicOperation create =
                        new CreateTopicOperation(topic.getID(),
                        new Subscriber(topic, thisNode),
                        new Event(PubSubConstants.EVENT_NEWTOPIC));
                trans.addOperation(create);
                JCSyncCreateCollectionRequest req =
                        new JCSyncCreateCollectionRequest(thisNode, parent.getNodeInfo(),
                        tid, trans.getID(), op,"...");
                JCSyncMessageCarrier msg = new JCSyncMessageCarrier(req, topic.getAccessControlRules());
                log.trace("Sending " + JCSyncConstans.messages.get(req.getMessageType())
                        + " request for '" + topic.getID() + "'...");
                log.trace(msg);
                //Adding transaction and starting timer for it
                log.trace("Creating transaction:" + trans);
                addTransaction(trans);
                if (sendMessage(msg, PubSubTransport.ROUTING_DIRECT, tid)) {
                    return 0;
                } else {
                    //if(sendMessage(msg, PubSubTransport.ROUTING_OVERLAY, tid))
                    //return 0;
                    //else return -2;
                    return -2;
                }
            } else {
                log.info("The node is not '" + tid + "' subscriber, so it can't publish events");
                return -3;
            }
        }
    }
    private int subscribeCollection(String collID, Transaction trans) {
        Topic topic = getTopic(collID);
        if (topic == null) {
            //Topic exists locally
            log.trace("Topic (collection) " + collID + " not exists locally, create topic first!");
            return -1;
        } else {
            NodeInfo thisNode = getNodeInfo();
            if (topic.getChild(thisNode.getID()) != null) {
                User parent = topic.getParent();
			if(parent == null){
				//this node is topic root
				parent = new User(thisNode);
			}
                SubscribeOperation oper =
                        new SubscribeOperation(topic.getID(),
                        new Subscriber(topic, thisNode),
                        new Event(PubSubConstants.EVENT_NEWTOPIC));
                trans.addOperation(oper);
                JCSyncGetConstructorRequest req =
                        new JCSyncGetConstructorRequest(thisNode, parent.getNodeInfo(),
                        collID, trans.getID(),"...");
                JCSyncMessageCarrier msg = new JCSyncMessageCarrier(req, topic.getAccessControlRules());
                log.trace("Sending " + JCSyncConstans.messages.get(req.getMessageType())
                        + " request for '" + topic.getID() + "'...");
                log.trace(msg);
                //Adding transaction and starting timer for it
                log.trace("Creating transaction:" + trans);
                addTransaction(trans);
                if (sendMessage(msg, PubSubTransport.ROUTING_DIRECT, collID)) {
                    return 0;
                } else {
                    //if(sendMessage(msg, PubSubTransport.ROUTING_OVERLAY, collID))
                    //return 0;
                    //else return -2;
                    return -2;
                }
            } else {
                log.info("The node is not '" + collID + "' subscriber, so it can't publish events");
                return -3;
            }
        }
    }
    /**
     *
     * @param op
     * @param subscribe_if_exists
     * @return response code for this operation:<br>
     * JCSyncConstans.J_ERR_COLLECTION_NAME_RESERVED - if the collection (topic ID) is already in use
     */
    public int createCollection(JCSyncCreateCollectionMethod op){
        if(getTopic(op.getCollectionID())!=null){
            log.error("Error while trying to create collection: topic with id: "+op.getCollectionID()+" exists locally!");
            return JCSyncConstans.J_ERR_COLLECTION_NAME_RESERVED;
        }
        Transaction trans = new Transaction(null, new Topic(op.getCollectionID()));
        //first of all create topic which will be connected with this collection
        initCreateTopicLocker(op.getCollectionID(), trans.getID());
        createTopic_(op.getCollectionID(), true, trans);
        int respCode =
                lockCreateTopic(op.getCollectionID(), trans.getID());

        //if the topic for the collection was created
        if(respCode == JCSyncConstans.J_RESP_TOPIC_CREATED){
            trans = new Transaction(null, getTopic(op.getCollectionID()));
            initCreateCollectionLocker(op.getCollectionID(), trans.getID());
            if(createCollection(op, trans) == 0){
                //sent successfull - thats it - now will wait for CreateCollectionIndication
                return lockCreateCollection(op.getCollectionID(), trans.getID());
            }
            // transport error
            else return JCSyncConstans.J_ERR_TRANSPORT_ERROR;
        //if the collection exists
        }
        //other error
        else return respCode;
        
//        if (respCode == -1) {
//            trans = new Transaction(null, this.networkLayer.getTopic(op.getCollectionID()));
//            this.concurrencymanager.initCreateCollectionLocker(op.getCollectionID(), trans.getID());
//            respCode = this.networkLayer.createCollection(op, trans);
//            if (respCode == 0) {
//                this.concurrencymanager.lockCreateCollection(op.getCollectionID(), trans.getID());
//            } else {
//                log.fatal("Unable to create collection, reason: " + respCode);
//                throw new IllegalArgumentException("Unable to create collection, reason: " + respCode);
//            }
//        } else if (respCode == 4) {
//            trans = new Transaction(null,new Topic(op.getCollectionID()));
//            this.concurrencymanager.initCreateTopicLocker(op.getCollectionID(), trans.getID());
//            this.networkLayer.subscribe(op.getCollectionID(), trans);
//            respCode =
//                    this.concurrencymanager.lockCreateTopic(op.getCollectionID(), trans.getID());
//            if (respCode == -1 || respCode == 200) {
//                   this.collections.put(op.getCollectionID(), AbstractCollectionsManager.getInstance().makeCollection(op));
//            } else {
//                log.fatal("Unable to create collection, reason: " + respCode);
//                throw new IllegalArgumentException("Unable to create collection, reason: " + respCode);
//            }
//        } else {
//            log.fatal("Unable to create topic fo collection, reason: " + respCode);
//            throw new IllegalArgumentException("Unable to create topic fo collection, reason: " + respCode);
//        }
    }

    public int subscribeCollection(String collID){
        int respCode = 0;
        Topic topic = getTopic(collID);
        if(topic ==null) topic = new Topic(collID);
        Transaction t = new Transaction(null,  topic);
        initCreateTopicLocker(collID, t.getID());
        subscribe(t.getTopic().getID(), t);
            respCode =
                lockCreateTopic(topic.getID(), t.getID());

        //if the topic for the collection was created
        if(respCode == JCSyncConstans.J_RESP_GENERAL_SUCCESS){
            t = new Transaction(null, getTopic(collID));
            initCreateCollectionLocker(collID, t.getID());
            if(subscribeCollection(collID, t) == 0){
                //sent successfull - thats it - now will wait for CreateCollectionIndication
                return lockCreateCollection(collID, t.getID());
            }
            // transport error
            else return JCSyncConstans.J_ERR_TRANSPORT_ERROR;
        //if the collection exists
        }
        return respCode;
    }

    public int publishWriteOperation(JCSyncMethod op, Transaction trans) {
        String tid = op.getCollectionID();
        Topic t ;
        t = getTopic(tid);
        if (t == null) {
            //Topic exists locally
            log.trace("Topic (collection) " + tid + " not exists!");
            log.trace("Invoking ONERROR callback for '" + tid + "'");
            getNode().getCallback().onPubSubError(getNode(), tid,
                    PubSubConstants.OPERATION_CREATETOPIC,
                    NodeError.TOPICEXISTSERR);
            return -1;
        } else {
            NodeInfo thisNode = getNodeInfo();
            if (t.getChild(thisNode.getID()) != null) {
                User parent = t.getParent();
                if (parent == null) {
                    //this node is topic root
                    parent = new User(thisNode);
                }
                
                PublishOperation o = new PublishOperation(tid,
                        new Subscriber(t, thisNode),
                        new Event(PubSubConstants.EVENT_CUSTOM));                
                JCSyncInvokeMethodRequest req =
                        new JCSyncInvokeMethodRequest(thisNode,
                        parent.getNodeInfo(), tid, trans.getID(), op,"...");
                trans.addOperation(o);                
                JCSyncMessageCarrier msg = new JCSyncMessageCarrier(req, t.getAccessControlRules());
                //Adding transaction and starting timer for it
                addTransaction(trans);
                log.trace("Sending message: "+msg);
                if(sendMessage(msg, PubSubTransport.ROUTING_DIRECT, tid)){
                    msg = null;
                    req = null;
                    return trans.getID();
                }
                else {
                    log.error("Error while sending message: "+msg);
                    msg = null;
                    return -5;
                }
                
            } else {
                log.error("The node is not '" + tid + "' subscriber, so it can't publish events");
                return -1;
            }
        }
        

    }

    public void sendResponse(short respCode, JCSyncMessageCarrier req, Topic t, short requested_method) {
        JCSyncGeneralResponse jresp = null;
        JCSyncMessageCarrier resp = null;
        //if (msg instanceof JCSyncCreateCollectionRequest && respCode == JCSyncConstans.J_RESP_GENERAL_SUCCESS) {
            jresp = new JCSyncGeneralResponse(getNodeInfo(), req.getSourceInfo(), req.getCollectionID(), req.getTransactionID(),
                    respCode, requested_method,"...");
            resp = new JCSyncMessageCarrier(jresp, t.getAccessControlRules());
            boolean b = sendMessage(resp, PubSubTransport.ROUTING_DIRECT, null);
            
        //}
      //  else {
//			resp = new StandardResponse(req.getTransactionID(), respCode,
//					                    getNodeInfo(), req.getSourceInfo(),
//					                    req.getTopicID());
//			sendMessage(resp, PubSubTransport.ROUTING_DIRECT, null);
     //   }
        log.trace("Response " + respCode + " for " + JCSyncConstans.messages.get(resp.getInternalMessage().getDetailedMethodID())
                + " sent to '" + resp.getDestinationInfo() + "'");
        jresp = null;
        resp = null;
    }


    public void createTopic_(String topicID, boolean b, Transaction trans) {        
		createTopic_(topicID, b, new AccessControlRules(trans.getTopic()),trans);
    }

    private void createTopic_(String topicID, boolean subscribe, AccessControlRules acRules, Transaction trans) {
        String tid = (String)topicID;
		Topic newTopic = getTopic(tid);
//                if(newTopic==null) {
//                    newTopic = getTopicStateLogger().getTopic(tid);
//                    if(newTopic!=null)addTopic(newTopic,false);
//                }
		if(newTopic != null){
			//Topic exists locally
			log.trace("Topic "+tid+" exists locally...");
			log.trace("Invoking ONERROR callback for '"+tid+"'");
                        for (NodeCallback listener : getNode().getCallbacks()) {
				listener.onPubSubError(getNode(), tid,
                        PubSubConstants.OPERATION_CREATETOPIC,
                        NodeError.TOPICEXISTSERR);
			}
                        
//
//			getNode().getCallback().onPubSubError(getNode(), tid,
//					                                        PubSubConstants.OPERATION_CREATETOPIC,
//					                                        NodeError.TOPICEXISTSERR);
                        unlockCreateTopic(newTopic.getID(), trans.getID(), JCSyncConstans.J_ERR_COLLECTION_NAME_RESERVED);
		}
		else{
			newTopic = trans.getTopic();
			NodeInfo thisNode = getNodeInfo();
			AccessControlRules ac = (AccessControlRules)acRules;
			newTopic.setAccessControlRules(ac);
			newTopic.setOwner(new Subscriber(newTopic, getNodeInfo()));
			CreateTopicOperation create =
                                new CreateTopicOperation(newTopic.getID(),
                                                            new Subscriber(newTopic, thisNode),
                                                            new Event(PubSubConstants.EVENT_NEWTOPIC)
                                                         );
                        trans.addOperation(create);
			CreateTopicRequest msg = new CreateTopicRequest(thisNode,
                                                            new NodeInfo(""), tid, ac,
                                                            PubSubConstants.CREATETOPICFLAG_NEWTOPIC,
                                                            trans.getID());
			if(subscribe){
				SubscribeOperation sub = new SubscribeOperation(newTopic.getID(),
                        	                                    new Subscriber(newTopic, thisNode),
                                                                new Event(PubSubConstants.EVENT_ALL));
				trans.addOperation(sub);
				msg.addSubscriber(new User(thisNode));
				newTopic.addSubscriber(thisNode);
			}
			log.trace("Sending "+PubSubConstants.STR_OPERATION[PubSubConstants.OPERATION_CREATETOPIC]+
					     " request for '"+newTopic.getID()+"'...");
			//Adding transaction and starting timer for it
			log.trace("Creating transaction "+trans);
			addTransaction(trans);
			sendMessage(msg, PubSubTransport.ROUTING_OVERLAY, tid);
		}
    }

    public void subscribe(String tid, Transaction trans) {
        InterestConditions ic = new InterestConditions(trans.getTopic());
        int eventIndex = PubSubConstants.HISTORY_ALL;
        Topic t = getTopicStateLogger().getTopic((String)tid);
        if(t!=null){
            eventIndex = t.getCurrentOperationID();
        }
		t = getTopic(tid);
		if(t != null){
			//Topic exists locally
			log.trace("Topic "+tid+" exists - subscribing locally...");
			t.addSubscriber(getNodeInfo());
			t.getChild(getNodeInfo().getID()).getSubscription(t.getID()).setInterestConditions((InterestConditions)ic);
			for (NodeCallback listener : getNode().getCallbacks()) {
				listener.onTopicSubscribe(getNode(), t.getID());
			}
                        unlockCreateTopic(tid, trans.getID(), JCSyncConstans.J_RESP_GENERAL_SUCCESS);
		}
		else{
			//---------------------------------------------------------------------
			//TODO: [DHT-based] this section works only for the DHT-based networks
			//---------------------------------------------------------------------
			NodeInfo thisNode = getNodeInfo();
			t = trans.getTopic();
			t.addSubscriber(thisNode);
			t.getChild(thisNode.getID()).getSubscription(t.getID()).setInterestConditions((InterestConditions)ic);
			int distance = getNode().getDistance(tid, thisNode.getID());
			SubscribeOperation o = new SubscribeOperation(t.getID(),
					                                      new Subscriber(t, thisNode),
					                                      new Event(PubSubConstants.EVENT_ALL));
			trans.addOperation(o);
			SubscribeRequest msg = new SubscribeRequest(trans.getID(), thisNode,
					                                    new NodeInfo(""), tid,
					                                    100000000,
					                                    eventIndex,
					                                    distance);
			//Adding transaction and starting timer for it
			addTransaction(trans);
			sendMessage(msg, PubSubTransport.ROUTING_OVERLAY, tid);
			//---------------------------------------------------------------------
		}
    }

    public JCSyncCoreAlgorithm getJCSyncCore() {
        Boolean b;
        while(true){
            b = null;
            b = isConnected();
            if(b!=null){
                if(b==Boolean.FALSE) return null;
                else if(b==Boolean.TRUE) return (JCSyncCoreAlgorithm) this.jcsyncCore;
            }
            else {
                try {
                   Thread.currentThread().sleep(10);
                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(PubSubWrapper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }

    public void collectionCreated(String collectionID, int transactionID_, short respCode) {
        unlockCreateCollection(collectionID, transactionID_, respCode);
    }

    public void sendDirect(JCSyncMessage msg) {
                Topic t = getTopic(msg.getCollectionID());
                msg.setSourceInfo(getNodeInfo());
                Subscriber sb = new Subscriber(msg.getPublisher(), t);
                NotifyOperation o = new NotifyOperation(t.getID(),
				                                sb,
				                                new Event(PubSubConstants.EVENT_CUSTOM));
                Transaction trans = new Transaction(o);
                msg.setTransactionID(trans.getID());
                JCSyncMessageCarrier msg_ = new JCSyncMessageCarrier(msg, t.getAccessControlRules());
                //Adding transaction and starting timer for it
                addTransaction(trans);
                log.trace("Sending message: "+msg);
                if(!sendMessage(msg_, PubSubTransport.ROUTING_DIRECT, t.getID())){
                    log.fatal("Unable to send message!");
                }
                sb = null;
                msg_ = null;
    }
    private static class NodeCallback_ implements NodeCallback {
        Boolean networkInitialized = null;
        public void onDisconnect(Node node) {
        log.info("Disconnected from network");
    }
        public Boolean isInitialized(){
            return networkInitialized;
        }

    public void onOverlayError(Node node, Object sourceID, int errorCode) {
        log.error("OnOverlayError invoked, error code: " + errorCode);
        networkInitialized = false;
    }

    public void onJoin(Node node) {
        networkInitialized = true;
    }

    public void onUserLookup(Node node, Object userInfo) {
        NodeInfo info = (NodeInfo) userInfo;
        log.debug("OnUserLookup result: " + info.getIP() + ":" + info.getPort());
    }

    public void onTopicRemove(Node node, Object topicID) {
        log.debug("onTopicRemove for topic '" + topicID + "' callback invoked");
    }

    public void onTopicCreate(Node node, Object topicID, int transID) {
//        log.debug("onTopicCreate for topic '" + topicID + "' callback invoked");
//        this.concurrencymanager.unlockCreateTopic((String) topicID, transID, -1);
    }

    public void onTopicSubscribe(Node node, Object topicID) {
        log.debug("onTopicSubscribe for topic '" + topicID + "' callback invoked");
    }

    @Override
    public void onInsertObject(Node node, NetworkObject object) {
        log.debug("onInsertObject for key '" + object.getKey() + "' callback invoked");
    }

    @Override
    public void onPubSubError(Node node, Object topicID, byte operationType, int errorCode, int transID) {
//        if (operationType == PubSubConstants.OPERATION_CREATETOPIC) {
//            if (this.collections.containsKey((String) topicID)) {
//                this.concurrencymanager.unlockCreateTopic((String) topicID, transID, errorCode);
//            }
//        }

    }

    @Override
    public void onTopicNotify(Node node, Object topicID, byte[] message,boolean b) {
        log.debug(" - onTopicNotify for topic '" + topicID + "' callback invoked, message: "+new String(message)+", historical event?: "+b);
    }

    @Override
    public void onTopicUnsubscribe(Node node, Object topicID) {
        log.debug(" - onTopicUnsubscribe for topic '" + topicID + "' callback invoked");
    }

    @Override
    public void onObjectLookup(Node node, Object object) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onDeliverRequest(List<NetworkObject> objectList) {
        boolean result = true;
        NetworkObject obj = objectList.get(0);
        if (obj.getType() == NetworkObject.TYPE_PROTOTRUST) {
            byte[] msgbytes = obj.getValue();
            //Protocol-specific parsing goes here...

            /* Return 'false' to prevent the MESSAGE object
             * from being inserted into DHT
             */
            result = false;
        }
        return result;
    }

    @Override
    public boolean onForwardingRequest(List<NetworkObject> objectList) {
        boolean result = true;
        NetworkObject obj = objectList.get(0);
        if (obj.getType() == NetworkObject.TYPE_PROTOTRUST) {
            byte[] msgbytes = obj.getValue();
            //Protocol-specific parsing goes here...

            /* Return 'false' to discard P2PP insert request
             * encapsulating ProtoTrust message
             */
        }
        return result;
    }
    public void onTopicCreate(Node node, Object topicID) {
    }

    public void onTopicSubscribe(Node node, Object topicID, int transID) {
        log.debug("onTopicCreate for topic '" + topicID + "' callback invoked");
//        try{
//        this.concurrencymanager.unlockCreateTopic((String) topicID, transID, -1);
//        }catch(Exception e){
//
//        }
    }

    public void onOverlayError(Node node, Object sourceID, int errorCode, int transID) {
        networkInitialized = false;
    }

    public void onPubSubError(Node node, Object topicID, byte operationType, int errorCode) {
    }
    };

    public boolean iAmRoot(String collectionID) {
        return getTopic(collectionID).isTopicRoot();
    }

    public void forwardToParent(JCSyncMessageCarrier req) {
        Topic t = getTopic(req.getCollectionID());
        forwardToParent(req, t);
    }

    public void sendResponse(short resp_code,short method_type, JCSyncMessageCarrier req) {
        Topic t = null;
        t = getTopic(req.getCollectionID());
        sendResponse(resp_code, req, t, method_type);
    }

    public void forwardToChlidren_(JCSyncMessage msg) {
        Topic t = getTopic(msg.getCollectionID());
        Vector<String>children = t.getChildren();
        
                Subscriber child;
		boolean isThisNodeSubscribed = false;

        for (int i = 0; i < children.size(); i++) {
            child = t.getChild(children.get(i));
			if(child.getNodeInfo().equals(getNodeInfo())){
				//do nothing
			}else{
                            forwardToChild_(msg, child);
                        }
        }
                children = null;
//		if(isThisNodeSubscribed || t.isTopicRoot()){
//			/*
//			 * If the received notification refers to one of the 'special' events
//			 * (f.e. remove topic) the procedure for this node can only be performed
//			 * after forwarding it to the children.
//			 */
//			NotifyIndication ind = (NotifyIndication)msg;
//			switch(ind.getEventType()){
//			case PubSubConstants.EVENT_MODIFYAC:
//				getTopic(ind.getTopicID()).setAccessControlRules(new AccessControlRules(ind.getMessage()));
//				logger.debug(PubSubConstants.STR_EVENT[ind.getEventType()]+ " received "+
//						     "("+ind.getTopicID()+")");
//				break;
//			case PubSubConstants.EVENT_REMOVETOPIC:
//				logger.debug(PubSubConstants.STR_EVENT[ind.getEventType()]+ " received "+
//					     " ("+ind.getTopicID()+")");
//				removeTopic(ind.getTopicID());
//				if(isThisNodeSubscribed)
//					/*
//					 * Invoked callback only, if the node is the topic subscriber
//					 * as well, not when it is only the root for this channel
//					 */
//					getNode().getCallback().onTopicRemove(getNode(), t.getID());
//				break;
//			default: break;
//			}
//		}
        
    }

    private void forwardToChild_(JCSyncMessage msg, Subscriber child) {
                Topic t = getTopic(msg.getCollectionID());
                msg.setSourceInfo(getNodeInfo());
                msg.setDestinationInfo(child.getNodeInfo());
                Subscriber sb = new Subscriber(msg.getPublisher(), t);
                NotifyOperation o = new NotifyOperation(t.getID(),
				                                sb,
				                                new Event(PubSubConstants.EVENT_CUSTOM));
                Transaction trans = new Transaction(o);
                msg.setTransactionID(trans.getID());
                JCSyncMessageCarrier msg_ = new JCSyncMessageCarrier(msg, t.getAccessControlRules());
                //Adding transaction and starting timer for it
                addTransaction(trans);
                log.trace("Sending message: "+msg);
                if(!sendMessage(msg_, PubSubTransport.ROUTING_DIRECT, t.getID())){
                    log.fatal("Unable to send message!");
                }
                sb = null;
                msg_ = null;
    }
    protected class Key{
            public String id;
            public int transID;
            public Key(String id, int transID){
                this.id = id;
                this.transID = transID;
            }

        @Override
        public boolean equals(Object obj) {
            Key b = (Key) obj;
            if(this.id.equals(b.id)){
                if(this.transID== b.transID) return true;
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
            return ((String)(id+transID)).hashCode();
        }

        @Override
        public String toString() {
            return "collID: "+this.id+", transID: "+this.transID;
        }


        }
        protected class Locker{
            public Semaphore semaphore;
            public int respCode;
            public Locker(Semaphore sem, int resp){
                this.semaphore = sem;
                this.respCode = resp;
            }
            public void setRespCode(int resp){
                this.respCode = resp;
            }

        @Override
        protected void finalize() throws Throwable {
            this.semaphore = null;
            super.finalize();
        }

        }
    public static NodeCallback getNodeCallback(){
        return callback;
    }
    protected synchronized void initCreateCollectionLocker(String collID, int transID) {
        log.debug("Preparing locker createCollection for: " + collID + ", tID:" + transID + " , thread:" + Thread.currentThread().getName());
        try {
            Semaphore z = new Semaphore(1);
            z.acquire();
            Key k = new Key(collID, transID);
            Locker l = new Locker(z, 0);
            constructorLockers.put(k, l);
        } catch (InterruptedException ex) {
        }
    }
    
    protected int lockCreateTopic(String collID, int transID) {
        log.debug("Locking createTopic for: "+ collID + ", tID:" + transID + " , thread:" + Thread.currentThread().getName());
        Key k;
        Locker v;
        try {
            k = new Key(collID, transID);
            v = topicLockers.get(k);
            v.semaphore.acquire();
            topicLockers.remove(k);
            return v.respCode;
        } catch (Exception ex) {
            log.fatal("Fatal error while locking create topic locker: ", ex);
        } finally {
            k = null;
            v = null;
            log.debug("Locker createTopic for: " + collID + ", tID:" + transID + " is FREE, thread:" + Thread.currentThread().getName());
        }
        
        return -1;
    }

    protected void unlockCreateTopic(String collID, int transID, int respCode) {
        log.debug("Unlocking createTopic for: "+ collID + ", tID:" + transID + " , thread:" + Thread.currentThread().getName());
        Key k = new Key(collID, transID);
        Locker r = topicLockers.get(k);
        if(r==null) return;
        topicLockers.get(k).setRespCode(respCode);
        topicLockers.get(k).semaphore.release();
    }

    protected synchronized void initCreateTopicLocker(String collID, int transID) {
        log.debug("Preparing locker createTopic for: " + collID + ", tID:" + transID + " , thread:" + Thread.currentThread().getName());
        try {
            Semaphore z = new Semaphore(1);
            z.acquire();
            Key k = new Key(collID, transID);
            Locker l = new Locker(z, 0);
            topicLockers.put(k, l);
        } catch (InterruptedException ex) {
        }
    }
    protected int lockCreateCollection(String collID, int transID) {
        log.debug("Locking createCollection for: "+ collID + ", tID:" + transID + " , thread:" + Thread.currentThread().getName());
        Key k;
        Locker v;
        try {
            k = new Key(collID, transID);
            v = constructorLockers.get(k);
            v.semaphore.acquire();
            constructorLockers.remove(k);
            return v.respCode;
        } catch (Exception ex) {
            log.fatal("Fatal error while locking create collection locker: ", ex);
        } finally {
            k = null;
            v = null;
            log.debug("Locker createCollection for: " + collID + ", tID:" + transID + " is FREE, thread:" + Thread.currentThread().getName());
        }
        
        return -1;
    }

    protected void unlockCreateCollection(String collID, int transID, int respCode) {
        log.debug("Unlocking createCollection for: " + collID + ", tID:" + transID + " , thread:" + Thread.currentThread().getName());
        //TODO make below in all unlock operation
        Key k = new Key(collID, transID);
        Locker v = constructorLockers.get(k);
        if (v != null) {
            v.setRespCode(respCode);
            v.semaphore.release();
        } else {
            log.fatal("Key: " + k + " does not exist!");
        }
    }

    
}
