/**
 * Contains objects implementing publish-subscribe topology layer.
 */
package pl.edu.pjwstk.mteam.pubsub.topology;

import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.message.request.CreateTopicRequest;
import pl.edu.pjwstk.mteam.pubsub.message.request.SubscribeRequest;
import pl.edu.pjwstk.mteam.pubsub.message.response.StandardResponse;

/**
 * Set of methods used for processing messages which affect topology structure.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public interface TopologyMessageInterface{
	
	/**
	 * Method used for processing 'subscribe' requests, which this node is 
	 * destination for.
	 * @param req Incoming subscribe request.
	 * @param t Topic from this node's topic list, this message is associated with.
	 * @return If the value is <code>true</code>, Core Algorithm component checks if
	 *         it does not have to send notifications containing entries from the 
	 *         topic history.
	 */
	boolean onDeliverSubscribe(SubscribeRequest req, Topic t);
	
	/**
	 * Method used for processing 'subscribe' requests, which this node is 
	 * forwarding.
	 * @param req Incoming subscribe request.
	 * @param t Topic from this node's topic list, this message is associated with.
	 * @return If the value is <code>true</code>, Core Algorithm component checks if
	 *         it does not have to send notifications containing entries from the 
	 *         topic history.
	 */
	boolean onForwardingSubscribe(SubscribeRequest req, Topic t);
	
	/**
	 * Method invoked after receiving topic transfer request. Do not create the topic
	 * in this method - if it is necessary, it is created by publish-subscribe 
	 * manager.
	 * @param req Create topic request with appropriate flag set to 1, to 
	 *            indicate that this is topic transfer.
	 * @param t Requested topic (already existing or just created by 
	 *          publish-subscribe manager).
	 */
	void onDeliverTransferTopic(CreateTopicRequest req, Topic t);
	
	/**
	 * Invoked after receiving response to transfer topic request.
	 * @param res
	 * @param t
	 */
	void onDeliverTransferTopicResponse(StandardResponse res, Topic t);       
}
