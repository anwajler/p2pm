package pl.edu.pjwstk.mteam.pubsub.core;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;

import pl.edu.pjwstk.mteam.pubsub.interestconditions.InterestConditions;
import pl.edu.pjwstk.mteam.pubsub.logging.Logger;

import pl.edu.pjwstk.mteam.core.AbstractCoreAlgorithm;
import pl.edu.pjwstk.mteam.core.Node;
import pl.edu.pjwstk.mteam.core.NodeCallback;
import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.accesscontrol.AccessControlRules;
import pl.edu.pjwstk.mteam.pubsub.algorithm.AlgorithmConfigurator;
import pl.edu.pjwstk.mteam.pubsub.algorithm.CustomizableAlgorithm;
import pl.edu.pjwstk.mteam.pubsub.algorithm.implementation.DefaultAlgorithmConfigurator;
import pl.edu.pjwstk.mteam.pubsub.message.PubSubMessage;
import pl.edu.pjwstk.mteam.pubsub.message.PubSubMessageListener;
import pl.edu.pjwstk.mteam.pubsub.message.indication.KeepAliveIndication;
import pl.edu.pjwstk.mteam.pubsub.message.indication.NotifyIndication;
import pl.edu.pjwstk.mteam.pubsub.message.indication.PubSubIndication;
import pl.edu.pjwstk.mteam.pubsub.message.request.CreateTopicRequest;
import pl.edu.pjwstk.mteam.pubsub.message.request.PubSubRequest;
import pl.edu.pjwstk.mteam.pubsub.message.request.PublishRequest;
import pl.edu.pjwstk.mteam.pubsub.message.request.SubscribeRequest;
import pl.edu.pjwstk.mteam.pubsub.message.request.UnsubscribeRequest;
import pl.edu.pjwstk.mteam.pubsub.message.response.PubSubResponse;
import pl.edu.pjwstk.mteam.pubsub.message.response.StandardResponse;
import pl.edu.pjwstk.mteam.pubsub.operation.NotifyOperation;
import pl.edu.pjwstk.mteam.pubsub.operation.PublishOperation;
import pl.edu.pjwstk.mteam.pubsub.operation.SubscribeOperation;
import pl.edu.pjwstk.mteam.pubsub.topology.TopologyManager;
import pl.edu.pjwstk.mteam.pubsub.topology.implementation.TreeTopology;
import pl.edu.pjwstk.mteam.pubsub.topology.maintenance.message.TopologyCacheUpdateRequest;
import pl.edu.pjwstk.mteam.pubsub.transport.PubSubTransport;
import pl.edu.pjwstk.mteam.pubsub.util.persistence.StateLoggerManager;
import pl.edu.pjwstk.mteam.pubsub.util.persistence.TopicStateLogger;
import pl.edu.pjwstk.mteam.pubsub.util.persistence.TransactionStateLogger;
import pl.edu.pjwstk.mteam.pubsub.util.persistence.db.DBConnection;

public class CoreAlgorithm extends AbstractCoreAlgorithm implements PubSubMessageListener {

    private static Logger logger = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.core.PubSubManager");
    protected AlgorithmConfigurator algChooser;
    protected TopologyManager topologyManager;
    //PubSubTransport - to mi sie przyda
    protected PubSubTransport transport;
    /**
     * List of topics, this node is root/forwarder/subscriber for.
     */
    protected Hashtable<String, Topic> topics;
    /**
     * List of pending transactions.
     */
    //protected Hashtable<Integer, Transaction> transactions;
    protected TransactionStateLogger transaction_logger;
    protected TopicStateLogger topic_logger;
    //protected Hashtable<Integer, Transaction> maintenanceTransaction;
    private Thread keepAliveSender;
    private Maintenance_Keep_Alive_Sender keepAliveSender_;
    private boolean isRunning = false;
    protected DBConnection t_db_conn;

    /**
     * Creates new publish-subscribe manager.
     * @param port Publish-subscribe port number.
     * @param n <code>P2PNode</code> object, this publish-subscribe manager
     *          is associated with.
     */
    public CoreAlgorithm(int port, Node n) {
        super(n);
        algChooser = new DefaultAlgorithmConfigurator();
        topologyManager = new TreeTopology(this);
        transport = new PubSubTransport(this, port, n);
        n.setMessageListener(transport);
        topics = new Hashtable<String, Topic>();
        this.transaction_logger = StateLoggerManager.getTransactionStateLogger();
        //transactions = new Hashtable<Integer, Transaction>();
        //this.keepAliveSender_ = new Maintenance_Keep_Alive_Sender(this, topics.elements());
        this.topic_logger = StateLoggerManager.getTopicStateLogger();
        //this.maintenanceTransaction = new Hashtable<Integer, Transaction>();
    }

    public CoreAlgorithm(int port, Node n, AlgorithmConfigurator chooser) {
        this(port, n);
        algChooser = chooser;
    }

    public CoreAlgorithm(int port, Node n, TopologyManager topology) {
        this(port, n);
        topologyManager = topology;
    }

    /*
     * TODO: replace 'public' with 'protected'
     */
    public void setCustomizableAlgorithm() {
        String algName = getNode().getOverlayAlgorithm();
        algorithm = algChooser.chooseAlgorithm(algName);
        algorithm.setCoreAlgorithm(this);
    }

    public void setTopology(TopologyManager t) {
        topologyManager = t;
    }

    public CustomizableAlgorithm getCustomizableAlgorithm() {
        return (CustomizableAlgorithm) super.getCustomizableAlgorithm();
    }

    public TopologyManager getTopology() {
        return topologyManager;
    }

    /**
     *
     * @return NodeInfo object, containing information about the node, this
     *         publish-subscribe manager is associated with.
     */
    public NodeInfo getNodeInfo() {
        Node node = getNode();
        return new NodeInfo(node.getID(), node.getIP(), node.getUserName(), transport.getPort());
    }
    private String debug_id = "";

    public String getID() {
        return this.debug_id;
    }

    public PubSubTransport getTransport() {
        return transport;
    }

    /**
     * Initializes the appropriate Customizable Algorithm component.
     */
    public void init() {
        this.debug_id = getNode().getID();
        setCustomizableAlgorithm();
        initCacheDBConnections();
        //this.keepAliveSender = new Thread(this.keepAliveSender_);
        //this.keepAliveSender.start();
        //this.isRunning = true;
    }

    public void DEBUG_beforeNetworkLeave() {
        this.isRunning = false;
        getCustomizableAlgorithm().beforeNetworkLeave(topics);
        this.topics = null;
        this.topics = new Hashtable<String, Topic>();
    }

    //------------------------------------------------------------
    // Topics utilities
    //------------------------------------------------------------
    public void addTopic(Topic t) {
        if (topics.containsKey(t.getID())) {
            try {
                throw new Exception("Topic already exists");
            } catch (Exception ex) {
                logger.fatal("", ex);
            }
        } else {
            topics.put(t.getID(), t);
            if(this.topic_logger.getTopic(t.getID())==null){
                this.topic_logger.addNewTopic(t);
            }else{
                this.topic_logger.clearTopicState(t.getID());
            }
        }

    }

    public void addTopic(Topic t, boolean store) {
        if (topics.containsKey(t.getID())) {
            try {
                throw new Exception("Topic already exists");
            } catch (Exception ex) {
                logger.fatal("", ex);
            }
        } else {
            topics.put(t.getID(), t);
            if (store) {
                this.topic_logger.addNewTopic(t);
            }
        }
    }

    public void removeTopic(String topicId) {
        Topic forRemoval = topics.get(topicId);
        Vector<String> chldrn = forRemoval.getChildren();
        for (int i = 0; i < chldrn.size(); i++) {
            logger.trace("Trying to remove subscriber: " + chldrn.get(i));
            forRemoval.removeSubscriber(chldrn.get(i));
        }
        forRemoval.setParent(null);
        topics.remove(topicId);
    }

    public Topic getTopic(String topicId) {
        return topics.get(topicId);
    }

    public boolean hasTopic(String topicId) {
        return topics.containsKey(topicId);
    }

    public void DEBUG_showTopics() {
        Enumeration<Topic> tpcs = topics.elements();
        String msg = "\nTopics for '" + getNodeInfo() + "':\n";
        while (tpcs.hasMoreElements()) {
            msg += tpcs.nextElement();
        }
        logger.debug(msg);
    }

    public String DEBUG_showTopics_() {
        Enumeration<Topic> tpcs = topics.elements();
        String msg = "\nTopics for '" + getNodeInfo() + "':\n";
        while (tpcs.hasMoreElements()) {
            msg += tpcs.nextElement().DEBUG_toString();
        }
        logger.debug(msg);
        return msg;
    }

    public void modifyInterestConditions(String topicID, InterestConditions ic) {
        Topic t = topics.get(topicID);
        t.getChild(getNodeInfo().getID()).getSubscription(t.getID()).setInterestConditions(ic);
    }

    //------------------------------------------------------------
    //------------------------------------------------------------
    // Transactions utilities
    //------------------------------------------------------------
    /**
     * Adds new pending transaction.
     * @param t New transaction.
     */
    public void addTransaction(Transaction t) {
        //transactions.put(t.getID(), t);
        //t.begin();
        this.transaction_logger.addTransaction(t);
    }

    /**
     * Adds new pending keep-alive transaction.
     * @param t New transaction.
     */
    public void addKeepAliveTransaction(Transaction trans) {
        //this.maintenanceTransaction.put(trans.getID(),trans);
        //trans.begin();
        this.transaction_logger.addTransaction(trans);
    }

    public Transaction getKeepAliveTransaction(int tID) {
        //return this.maintenanceTransaction.get(tID);
        return this.transaction_logger.getTransaction(tID);
    }

    /**
     * Removes the specified transaction.
     * @param transID ID of the transaction for removal.
     * @return Removed transaction or <code>null</code> if there is no transaction with
     *         the specified ID.
     */
    @Deprecated
    public Transaction removeTransaction(int transID) {
        return null;
    }

    public void DEBUG_showTransactions() {
        //Enumeration<Transaction> trnscs = transactions.elements();
        Enumeration<Transaction> trnscs = this.transaction_logger.getPendingTransactions();
        String msg = "\nPending transactions for '" + getNodeInfo() + "':\n";
        while (trnscs.hasMoreElements()) {
            msg += trnscs.nextElement();
        }
        logger.debug(msg);
    }

    //------------------------------------------------------------
    //------------------------------------------------------------
    // Messages processing
    //------------------------------------------------------------
    public boolean onDeliverIndication(PubSubIndication ind) {
        Topic t = topics.get(ind.getTopicID());
        if (t != null) { //node stores information about this topic
            AccessControlRules ac = t.getAccessControlRules();
            if (ind instanceof NotifyIndication) {
                logger.trace("Received notify indication...");
                NotifyOperation o = new NotifyOperation(t.getID(),
                        new Subscriber(t, ind.getSourceInfo()),
                        new Event(((NotifyIndication) ind).getEventType()));
                if (ac.isAllowed(o)) {
                    logger.trace("Notify "
                            + PubSubConstants.STR_EVENT[((NotifyIndication) ind).getEventType()]
                            + "('" + t.getID() + "') allowed for user '"
                            + o.getUser() + "'");
                    getCustomizableAlgorithm().onDeliverNotify((NotifyIndication) ind, t);
                } else {
                    logger.trace("Notify "
                            + PubSubConstants.STR_EVENT[((NotifyIndication) ind).getEventType()]
                            + "('" + t.getID() + "' forbidden for user '"
                            + o.getUser() + "'");
                }
            } else if (ind instanceof KeepAliveIndication) {
                KeepAliveIndication kal = (KeepAliveIndication) ind;
                logger.trace("[MAINT] " + getNodeInfo().getName() + " - received keep-alive indication from node: " + kal.getSourceInfo() + " ...");
                if (kal.getKeepAliveType() == KeepAliveIndication.KEEP_ALIVE_NORMAL) {
                    // s3544 do nothink now - keep alive is sending only by direct connection
                } else if (kal.getKeepAliveType() == KeepAliveIndication.KEEP_ALIVE_PARENT_FAILS) {
                    /*
                     * s3544
                     * child own child fail:
                     * send keep alive to it (if fail then remove child from topic) and cache update request to source node
                     *
                     */

                    logger.debug("MAINT " + getNodeInfo().getName() + " - received keepAliveIndication with failed topic child, topicID: " + kal.getTopicID() + ", source: " + kal.getSourceInfo() + ", failedNode: " + kal.getFailedNode());

                    logger.debug("MAINT " + getNodeInfo().getName() + " - checking failed node: " + kal.getFailedNode());

                    if (!getCustomizableAlgorithm().keepAlive(kal.getFailedNode(), t.getID())) {
                        logger.debug("MAINT " + getNodeInfo().getName() + " - node fail: " + kal.getFailedNode() + " , removing from child and send cache update to other child");
                        if (t.removeSubscriber(kal.getFailedNode())) {
                            getCustomizableAlgorithm().maintenanceCacheUpdate(t, kal.getFailedNode(), PubSubConstants.MAINTENANCE_DELETE_NODE);
                        }

                    }

                    SubscribeOperation o = new SubscribeOperation(kal.getTopicID(),
                            new Subscriber(kal.getSourceInfo().getName(), new Topic(kal.getTopicID())),
                            new Event(PubSubConstants.EVENT_ALL));
                    if (ac.isAllowed(o)) {
                        getCustomizableAlgorithm().maintenanceCacheUpdate(t, kal.getSourceInfo(), PubSubConstants.MAINTENANCE_NEW_NODE_CONNECTED);
                        t.addSubscriber(kal.getSourceInfo());
                    } else {
                        logger.debug("MAINT [ERROR] - node not allowed in this topic");
                    }
                } else if (kal.getKeepAliveType() == KeepAliveIndication.ROOT_FAILS) {
                    t.setParent(null);
                    t.setGrandParent(null);
                    if (t.getProposedNewRoot().getID().equals(getID())) {
                        t.setProposedNewRoot(null);
                    }
                    //if (kal.getFailedNode().getID().equals(t.getParent().getNodeInfo().getID())) {
                    SubscribeOperation o = new SubscribeOperation(kal.getTopicID(),
                            new Subscriber(kal.getSourceInfo().getName(), new Topic(kal.getTopicID())),
                            new Event(PubSubConstants.EVENT_ALL));
                    if (ac.isAllowed(o)) {
                        getCustomizableAlgorithm().maintenanceCacheUpdate(t, kal.getSourceInfo(), PubSubConstants.MAINTENANCE_NEW_NODE_CONNECTED);
                        t.addSubscriber(kal.getSourceInfo());
                    } else {
                        logger.debug("MAINT [ERROR] - node not allowed in this topic");
                    }
                    //}
                }
            }
        }
        /* TODO: Add passing the notify associated with an unknown topic to the customizable algorithm -
        it may be required by the unstructured networks (gossiping/random walks, etc.).
        else{
        if(ind instanceof NotifyIndication){
        getCustomizableAlgorithm().onDeliverUnknownIndication((NotifyIndication)ind);
        }
        }
         */
        return false;
    }

    public void terminateTransaction(int tID, byte code) {
        this.transaction_logger.markTransactionEnd(tID, code);
    }

    public boolean onDeliverRequest(PubSubRequest req) {
        logger.trace(this.getNodeInfo().getName() + " - Searching for topic '" + req.getTopicID() + "'....");
        Topic t = getTopic(req.getTopicID());
        if (t != null) { //node stores information about this topic
            AccessControlRules ac = t.getAccessControlRules();
            if (req instanceof SubscribeRequest) {
                SubscribeOperation o = new SubscribeOperation(req.getTopicID(),
                        new Subscriber(req.getSourceInfo().getName(), new Topic(req.getTopicID())),
                        new Event(PubSubConstants.EVENT_ALL));
                if (ac.isAllowed(o)) {
                    Subscriber s = t.getChild(req.getSourceInfo().getID());
                    if (s != null) {
                        logger.trace(this.getNodeInfo().getName() + " - Received resubscribe request from '" + req.getSourceInfo() + "'...");
                        //TODO: handle resubscription issues
                    } else {
                        logger.trace(this.getNodeInfo().getName() + " - Received subscribe request from '" + req.getSourceInfo() + "'....");
                        boolean retValue = topologyManager.onDeliverSubscribe((SubscribeRequest) req, t);
                        /* TODO: if request accepted - historical events should be sent here!!!
                         *       onDeliver/ForwardingSubscribe should return operation results
                         */
                        if (retValue) {
                            getCustomizableAlgorithm().maintenanceCacheUpdate(t, req.getSourceInfo(), PubSubConstants.MAINTENANCE_NEW_NODE_CONNECTED);
                            synchroniseTopic((SubscribeRequest) req, t);
                        }
                    }
                } else {
                    logger.trace(this.getNodeInfo().getName() + " - Subscribe operation not allowed for '" + req.getSourceInfo().getName() + "'...");
                    sendResponse(PubSubConstants.RESP_FORBIDDEN, req, t);
                }
            } else if (req instanceof PublishRequest) {
                PublishRequest pubreq = (PublishRequest) req;
                PublishOperation o = new PublishOperation(t.getID(),
                        new Subscriber(pubreq.getPublisher(),
                        t),
                        new Event(pubreq.getEventType()));
                if (ac.isAllowed(o)) {
                    getCustomizableAlgorithm().onDeliverPublish(pubreq, t);
                } else {
                    logger.trace(this.getNodeInfo().getName() + " - Publish " + PubSubConstants.STR_EVENT[pubreq.getEventType()]
                            + " operation not allowed for '" + req.getSourceInfo().getName()
                            + "'...");
                    sendResponse(PubSubConstants.RESP_FORBIDDEN, req, t);
                }
            } else if (req instanceof TopologyCacheUpdateRequest) {
                getCustomizableAlgorithm().onDeliverCacheUpdateRequest((TopologyCacheUpdateRequest) req);
            } else if (req instanceof UnsubscribeRequest) {
                logger.trace(this.getNodeInfo().getName() + " - Received unsubscribe request from " + req.getSourceInfo());
                getCustomizableAlgorithm().onDeliverUnsubscribe((UnsubscribeRequest) req, t);
            } else if (req instanceof CreateTopicRequest) {
                CreateTopicRequest crtpcreq = (CreateTopicRequest) req;
                switch (crtpcreq.getFlag()) {
                    case PubSubConstants.CREATETOPICFLAG_NEWTOPIC:
                        logger.trace("Topic " + t.getID() + " already exists.....");
                        sendResponse(PubSubConstants.RESP_ALREADYEXISTS, req, t);
                        break;
                    case PubSubConstants.CREATETOPICFLAG_TRANSFERTOPIC:
                        /*
                         * This may happen, while repairing multicast topology - just don't create
                         * the topic.
                         */
                        logger.trace("Accepting extisting topic transfer (" + t.getID() + ")");
                        topologyManager.onDeliverTransferTopic(crtpcreq, t);
                        break;
                }
            }
        } else {
            logger.trace("Topic '" + req.getTopicID() + "' not found...");
            if (req instanceof CreateTopicRequest) {
                CreateTopicRequest crtpcreq = (CreateTopicRequest) req;
                switch (crtpcreq.getFlag()) {
                    case PubSubConstants.CREATETOPICFLAG_NEWTOPIC:
                        logger.trace("Accepting create topic request (" + req.getTopicID() + ")...");
                        getCustomizableAlgorithm().onDeliverCreateTopic(crtpcreq);
                        break;
                    case PubSubConstants.CREATETOPICFLAG_TRANSFERTOPIC:
                        Topic transferedTopic = new Topic(req.getTopicID());
                        transferedTopic.setAccessControlRules(crtpcreq.getAccessRules());
                        logger.trace("Accepting topic transfer request (" + req.getTopicID() + ")...");
                        addTopic(transferedTopic);
                        topologyManager.onDeliverTransferTopic(crtpcreq, transferedTopic);
                        break;
                }
            } else {
                sendResponse(PubSubConstants.RESP_DOESNOTEXIST, req, null);
            }
        }
        return false;
    }

    public boolean onDeliverResponse(PubSubResponse res) {
        logger.debug("OnDeliverResponse, topicID: "+res.getTopicID()+", type: "+res.getResponseCode()+", source name: "+res.getSourceInfo().getName()+", destination name: "+res.getDestinationInfo().getName());
        //Transaction t = transactions.remove(res.getTransactionID());
        Transaction t = null;
        try{
         t = this.transaction_logger.getTransaction(res.getTransactionID());
        }catch(IllegalArgumentException e){
           t = null;
           logger.error("Transaction: "+res.getTransactionID()+" - doesn't exists!");
        }
        if (t != null) {
            //Stopping the timer associated with this transaction
            //t.terminate();
            terminateTransaction(t.getID(), Transaction.COMPLETED);
            Operation o = t.getOperation();
            logger.trace("Received response associated with the " + t);
            switch (o.getType()) {
                case PubSubConstants.OPERATION_CREATETOPIC:
                    if (o.getEvent().getType() == PubSubConstants.EVENT_NEWTOPIC) {
                        getCustomizableAlgorithm().onDeliverCreateTopicResponse(res, t);
                    } else {
                        Topic topic = getTopic(o.getID());
                        topologyManager.onDeliverTransferTopicResponse((StandardResponse) res,
                                topic);
                    }
                    break;
                case PubSubConstants.OPERATION_SUBSCRIBE:
                    getCustomizableAlgorithm().onDeliverSubscribeResponse(res, t);
                    break;
                case PubSubConstants.OPERATION_UNSUBSCRIBE:
                    getCustomizableAlgorithm().onDeliverUnsubscribeResponse(res, t);
                    break;
                case PubSubConstants.OPERATION_PUBLISH:
                    getCustomizableAlgorithm().onDeliverPublishResponse(res, t);
                    break;
            }
        } else {
            logger.error(getNodeInfo().getName() + " --- Received response for the transaction " + res.getTransactionID() + " which does not exist...");
        }
        return false;
    }

    public boolean onForwardingRequest(PubSubRequest req) {
        logger.trace("Searching for topic '" + req.getTopicID() + "'....");
        Topic t = getTopic(req.getTopicID());
        if (t != null) { //node stores information about this topic
            AccessControlRules ac = t.getAccessControlRules();
            if (req instanceof SubscribeRequest) {
                SubscribeOperation o = new SubscribeOperation(req.getTopicID(),
                        new Subscriber(req.getSourceInfo().getName(), new Topic(req.getTopicID())),
                        new Event(PubSubConstants.EVENT_ALL));
                if (ac.isAllowed(o)) {
                    logger.trace("Forwarding subscribe request from '" + req.getSourceInfo() + "'....");
                    topologyManager.onForwardingSubscribe((SubscribeRequest) req, t);
                } else {
                    logger.trace("Subscribe operation not allowed for '" + req.getSourceInfo().getName() + "'...");
                    sendResponse(PubSubConstants.RESP_FORBIDDEN, req, t);
                }
            } else if (req instanceof PublishRequest) {
                PublishRequest pubreq = (PublishRequest) req;
                PublishOperation o = new PublishOperation(t.getID(),
                        new Subscriber(pubreq.getPublisher(),
                        t),
                        new Event(pubreq.getEventType()));
                if (ac.isAllowed(o)) {
                    /*
                     * In this case Customizable Algorithm may decide, whether to forward the
                     * message using P2PP/RELOAD layer or not
                     */
                    return getCustomizableAlgorithm().onForwardingPublish(pubreq, t);
                } else {
                    logger.trace("Publish " + PubSubConstants.STR_EVENT[pubreq.getEventType()]
                            + " operation not allowed for '" + req.getSourceInfo().getName()
                            + "'...");
                    sendResponse(PubSubConstants.RESP_FORBIDDEN, req, t);
                }
            } else if (req instanceof CreateTopicRequest) {
                CreateTopicRequest crtpcreq = (CreateTopicRequest) req;
                switch (crtpcreq.getFlag()) {
                    case PubSubConstants.CREATETOPICFLAG_NEWTOPIC:
                        logger.trace("Topic " + crtpcreq.getTopicID() + " already exists....");
                        sendResponse(PubSubConstants.RESP_ALREADYEXISTS, req, t);
                        break;
                    case PubSubConstants.CREATETOPICFLAG_TRANSFERTOPIC:
                        /*
                         * Topic exists, but it is only a transfer, so the request should be
                         * forwarded to its destination
                         */
                        logger.trace("Forwarding existing topic transfer (" + crtpcreq.getTopicID()
                                + ")...");
                        return true;
                }

            }
            /*message has been processed by this node, so inform transport layer
            that it should discard it*/
            return false;
        } else {
            boolean continueStdProcessing = true;
            if (req instanceof CreateTopicRequest) {
                /*
                 * The message will be forwarded anyway, this method may only be used by
                 * the Algorithm component for gathering information.
                 */
                logger.trace("Invoking onForwardingCreateTopic...");
                getCustomizableAlgorithm().onForwardingCreateTopic((CreateTopicRequest) req);
            }
            /* TODO: Add the subscribe forwarding handler to implement Scribe 'forwarders'
            if(req instanceof SubscribeRequest){
            //node may want to discard the original message like Scribe does!!!
            continueStdProcessing = getCustomizableAlgorithm().onForwardingUnknownSubscribe((SubscribeRequest)req);
            }
             */
            //inform transport layer, whether it should forward the message
            return continueStdProcessing;
        }
    }

    //-------------------------------------------------------------
    //-------------------------------------------------------------
    // Communication
    //-------------------------------------------------------------
    public boolean sendMessage(PubSubMessage msg, byte routingType, String key) {
        switch (routingType) {
            case PubSubTransport.ROUTING_OVERLAY:
                transport.sendThroughOverlay(msg, key);
                return true;
            case PubSubTransport.ROUTING_DIRECT:
                return transport.sendDirectly(msg);
                //transport.sendThroughOverlay(msg, msg.getTopicID());
                //return true;
            default:
                return false;
        }
    }

    public void forwardToParent(PubSubMessage msg, Topic t) {
        NodeInfo newDest = t.getParent().getNodeInfo();
        msg.getDestinationInfo().setID(newDest.getID());
        msg.getDestinationInfo().setIP(newDest.getIP());
        msg.getDestinationInfo().setName(newDest.getName());
        msg.getDestinationInfo().setPort(newDest.getPort());
        sendMessage(msg, PubSubTransport.ROUTING_DIRECT, null);
        logger.trace("Forwarding " + PubSubConstants.STR_OPERATION[msg.getType()]
                + " to parent for topic '" + t.getID() + "': " + msg.getDestinationInfo());
    }

    public void forwardToOtherNode(PubSubMessage msg, NodeInfo newDest) {
        msg.getDestinationInfo().setID(newDest.getID());
        msg.getDestinationInfo().setIP(newDest.getIP());
        msg.getDestinationInfo().setName(newDest.getName());
        msg.getDestinationInfo().setPort(newDest.getPort());
        sendMessage(msg, PubSubTransport.ROUTING_DIRECT, null);
        logger.trace("Forwarding " + PubSubConstants.STR_OPERATION[msg.getType()]
                + " to parent for topic '" + msg.getTopicID() + "': " + msg.getDestinationInfo());
    }

    /**
     * For the {@link PubSubConstants#EVENT_CUSTOM} notify messages, if the child is this node, it does not forward
     * the message, but invokes higher-layer callback.
     * @param msg
     * @param child
     */
    public void forwardToChild(PubSubMessage msg, Subscriber child) {
        String thisNodeId = getNodeInfo().getID();
        NotifyIndication ind = (NotifyIndication) msg;
        Topic t = topics.get(ind.getTopicID());
        NotifyOperation o = new NotifyOperation(ind.getTopicID(),
                new Subscriber(ind.getPublisher(), t),
                new Event(PubSubConstants.EVENT_CUSTOM));
        if (child.getNodeInfo().getID().equals(thisNodeId) && msg instanceof NotifyIndication) {
            if (ind.getEventType() == PubSubConstants.EVENT_CUSTOM) {
                if (child.getSubscription(t.getID()).getInterestConditions().isInteresting(o)) {
                    logger.trace("Invoking notify callback: "
                            + PubSubConstants.STR_EVENT[ind.getEventType()]
                            + " - " + new String(ind.getMessage()) + " ("
                            + ind.getTopicID() + ")");
                    for (NodeCallback listener : getNode().getCallbacks()) {
                        listener.onTopicNotify(getNode(), ind.getTopicID(),
                                ind.getMessage(), ind.isHistorical());
                    }
//
//					getNode().getCallback().onTopicNotify(getNode(), ind.getTopicID(),
//			    		                         		  ind.getMessage());
                } else {
                    logger.trace("Notify "
                            + PubSubConstants.STR_EVENT[ind.getEventType()]
                            + " - " + new String(ind.getMessage()) + " ("
                            + ind.getTopicID() + ") is not interesting for "
                            + child.getNodeInfo());
                }
                if (ind.getEventType() == PubSubConstants.EVENT_CUSTOM) {
                    if (ind.getOperationID() == (t.getCurrentOperationID() + 1)) {
                        storeNotifyIndication(ind);
                    } else {
                        logger.error("TODO Received operationID from indication mismatch with current ID from topic, these topic need to be synchronised, proceeding ...");
                        //todo bucior send synchroRequest to parent
                    }
                }
            }

        } else {
            if (child.getSubscription(t.getID()).getInterestConditions().isInteresting(o)) {
                NodeInfo thisNode = getNodeInfo();
                msg.getSourceInfo().setID(thisNode.getID());
                msg.getSourceInfo().setIP(thisNode.getIP());
                msg.getSourceInfo().setName(thisNode.getName());
                msg.getSourceInfo().setPort(thisNode.getPort());

                msg.getDestinationInfo().setID(child.getNodeInfo().getID());
                msg.getDestinationInfo().setIP(child.getNodeInfo().getIP());
                msg.getDestinationInfo().setName(child.getNodeInfo().getName());
                msg.getDestinationInfo().setPort(child.getNodeInfo().getPort());
                sendMessage(msg, PubSubTransport.ROUTING_DIRECT, null);
                logger.trace("Forwarding notify to child: " + msg.getDestinationInfo());
            } else {
                logger.trace("Notify "
                        + PubSubConstants.STR_EVENT[ind.getEventType()]
                        + " - " + new String(ind.getMessage()) + " ("
                        + ind.getTopicID() + ") is not interesting for "
                        + child.getNodeInfo());
            }
        }
    }

    public void sendResponse(int respCode, PubSubRequest req, Topic t) {
        StandardResponse resp = null;
        if (req instanceof SubscribeRequest && respCode == PubSubConstants.RESP_SUCCESS) {
            resp = new StandardResponse(req.getTransactionID(),
                    getNodeInfo(),
                    req.getSourceInfo(),
                    req.getTopicID(),
                    t.getAccessControlRules());
            sendMessage(resp, PubSubTransport.ROUTING_DIRECT, null);
        } else {
            resp = new StandardResponse(req.getTransactionID(), respCode,
                    getNodeInfo(), req.getSourceInfo(),
                    req.getTopicID());
            sendMessage(resp, PubSubTransport.ROUTING_DIRECT, null);
        }
        logger.trace("Response " + respCode + " for " + PubSubConstants.STR_OPERATION[req.getType()]
                + " sent to '" + resp.getDestinationInfo() + "'");
    }

    //TODO s3544 build and send response for kepalive ind:
    // with cache update and before check AC rulest and add user to topic children
    public void sendResponse(int respCode, PubSubIndication req, Topic t) {
        StandardResponse resp = null;

        logger.trace("Response " + respCode + " for " + PubSubConstants.STR_OPERATION[req.getType()]
                + " sent to '" + resp.getDestinationInfo() + "'");
    }

    public void forwardToChildren(PubSubMessage msg, Topic t) {
        Vector<String> children = t.getChildren();
        //Iterator<String> iter = children.iterator();
        Subscriber child;
        boolean isThisNodeSubscribed = false;
         for (String childName : children) {
             child = t.getChild(childName);
            forwardToChild(msg, child);
            if (child.getNodeInfo().equals(getNodeInfo())) {
                isThisNodeSubscribed = true;
            }
         }
//        while (iter.hasNext()) {
//            child = t.getChild((String) iter.next());
//            forwardToChild(msg, child);
//            if (child.getNodeInfo().equals(getNodeInfo())) {
//                isThisNodeSubscribed = true;
//            }
//        }
        if (isThisNodeSubscribed || t.isTopicRoot()) {
            /*
             * If the received notification refers to one of the 'special' events
             * (f.e. remove topic) the procedure for this node can only be performed
             * after forwarding it to the children.
             */
            NotifyIndication ind = (NotifyIndication) msg;
            switch (ind.getEventType()) {
                case PubSubConstants.EVENT_MODIFYAC:
                    getTopic(ind.getTopicID()).setAccessControlRules(new AccessControlRules(ind.getMessage()));
                    logger.debug(PubSubConstants.STR_EVENT[ind.getEventType()] + " received "
                            + "(" + ind.getTopicID() + ")");
                    break;
                case PubSubConstants.EVENT_REMOVETOPIC:
                    logger.debug(PubSubConstants.STR_EVENT[ind.getEventType()] + " received "
                            + " (" + ind.getTopicID() + ")");
                    removeTopic(ind.getTopicID());
                    if (isThisNodeSubscribed) /*
                     * Invoked callback only, if the node is the topic subscriber
                     * as well, not when it is only the root for this channel
                     */ {
                        for (NodeCallback listener : getNode().getCallbacks()) {
                            listener.onTopicRemove(getNode(), t.getID());
                        }
                    }

//					getNode().getCallback().onTopicRemove(getNode(), t.getID());
                    break;
//                case PubSubConstants.EVENT_CUSTOM:
//                    storeNotifyIndication(ind);
//                    break;
                default:
                    break;
            }
        }
    }

    /* TODO: Add the forwardToChildren(String excludeID) method - for the distributed
     *       event consuming
     */
    //--------------------------------------------------------------
    public NodeInfo whereToForwardRequest(Topic t, SubscribeRequest req) {
        switch (req.getType()) {
            case PubSubConstants.MSG_SUBSCRIBE:
                return getCustomizableAlgorithm().whereToForwardSubscribeRequest(t, (SubscribeRequest) req);
            default:
                return null;
        }
    }

    //------------------------------------------------------------------
    // The methods below are invoked form P2PNode callbacks!!!
    protected boolean onSubscriptionInfoLookupReceived(/*List<SubscriptionInfo> objectList*/) {
        //TODO: implement this!!!
        return true;
    }

    protected Enumeration<Topic> getTopics() {
        return this.topics.elements();
    }

    public void removeKeepAliveTransaction(int iD) {
        //this.maintenanceTransaction.remove(iD);
        this.transaction_logger.markTransactionEnd(iD, Transaction.TO_REMOVE);
    }

    protected boolean isRunning() {
        return this.isRunning;
    }

    public void DEBUG_clearTopics() {
        this.topics = null;
        this.topics = new Hashtable<String, Topic>();
    }

    public void storeNotifyIndication(NotifyIndication ni) {
        Topic c = getTopic(ni.getTopicID());
        c.increaseCurrentOperation();
        if ((c.getCurrentOperationID()) == ni.getOperationID()) {
            this.topic_logger.onDeliverPublishIndication(ni.getOperationID(), ni);
        } else {
            this.topic_logger.onDeliverPublishIndication(c.getCurrentOperationID(), ni);
        }
    }

    private void initCacheDBConnections() {
        if(this.topic_logger instanceof DBConnection)
        t_db_conn = DBConnection.getConnection();
    }

    public TopicStateLogger getTopicStateLogger() {
        return this.topic_logger;
    }

    private void synchroniseTopic(SubscribeRequest req, Topic t) {
        ArrayList<NotifyIndication> indications_to_send = null;
        NotifyIndication ni = null;
        NotifyIndication ni_ = null;
        NodeInfo thisNode = getNodeInfo();
        NodeInfo source = req.getSourceInfo();
        User publisher = null;
        int current_operationID = t.getCurrentOperationID();
        int received_operationID = req.getEventIndex();
        try {
            if (received_operationID != current_operationID) {
                logger.trace("Received synchroniseTopic request for operation ID:"+received_operationID+" , current topic operation: "+current_operationID+" [topic="+t.getID()+"]");
                if (received_operationID == PubSubConstants.HISTORY_NONE) {
                    //ignore - Node doesn't want do receive history publish events for these topic
                } else {
                    indications_to_send = this.topic_logger.getPublishOperations(req.getTopicID(),((received_operationID == PubSubConstants.HISTORY_ALL) ? 0 : received_operationID));
                    logger.trace("Preparing to sent historical notifyIndications: topicID:" + req.getTopicID() + ", source: " + source.getName() + ",receiver operationID: " + received_operationID + ", history indications to sent: " + indications_to_send.size());
                    if (indications_to_send.size() > 0) {
                        publisher = new User(indications_to_send.get(0).getPublisher());
                    }
                    for (int i = 0; i < indications_to_send.size(); i++) {
                        ni = indications_to_send.get(i);
                        ni_ = new NotifyIndication(
                                thisNode,
                                source, t.getID(),
                                ni.getEventType(), ni.getMessage(), true,
                                publisher, ni.getOperationID());
                        sendMessage(ni_, PubSubTransport.ROUTING_DIRECT, null);
                        //need to wait a while to proper sockets work
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException ex) {
                        }
                    }

                }
            }
        } finally {
            indications_to_send = null;
            ni = null;
            ni_ = null;
            publisher = null;
            source = null;
        }
    }

    /*
     * protected void onNeighborJoin
     */
}

class Maintenance_Keep_Alive_Sender implements Runnable {

    protected CoreAlgorithm core;
    protected Enumeration<Topic> topics;

    public Maintenance_Keep_Alive_Sender(CoreAlgorithm core, Enumeration<Topic> topics) {
        this.core = core;
    }

    public void run() {
        Topic topic;
        while (this.core.isRunning()) {
            try {
                topics = core.getTopics();
                while (topics.hasMoreElements()) {
                    topic = topics.nextElement();
                    core.getCustomizableAlgorithm().keepAlive(topic);
                    Thread.sleep(1000);
                }

                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                //ignore
            }
        }
    }
}
