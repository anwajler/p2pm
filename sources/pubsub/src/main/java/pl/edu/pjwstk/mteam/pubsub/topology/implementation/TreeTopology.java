/**
 * Contains implementations of publish-subscribe topology managers.
 */
package pl.edu.pjwstk.mteam.pubsub.topology.implementation;

import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.logging.Logger;

import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.CoreAlgorithm;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.message.request.CreateTopicRequest;
import pl.edu.pjwstk.mteam.pubsub.message.request.SubscribeRequest;
import pl.edu.pjwstk.mteam.pubsub.message.response.StandardResponse;
import pl.edu.pjwstk.mteam.pubsub.topology.TopologyManager;

/**
 * Class representing publish-subscribe multicast tree.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class TreeTopology extends TopologyManager {
	static Logger logger = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.topology.implementation.TreeTopology");
	
	/**
	 * Creates star topology.
	 * @param pubsubMngr Publish-Subscribe Manager, this topology i associated with.
	 */
	public TreeTopology(CoreAlgorithm pubsubMngr){
		super(pubsubMngr);
	}

	/**
	 * Invoked from PubSubManager's onDeliverRequest method.
	 * @param req Incoming subscribe request.
	 * @param t Topic from node's topic list, which this request is associated with.
	 * @return If the value is <code>true</code>, Core Algorithm component checks if
	 *         it does not have to send notifications containing entries from the 
	 *         topic history.
	 */
	public boolean onDeliverSubscribe(SubscribeRequest req, Topic t) {
		NodeInfo whereToForward = pubsubManager.whereToForwardRequest(t, req);
		if(whereToForward == null){			
	        return true;
		}
		else if(whereToForward.equals(t.getParent().getNodeInfo())){
		    pubsubManager.forwardToParent(req, t);
		    logger.info("Subscriber '"+req.getSourceInfo()+
		    		    "' forwarded to the parent for the topic '"+t.getID()+"'.....\n");
		    return false;
		}
		else{
			pubsubManager.forwardToOtherNode(req, whereToForward);
			logger.info("Subscriber '"+req.getSourceInfo()+
	    		        "' forwarded to a different '"+t.getID()+"' subscriber.....\n");
			return false;
		}
	}

	/**
	 * Invoked from PubSubManager's onForwardingRequest method.
	 * @param req Incoming subscribe request.
	 * @param t Topic from node's topic list, which this request is associated with.
	 * @return If the value is <code>true</code>, Core Algorithm component checks if
	 *         it does not have to send notifications containing entries from the 
	 *         topic history.
	 */
	public boolean onForwardingSubscribe(SubscribeRequest req, Topic t) {
		return onDeliverSubscribe(req, t);
	}

	@Override
	public void onDeliverTransferTopic(CreateTopicRequest req, Topic t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDeliverTransferTopicResponse(StandardResponse res, Topic t) {
		// TODO Auto-generated method stub
		
	}
}
