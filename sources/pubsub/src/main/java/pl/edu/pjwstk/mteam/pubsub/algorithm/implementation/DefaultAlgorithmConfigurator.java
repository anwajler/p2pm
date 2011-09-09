/**
 * Contains objects associated with publish-subscribe Algorithm component.
 */
package pl.edu.pjwstk.mteam.pubsub.algorithm.implementation;

import pl.edu.pjwstk.mteam.pubsub.logging.Logger;

import pl.edu.pjwstk.mteam.pubsub.algorithm.AlgorithmConfigurator;
import pl.edu.pjwstk.mteam.pubsub.algorithm.CustomizableAlgorithm;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.message.PubSubMessage;

/**
 * Contains default implementation of method used for choosing appropriate publish-subscribe algorithm
 * for particular overlay type. 
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class DefaultAlgorithmConfigurator implements AlgorithmConfigurator{
	static Logger logger = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.algorithm");

	/**
	 * For all overlay types returns {@link DefaultCustomizableAlgorithm}.
	 */
	public CustomizableAlgorithm chooseAlgorithm(String algorithmName){
		//registering messages for the algorithm
		PubSubMessage.registerMessageType("pl.edu.pjwstk.mteam.pubsub.message.request.SubscribeRequest", PubSubConstants.MSG_SUBSCRIBE);
		PubSubMessage.registerMessageType("pl.edu.pjwstk.mteam.pubsub.message.request.CreateTopicRequest", PubSubConstants.MSG_CREATETOPIC);
		PubSubMessage.registerMessageType("pl.edu.pjwstk.mteam.pubsub.message.response.StandardResponse", PubSubConstants.MSG_STDRESPONSE);
		PubSubMessage.registerMessageType("pl.edu.pjwstk.mteam.pubsub.message.request.UnsubscribeRequest", PubSubConstants.MSG_UNSUBSCRIBE);
		PubSubMessage.registerMessageType("pl.edu.pjwstk.mteam.pubsub.message.request.PublishRequest", PubSubConstants.MSG_PUBLISH);
		PubSubMessage.registerMessageType("pl.edu.pjwstk.mteam.pubsub.message.indication.NotifyIndication", PubSubConstants.MSG_NOTIFY);
		PubSubMessage.registerMessageType("pl.edu.pjwstk.mteam.pubsub.message.indication.KeepAliveIndication", PubSubConstants.MSG_KEEPALIVE);
		PubSubMessage.registerMessageType("pl.edu.pjwstk.mteam.pubsub.topology.maintenance.message.TopologyCacheUpdateRequest", PubSubConstants.MSG_MAINTENANCE_CACHE_UPDATE);
                
		/*
		 * TODO: Here algorithm could be parameterized (f.e. caches sizes could be 0 
		 * or very small for structured overlays and larger for unstructured ones,
		 * because the cost of repairing the structure without it is larger)
		 */
		return new DefaultCustomizableAlgorithm();
	}

}
