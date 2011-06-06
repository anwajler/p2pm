/*
 * Created on 2008-07-10
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pl.edu.pjwstk.mteam.core;

import java.util.Hashtable;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.topology.maintenance.message.TopologyCacheUpdateRequest;

public interface PubSubInterface {

//utworzenie nowego tematu/ typu zdarzenia
	/**
	 * Creates a new topic with the default Access Control Rules. By default only the
	 * topic owner is allowed to remove the topic or modify its Access Control
	 * @param topicID Topic identifier (String value).
	 * @param subscribe If this value is <code>true</code>, after a successful topic
	 *                  creation the node will automatically become its subscriber. After
	 *                  completing this operation two callbacks will be invoked - 
	 *                  {@link NodeCallback#onTopicCreate(Node, Object)} and
	 *                  {@link NodeCallback#onTopicSubscribe(Node, Object)}.
	 */
	void createTopic(Object topicID, boolean subscribe);
	
	/**
	 * Creates a new topic with custom Access Control Rules.
	 * @param topicID Topic identifier (String value).
	 * @param subscribe If this value is <code>true</code>, after a successful topic
	 *                  creation node will automatically become its subscriber. After
	 *                  completing this operation two callbacks will be invoked - 
	 *                  {@link NodeCallback#onTopicCreate(Node, Object)} and
	 *                  {@link NodeCallback#onTopicSubscribe(Node, Object)}.
	 * @param acRules New <code>AccessControlRules</code> defined for this topic.
	 */
	void createTopic(Object topicID, boolean subscribe, Object acRules);

//usunięcie tematu/typu zdarzenia
	void removeTopic(Object topicID);
	
	/**
	 * Modifies the Access Control rules defined for this topic.
	 * 
	 * @param topicID String value.
	 * @param acRules <code>AccessControlRules</code> defined for this topic.
	 */
	void modifyAccessControlRules(Object topicID, Object acRules);
	
	/**
	 * Modifies the Interest Conditions defined by this node for the specified topic.
	 * 
	 * @param topicID String value.
	 * @param ic <code>InterestConditions</code> defined for this topic.
	 */
	void modifyInterestConditions(Object topicID, Object ic);
	
//ogłaszanie zdarzenia
	void networkPublish(Object topic, byte[] message);
	
//rejestrowanie się do zdarzenia w sieci (np monitoruję dostępność moich znajomych)
	/**
	 * Subscribes for the new topic with the default settings which are defined as follows:<p>
	 * <li> All the events are interesting. One can change these settings later on
	 *      using:<p>
	 *      <code>PubSubsManager psmngr = node.getPubSubManager();</code><p>
	 *      <code>InterestConditions ic = psmngr.getTopic("Plants").getInterestConditions()</code><p>
	 *      <code>ic.getRule(PubSubConstants.OPERATION_NOTIFY).addUser(PubSubConstants.EVENT_CUSTOM, new User("paulina"));</code><p>
	 * <li> Node does not want to receive any events from topic history.     
	 *      
	 * @param topic Topic id (String value).
	 */
	void networkSubscribe(Object topic);
	
	/**
	 * Subscribes for the new topic with the customized parameters:
	 * @param topic Topic id (String value).
	 * @param ic <code>InterestConditions</code> object containing the lists of the users
	 *           which generate events that are interesting for this node.
	 * @param eventIndex Value indicating which events from the topic history node wants to
	 *                   receive after successfully completing subscribe operation.<p>
	 *                   Acceptable values are:<p>
	 *                   <li> {@link PubSubConstants#HISTORY_NONE} - if the node does not want to
	 *                                                               receive any events
	 *                   <li> {@link PubSubConstants#HISTORY_ALL} - if the node wants to receive all
	 *                                                              the events stored in the topic
	 *                                                              history
	 *                   <li> any other value that is >= 0 - indicates the index of the last received 
	 *                                                       event for this topic. All the events with
	 *                                                       greater index will be passed to this node
	 *                                                       by its parent after accepting its request.
	 */
	void networkSubscribe(Object topic, Object ic, int eventIndex);
	
//wyrejestrowanie się od zdarzenia w sieci
	void networkUnsubscribe(Object topic);

        void maintenanceCacheUpdate(Object topic, Object nodeInfo,byte eventType);

        void onDeliverCacheUpdateRequest(TopologyCacheUpdateRequest req);

        void beforeNetworkLeave(Hashtable<String,Topic> topics);

        boolean keepAlive(Topic topic);

        boolean keepAlive(NodeInfo destination, Object topicID);

}
