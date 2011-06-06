package pl.edu.pjwstk.mteam.pubsub.algorithm;

import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.core.Transaction;
import pl.edu.pjwstk.mteam.pubsub.message.indication.NotifyIndication;
import pl.edu.pjwstk.mteam.pubsub.message.request.CreateTopicRequest;
import pl.edu.pjwstk.mteam.pubsub.message.request.PublishRequest;
import pl.edu.pjwstk.mteam.pubsub.message.request.UnsubscribeRequest;
import pl.edu.pjwstk.mteam.pubsub.message.response.PubSubResponse;

public interface AlgorithmMessageInterface {
	
	/**
	 * Invoked by PubSubManager after checking, that particular topic doesn't exist.
	 */
	void onDeliverCreateTopic(CreateTopicRequest req);
	
	/**
	 * Invoked by PubSubManager after checking, that particular topic doesn't exist.
	 * The P2PP/RELOAD insert request will be forwarded to the next hop after 
	 * this method invocation. 
	 */
	void onForwardingCreateTopic(CreateTopicRequest req);
	
	/**
	 * Invoked by PubSubManager after checking, that particular topic exists. 
	 * Algorithm component must check if request originator is subscribed.
	 */
	void onDeliverUnsubscribe(UnsubscribeRequest req, Topic t);
	
	/**
	 * Invoked by PubSubManager after checking, that particular topic exists  and 
	 * request originator is allowed to generate events of specified type. 
	 */
	void onDeliverPublish(PublishRequest req, Topic t);
	
	/**
	 * Invoked by PubSubManager after checking, that particular topic exists  and 
	 * request originator is allowed to generate events of specified type. 
	 * @return value indicating, whether the message should be forwarded by the 
	 *         P2PP/RELOAD layer or not.
	 */
	boolean onForwardingPublish(PublishRequest req, Topic t);
	
	/**
	 * Invoked by PubSubManager after checking, that particular topic exists  and 
	 * request originator is allowed to send event notifications to this node. 
	 */
	void onDeliverNotify(NotifyIndication ind, Topic t);
	
	/**
	 * When this method is invoked, the transaction is already removed and
	 * terminated. Algorithm should only invoke appropriate callbacks etc.
	 */
	void onDeliverCreateTopicResponse(PubSubResponse res, Transaction t);
	
	/**
	 * When this method is invoked, the transaction is already removed and
	 * terminated. Algorithm should only invoke appropriate callbacks etc.
	 */
    void onDeliverSubscribeResponse(PubSubResponse res, Transaction t);
   
    /**
	 * When this method is invoked, the transaction is already removed and
	 * terminated. Algorithm should only invoke appropriate callbacks etc.
	 */
    void onDeliverUnsubscribeResponse(PubSubResponse res, Transaction t);
    
    /**
	 * When this method is invoked, the transaction is already removed and
	 * terminated. Algorithm should only invoke appropriate callbacks etc.
	 */
    void onDeliverPublishResponse(PubSubResponse res, Transaction t);   
	
	
	//TODO: Add keep-alive handlers
	
}
