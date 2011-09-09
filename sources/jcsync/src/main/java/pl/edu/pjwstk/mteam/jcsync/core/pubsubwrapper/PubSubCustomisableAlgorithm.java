package pl.edu.pjwstk.mteam.jcsync.core.pubsubwrapper;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.core.NodeCallback;
import pl.edu.pjwstk.mteam.core.NodeError;
import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncConstans;
import pl.edu.pjwstk.mteam.pubsub.algorithm.implementation.DefaultCustomizableAlgorithm;
import pl.edu.pjwstk.mteam.pubsub.core.Operation;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.Subscriber;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.core.Transaction;
import pl.edu.pjwstk.mteam.pubsub.core.User;
import pl.edu.pjwstk.mteam.pubsub.message.indication.NotifyIndication;
import pl.edu.pjwstk.mteam.pubsub.message.request.CreateTopicRequest;
import pl.edu.pjwstk.mteam.pubsub.message.request.PublishRequest;
import pl.edu.pjwstk.mteam.pubsub.message.request.SubscribeRequest;
import pl.edu.pjwstk.mteam.pubsub.message.request.UnsubscribeRequest;
import pl.edu.pjwstk.mteam.pubsub.message.response.PubSubResponse;
import pl.edu.pjwstk.mteam.pubsub.message.response.StandardResponse;
import pl.edu.pjwstk.mteam.pubsub.topology.maintenance.message.TopologyCacheUpdateRequest;

/**
 *
 * @author Piotr Bucior
 */
class PubSubCustomisableAlgorithm extends DefaultCustomizableAlgorithm {

    private static final Logger log = Logger.getLogger("pubsubwrapper.P-BCuAlg");

    public PubSubCustomisableAlgorithm() {
    }

    @Override
    public void createTopic(Object topicID, boolean subscribe) {
        super.createTopic(topicID, subscribe);
    }

    @Override
    public void createTopic(Object topicID, boolean subscribe, Object acRules) {
        super.createTopic(topicID, subscribe, acRules);
    }

    @Override
    public void removeTopic(Object topicID) {
        super.removeTopic(topicID);
    }

    @Override
    public void modifyAccessControlRules(Object topicID, Object acRules) {
        super.modifyAccessControlRules(topicID, acRules);
    }

    @Override
    public void modifyInterestConditions(Object topicID, Object ic) {
        super.modifyInterestConditions(topicID, ic);
    }

    @Override
    public void networkPublish(Object topic, byte[] message) {
        super.networkPublish(topic, message);
    }

    @Override
    public void networkSubscribe(Object topic) {
        super.networkSubscribe(topic);
    }

    @Override
    public void networkSubscribe(Object topic, Object ic, int eventIndex) {
        super.networkSubscribe(topic, ic, eventIndex);
    }

    @Override
    public void networkUnsubscribe(Object topic) {
        super.networkUnsubscribe(topic);
    }

    @Override
    public void maintenanceCacheUpdate(Object topic, Object nodeInfo, byte eventType) {
        super.maintenanceCacheUpdate(topic, nodeInfo, eventType);
    }

    @Override
    public void onDeliverCacheUpdateRequest(TopologyCacheUpdateRequest req) {
        super.onDeliverCacheUpdateRequest(req);
    }

    @Override
    public void beforeNetworkLeave(Hashtable<String, Topic> topics) {
        super.beforeNetworkLeave(topics);
    }

    @Override
    public boolean keepAlive(Topic topic) {
        return super.keepAlive(topic);
    }

    @Override
    public boolean keepAlive(NodeInfo destination, Object topicID) {
        return super.keepAlive(destination, topicID);
    }

    @Override
    public void onDeliverCreateTopic(CreateTopicRequest req) {
        Topic newTopic = new Topic(req.getTopicID());
        newTopic.setAccessControlRules(req.getAccessRules());
        /*
         * DODALEM TO
         */
        newTopic.setOwner(new Subscriber(newTopic, req.getSourceInfo()));
        /* if the request originator wanted to subscribe automatically - add it to the
         * subscribers list
         */
        Vector<User> subscribers = req.getSubscribers();
        for (int i = 0; i < req.getSubscribers().size(); i++) {
            newTopic.addSubscriber(subscribers.get(i).getNodeInfo());
        }
        getCoreAlgorithm().addTopic(newTopic);
        log.trace("Topic '" + req.getTopicID() + "' successfully created....");
        getCoreAlgorithm().sendResponse(PubSubConstants.RESP_SUCCESS, req, newTopic);
    }

    @Override
    public void onForwardingCreateTopic(CreateTopicRequest req) {
        super.onForwardingCreateTopic(req);
    }

    @Override
    public void onDeliverUnsubscribe(UnsubscribeRequest req, Topic t) {
        super.onDeliverUnsubscribe(req, t);
    }

    @Override
    public void onDeliverPublish(PublishRequest req, Topic t) {
        super.onDeliverPublish(req, t);
    }

    @Override
    public boolean onForwardingPublish(PublishRequest req, Topic t) {
        return super.onForwardingPublish(req, t);
    }

    @Override
    public void onDeliverNotify(NotifyIndication ind, Topic t) {
        super.onDeliverNotify(ind, t);
    }

    @Override
    public void onDeliverCreateTopicResponse(PubSubResponse res, Transaction t) {
        log.trace("Completing " + t + "...");
        /*
         * CreateTopic may me associated also with subscribing to a topic, and if it
         * is so - 'onTopicSubscribe' callback invocation is needed
         */
        if (res.getResponseCode() == PubSubConstants.RESP_SUCCESS) {
            Iterator<Operation> operations = t.getOperations().iterator();
            while (operations.hasNext()) {
                Operation o = operations.next();
                switch (o.getType()) {
                    case PubSubConstants.OPERATION_CREATETOPIC:
                        log.trace("Invoking callback for " + PubSubConstants.STR_OPERATION[o.getType()]);
                        for (NodeCallback listener : getCoreAlgorithm().getNode().getCallbacks()) {
						listener.onTopicCreate(getCoreAlgorithm().getNode(), o.getID(),res.getTransactionID());
					}
                        //getCoreAlgorithm().getNode().getCallback().onTopicCreate(getCoreAlgorithm().getNode(), o.getID(), res.getTransactionID());
                        break;
                    case PubSubConstants.OPERATION_SUBSCRIBE:
                        Topic topic = getCoreAlgorithm().getTopic(t.getTopic().getID());
                        if (topic == null) {
                            topic = t.getTopic();
                            topic.setParent(res.getSourceInfo());
                            getCoreAlgorithm().addTopic(topic);
                            /* TODO: [DHT-based] For unstructured networks also insert
                             *       the SUBSCRIPTIONINFO object
                             */
                        }
                        log.trace("Invoking callback for " + PubSubConstants.STR_OPERATION[o.getType()]);
                        for (NodeCallback listener : getCoreAlgorithm().getNode().getCallbacks()) {
						listener.onTopicSubscribe(getCoreAlgorithm().getNode(), o.getID(),res.getTransactionID());
					}
                        //getCoreAlgorithm().getNode().getCallback().onTopicSubscribe(getCoreAlgorithm().getNode(), o.getID(), res.getTransactionID());
                        break;
                }
            }
            ((PubSubWrapper) getCoreAlgorithm()).unlockCreateTopic(res.getTopicID(), t.getID(), JCSyncConstans.J_RESP_TOPIC_CREATED);
        } else {
            t.getTopic().removeSubscriber(getCoreAlgorithm().getNodeInfo().getID());
            log.trace("Invoking ONERROR callback for '" + t.getOperation().getID() + "': "
                    + res.getResponseCode());
            for (NodeCallback listener : getCoreAlgorithm().getNode().getCallbacks()) {
				listener.onPubSubError(getCoreAlgorithm().getNode(),
                        t.getOperation().getID(),
                        PubSubConstants.OPERATION_CREATETOPIC,
                        NodeError.TOPICEXISTSERR, res.getTransactionID());
			}

//            getCoreAlgorithm().getNode().getCallback().onPubSubError(getCoreAlgorithm().getNode(),
//                    t.getOperation().getID(),
//                    PubSubConstants.OPERATION_CREATETOPIC,
//                    NodeError.TOPICEXISTSERR, res.getTransactionID());
            ((PubSubWrapper) getCoreAlgorithm()).unlockCreateTopic(res.getTopicID(), t.getID(), JCSyncConstans.J_ERR_COLLECTION_NAME_RESERVED);
        }
    }

    @Override
    public void onDeliverSubscribeResponse(PubSubResponse res, Transaction t) {
        int respcode = res.getResponseCode();
        byte needToBeStored = 0;
        if (respcode == PubSubConstants.RESP_SUCCESS) {
            Topic topic =
                            getCoreAlgorithm().getTopicStateLogger().getTopic(res.getTopicID());
            if(topic == null) {
                needToBeStored = 1;
                topic = t.getTopic();
            }else{
                //add itself as subscriber 
                topic.addSubscriber(getCoreAlgorithm().getNodeInfo());
            }
                topic.setAccessControlRules(((StandardResponse) res).getAccessControlRules());
                topic.setParent(res.getSourceInfo());
                if(needToBeStored==1){
                    getCoreAlgorithm().addTopic(topic, true);
                }else{
                    getCoreAlgorithm().addTopic(topic, false);
                }

            /* TODO: [DHT-based] For unstructured networks also insert
             *       the SUBSCRIPTIONINFO object
             */
            log.trace("Invoking callback for " + PubSubConstants.STR_OPERATION[t.getOperation().getType()]);
            for (NodeCallback listener : getCoreAlgorithm().getNode().getCallbacks()) {
				listener.onTopicSubscribe(getCoreAlgorithm().getNode(), t.getOperation().getID());
			}

            //getCoreAlgorithm().getNode().getCallback().onTopicSubscribe(getCoreAlgorithm().getNode(), t.getOperation().getID(), res.getTransactionID());
            ((PubSubWrapper) getCoreAlgorithm()).unlockCreateTopic(res.getTopicID(), t.getID(), JCSyncConstans.J_RESP_GENERAL_SUCCESS);
        } else {
            /*
             * The subscriber was added before the topic was accepted - it has to be
             * removed because there may be static lists to be cleared for the specified
             * subscriber
             */
            t.getTopic().removeSubscriber(getCoreAlgorithm().getNodeInfo().getID());
            switch (respcode) {
                case PubSubConstants.RESP_DOESNOTEXIST:
                    log.trace("Received 404 response- looking for topic in local persistent storage ...");
                    Topic topic =
                            getCoreAlgorithm().getTopicStateLogger().getTopic(res.getTopicID());
                    if (topic != null) {
                        log.trace("Topic: " + topic.getID() + " found locally!");
                        getCoreAlgorithm().addTopic(topic, false);
                        networkSubscribe(topic.getID());
                        return;
                    }
                    log.trace("Invoking onPubSubError callback for the "
                            + PubSubConstants.STR_OPERATION[t.getOperation().getType()]
                            + " operation (" + respcode + ")");
                    for (NodeCallback listener : getCoreAlgorithm().getNode().getCallbacks()) {
					listener.onPubSubError(getCoreAlgorithm().getNode(),
	                        res.getTopicID(),
	                        t.getOperation().getType(),
	                        NodeError.NOSUCHTOPICERR);
				}
//
//                    getCoreAlgorithm().getNode().getCallback().onPubSubError(getCoreAlgorithm().getNode(),
//                            res.getTopicID(),
//                            t.getOperation().getType(),
//                            NodeError.NOSUCHTOPICERR, res.getTransactionID());
                    ((PubSubWrapper) getCoreAlgorithm()).unlockCreateTopic(res.getTopicID(), t.getID(), JCSyncConstans.J_ERR_GENERAL_ERROR);
                    log.trace("Invoking onPubSubError callback for the "
                            + PubSubConstants.STR_OPERATION[t.getOperation().getType()]
                            + " operation (" + respcode + ")");
                    break;
                case PubSubConstants.RESP_FORBIDDEN:
                    log.trace("Invoking onPubSubError callback for the "
                            + PubSubConstants.STR_OPERATION[t.getOperation().getType()]
                            + " operation (" + respcode + ")");
                    for (NodeCallback listener : getCoreAlgorithm().getNode().getCallbacks()) {
					listener.onPubSubError(getCoreAlgorithm().getNode(),
	                        res.getTopicID(),
	                        t.getOperation().getType(),
	                        NodeError.AUTHERR);
				}
//
//                    getCoreAlgorithm().getNode().getCallback().onPubSubError(getCoreAlgorithm().getNode(),
//                            res.getTopicID(),
//                            t.getOperation().getType(),
//                            NodeError.AUTHERR, res.getTransactionID());
                    ((PubSubWrapper) getCoreAlgorithm()).unlockCreateTopic(res.getTopicID(), t.getID(), JCSyncConstans.J_ERR_COLLECTION_AUTH_ERROR);
                    break;
            }
        }
    }

    @Override
    public void onDeliverUnsubscribeResponse(PubSubResponse res, Transaction t) {
        super.onDeliverUnsubscribeResponse(res, t);
    }

    @Override
    public void onDeliverPublishResponse(PubSubResponse res, Transaction t) {
        super.onDeliverPublishResponse(res, t);
    }

    @Override
    public NodeInfo whereToForwardSubscribeRequest(Topic t, SubscribeRequest req) {
        return super.whereToForwardSubscribeRequest(t, req);
    }
}
