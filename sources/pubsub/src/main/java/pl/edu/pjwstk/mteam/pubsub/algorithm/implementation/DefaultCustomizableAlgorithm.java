/**
 * Contains objects associated with publish-subscribe Algorithm component.
 */
package pl.edu.pjwstk.mteam.pubsub.algorithm.implementation;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import pl.edu.pjwstk.mteam.core.NodeCallback;

import pl.edu.pjwstk.mteam.core.NodeError;
import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.interestconditions.InterestConditions;
import pl.edu.pjwstk.mteam.pubsub.logging.Logger;

import pl.edu.pjwstk.mteam.pubsub.accesscontrol.AccessControlRules;
import pl.edu.pjwstk.mteam.pubsub.algorithm.CustomizableAlgorithm;
import pl.edu.pjwstk.mteam.pubsub.core.Event;
import pl.edu.pjwstk.mteam.pubsub.core.Operation;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.Subscriber;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.core.Transaction;
import pl.edu.pjwstk.mteam.pubsub.core.User;
import pl.edu.pjwstk.mteam.pubsub.message.indication.KeepAliveIndication;
import pl.edu.pjwstk.mteam.pubsub.message.indication.NotifyIndication;
import pl.edu.pjwstk.mteam.pubsub.message.request.CreateTopicRequest;
import pl.edu.pjwstk.mteam.pubsub.message.request.PublishRequest;
import pl.edu.pjwstk.mteam.pubsub.message.request.SubscribeRequest;
import pl.edu.pjwstk.mteam.pubsub.message.request.UnsubscribeRequest;
import pl.edu.pjwstk.mteam.pubsub.message.response.PubSubResponse;
import pl.edu.pjwstk.mteam.pubsub.message.response.StandardResponse;
import pl.edu.pjwstk.mteam.pubsub.operation.CreateTopicOperation;
import pl.edu.pjwstk.mteam.pubsub.operation.KeepAliveOperation;
import pl.edu.pjwstk.mteam.pubsub.operation.PublishOperation;
import pl.edu.pjwstk.mteam.pubsub.operation.SubscribeOperation;
import pl.edu.pjwstk.mteam.pubsub.operation.UnsubscribeOperation;
import pl.edu.pjwstk.mteam.pubsub.topology.maintenance.message.TopologyCacheUpdateRequest;
import pl.edu.pjwstk.mteam.pubsub.transport.PubSubTransport;

/**
 * Default publish-subscribe algorithm for both structured and unstructured overlays.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class DefaultCustomizableAlgorithm extends CustomizableAlgorithm {

    private static Logger logger = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.algorithm.implementation.DefaultAlgorithm");
    private static Logger log_M = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.topology.maintenance");
    private boolean use_maintenance = true;

    /**
     * Invoked by publish-subscribe manager when topology checks, whether to
     * accept new subscriber itself or pass it to some other node.
     * @param req Incoming subscribe request.
     */
    public NodeInfo whereToForwardSubscribeRequest(Topic t, SubscribeRequest req) {
        Subscriber parent = t.getParent();
        if (parent == null) {
            /*
             *  In this algorithm topic root always accepts the new subscriber
             */
            return null;
        }
        if (req.getDistance() == -1) {
            /* Means that this is unstructured overlay and node should check if the
             * message sender's node id is greater than its own one (then accept
             * the request - return 'true'. Otherwise return 'false'.
             */
            int reqOrigId = Integer.parseInt(req.getSourceInfo().getID());
            int thisNodeId = Integer.parseInt(getCoreAlgorithm().getNodeInfo().getID());
            if (reqOrigId > thisNodeId) {
                return null;
            } else {
                return parent.getNodeInfo();
            }
        } else {
            /*
             * This is DHT-based overlay, so compare distances
             */
            if (req.getDistance() > t.getDistance()) {
                return null;
            } else {
                return parent.getNodeInfo();
            }
        }
    }

    @Override
    public void onDeliverCreateTopic(CreateTopicRequest req) {
        /*
         * Already known, that the topic does not exist, so only create it, add
         * the new subscriber if needed, and send OK response
         */
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
        logger.trace("Topic '" + req.getTopicID() + "' successfully created....");
        getCoreAlgorithm().sendResponse(PubSubConstants.RESP_SUCCESS, req, newTopic);
    }

    @Override
    public void onDeliverNotify(NotifyIndication ind, Topic t) {        
        getCoreAlgorithm().forwardToChildren(ind, t);
    }

    @Override
    public void onDeliverPublish(PublishRequest req, Topic t) {
        if (t.isTopicRoot()) {
            getCoreAlgorithm().sendResponse(PubSubConstants.RESP_SUCCESS, req, t);
            /*
             * Message destination is default, because it will be filled in by
             * the forwardToChild method.
             */
            NotifyIndication notify = new NotifyIndication(
                    getCoreAlgorithm().getNodeInfo(),
                    new NodeInfo(""), t.getID(),
                    req.getEventType(), req.getMessage(), false,
                    new User(req.getPublisher()), t.getCurrentOperationID() + 1);
            getCoreAlgorithm().forwardToChildren(notify, t);
        } else {
            getCoreAlgorithm().forwardToParent(req, t);
        }
    }

    @Override
    public void onDeliverUnsubscribe(UnsubscribeRequest req, Topic t) {
        //TODO: this part is not compatible with the new version of the specification
        Subscriber s = t.getChild(req.getSourceInfo().getID());
        if (s == null) {
            logger.debug(req.getSourceInfo() + "is not a '" + t.getID() + "' subscriber - can't "
                    + PubSubConstants.STR_OPERATION[PubSubConstants.OPERATION_UNSUBSCRIBE]);
            getCoreAlgorithm().sendResponse(PubSubConstants.RESP_DOESNOTEXIST, req, t);
        } else {
            logger.debug("Removing " + req.getSourceInfo() + " from '" + t.getID() + "subscribers...");
            t.removeSubscriber(req.getSourceInfo().getID());
            getCoreAlgorithm().sendResponse(PubSubConstants.RESP_SUCCESS, req, t);
            /*
             * TODO: If there are no more children (removeSubscriber returns 'true' - this
             *       node should probably unsubscribe itself automatically
             */
        }
    }

    @Override
    public void onForwardingCreateTopic(CreateTopicRequest req) {
        /*
         * This method could be used for gathering additional
         * information about topics in this network. It is not used
         * by this algorithm implementation.
         */
    }

    @Override
    public boolean onForwardingPublish(PublishRequest req, Topic t) {
        onDeliverPublish(req, t);
        //The message has been processed by this node, so tell the P2PP/RELOAD layer not
        //to forward insert request containing the message
        return false;
    }

    @Override
    public void onDeliverCreateTopicResponse(PubSubResponse res, Transaction t) {
        logger.trace("Completing " + t + "...");
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
                        logger.trace("Invoking callback for " + PubSubConstants.STR_OPERATION[o.getType()]);
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
                        logger.trace("Invoking callback for " + PubSubConstants.STR_OPERATION[o.getType()]);
                        for (NodeCallback listener : getCoreAlgorithm().getNode().getCallbacks()) {
						listener.onTopicSubscribe(getCoreAlgorithm().getNode(), o.getID(),res.getTransactionID());
					}

                        //getCoreAlgorithm().getNode().getCallback().onTopicSubscribe(getCoreAlgorithm().getNode(), o.getID(), res.getTransactionID());
                        break;
                }
            }
        } else {
            t.getTopic().removeSubscriber(getCoreAlgorithm().getNodeInfo().getID());
            logger.trace("Invoking ONERROR callback for '" + t.getOperation().getID() + "': "
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
        }
    }

    @Override
    public void onDeliverPublishResponse(PubSubResponse res, Transaction t) {
        int respcode = res.getResponseCode();
        if (respcode == PubSubConstants.RESP_FORBIDDEN
                || respcode == PubSubConstants.RESP_DOESNOTEXIST) {
            logger.trace("Invoking onPubSubError callback for the "
                    + PubSubConstants.STR_OPERATION[t.getOperation().getType()]
                    + " operation (" + respcode + ")");
            switch (respcode) {
                case PubSubConstants.RESP_FORBIDDEN:
                    for (NodeCallback listener : getCoreAlgorithm().getNode().getCallbacks()) {
					listener.onPubSubError(getCoreAlgorithm().getNode(),
	                        res.getTopicID(),
	                        t.getOperation().getType(),
	                        NodeError.AUTHERR);
				}

//                    getCoreAlgorithm().getNode().getCallback().onPubSubError(getCoreAlgorithm().getNode(),
//                            res.getTopicID(),
//                            t.getOperation().getType(),
//                            NodeError.AUTHERR);
                    break;
                case PubSubConstants.RESP_DOESNOTEXIST:
                    for (NodeCallback listener : getCoreAlgorithm().getNode().getCallbacks()) {
					listener.onPubSubError(getCoreAlgorithm().getNode(),
	                        res.getTopicID(),
	                        t.getOperation().getType(),
	                        NodeError.NOSUCHTOPICERR);
				}

//                    getCoreAlgorithm().getNode().getCallback().onPubSubError(getCoreAlgorithm().getNode(),
//                            res.getTopicID(),
//                            t.getOperation().getType(),
//                            NodeError.NOSUCHTOPICERR);
                    break;
            }

        } else {
            /*
             * Do not invoke callback here - it will be invoked after receiving the notify
             * message
             */
            logger.trace(PubSubConstants.STR_OPERATION[t.getOperation().getType()] + " "
                    + PubSubConstants.STR_EVENT[t.getOperation().getEvent().getType()]
                    + " event for '" + t.getTopic().getID() + "' operation accepted");
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
            logger.trace("Invoking callback for " + PubSubConstants.STR_OPERATION[t.getOperation().getType()]);
            for (NodeCallback listener : getCoreAlgorithm().getNode().getCallbacks()) {
				listener.onTopicSubscribe(getCoreAlgorithm().getNode(), t.getOperation().getID());
			}

           // getCoreAlgorithm().getNode().getCallback().onTopicSubscribe(getCoreAlgorithm().getNode(), t.getOperation().getID());
        } else {
            /*
             * The subscriber was added before the topic was accepted - it has to be
             * removed because there may be static lists to be cleared for the specified
             * subscriber
             */
            t.getTopic().removeSubscriber(getCoreAlgorithm().getNodeInfo().getID());
            switch (respcode) {
                case PubSubConstants.RESP_DOESNOTEXIST:
                    logger.trace("Received 404 response- looking for topic in local persistent storage ...");
                    Topic topic =
                            getCoreAlgorithm().getTopicStateLogger().getTopic(res.getTopicID());
                    if (topic != null) {
                        logger.trace("Topic: " + topic.getID() + " found locally!");
                        getCoreAlgorithm().addTopic(topic, false);
                        networkSubscribe(topic, new InterestConditions(topic),
                            PubSubConstants.HISTORY_NONE);
                        return;
                    }
                    logger.trace("Invoking onPubSubError callback for the "
                            + PubSubConstants.STR_OPERATION[t.getOperation().getType()]
                            + " operation (" + respcode + ")");
                    for (NodeCallback listener : getCoreAlgorithm().getNode().getCallbacks()) {
					listener.onPubSubError(getCoreAlgorithm().getNode(),
	                        res.getTopicID(),
	                        t.getOperation().getType(),
	                        NodeError.NOSUCHTOPICERR);
				}
//                    getCoreAlgorithm().getNode().getCallback().onPubSubError(getCoreAlgorithm().getNode(),
//                            res.getTopicID(),
//                            t.getOperation().getType(),
//                            NodeError.NOSUCHTOPICERR, res.getTransactionID());
                    break;
                case PubSubConstants.RESP_FORBIDDEN:
                    logger.trace("Invoking onPubSubError callback for the "
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
                    break;
            }
        }
    }

    @Override
    public void onDeliverUnsubscribeResponse(PubSubResponse res, Transaction t) {
        logger.trace("Invoking " + PubSubConstants.STR_OPERATION + " callback for '"
                + t.getTopic().getID() + "'.....");
        for (NodeCallback listener : getCoreAlgorithm().getNode().getCallbacks()) {
			listener.onTopicUnsubscribe(getCoreAlgorithm().getNode(),
                    t.getTopic().getID());
		}

//        getCoreAlgorithm().getNode().getCallback().onTopicUnsubscribe(getCoreAlgorithm().getNode(),
//                t.getTopic().getID());
    }

    @Override
    public void createTopic(Object topicID, boolean subscribe) {
        Topic t = new Topic((String) topicID);
        createTopic(topicID, subscribe, new AccessControlRules(t));
    }

    @Override
    public void createTopic(Object topicID, boolean subscribe, Object acRules) {
        String tid = (String) topicID;
        Topic newTopic = getCoreAlgorithm().getTopic(tid);
        if(newTopic==null) newTopic = getCoreAlgorithm().getTopicStateLogger().getTopic(tid);
        if (newTopic != null) {
            //Topic exists locally
            logger.trace("Topic " + tid + " exists locally...");
            logger.trace("Invoking ONERROR callback for '" + tid + "'");
            for (NodeCallback listener : getCoreAlgorithm().getNode().getCallbacks()) {
				listener.onPubSubError(getCoreAlgorithm().getNode(), tid,
                        PubSubConstants.OPERATION_CREATETOPIC,
                        NodeError.TOPICEXISTSERR);
			}

//            getCoreAlgorithm().getNode().getCallback().onPubSubError(getCoreAlgorithm().getNode(), tid,
//                    PubSubConstants.OPERATION_CREATETOPIC,
//                    NodeError.TOPICEXISTSERR);
        } else {
            newTopic = new Topic(tid);
            NodeInfo thisNode = getCoreAlgorithm().getNodeInfo();
            AccessControlRules ac = (AccessControlRules) acRules;
            newTopic.setAccessControlRules(ac);
            newTopic.setOwner(new Subscriber(newTopic, getCoreAlgorithm().getNodeInfo()));
            CreateTopicOperation create =
                    new CreateTopicOperation(newTopic.getID(),
                    new Subscriber(newTopic, thisNode),
                    new Event(PubSubConstants.EVENT_NEWTOPIC));
            Transaction trans = new Transaction(create, newTopic);
            CreateTopicRequest msg = new CreateTopicRequest(thisNode,
                    new NodeInfo(""), tid, ac,
                    PubSubConstants.CREATETOPICFLAG_NEWTOPIC,
                    trans.getID());
            if (subscribe) {
                SubscribeOperation sub = new SubscribeOperation(newTopic.getID(),
                        new Subscriber(newTopic, thisNode),
                        new Event(PubSubConstants.EVENT_ALL));
                trans.addOperation(sub);
                msg.addSubscriber(new User(thisNode));
                newTopic.addSubscriber(thisNode);
            }
            logger.trace("Sending " + PubSubConstants.STR_OPERATION[PubSubConstants.OPERATION_CREATETOPIC]
                    + " request for '" + newTopic.getID() + "'...");
            //Adding transaction and starting timer for it
            logger.trace("Creating transaction " + trans);
            getCoreAlgorithm().addTransaction(trans);
            getCoreAlgorithm().sendMessage(msg, PubSubTransport.ROUTING_OVERLAY, tid);
        }
    }

    @Override
    public void networkPublish(Object topic, byte[] message) {
        String tid = (String) topic;
        NodeInfo thisNode = getCoreAlgorithm().getNodeInfo();
        Topic t = getCoreAlgorithm().getTopic(tid);
        if (t != null && t.getChild(thisNode.getID()) != null) {
            User parent = t.getParent();
            if (parent == null) {
                //this node is topic root
                parent = new User(thisNode);
            }
            PublishOperation o = new PublishOperation(tid,
                    new Subscriber(t, thisNode),
                    new Event(PubSubConstants.EVENT_CUSTOM));
            Transaction trans = new Transaction(o, t);
            PublishRequest msg = new PublishRequest(trans.getID(), thisNode,
                    parent.getNodeInfo(), tid,
                    PubSubConstants.EVENT_CUSTOM, message,
                    new User(thisNode.getName()));
            //Adding transaction and starting timer for it
            getCoreAlgorithm().addTransaction(trans);
            getCoreAlgorithm().sendMessage(msg, PubSubTransport.ROUTING_DIRECT, null);
        } else {
            logger.info("The node is not '" + tid + "' subscriber, so it can't publish events");
        }
    }

    @Override
    public void networkSubscribe(Object topic) {
        Topic t = getCoreAlgorithm().getTopicStateLogger().getTopic((String)topic);
        if(t!=null)
        networkSubscribe(topic, new InterestConditions(new Topic((String) topic)),
                t.getCurrentOperationID());
        else
            networkSubscribe(topic, new InterestConditions(new Topic((String) topic)),
                PubSubConstants.HISTORY_ALL);
    }

    @Override
    public void networkSubscribe(Object topic, Object ic, int eventIndex) {
        Topic t = null;
        String tid = null;
        if(topic instanceof String){
            tid = (String) topic;
            t = getCoreAlgorithm().getTopic(tid);
        }else{
            t = (Topic) topic;
            tid = t.getID();
        }
        if (t != null) {
            //Topic exists locally
            logger.trace("Topic " + tid + " exists - subscribing locally...");
            t.addSubscriber(getCoreAlgorithm().getNodeInfo());
            t.getChild(getCoreAlgorithm().getNodeInfo().getID()).getSubscription(t.getID()).setInterestConditions((InterestConditions) ic);
            for (NodeCallback listener : getCoreAlgorithm().getNode().getCallbacks()) {
				listener.onTopicSubscribe(getCoreAlgorithm().getNode(), t.getID());
			}

//            getCoreAlgorithm().getNode().getCallback().onTopicSubscribe(getCoreAlgorithm().getNode(), t.getID());
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

    @Override
    public void networkUnsubscribe(Object topic) {
        //---------------------------------------------------------------------
        //TODO: [DHT-based] Some SUBSCRIPTIONINFO object should probably be
        //      removed here
        //---------------------------------------------------------------------
        String topicID = (String) topic;
        Topic associatedTopic = getCoreAlgorithm().getTopic(topicID);
        NodeInfo thisNode = getCoreAlgorithm().getNodeInfo();
        if (associatedTopic == null) {
            logger.trace("Can't unsubscribe for the topic '" + topicID + "' doesn't exist....");
            for (NodeCallback listener : getCoreAlgorithm().getNode().getCallbacks()) {
				listener.onPubSubError(getCoreAlgorithm().getNode(), topicID,
                        PubSubConstants.OPERATION_UNSUBSCRIBE,
                        NodeError.NOSUCHTOPICERR);
			}
//            getCoreAlgorithm().getNode().getCallback().onPubSubError(getCoreAlgorithm().getNode(), topicID,
//                    PubSubConstants.OPERATION_UNSUBSCRIBE,
//                    NodeError.NOSUCHTOPICERR);
        } else {
            Subscriber thisSubscriber = associatedTopic.getChild(thisNode.getID());
            if (thisSubscriber == null) {
                logger.trace("Can't unsubscribe for the user " + thisNode + " is not a '" + topicID
                        + "' subscriber....");
                for (NodeCallback listener : getCoreAlgorithm().getNode().getCallbacks()) {
					listener.onPubSubError(getCoreAlgorithm().getNode(), topicID,
                            PubSubConstants.OPERATION_UNSUBSCRIBE,
                            NodeError.NOSUCHSUBSCRIBERERR);
				}
//
//                getCoreAlgorithm().getNode().getCallback().onPubSubError(getCoreAlgorithm().getNode(), topicID,
//                        PubSubConstants.OPERATION_UNSUBSCRIBE,
//                        NodeError.NOSUCHSUBSCRIBERERR);
            } else {
                boolean hasNoMoreChildren = associatedTopic.removeSubscriber(thisNode.getID());
                if (hasNoMoreChildren) {
                    if (associatedTopic.isTopicRoot()) {
                        /*
                         * The topic itself can't be removed, and there is no need to send
                         * the unsubscribe request.
                         */
                        logger.trace("No more children, but can't remove '" + topicID
                                + "' for this is topic root...");
                        logger.trace("Invoking " + PubSubConstants.STR_OPERATION[PubSubConstants.OPERATION_UNSUBSCRIBE]
                                + " callback for '" + topicID + "'....");
                        for (NodeCallback listener : getCoreAlgorithm().getNode().getCallbacks()) {
							listener.onTopicUnsubscribe(getCoreAlgorithm().getNode(), topicID);
						}

//                        getCoreAlgorithm().getNode().getCallback().onTopicUnsubscribe(getCoreAlgorithm().getNode(), topicID);
                    } else {
                        /*
                         * Subscriber and topic removed, callback will be invoked after receiving
                         * unsubscribe response
                         */
                        logger.trace("No more children for '" + topicID
                                + "' - sending unsubscribe request...");
                        UnsubscribeOperation o = new UnsubscribeOperation(topicID,
                                new Subscriber(associatedTopic, thisNode),
                                new Event(PubSubConstants.EVENT_ALL));
                        Transaction t = new Transaction(o, associatedTopic);
                        UnsubscribeRequest req = new UnsubscribeRequest(t.getID(), thisNode,
                                associatedTopic.getParent().getNodeInfo(),
                                topicID);
                        getCoreAlgorithm().removeTopic(associatedTopic.getID());
                        getCoreAlgorithm().addTransaction(t);
                        getCoreAlgorithm().sendMessage(req, PubSubTransport.ROUTING_DIRECT, null);
                    }
                } else {
                    /*
                     * Subscriber removed, but can't send unsubscribe request to the parent
                     * for there are other nodes subscribed for this topic via this node
                     */
                    logger.trace("Subscriber " + thisNode + " removed from '" + topicID
                            + "' but can't send unsubscribe request to the parent, because "
                            + "there are more subscribers....");
                    for (NodeCallback listener : getCoreAlgorithm().getNode().getCallbacks()) {
						listener.onTopicUnsubscribe(getCoreAlgorithm().getNode(),
                                topicID);
					}
//
//                    getCoreAlgorithm().getNode().getCallback().onTopicUnsubscribe(getCoreAlgorithm().getNode(),
//                            topicID);
                    logger.trace("Invoking " + PubSubConstants.STR_OPERATION[PubSubConstants.OPERATION_UNSUBSCRIBE]
                            + " callback for '" + topicID + "'...");
                }
            }
        }
    }

    @Override
    public void removeTopic(Object topicID) {
        String tid = (String) topicID;
        Topic t = new Topic(tid);
        NodeInfo thisNode = getCoreAlgorithm().getNodeInfo();
        Subscriber parent = t.getParent();
        if (parent == null) {
            //this node is topic root
            parent = new Subscriber(t, thisNode);
        }
        PublishOperation o = new PublishOperation(tid,
                new Subscriber(t,
                thisNode),
                new Event(PubSubConstants.EVENT_REMOVETOPIC));
        Transaction trans = new Transaction(o, t);
        PublishRequest msg = new PublishRequest(
                trans.getID(),
                thisNode,
                parent.getNodeInfo(), tid,
                PubSubConstants.EVENT_REMOVETOPIC, null,
                new User(thisNode.getName()));
        //Adding transaction and starting timer for it
        getCoreAlgorithm().addTransaction(trans);
        getCoreAlgorithm().sendMessage(msg, PubSubTransport.ROUTING_DIRECT, null);
    }

    @Override
    public void modifyAccessControlRules(Object topicID, Object acRules) {
        String tid = (String) topicID;
        AccessControlRules ac = (AccessControlRules) acRules;
        Topic t = new Topic(tid);
        NodeInfo thisNode = getCoreAlgorithm().getNodeInfo();
        Subscriber parent = t.getParent();
        if (parent == null) {
            //this node is topic root
            parent = new Subscriber(t, thisNode);
        }
        PublishOperation o = new PublishOperation(tid,
                new Subscriber(t,
                thisNode),
                new Event(PubSubConstants.EVENT_MODIFYAC));
        Transaction trans = new Transaction(o, t);
        PublishRequest msg = new PublishRequest(
                trans.getID(),
                thisNode,
                parent.getNodeInfo(), tid,
                PubSubConstants.EVENT_MODIFYAC, ac.encode(),
                new User(thisNode.getName()));
        //Adding pending transaction and starting timer for it
        getCoreAlgorithm().addTransaction(trans);
        getCoreAlgorithm().sendMessage(msg, PubSubTransport.ROUTING_DIRECT, null);
    }

    public void modifyInterestConditions(Object topicID, Object ic) {
        getCoreAlgorithm().modifyInterestConditions((String) topicID, (InterestConditions) ic);
    }

    /**
     * invoked to send cache update request
     * @param topic the topic in which chache will must be updated
     * @param nodeInfo involvedNode (to add/deleto from cache)
     * @param eventType type of cacheUpdate operation
     */
    public void maintenanceCacheUpdate(Object topic, Object nodeInfo, byte eventType) {
        if (!use_maintenance) {
            return;
        }
        Topic t = (Topic) topic;
        NodeInfo node = (NodeInfo) nodeInfo;
        NodeInfo me = getCoreAlgorithm().getNodeInfo();
        TopologyCacheUpdateRequest req = null;
        log_M.debug("[MAINT] " + getCoreAlgorithm().getNodeInfo().getName() + " - cache update for topic: " + t.getID() + " , involved node: " + node.getName() + " event type: " + eventType);
        /*
         *   new node connected - must update cache - 2 scenarios:
         * 1. if this.node is topic root - send new node as neighbourn to other child
         * 2. if this.node isn't topics root - send to him own parent (as grandparent)
         * 
         */
        if (eventType == PubSubConstants.MAINTENANCE_NEW_NODE_CONNECTED) {
            //neighbourns cache update
            if (t.isTopicRoot()) {
                //sending new node as new neighbour to all child
                Vector<String> children = t.getChildren();

                /**
                 * checking that new node may change proposed new root
                 */
                NodeInfo proposedRoot = t.getProposedNewRoot();
                int newDistance = 0;
                int distance = Integer.MAX_VALUE;
                if (proposedRoot != null) {
                    distance = getCoreAlgorithm().getNode().getDistance(t.getID(), proposedRoot.getID());
                } else {
                    for (String childName : children) {
                        newDistance = getCoreAlgorithm().getNode().getDistance(t.getID(), t.getChild(childName).getNodeInfo().getID());
                        if (newDistance < distance) {
                            proposedRoot = t.getChild(childName).getNodeInfo();
                        }
                    }
                }
                newDistance = getCoreAlgorithm().getNode().getDistance(t.getID(), node.getID());
                if (newDistance < distance) {
                    proposedRoot = node;
                }
                t.setProposedNewRoot(proposedRoot);
                /**
                 * //checking that new node may change proposed new root
                 */
                children = t.getChildren();
                for (String childName : children) {
                    if (childName.compareTo(getCoreAlgorithm().getNode().getID()) == 0 || childName.compareTo(node.getID()) == 0) {
                        continue;
                    }
                    Subscriber child = t.getChild(childName);
                    req = new TopologyCacheUpdateRequest(me, child.getNodeInfo(), t.getID(), TopologyCacheUpdateRequest.STORE_NEW_NEIGHBOUR_NODE_TO_CACHE, -1);
                    req.addInvolvedNode(node);
                    req.setParentID(me);
                    //proposed new root
                    req.setGrandParentID(proposedRoot);
                    getCoreAlgorithm().sendMessage(req, PubSubTransport.ROUTING_DIRECT, null);
                }
                //sending all neighbourns for new node
                req = new TopologyCacheUpdateRequest(me, node, t.getID(), TopologyCacheUpdateRequest.STORE_NEW_NEIGHBOUR_NODE_TO_CACHE, -1);
                req.setParentID(me);
                req.setGrandParentID(proposedRoot);
                for (String childName : children) {
                    if (childName.equals(me.getID()) || childName.equals(node.getID())) {
                        continue;
                    }
                    Subscriber child = t.getChild(childName);
                    req.addInvolvedNode(child.getNodeInfo());
                }
                getCoreAlgorithm().sendMessage(req, PubSubTransport.ROUTING_DIRECT, null);
            } else {
                //update gparent cache for new node
                req = new TopologyCacheUpdateRequest(getCoreAlgorithm().getNodeInfo(), node, t.getID(), TopologyCacheUpdateRequest.UPDATE_GRANDPARENT_IN_CACHE, -1);
                req.setParentID(getCoreAlgorithm().getNodeInfo());
                req.setGrandParentID(t.getParent().getNodeInfo());
                getCoreAlgorithm().sendMessage(req, PubSubTransport.ROUTING_DIRECT, null);
            }
        } else if (eventType == PubSubConstants.MAINTENANCE_DELETE_NODE) {
            if (t.isTopicRoot()) {
                //sending failed node to all child
                Vector<String> children = t.getChildren();
                for (String childName : children) {
                    if (childName.compareTo(getCoreAlgorithm().getNode().getID()) == 0 || childName.compareTo(node.getID()) == 0) {
                        continue;
                    }
                    Subscriber child = t.getChild(childName);
                    req = new TopologyCacheUpdateRequest(getCoreAlgorithm().getNodeInfo(), child.getNodeInfo(), t.getID(), TopologyCacheUpdateRequest.REMOVE_NEIGHBOUR_FROM_CACHE, -1);
                    req.addInvolvedNode(node);
                    req.setParentID(getCoreAlgorithm().getNodeInfo());
                    req.setGrandParentID(null);
                    getCoreAlgorithm().sendMessage(req, PubSubTransport.ROUTING_DIRECT, null);
                }
            }
        }

    }

    public void onDeliverCacheUpdateRequest(TopologyCacheUpdateRequest req) {
        byte eventType = req.getEventType();
        Topic t = getCoreAlgorithm().getTopic(req.getTopicID());
        log_M.debug("[MAINT] " + getCoreAlgorithm().getNodeInfo().getName() + " - onDeliverCacheUpdate for topic: " + req);
        if (t == null) {
            log_M.fatal("[MAINT] " + getCoreAlgorithm().getNodeInfo().getName() + " - topic not found " + t.getID());
            return;
        }
        if (eventType == TopologyCacheUpdateRequest.STORE_NEW_NEIGHBOUR_NODE_TO_CACHE) {
            try{
            if (req.getParentNodeInfo() == null || !req.getParentNodeInfo().getID().equals(t.getParent().getNodeInfo().getID())) {
                log_M.error("[MAINT] " + getCoreAlgorithm().getNodeInfo().getName() + " - unknown topic parent, skipping cache update request for topic: " + t.getID());
                return;
            }
            //proposed new root
            t.setProposedNewRoot(req.getGrandParentNodeInfo());
            t.setGrandParent(null);
            if (req.getInvolvedNodeInfos() != null) {
                t.addNeighbourns(req.getInvolvedNodeInfos());
            }
            }catch(Exception e){
                log_M.fatal("Error while caching new neighbour node: "+e.getMessage(), e);
            }
        } else if (eventType == TopologyCacheUpdateRequest.UPDATE_GRANDPARENT_IN_CACHE) {
            if (req.getParentNodeInfo() == null || !req.getParentNodeInfo().getID().equals(t.getParent().getNodeInfo().getID())) {
                log_M.error("[MAINT] " + getCoreAlgorithm().getNodeInfo().getName() + " - unknown topic parent, skipping cache update request for topic: " + t.getID());
                return;
            }
            t.setGrandParent(req.getGrandParentNodeInfo());
        } else if (eventType == TopologyCacheUpdateRequest.REMOVE_NEIGHBOUR_FROM_CACHE) {
            t.removeNeighbour(req.getInvolvedNodeInfos());
        } /*
         * TODO s3544 sourceID = 00000000 - broken on below req's
         */ //ignore PARENT_LEAVE - incoming parent will update cache on deliver CHILD_LEAVE request type
        else if (eventType == TopologyCacheUpdateRequest.PARENT_LEAVE) {
////            //non-root is leaving
////            if(t.getGrandParent()!=null){
////            }
////            //if root is leaving
////            else{
////                //TODO s3544 implement
////            }
        } else if (eventType == TopologyCacheUpdateRequest.CHILD_LEAVE) {
            Vector<NodeInfo> involvedNodes = req.getInvolvedNodeInfos();
            if (involvedNodes.size() > 0) {
                t.removeSubscriber(involvedNodes.get(0));
                maintenanceCacheUpdate(t, involvedNodes.get(0), PubSubConstants.MAINTENANCE_DELETE_NODE);
                involvedNodes.remove(0);
            }
            for (NodeInfo ni : involvedNodes) {
                t.addSubscriber(ni);
                maintenanceCacheUpdate(t, ni, PubSubConstants.MAINTENANCE_NEW_NODE_CONNECTED);
            }
        }
    }

    /**
     * calling before networkLeave
     */
    public void beforeNetworkLeave(Hashtable<String, Topic> topics) {
        TopologyCacheUpdateRequest req = null;
        NodeInfo node = new NodeInfo(getCoreAlgorithm().getID(), "", 0);
        try {
            Topic t;
            Enumeration<Topic> allTopics = topics.elements();
            while (allTopics.hasMoreElements()) {
                t = allTopics.nextElement();
                Vector<String> children = t.getChildren();
                for (String childName : children) {
                    if (childName.compareTo(node.getID()) == 0) {
                        continue;
                    }
                    Subscriber child = t.getChild(childName);
                    req = new TopologyCacheUpdateRequest(getCoreAlgorithm().getNodeInfo(), child.getNodeInfo(), t.getID(), TopologyCacheUpdateRequest.PARENT_LEAVE, -1);
                    req.setParentID(getCoreAlgorithm().getNodeInfo());
                    req.setGrandParentID(t.getGrandParent().getNodeInfo());
                    getCoreAlgorithm().sendMessage(req, PubSubTransport.ROUTING_DIRECT, null);
                }
                if (t.getParent() != null) {
                    req = new TopologyCacheUpdateRequest(getCoreAlgorithm().getNodeInfo(), t.getParent().getNodeInfo(), t.getID(), TopologyCacheUpdateRequest.CHILD_LEAVE, -1);
                    children = null;
                    children = t.getChildren();
                    req.addInvolvedNode(node);
                    for (String childName : children) {
                        if (childName.equals(getCoreAlgorithm().getNodeInfo().getID()) || childName.equals(node.getID())) {
                            continue;
                        }
                        Subscriber child = t.getChild(childName);
                        req.addInvolvedNode(child.getNodeInfo());
                    }
                }
                getCoreAlgorithm().sendMessage(req, PubSubTransport.ROUTING_DIRECT, null);

            }
        } catch (Exception e) {
            //ignore
        }
    }
    protected KeepAliveIndication msg;

    /**
     * invoking by CoreAlgorithm to send keepAliveIndication
     * @param topic selected topic
     * @return true if the destination node received keepAliveIndication
     */
    public boolean keepAlive(Topic topic) {
        Subscriber parent = topic.getParent();
        NodeInfo thisNode = getCoreAlgorithm().getNodeInfo();
        if (parent == null) {
            //this node is topic root
            return true;
        }
        KeepAliveOperation o = new KeepAliveOperation(topic.getID(), new Subscriber(topic, thisNode), new Event(PubSubConstants.EVENT_CUSTOM));
        Transaction trans = new Transaction(o, topic);
        msg = new KeepAliveIndication(thisNode, parent.getNodeInfo(), topic.getID(), trans.getID());
        getCoreAlgorithm().addKeepAliveTransaction(trans);
        if (getCoreAlgorithm().getTransport().sendDirectly(msg)) {
            //s3544 if true we can stop the transaction
            //getCoreAlgorithm().getKeepAliveTransaction(trans.getID()).terminate();
            getCoreAlgorithm().terminateTransaction(trans.getID(), Transaction.COMPLETED);
            return true;
        } else {
            //s3544 sending keep alive to g-parent
            Subscriber gparent = topic.getGrandParent();
            if (gparent != null) {
                o = new KeepAliveOperation(topic.getID(), new Subscriber(topic, thisNode), new Event(PubSubConstants.EVENT_CUSTOM));
                trans = new Transaction(o, topic);
                msg = null;
                msg = new KeepAliveIndication(thisNode, gparent.getNodeInfo(), topic.getID(), trans.getID());
                msg.setKeepAliveType(KeepAliveIndication.KEEP_ALIVE_PARENT_FAILS);
                msg.setFailedNode(parent.getNodeInfo());
                getCoreAlgorithm().addKeepAliveTransaction(trans);
                if (getCoreAlgorithm().getTransport().sendDirectly(msg)) {
                    //s3544 if true we can stop the transaction and wait for cache update request from gparent
                    //getCoreAlgorithm().getKeepAliveTransaction(trans.getID()).terminate();
                    getCoreAlgorithm().terminateTransaction(trans.getID(), Transaction.COMPLETED);
                    topic.setParent(gparent.getNodeInfo());
                    return true;
                } else {
                    //TODO s3544 invoke resubscribe for topic topic, subscribe fail need to create Topic topic.
                    return false;
                }
            } else {
                NodeInfo proposedNewRoot = topic.getProposedNewRoot();
                if (proposedNewRoot != null && !proposedNewRoot.getID().equals(thisNode.getID())) {
                    o = new KeepAliveOperation(topic.getID(), new Subscriber(topic, thisNode), new Event(PubSubConstants.EVENT_CUSTOM));
                    trans = new Transaction(o, topic);
                    msg = null;
                    msg = new KeepAliveIndication(thisNode, proposedNewRoot, topic.getID(), trans.getID());
                    msg.setKeepAliveType(KeepAliveIndication.ROOT_FAILS);
                    msg.setFailedNode(parent.getNodeInfo());
                    getCoreAlgorithm().addKeepAliveTransaction(trans);
                    if (getCoreAlgorithm().getTransport().sendDirectly(msg)) {
                        //s3544 if true we can stop the transaction and wait for cache update request from gparent
                        //getCoreAlgorithm().getKeepAliveTransaction(trans.getID()).terminate();
                        getCoreAlgorithm().terminateTransaction(trans.getID(), Transaction.COMPLETED);
                        topic.setParent(proposedNewRoot);
                        topic.removeNeighbour(proposedNewRoot.getID());
                        return true;
                    } else {
                        //TODO s3544 typing new root after proposedNewRoot fails
                    }
                } else if (topic.getNeighbours().size() == 0) {
                    //become new topic root
                    topic.setParent(null);
                    topic.setGrandParent(null);
                    //TODO s3544 update child cache
                } else {
                    //TODO s3544 typing new root after proposedNewRoot fails
                }
            }
            return false;
        }
    }

    /**
     * invoking by CoreAlgorithm to send keepAliveIndication to prefer destination node
     * @param topic selected topic
     * @param destination node where indication will be send
     * @return true if the destination node received keepAliveIndication
     */
    public boolean keepAlive(NodeInfo destination, Object topicID) {
        Topic t = getCoreAlgorithm().getTopic((String) topicID);
        if (t == null) {
            //TODO s3544 make some logs here
            return true;
        }
        NodeInfo thisNode = getCoreAlgorithm().getNodeInfo();
        KeepAliveOperation o = new KeepAliveOperation(t.getID(), new Subscriber(t, thisNode), new Event(PubSubConstants.EVENT_CUSTOM));
        Transaction trans = new Transaction(o, t);
        msg = new KeepAliveIndication(thisNode, destination, t.getID(), trans.getID());
        getCoreAlgorithm().addKeepAliveTransaction(trans);
        if (getCoreAlgorithm().getTransport().sendDirectly(msg)) {
            //if true we can stop the transaction
            //getCoreAlgorithm().getKeepAliveTransaction(trans.getID()).terminate();
            getCoreAlgorithm().terminateTransaction(trans.getID(), Transaction.COMPLETED);
            return true;
            //getCoreAlgorithm().removeKeepAliveTransaction(trans.getID());
        } else {
            return false;
        }
    }

    public boolean keepAlive(NodeInfo destination, Object topicID, byte keepAliveType) {
        Topic t = getCoreAlgorithm().getTopic((String) topicID);
        if (t == null) {
            //TODO s3544 make some logs here
            return true;
        }
        NodeInfo thisNode = getCoreAlgorithm().getNodeInfo();
        KeepAliveOperation o = new KeepAliveOperation(t.getID(), new Subscriber(t, thisNode), new Event(PubSubConstants.EVENT_CUSTOM));
        Transaction trans = new Transaction(o, t);
        msg = new KeepAliveIndication(thisNode, destination, t.getID(), trans.getID());
        msg.setKeepAliveType(KeepAliveIndication.ROOT_FAILS);
        getCoreAlgorithm().addKeepAliveTransaction(trans);
        if (getCoreAlgorithm().getTransport().sendDirectly(msg)) {
            //if true we can stop the transaction
            //getCoreAlgorithm().getKeepAliveTransaction(trans.getID()).terminate();
            getCoreAlgorithm().terminateTransaction(trans.getID(), Transaction.COMPLETED);
            return true;
            //getCoreAlgorithm().removeKeepAliveTransaction(trans.getID());
        } else {
            return false;
        }
    }
}
