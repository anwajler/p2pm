package pl.edu.pjwstk.mteam.jcsync.core.pubsub;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.core.NetworkObject;
import pl.edu.pjwstk.mteam.core.Node;
import pl.edu.pjwstk.mteam.core.NodeCallback;
import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncConstans;
import pl.edu.pjwstk.mteam.jcsync.operation.JCsyncAbstractOperation;
import pl.edu.pjwstk.mteam.jcsync.operation.RegisteredOperations;
import pl.edu.pjwstk.mteam.pubsub.algorithm.implementation.DefaultCustomizableAlgorithm;
import pl.edu.pjwstk.mteam.pubsub.core.Event;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.Subscriber;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.core.Transaction;
import pl.edu.pjwstk.mteam.pubsub.core.User;
import pl.edu.pjwstk.mteam.pubsub.interestconditions.InterestConditions;
import pl.edu.pjwstk.mteam.pubsub.message.PubSubMessage;
import pl.edu.pjwstk.mteam.pubsub.message.indication.NotifyIndication;
import pl.edu.pjwstk.mteam.pubsub.message.request.PublishRequest;
import pl.edu.pjwstk.mteam.pubsub.message.request.SubscribeRequest;
import pl.edu.pjwstk.mteam.pubsub.message.response.PubSubResponse;
import pl.edu.pjwstk.mteam.pubsub.operation.PublishOperation;
import pl.edu.pjwstk.mteam.pubsub.operation.SubscribeOperation;
import pl.edu.pjwstk.mteam.pubsub.transport.PubSubTransport;

/**
 * In extension of {@link DefaultCustomizableAlgorithm DefaultCustomizableAlgorithm} 
 * adapted to jcync.<br>
 * The main changes are overloaded methods to intercept messages associated 
 * with jcsync.To determines whether the message is related with JCsync one of
 * three method are used:<br>
 * - {@link PubSubCustomisableAlgorithm#whetherItIsJCSyncMessage(pl.edu.pjwstk.mteam.pubsub.message.request.PublishRequest) whetherItIsJCSyncMessage(pl.edu.pjwstk.mteam.pubsub.message.request.PublishRequest)}<br>
 * <pre>
 * private boolean whetherItIsJCSyncMessage(NotifyIndication req) {
        <strong>if ((req.getEventType() & RegisteredOperations.OP_GENERIC_JCSYNC_OPERATION) == RegisteredOperations.OP_GENERIC_JCSYNC_OPERATION) {
            return true;
        } </strong>
        return false;
    }
 * </pre>
 * - {@link PubSubCustomisableAlgorithm#whetherItIsJCSyncMessage(pl.edu.pjwstk.mteam.pubsub.message.request.PublishRequest) whetherItIsJCSyncMessage(pl.edu.pjwstk.mteam.pubsub.message.request.PublishRequest)}<br>
 * <pre>
 * private boolean whetherItIsJCSyncMessage(PublishRequest req) {
        <strong>if ((req.getEventType() & RegisteredOperations.OP_GENERIC_JCSYNC_OPERATION) == RegisteredOperations.OP_GENERIC_JCSYNC_OPERATION) {
            return true;
        }</strong>
        return false;
    }
 * </pre>
 * - {@link PubSubCustomisableAlgorithm#whetherItIsJCSyncMessage(byte[]) whetherItIsJCSyncMessage(byte[])}<br>
 * <pre>
 * private boolean whetherItIsJCSyncMessage(byte[] message) throws Exception {
        if (message != null && message.length >= 2) {
            int b1 = message[0] & 0xff;
            int b2 = message[1] & 0xff;
            if ((b1 | b2) < 0) {
                throw new Exception("An error occurred while parsing message!");
            }
            short msgType = (short) ((b1 << 8) + (b2 << 0));
            <strong>if ((msgType & RegisteredOperations.OP_GENERIC_JCSYNC_OPERATION) == RegisteredOperations.OP_GENERIC_JCSYNC_OPERATION) {
                return true;
            }</strong>
        }
        return false;
    }
 * </pre>
 * 
 * @author Piotr Bucior
 */
public class PubSubCustomisableAlgorithm extends DefaultCustomizableAlgorithm {

    private static final Logger log = Logger.getLogger("pl.edu.pjwstk.mteam.jcsync.core.pubsub.PubSubCustomisableAlgorithm");
    private final static boolean TRUE = true;
    private final static boolean FALSE = false;
    /**
     * jcsync core related with this pub sub layer instance
     */
    private MessageDeliveryObserver observer;
    private final ArrayList<String> knownTopicsName;
    private final NodeCallback nCallback;

    /**
     * Creates new instance of class.
     */
    public PubSubCustomisableAlgorithm() {
        //topic which will be related with shared objects
        this.knownTopicsName = new ArrayList<String>(50);
        this.nCallback = generateNodeCallback();
    }

//    public <Msg extends PubSubMessage> void sendMessage(Msg message) {
//        // ckecks method type, if the message is a not a sub message of JCSYNC_GENERIC_MESSAGE then throws exception
//        if (messageIsAJCSyncSubMessage(message)) {
//            if (messageIsAIndication(message)) {
//                getCoreAlgorithm().sendMessage(msg, PubSubTransport.ROUTING_OVERLAY, message.getTopicID());
//            } else if (messageIsARequest(message)) {
//                getCoreAlgorithm().sendMessage(msg, PubSubTransport.ROUTING_OVERLAY, message.getTopicID());
//            } else if (messageIsResponse(message)) {
//                getCoreAlgorithm().sendMessage(msg, PubSubTransport.ROUTING_OVERLAY, message.getTopicID());
//            } else {
//                // throw if the message type is not the one of three general types
//                throw new IllegalArgumentException("Unsuported message type!");
//            }
//        } else {
//            // throw if the message type is not the one of three general types
//            throw new IllegalArgumentException("Unsuported message type!");
//        }
//    }
    /**
     * Returns <tt>NodeCallback</tt> associated with this algorithm.
     * @return listener of this node
     */
    NodeCallback getNCallback() {
        return this.nCallback;
    }

    /**
     * Set <tt>MessageDeliveryObserver</tt> related with current algorithm.
     * @param observer <tt>MessageDeliveryObserver</tt> that will be associated 
     * with this instance of algorithm.
     */
    public void setMessageDeliveryObserver(MessageDeliveryObserver observer) {
        this.observer = observer;
    }

    /**
     * Informs algorithm that the given name is used by one of the shared object.
     * @param name shared object identifier.
     */
    public void registerSharedObjectName(String name) {
        if (!this.knownTopicsName.contains(name)) {
            this.knownTopicsName.add(name);
        }
    }
    
    /**
     * If the request is related with JCSync layer then informs related 
     * <tt>MessageDeliveryObserver</tt> about this fact, otherwise pass request 
     * to the superclass.
     * @param req delivered request.
     * @param t the topic object which delivered request is related for.
     */
    @Override
    public synchronized void onDeliverPublish(PublishRequest req, Topic t) {
        try {
            if (whetherItIsJCSyncMessage(req)) {
                log.trace("Received PublishRequest containing jcsync message.");
                this.observer.onDeliverRequest(req);
            } else {
                super.onDeliverPublish(req, t);
            }
        } catch (Exception ex) {
            log.error("An error occurred while processing pubsubRequest: ", ex);

        }
    }

    /**
     * If the indication is related with JCSync layer then informs related 
     * <tt>MessageDeliveryObserver</tt> about this fact, otherwise pass indication 
     * to the superclass.
     * @param ind delivered indication.
     * @param t the topic object which delivered indication is related for.
     */
    @Override
    public void onDeliverNotify(NotifyIndication ind, Topic t) {        
//        if(!this.knownTopicsName.contains(ind.getTopicID())){
//            super.onDeliverNotify(ind, t);
//        }
        //check if the message is one of the jcsync message
        if (whetherItIsJCSyncMessage(ind)) {
            log.trace(getCoreAlgorithm().getNodeInfo().getName()
                    + " (onDeliverNotify) - captured NotifyIndication: " + RegisteredOperations.OP_NAMES.get(ind.getEventType())
                    + ", direct: " + ind.isDirect()+", publisher: "+ind.getPublisher());
            JCsyncAbstractOperation op = JCsyncAbstractOperation.encode(ind.getMessage());
            if (ind.isDirect()) {
                this.observer.onDeliverIndication(op);
            }else{
                // if received indication is already stored (==already received) then do not pass it to the jcsync core
                if(getCoreAlgorithm().storeNotifyIndication(ind)){
                    this.observer.onDeliverIndication(op);
                }
                else{
                    log.trace("Received indication, that was already received in the past, will be skipped : "+ind);
                    return;
                }
            }           
            //if the message was not sent only to this node
            if (!ind.isDirect()) {                
                //sent to children without this node
                Vector<String> children = t.getChildren();
                Subscriber child;
                for (String childName : children) {
                    child = t.getChild(childName);
                    if (!child.getNodeInfo().equals(getCoreAlgorithm().getNodeInfo())) {
                        getCoreAlgorithm().forwardToChild(msg, child);
                        log.trace(getCoreAlgorithm().getNodeInfo().getName() + " (onDeliverNotify) - forwarded to child: " + RegisteredOperations.OP_NAMES.get(ind.getEventType())
                                + ", direct: " + ind.isDirect() + ", child:" + child.getNodeInfo().getName() + "@" + child.getNodeInfo().getIP());
                    }
                }
            }
        } else {
            super.onDeliverNotify(ind, t);
        }

    }

    /**
     * Publish given <tt>message</tt> on the overlay.
     * @param topic topic object that message is related for.
     * @param message encoded message.
     * @param eventType event type of given message.
     * @param trans transaction which will be assigned with this published message.
     */
    public void networkPublish_(Object topic, byte[] message, short eventType, Transaction trans) {
        String tid = (String) topic;
        NodeInfo thisNode = getCoreAlgorithm().getNodeInfo();
        Topic t = getCoreAlgorithm().getTopic(tid);
        if (t != null && t.getChild(thisNode.getID()) != null) {
            User parent = t.getParent();
            if (parent == null) {
                //this node is topic root
                parent = new User(thisNode);
            }
            PublishRequest msg = new PublishRequest(trans.getID(), thisNode,
                    parent.getNodeInfo(), tid,
                    eventType, message,
                    new User(thisNode.getName()));
            getCoreAlgorithm().sendMessage(msg, PubSubTransport.ROUTING_DIRECT, null);
        } else {
            log.info("The node is not '" + tid + "' subscriber, so it can't publish events");
        }
    }

    /**
     * Returns next transaction that will be used to send message.
     * @param topicID topic identifier which the transaction will be related.
     * @param eventType message type that will be related with this transaction.
     * @return transaction related with given <tt>topicID</tt> and <tt>eventType</tt>
     */
    public synchronized Transaction nextPublishTransaction(String topicID, short eventType) {
        NodeInfo thisNode = getCoreAlgorithm().getNodeInfo();
        Topic t = getCoreAlgorithm().getTopic(topicID);
        if (t != null && t.getChild(thisNode.getID()) != null) {

            PublishOperation o = new PublishOperation(topicID,
                    new Subscriber(t, thisNode),
                    new Event(eventType));
            Transaction trans = new Transaction(o, t);
            //Adding transaction and starting timer for it
            getCoreAlgorithm().addTransaction(trans);
            return trans;
        }
        return null;
    }
    /**
     * If the response is related with JCsync layer then <tt>MessageDeliveryObserver</tt>
     * will be informed about this fact, otherwise response is passed to the superclass.
     * @param res delivered response.
     * @param t transaction which is related with posted request. 
     */
    @Override
    public void onDeliverPublishResponse(PubSubResponse res, Transaction t) {
        if ((t.getOperation().getEvent().getType()
                & RegisteredOperations.OP_GENERIC_JCSYNC_OPERATION)
                == RegisteredOperations.OP_GENERIC_JCSYNC_OPERATION) {
            this.observer.onDeliverPubSubResponse(res.getTopicID(), t.getOperation().getEvent().getType(), (short) res.getResponseCode(), res.getTransactionID());
        } else {
            super.onDeliverPublishResponse(res, t);
        }
    }

//    /**
//     * Creates new topic which will be used to identify shared object.
//     * @param topicID topic name
//     * @param subscribe if true the
//     * @param acRules
//     * @param trans
//     */
//    public void createTopic_(Object topicID, boolean subscribe, Object acRules, Transaction trans) {
//        String tid = (String) topicID;
//        Topic newTopic = getCoreAlgorithm().getTopic(tid);
//        long transID = -1;
//        if (newTopic == null) {
//            newTopic = getCoreAlgorithm().getTopicStateLogger().getTopic(tid);
//        }
//        if (newTopic != null) {
//            //Topic exists locally
//            log.trace("Topic " + tid + " exists locally...");
//            log.trace("Invoking ONERROR callback for '" + tid + "'");
//            for (NodeCallback listener : getCoreAlgorithm().getNode().getCallbacks()) {
//                listener.onPubSubError(getCoreAlgorithm().getNode(), tid,
//                        PubSubConstants.OPERATION_CREATETOPIC,
//                        NodeError.TOPICEXISTSERR);
//            }
//
////            getCoreAlgorithm().getNode().getCallback().onPubSubError(getCoreAlgorithm().getNode(), tid,
////                    PubSubConstants.OPERATION_CREATETOPIC,
////                    NodeError.TOPICEXISTSERR);
//        } else {
//            newTopic = new Topic(tid);
//            NodeInfo thisNode = getCoreAlgorithm().getNodeInfo();
//            AccessControlRules ac = (AccessControlRules) acRules;
//            newTopic.setAccessControlRules(ac);
//            newTopic.setOwner(new Subscriber(newTopic, getCoreAlgorithm().getNodeInfo()));
//            CreateTopicOperation create =
//                    new CreateTopicOperation(newTopic.getID(),
//                    new Subscriber(newTopic, thisNode),
//                    new Event(PubSubConstants.EVENT_NEWTOPIC));
//            CreateTopicRequest msg = new CreateTopicRequest(thisNode,
//                    new NodeInfo(""), tid, ac,
//                    PubSubConstants.CREATETOPICFLAG_NEWTOPIC,
//                    trans.getID());
//            if (subscribe) {
//                SubscribeOperation sub = new SubscribeOperation(newTopic.getID(),
//                        new Subscriber(newTopic, thisNode),
//                        new Event(PubSubConstants.EVENT_ALL));
//                trans.addOperation(sub);
//                msg.addSubscriber(new User(thisNode));
//                newTopic.addSubscriber(thisNode);
//            }
//            log.trace("Sending request for:" + newTopic.getID() + "@" + PubSubConstants.STR_OPERATION.get(PubSubConstants.OPERATION_CREATETOPIC));
//            getCoreAlgorithm().sendMessage(msg, PubSubTransport.ROUTING_OVERLAY, tid);
//        }
//    }

    @Override
    public void networkSubscribe(Object topic, Object ic, int eventIndex) {
        Topic t = null;
        String tid = null;
        if (topic instanceof String) {
            tid = (String) topic;
            t = getCoreAlgorithm().getTopic(tid);
        } else {
            t = (Topic) topic;
            tid = t.getID();
        }
        if (t != null) {
            NodeInfo thisNode = getCoreAlgorithm().getNodeInfo();
            if (t.isTopicRoot(getCoreAlgorithm().getNodeInfo().getID())) {
                //Topic exists locally
                log.trace(getCoreAlgorithm().getNodeInfo().getName() + " - Topic " + tid + " exists - subscribing locally...");
                t.addSubscriber(getCoreAlgorithm().getNodeInfo());
                t.getChild(getCoreAlgorithm().getNodeInfo().getID()).getSubscription(t.getID()).setInterestConditions((InterestConditions) ic);
                for (NodeCallback listener : getCoreAlgorithm().getNode().getCallbacks()) {
                    listener.onTopicSubscribe(getCoreAlgorithm().getNode(), t.getID());
                }
            } else {
//            getCoreAlgorithm().getNode().getCallback().onTopicSubscribe(getCoreAlgorithm().getNode(), t.getID());

//                t = new Topic(tid);
//                t.addSubscriber(thisNode);
//                t.getChild(thisNode.getID()).getSubscription(t.getID()).setInterestConditions((InterestConditions) ic);
                int distance = getCoreAlgorithm().getNode().getDistance(tid, thisNode.getID());
                SubscribeOperation o = new SubscribeOperation(t.getID(),
                        new Subscriber(t, thisNode),
                        new Event(PubSubConstants.EVENT_ALL));
                Transaction trans = new Transaction(o, t);
                SubscribeRequest msg = new SubscribeRequest(trans.getID(), thisNode,
                        new NodeInfo(""), tid,
                        100000000,
                        eventIndex,
                        distance);
                //Adding transaction and starting timer for it
                getCoreAlgorithm().addTransaction(trans);
                getCoreAlgorithm().sendMessage(msg, PubSubTransport.ROUTING_OVERLAY, tid);
            }
        } else {
            //---------------------------------------------------------------------
            //TODO: [DHT-based] this section works only for the DHT-based networks
            //---------------------------------------------------------------------
            NodeInfo thisNode = getCoreAlgorithm().getNodeInfo();
            t = new Topic(tid);
            t.addSubscriber(thisNode);
            t.getChild(thisNode.getID()).getSubscription(t.getID()).setInterestConditions((InterestConditions) ic);
            int distance = getCoreAlgorithm().getNode().getDistance(tid, thisNode.getID());
            SubscribeOperation o = new SubscribeOperation(t.getID(),
                    new Subscriber(t, thisNode),
                    new Event(PubSubConstants.EVENT_ALL));
            Transaction trans = new Transaction(o, t);
            SubscribeRequest msg = new SubscribeRequest(trans.getID(), thisNode,
                    new NodeInfo(""), tid,
                    100000000,
                    eventIndex,
                    distance);
            //Adding transaction and starting timer for it
            getCoreAlgorithm().addTransaction(trans);
            getCoreAlgorithm().sendMessage(msg, PubSubTransport.ROUTING_OVERLAY, tid);
            //---------------------------------------------------------------------
        }
    }

//    /**
//     * 
//     * @param topicID
//     * @param eventType
//     * @return
//     */
//    public Transaction nextCreateTopicTransaction(String topicID, short eventType) {
//        String tid = (String) topicID;
//        Topic newTopic = getCoreAlgorithm().getTopic(tid);
//        long transID = -1;
//        if (newTopic == null) {
//            newTopic = getCoreAlgorithm().getTopicStateLogger().getTopic(tid);
//        }
//        if (newTopic != null) {
//            return null;
//        } else {
//            newTopic = new Topic(tid);
//            NodeInfo thisNode = getCoreAlgorithm().getNodeInfo();
//            newTopic.setOwner(new Subscriber(newTopic, getCoreAlgorithm().getNodeInfo()));
//            CreateTopicOperation create =
//                    new CreateTopicOperation(newTopic.getID(),
//                    new Subscriber(newTopic, thisNode),
//                    new Event(PubSubConstants.EVENT_NEWTOPIC));
//            Transaction trans = new Transaction(create, newTopic);
//            log.trace("Creating transaction " + trans);
//            getCoreAlgorithm().addTransaction(trans);
//            return trans;
//        }
//
//    }

    /**
     * Checks whether given <tt>NotifyIndication</tt> is related with jcsync.
     * @return true if given indication is related with jcync.
     */
    private boolean whetherItIsJCSyncMessage(NotifyIndication req) {
        if ((req.getEventType() & RegisteredOperations.OP_GENERIC_JCSYNC_OPERATION) == RegisteredOperations.OP_GENERIC_JCSYNC_OPERATION) {
            return true;
        }
        return false;
    }
    /**
     * Checks whether given <tt>PublishRequest</tt> is related with jcsync.
     * @return true if given request is related with jcsync.
     */
    private boolean whetherItIsJCSyncMessage(PublishRequest req) {
        if ((req.getEventType() & RegisteredOperations.OP_GENERIC_JCSYNC_OPERATION) == RegisteredOperations.OP_GENERIC_JCSYNC_OPERATION) {
            return true;
        }
        return false;
    }
    /**
     * Checks whether given encoded message is related with jcsync.
     * <br> It is used when {@link NodeCallback#onTopicNotify(pl.edu.pjwstk.mteam.core.Node, java.lang.Object, byte[], boolean, short) onTopicNotify(pl.edu.pjwstk.mteam.core.Node, java.lang.Object, byte[], boolean, short)} is called.
     * @param message encoded message
     * @return true if the message is related with jcsync.
     * @throws Exception any occurred exception during this operation.
     */
    private boolean whetherItIsJCSyncMessage(byte[] message) throws Exception {
        if (message != null && message.length >= 2) {
            int b1 = message[0] & 0xff;
            int b2 = message[1] & 0xff;
            if ((b1 | b2) < 0) {
                throw new Exception("An error occurred while parsing message!");
            }
            short msgType = (short) ((b1 << 8) + (b2 << 0));
            if ((msgType & RegisteredOperations.OP_GENERIC_JCSYNC_OPERATION) == RegisteredOperations.OP_GENERIC_JCSYNC_OPERATION) {
                return true;
            }
        }
        return false;
    }

    private <Msg extends PubSubMessage> boolean messageIsAIndication(Msg message) {
        if ((message.getType() & JCSyncConstans.JCSYNC_GENERIC_INDICATION)
                == JCSyncConstans.JCSYNC_GENERIC_INDICATION) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    private <Msg extends PubSubMessage> boolean messageIsResponse(Msg message) {
        if ((message.getType() & JCSyncConstans.JCSYNC_GENERIC_RESPONSE)
                == JCSyncConstans.JCSYNC_GENERIC_RESPONSE) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    private <Msg extends PubSubMessage> boolean messageIsARequest(Msg message) {
        if ((message.getType() & JCSyncConstans.JCSYNC_GENERIC_REQUEST)
                == JCSyncConstans.JCSYNC_GENERIC_REQUEST) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    private <Msg extends PubSubMessage> boolean messageIsAJCSyncSubMessage(Msg message) {
        if ((message.getType() & JCSyncConstans.JCSYNC_GENERIC_MESSAGE)
                == JCSyncConstans.JCSYNC_GENERIC_MESSAGE) {
            return TRUE;
        } else {
            return FALSE;
        }
    }
    
    private NodeCallback generateNodeCallback() {
        return new NodeCallback() {

            @Override
            public void onDisconnect(Node node) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onUserLookup(Node node, Object userInfo) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onObjectLookup(Node node, Object object) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onTopicNotify(Node node, Object topicID, byte[] message, boolean historical, short eventType) {
                try {
//                    if(knownTopicsName.contains((String)topicID)){
//                        return;
//                    }
                    if (whetherItIsJCSyncMessage(message)) {
                        JCsyncAbstractOperation op = JCsyncAbstractOperation.encode(message);
                        observer.onDeliverIndication(op);
                    } else {
                        log.trace(getCoreAlgorithm().getNodeInfo().getName() + "- (onTopicNotify) invoked with unknown message");
                    }
                } catch (Exception ex) {
                    log.error("", ex);
                }
            }

            @Override
            public void onTopicCreate(Node node, Object topicID) {
                if (knownTopicsName.contains((String) topicID)) {
                    observer.onDeliverPubSubResponse((String) topicID, PubSubConstants.MSG_CREATETOPIC, (short) PubSubConstants.RESP_SUCCESS, -1);
                }
            }

            @Override
            public void onTopicCreate(Node node, Object topicID, int transID) {
                if (knownTopicsName.contains((String) topicID)) {
                    observer.onDeliverPubSubResponse((String) topicID, PubSubConstants.MSG_CREATETOPIC, (short) PubSubConstants.RESP_SUCCESS, transID);
                }
            }

            @Override
            public void onTopicRemove(Node node, Object topicID) {
                if (knownTopicsName.contains((String) topicID)) {
                    observer.onDeliverPubSubResponse((String) topicID, PubSubConstants.EVENT_REMOVETOPIC, (short) PubSubConstants.RESP_SUCCESS, 1);
                }
            }

            @Override
            public void onTopicSubscribe(Node node, Object topicID) {
                if (knownTopicsName.contains((String) topicID)) {
                    observer.onDeliverPubSubResponse((String) topicID, PubSubConstants.MSG_SUBSCRIBE, (short) PubSubConstants.RESP_SUCCESS, -1);
                }
            }

            @Override
            public void onTopicSubscribe(Node node, Object topicID, int transID) {
                if (knownTopicsName.contains((String) topicID)) {
                    observer.onDeliverPubSubResponse((String) topicID, PubSubConstants.MSG_SUBSCRIBE, (short) PubSubConstants.RESP_SUCCESS, transID);
                }
            }

            @Override
            public void onTopicUnsubscribe(Node node, Object topicID, int respCode) {
                if (knownTopicsName.contains((String) topicID)) {
                    observer.onDeliverPubSubResponse((String) topicID, PubSubConstants.MSG_UNSUBSCRIBE, (short) respCode, -1);
                }
            }

            @Override
            public void onInsertObject(Node node, NetworkObject object) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onJoin(Node node) {
                //ignore
            }

            @Override
            public void onOverlayError(Node node, Object sourceID, int errorCode) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onOverlayError(Node node, Object sourceID, int errorCode, int transID) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onPubSubError(Node node, Object topicID, short operationType, int errorCode) {
//                if (knownTopicsName.contains((String) topicID)) {
//                    if (operationType == PubSubConstants.OPERATION_CREATETOPIC) {
//                        observer.onDeliverPubSubResponse((String) topicID, PubSubConstants.MSG_CREATETOPIC, (short) PubSubConstants.RESP_ALREADYEXISTS);
//                    } else if ((operationType == PubSubConstants.OPERATION_SUBSCRIBE)) {
//                        observer.onDeliverPubSubResponse((String) topicID, PubSubConstants.MSG_SUBSCRIBE, (short) errorCode);
//                    }
//                }
            }

            @Override
            public void onPubSubError(Node node, Object topicID, short operationType, int errorCode, int transID) {
                log.debug("(onPubSubError), topicID: " + topicID + ", operationType:" + operationType + ", errorCode:" + errorCode + ", transID:" + transID);
                if (knownTopicsName.contains((String) topicID)) {
                    if (operationType == PubSubConstants.OPERATION_CREATETOPIC) {
                        observer.onDeliverPubSubResponse((String) topicID, PubSubConstants.MSG_CREATETOPIC, (short) PubSubConstants.RESP_ALREADYEXISTS, transID);
                    } else if ((operationType == PubSubConstants.OPERATION_SUBSCRIBE)) {
                        observer.onDeliverPubSubResponse((String) topicID, PubSubConstants.MSG_SUBSCRIBE, (short) errorCode, transID);
                    } else if ((operationType == PubSubConstants.OPERATION_PUBLISH)) {
                        observer.onDeliverPubSubResponse((String) topicID, PubSubConstants.MSG_PUBLISH, (short) errorCode, transID);
                    }
                }
            }

            @Override
            public boolean onDeliverRequest(List<NetworkObject> objectList) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean onForwardingRequest(List<NetworkObject> objectList) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }
}
