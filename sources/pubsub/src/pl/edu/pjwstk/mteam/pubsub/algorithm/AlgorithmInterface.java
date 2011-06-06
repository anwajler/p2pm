/**
 * Contains objects associated with publish-subscribe Algorithm component.
 */
package pl.edu.pjwstk.mteam.pubsub.algorithm;

import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.message.request.SubscribeRequest;

/**
 * Interface defining method used for customizing topology component to fit algorithm
 * requirements. 
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public interface AlgorithmInterface{
	
	/**
	 * It is invoked for example by TreeTopology object just before accepting 
	 * subscribe request and allows publish-subscribe algorithm to define additional 
	 * rules for this operation. If these  requirements are fulfilled - node accepts 
	 * new subscriber itself. Otherwise it passes request to its parent in topology 
	 * structure. Topic root MUST always accept new subscriber, even if 
	 * whereToForwardRequest method returns <code>null</code>, unless this is forbidden by 
	 * Access Control Rules.
	 * @param t Topic, this request is associated with
	 * @param req Incoming subscribe request.
	 * @return Node to which the subscribe request should be forwarded or <code>null</code>,
	 *         if the request should be accepted by this node.
	 */
	NodeInfo whereToForwardSubscribeRequest(Topic t, SubscribeRequest req);

	//TODO: Add transaction/subscription timeout handlers

}
