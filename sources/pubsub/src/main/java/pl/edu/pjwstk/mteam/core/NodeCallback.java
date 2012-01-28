package pl.edu.pjwstk.mteam.core;

import java.util.List;

import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;

/**
 * This class represents a set of user-defined methods invoked after asynchronous 
 * operation is finished to pass the event to higher layer.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public interface NodeCallback {
	public void onDisconnect(Node node);
	/**
	 * @param node Lookup request sender
	 * @param userInfo NodeInfo object containing IP and port number
	 */
	public void onUserLookup(Node node, Object userInfo);
	/**
	 * @param node Lookup request sender
	 * @param object String value
	 */
	public void onObjectLookup(Node node, Object object);
	/**
	 * Called, when node receives topic notification message.
	 * @param node This node reference.
	 * @param topicID String value.
	 * @param message Message encapsulated in this event
         * @param historical true if this notify is from past
	 */
	public void onTopicNotify(Node node, Object topicID, byte[] message, boolean historical,short eventType);
	/**
	 * Called when topic is successfully created.
	 * @param node This node reference. 
	 * @param topicID Created topic ID.
	 */
	public void onTopicCreate(Node node, Object topicID);
        public void onTopicCreate(Node node, Object topicID, int transID);
	/**
	 * Called when node receives notification, that topic it is subscribed for
	 * has been removed.
	 * @param node This node reference.
	 * @param topicID String value.
	 */
	public void onTopicRemove(Node node, Object topicID);
	/**
	 * Called after successful subscription.
	 * @param node This node reference.
	 * @param topicID String value.
	 */
	public void onTopicSubscribe(Node node, Object topicID);
        public void onTopicSubscribe(Node node, Object topicID, int transID);
	/**
	 * Called after unsubscribing.
	 * @param node This node reference.
	 * @param topicID String value.
	 */
	public void onTopicUnsubscribe(Node node, Object topicID, int respCode);
	/**
	 * Called, when object is successfully inserted into DHT
	 * @param node Insert request sender
	 * @param object Inserted object (currently <code>null</code> value).
	 */
	public void onInsertObject(Node node, NetworkObject object);
	/**
	 * called, after node successfully joined network
	 * @param node Join request sender
	 */
	public void onJoin(Node node);
	/**
	 * Called on asynchronous overlay operation error  
	 * @param node Request sender
	 * @param sourceID Currently <code>null</code> value. Probably it will 
	 * 				   provide user/object ID in future. 
	 * @param errorCode Indicates, what caused the problem. Possible
	 * 					values are:<br>
	 *  				<li>{@link NodeError#BOOTSTRAPERR}
	 *  				<li>{@link NodeError#INSERTERR}
	 *  				<li>{@link NodeError#RLOOKUPERR}
	 *  				<li>{@link NodeError#ULOOKUPERR}
	 * 
	 */
	public void onOverlayError(Node node, Object sourceID, int errorCode);
        public void onOverlayError(Node node, Object sourceID, int errorCode, int transID);
	/**
	 * Called on asynchronous pub-sub operation error  
	 * @param node Request sender
	 * @param topicID Topic ID this error is associated with (currently
	 * 				   String value).
	 * @param operationType Type of the operation, this error is associated with. Possible
	 *                      values are:<p>
	 *                      <li>{@link PubSubConstants#OPERATION_CREATETOPIC}
	 *                      <li>{@link PubSubConstants#OPERATION_SUBSCRIBE}
	 *                      <li>{@link PubSubConstants#OPERATION_UNSUBSCRIBE}
	 *                      <li>{@link PubSubConstants#OPERATION_PUBLISH}
	 * @param errorCode Indicates, what caused the problem. Possible
	 * 					values are:<br>
	 *  				<li>{@link NodeError#TOPICEXISTSERR}
	 *  				<li>{@link NodeError#AUTHERR}
	 *  				<li>{@link NodeError#NOSUCHTOPICERR}
	 * 
	 */
	public void onPubSubError(Node node, Object topicID, short operationType, int errorCode);
        public void onPubSubError(Node node, Object topicID, short operationType, int errorCode, int transID);

	/**
	 * Invoked after receiving P2PP insert request containing a MESSAGE
	 * object (or objects).
	 * @param objectList List of objects encapsulating other protocol messages.
	 * @return Value indicating whether to continue standard processing
	 *         (insert these objects into DHT) or not.
	 */
	public boolean onDeliverRequest(List<NetworkObject> objectList);
	
	/**
	 * Invoked before forwarding P2PP insert request containing a MESSAGE
	 * object (or objects).
	 * @param objectList List of objects encapsulating other protocol messages.
	 * @return Value indicating whether to forward the message or discard it.
	 */
	public boolean onForwardingRequest(List<NetworkObject> objectList);
}
