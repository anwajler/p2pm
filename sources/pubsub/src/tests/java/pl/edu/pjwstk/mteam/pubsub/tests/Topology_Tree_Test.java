package pl.edu.pjwstk.mteam.pubsub.tests;

import java.util.List;
import pl.edu.pjwstk.mteam.p2p.P2PNode;
import pl.edu.pjwstk.mteam.pubsub.logging.Logger;
import org.junit.Test;

import pl.edu.pjwstk.mteam.core.NetworkObject;
import pl.edu.pjwstk.mteam.core.Node;
import pl.edu.pjwstk.mteam.core.NodeCallback;
import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.algorithm.implementation.DefaultCustomizableAlgorithm;
import pl.edu.pjwstk.mteam.pubsub.core.CoreAlgorithm;
import pl.edu.pjwstk.mteam.pubsub.core.Subscriber;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.message.request.SubscribeRequest;
import pl.edu.pjwstk.mteam.pubsub.topology.implementation.TreeTopology;

public class Topology_Tree_Test {
	static Logger logger = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.tests");
	
	static Topic topic;
	static CoreAlgorithm pubsubmngr;
	static DefaultCustomizableAlgorithm algorithm;
	static TreeTopology treeTopology;
	
	static{
		topic = new Topic("Plants");
		Subscriber subscriber = new Subscriber("paulina", topic);
		topic.setOwner(subscriber);
		Node n = new P2PNode(new NodeCallback(){

			@Override
			public void onDisconnect(Node node) {;}

			@Override
			public void onInsertObject(Node node, NetworkObject object) {;}

			@Override
			public void onJoin(Node node) {;}

			@Override
			public void onObjectLookup(Node node, Object object) {;}

			@Override
			public void onOverlayError(Node node, Object sourceID, int errorCode) {;}

			@Override
			public void onTopicCreate(Node node, Object topicID) {;}

			@Override
			public void onTopicRemove(Node node, Object topicID) {;}

			@Override
			public void onTopicSubscribe(Node node, Object topicID) {;}

			@Override
			public void onUserLookup(Node node, Object userInfo) {;}

			public boolean onDeliverRequest(List<NetworkObject> objectList) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public boolean onForwardingRequest(List<NetworkObject> objectList) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onTopicNotify(Node node, Object topicID, byte[] message, boolean historical, short eventType) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onTopicCreate(Node node, Object topicID, int transID) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onTopicSubscribe(Node node, Object topicID, int transID) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onTopicUnsubscribe(Node node, Object topicID, int respCode) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onOverlayError(Node node, Object sourceID, int errorCode, int transID) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onPubSubError(Node node, Object topicID, short operationType, int errorCode) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onPubSubError(Node node, Object topicID, short operationType, int errorCode, int transID) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
			
		});
		
		pubsubmngr = new CoreAlgorithm(9700, n);
		pubsubmngr.setCustomizableAlgorithm();
		algorithm = (DefaultCustomizableAlgorithm)pubsubmngr.getCustomizableAlgorithm();
		treeTopology = new TreeTopology(pubsubmngr);
	}
	
	@Test
	public void DefaultAlgorithm_TopicRoot_Test(){
		/*
		 * Unstructured overlay - request received by root (this node id > new subscriber
		 * id) 
		 * SHOULD BE ACCEPTED, BECAUSE IT IS TOPIC ROOT
		 */
		logger.trace("Entering test 1....");
		topic.setDistance(-1);
		logger.trace("This node info: "+pubsubmngr.getNodeInfo());
		logger.trace("This node distance: "+topic.getDistance());
		SubscribeRequest req = new SubscribeRequest(
				 7000,
				 new NodeInfo("12330", "10.69.40.111", 
				 "tiia", 9070),
				 new NodeInfo("12342", "10.69.40.222", 
				 "tiia2", 9072), "Plants",
				 100000000, -1, -1);
		logger.trace("Request originator's info: "+req.getSourceInfo());
		logger.trace("Request originator's distance: "+req.getDistance());
		topic.DEBUG_showParent();
		treeTopology.onDeliverSubscribe(req, topic);
	}
	
	@Test
	public void DefaultAlgorithm_PlainSubscriber_Test(){
		/*
		 * Unstructured overlay - request received by plain subscriber 
		 * (this node id > new subscriber id) 
		 * SHOULD BE FORWARDED, BECAUSE IT IS PLAIN SUBSCRIBER
		 */
		logger.trace("Entering test 2....");
		topic.setDistance(-1);
		logger.trace("This node info: "+pubsubmngr.getNodeInfo());
		logger.trace("This node distance: "+topic.getDistance());
		SubscribeRequest req = new SubscribeRequest(
				 7000,
				 new NodeInfo("12330", "10.69.40.111", 
				 "tiia", 9070),
				 new NodeInfo("12342", "10.69.40.222", 
				 "tiia2", 9072), "Plants",
				 100000000, -1, -1);
		logger.trace("Request originator's info: "+req.getSourceInfo());
		logger.trace("Request originator's distance: "+req.getDistance());
		topic.setParent(new NodeInfo("2345", "10.28.139.4", "ziuta", 4000));
		topic.DEBUG_showParent();
		treeTopology.onDeliverSubscribe(req, topic);
	}
	
	@Test
	public void DefaultAlgorithm_PlainSubscriber1_Test(){
		/*
		 * Unstructured overlay - request received by plain subscriber 
		 * (this node id < new subscriber id) 
		 * SHOULD BE ACCEPTED, BECAUSE NODE ID < NEW SUBSCRIBER ID
		 */
		logger.trace("Entering test 3....");
		topic.setDistance(-1);
		logger.trace("This node info: "+pubsubmngr.getNodeInfo());
		logger.trace("This node distance: "+topic.getDistance());
		SubscribeRequest req = new SubscribeRequest(
				 7000,
				 new NodeInfo("12347", "10.69.40.111", 
				 "tiia", 9070),
				 new NodeInfo("12342", "10.69.40.222", 
				 "tiia2", 9072), "Plants",
				 100000000, -1, -1);
		logger.trace("Request originator's info: "+req.getSourceInfo());
		logger.trace("Request originator's distance: "+req.getDistance());
		topic.setParent(new NodeInfo("2345", "10.28.139,4", "ziuta", 4000));
		topic.DEBUG_showParent();
		treeTopology.onDeliverSubscribe(req, topic);
	}
	
	@Test
	public void DefaultAlgorithm_PlainSubscriber2_Test(){
		/*
		 * DHT-based overlay - request received by plain subscriber 
		 * (this node distance > new subscriber distance) 
		 * SHOULD BE FORWARDED, BECAUSE IT IS PLAIN SUBSCRIBER
		 */
		logger.trace("Entering test 4....");
		topic.setDistance(40);
		logger.trace("This node info: "+pubsubmngr.getNodeInfo());
		logger.trace("This node distance: "+topic.getDistance());
		SubscribeRequest req = new SubscribeRequest(
				 7000,
				 new NodeInfo("12330", "10.69.40.111", 
				 "tiia", 9070),
				 new NodeInfo("12342", "10.69.40.222", 
				 "tiia2", 9072), "Plants",
				 100000000, -1, 10);
		logger.trace("Request originator's info: "+req.getSourceInfo());
		logger.trace("Request originator's distance: "+req.getDistance());
		topic.setParent(new NodeInfo("2345", "10.28.139,4", "ziuta", 4000));
		topic.DEBUG_showParent();
		treeTopology.onDeliverSubscribe(req, topic);
	}

	@Test
	public void DefaultAlgorithm_PlainSubscriber3_Test(){
		/*
		 * DHT-based overlay - request received by plain subscriber 
		 * (this node distance < new subscriber distance) 
		 * SHOULD BE ACCEPTED
		 */
		logger.trace("Entering test 5....");
		topic.setDistance(40);
		logger.trace("This node info: "+pubsubmngr.getNodeInfo());
		logger.trace("This node distance: "+topic.getDistance());
		SubscribeRequest req = new SubscribeRequest(
				 7000,
				 new NodeInfo("12330", "10.69.40.111", 
				 "tiia", 9070),
				 new NodeInfo("12342", "10.69.40.222", 
				 "tiia2", 9072), "Plants",
				 100000000, -1, 56);
		logger.trace("Request originator's info: "+req.getSourceInfo());
		logger.trace("Request originator's distance: "+req.getDistance());
		topic.setParent(new NodeInfo("2345", "10.28.139,4", "paulina", 4000));
		topic.DEBUG_showParent();
		treeTopology.onDeliverSubscribe(req, topic);
	}
}
