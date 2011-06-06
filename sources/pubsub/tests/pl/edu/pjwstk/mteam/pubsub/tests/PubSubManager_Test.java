package pl.edu.pjwstk.mteam.pubsub.tests;

import java.util.List;
import pl.edu.pjwstk.mteam.p2p.P2PNode;
import pl.edu.pjwstk.mteam.pubsub.logging.Logger;

import org.junit.*;

import pl.edu.pjwstk.mteam.core.NetworkObject;
import pl.edu.pjwstk.mteam.core.Node;
import pl.edu.pjwstk.mteam.core.NodeCallback;
import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.accesscontrol.AccessControlRules;
import pl.edu.pjwstk.mteam.pubsub.core.Event;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.CoreAlgorithm;
import pl.edu.pjwstk.mteam.pubsub.core.Subscriber;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.core.Transaction;
import pl.edu.pjwstk.mteam.pubsub.core.User;
import pl.edu.pjwstk.mteam.pubsub.message.indication.NotifyIndication;
import pl.edu.pjwstk.mteam.pubsub.message.request.CreateTopicRequest;
import pl.edu.pjwstk.mteam.pubsub.message.request.PublishRequest;
import pl.edu.pjwstk.mteam.pubsub.message.request.SubscribeRequest;
import pl.edu.pjwstk.mteam.pubsub.operation.CreateTopicOperation;

public class PubSubManager_Test {
	static Logger logger = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.tests");
	
	static Topic topic;
	static CoreAlgorithm pubsubmngr;
	static Subscriber user1;
	
	static{
		logger = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.tests");		
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
			public void onPubSubError(Node node, Object topicID,
					byte operationType, int errorCode) {;}

			@Override
			public void onTopicCreate(Node node, Object topicID) {;}

			@Override
			public void onTopicNotify(Node node, Object topicID, byte[] message) {;}

			@Override
			public void onTopicRemove(Node node, Object topicID) {;}

			@Override
			public void onTopicSubscribe(Node node, Object topicID) {;}

			@Override
			public void onUserLookup(Node node, Object userInfo) {;}

			@Override
			public void onTopicUnsubscribe(Node node, Object topicID) {
				// TODO Auto-generated method stub
				
			}

            public boolean onDeliverRequest(List<NetworkObject> objectList) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public boolean onForwardingRequest(List<NetworkObject> objectList) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
			
		});
		
		pubsubmngr = new CoreAlgorithm(9700, n);
		pubsubmngr.setCustomizableAlgorithm();
		pubsubmngr.addTopic(topic);
		
		user1 = new Subscriber("paulina", topic);
	}
	
	@Test
	public void PubSubManager_OnDeliverSubscribe_Test(){
		logger.trace("Entering test 1...");
		SubscribeRequest req = new SubscribeRequest(
				 7000,
				 new NodeInfo("13256", "10.69.40.111", 
				 "tiia", 9070),
				 new NodeInfo("24356", "10.69.40.222", 
				 "tiia2", 9072), "Plants",
				 100000000, -1, 30);
		pubsubmngr.onDeliverRequest(req);
		
		pubsubmngr.DEBUG_showTopics();
	}
	
	@Test
	public void PubSubManager_OnDeliverSubscribe_AccessDenied_Test(){
		logger.trace("Entering test 2...");
		logger.trace("Modifying AC rules for "+PubSubConstants.STR_OPERATION[PubSubConstants.OPERATION_SUBSCRIBE]+" operation..");
		topic.getAccessControlRules().getRule(PubSubConstants.OPERATION_SUBSCRIBE).addUser(PubSubConstants.EVENT_ALL, user1);
		SubscribeRequest req = new SubscribeRequest(
				 7000,
				 new NodeInfo("12345", "10.69.40.111", 
				 "tiia", 9070),
				 new NodeInfo("53672", "10.69.40.222", 
				 "tiia2", 9072), "Plants",
				 100000000, -1, 30);
		pubsubmngr.onDeliverRequest(req);
		
		req = new SubscribeRequest(
				 7000,
				 new NodeInfo("12340", "10.69.40.111", 
				 "paulina", 9070),
				 new NodeInfo("75689", "10.69.40.222", 
				 "tiia2", 9072), "Plants",
				 100000000, -1, 30);
		pubsubmngr.onDeliverRequest(req);
	}
	
	@Test
	public void PubSubManager_OnDeliverSubscribe_NoSuchTopic_Test(){
		logger.trace("Entering test 3...");
		SubscribeRequest req = new SubscribeRequest(
				 7000,
				 new NodeInfo("12645", "10.69.40.111", 
				 "ziuta", 9070),
				 new NodeInfo("53672", "10.69.40.222", 
				 "tiia2", 9072), "Software developement",
				 100000000, -1, 30);
		pubsubmngr.onDeliverRequest(req);
		
		
	}
	
	@Test
	public void PubSubManager_OnForwardingSubscribe_Test(){
		logger.trace("Entering test 4...");
		logger.trace("Modifying AC rules for "+PubSubConstants.STR_OPERATION[PubSubConstants.OPERATION_SUBSCRIBE]+" operation..");
		topic.getAccessControlRules().getRule(PubSubConstants.OPERATION_SUBSCRIBE).removeUser(PubSubConstants.EVENT_ALL, user1);
		SubscribeRequest req = new SubscribeRequest(
				 7000,
				 new NodeInfo("12645", "10.69.40.111", 
				 "brand new user", 9070),
				 new NodeInfo("53672", "10.69.40.222", 
				 "tiia2", 9072), "Plants",
				 100000000, -1, 30);
		pubsubmngr.onForwardingRequest(req);
	}
	
	@Test
	public void PubSubManager_OnDeliverIndiaction_CustomNotify_Test(){
		logger.trace("Entering test 5...");
		topic.DEBUG_showChildren();
		String msg = new String("Hello ;)");
		NotifyIndication ind = new NotifyIndication(
				 new NodeInfo("12345", "10.69.40.111", 
				 "tiia", 9070),
				 new NodeInfo("23147", "10.69.40.222", 
				 "tiia2", 9072), "Plants",
				 PubSubConstants.EVENT_CUSTOM, msg.getBytes(), true,
				 new User("tiia"));
		pubsubmngr.onDeliverIndication(ind);
	}
	
	@Test
	public void PubSubManager_OnDeliverIndiaction_ACMODIFIEDNotify_Test(){
		logger.trace("Entering test 6...");
		logger.trace(topic.getAccessControlRules());
		
		AccessControlRules newRules = new AccessControlRules(new Topic("Plants"));
		newRules.getRule(PubSubConstants.OPERATION_PUBLISH).addUser(PubSubConstants.EVENT_CUSTOM, user1);
		
		NotifyIndication ind = new NotifyIndication(
				 new NodeInfo("12345", "10.69.40.111", 
				 "tiia", 9070),
				 new NodeInfo("23147", "10.69.40.222", 
				 "tiia2", 9072), "Plants",
				 PubSubConstants.EVENT_MODIFYAC, newRules.encode(), true,
				 new User("tiia"));
		pubsubmngr.onDeliverIndication(ind);
		logger.trace(pubsubmngr.getTopic("Plants").getAccessControlRules());
	}
	
	@Test
	public void PubSubManager_OnDeliverIndiaction_REMOVETOPICNotify_Test(){
		logger.trace("Entering test 7...");
		
		NotifyIndication ind = new NotifyIndication(
				 new NodeInfo("12345", "10.69.40.111", 
				 "tiia", 9070),
				 new NodeInfo("23147", "10.69.40.222", 
				 "tiia2", 9072), "Plants",
				 PubSubConstants.EVENT_REMOVETOPIC, null, true,
				 new User("tiia"));
		pubsubmngr.onDeliverIndication(ind);
		pubsubmngr.DEBUG_showTopics();
	}
	
	/*
	 * The previous test removed the only topic from pubsubmngr, so add one before
	 * performing other tests.
	 */
	
	@Test
	public void PubSubManager_OnDeliverPublish_Root_Test(){
		/*
		 * Publish request reaches topic root
		 */
		logger.trace("Entering test 8 - onDeliverPublish...");
		new AccessControlRules(topic);
		pubsubmngr.addTopic(topic);
		pubsubmngr.DEBUG_showTopics();
		topic.DEBUG_showChildren();
		
		String msg = "Custom message :)";
		
		PublishRequest req = new PublishRequest(
				7000,
				 new NodeInfo("75684", "10.69.40.111", 
				 "ziuta", 9070),
				 new NodeInfo("93875", "10.69.40.222", 
				 "gustaw", 9072), "Plants",
				 PubSubConstants.EVENT_CUSTOM, msg.getBytes(),
				 new User("tiia"));
		
		pubsubmngr.onDeliverRequest(req);
	}
	
	@Test
	public void PubSubManager_OnDeliverPublish_Subscriber_Test(){
		/*
		 * Publish request reaches plain topic subscriber - it should be forwarded
		 * to parent
		 */
		logger.trace("Entering test 9 - onDeliverPublish (forwarding to parent)...");
	    topic.setParent(new NodeInfo("12011", "192.168.9.46", "zenek", 9050));
		String msg = "Custom message :)";
		
		PublishRequest req = new PublishRequest(
				7000,
				 new NodeInfo("75684", "10.69.40.111", 
				 "ziuta", 9070),
				 new NodeInfo("93875", "10.69.40.222", 
				 "gustaw", 9072), "Plants",
				 PubSubConstants.EVENT_CUSTOM, msg.getBytes(),
				 new User("tiia"));
		
		pubsubmngr.onDeliverRequest(req);
		
	}
	
	@Test
	public void PubSubManager_AddRemoveTransaction_Test(){
		logger.trace("Entering test 10...");
		Event e = new Event(PubSubConstants.EVENT_ALL);
		Subscriber owner = new Subscriber("paulina", topic);
		CreateTopicOperation o = new CreateTopicOperation(topic.getID(), owner, e);
		pubsubmngr.addTransaction(new Transaction(o));
		pubsubmngr.DEBUG_showTransactions();
		
		/* 
		 * Trying to remove transaction that does not exist.
		 */
		Transaction newTransaction = new Transaction(o);
		logger.trace("Trying to remove transaction "+newTransaction.getID()+"...");
		Transaction removed = pubsubmngr.removeTransaction(newTransaction.getID());
		if(removed == null){
			logger.trace("Remove operation failed - no transaction with id "+
					     newTransaction.getID()+" found...");
		}
		/* 
		 * Trying to remove existing transaction.
		 */
		logger.trace("Trying to remove existing transaction 1...");
		removed = pubsubmngr.removeTransaction(1);
		if(removed != null){
			logger.trace("Successfully removed transaction with id "+
					     removed.getID()+"...");
		}
		pubsubmngr.DEBUG_showTransactions();	
	}
	
/*	@Test
	public void PubSubManager_OnDeliverIndiaction_NotInteresting_Test(){
		logger.trace("Entering test 11...");
		topic.DEBUG_showChildren();
		
		topic.getInterestConditions().getRule(PubSubConstants.OPERATION_NOTIFY).addUser(PubSubConstants.EVENT_CUSTOM, 
				                                                                        new User("paulina"));
		
		String msg = new String("Hello ;)");
		NotifyIndication ind = new NotifyIndication(
				 new NodeInfo("12345", "10.69.40.111", 
				 "tiia", 9070),
				 new NodeInfo("23147", "10.69.40.222", 
				 "tiia2", 9072), "Plants",
				 PubSubConstants.EVENT_CUSTOM, msg.getBytes(), true,
				 new User("tiia"));
		pubsubmngr.onDeliverIndication(ind);
		
		topic.getInterestConditions().getRule(PubSubConstants.OPERATION_NOTIFY).removeUser(PubSubConstants.EVENT_CUSTOM,
				                                                                           new User("paulina"));
	}*/
	
	@Test
	public void PubSubManager_OnIncomingCreateNewTopic_AlreadyExists(){
		logger.trace("Entering test 12...");
		CreateTopicOperation o = new CreateTopicOperation("Plants",
				                                          new Subscriber("tiia", topic),
				                                          new Event(PubSubConstants.EVENT_NEWTOPIC));
		Transaction t = new Transaction(o);
		CreateTopicRequest req = new CreateTopicRequest(
				         new NodeInfo("12345", "10.69.40.111", 
						 "tiia", 9070),
						 new NodeInfo("23147", "10.69.40.222", 
								 "tiia2", 9072), "Plants",
					     PubSubConstants.CREATETOPICFLAG_NEWTOPIC,
					     t.getID());
		logger.trace("Invoking onDeliverRequest....");
		pubsubmngr.onDeliverRequest(req);
		logger.trace("Invoking onForwardingRequest....");
		pubsubmngr.onForwardingRequest(req);
	}
	
	@Test
	public void PubSubManager_OnIncomingCreateTopic_DoesntExists(){
		logger.trace("Entering test 13...");
		Topic top = new Topic("Software developement");
		CreateTopicOperation o = new CreateTopicOperation(top.getID(),
				                                          new Subscriber("tiia", top),
				                                          new Event(PubSubConstants.EVENT_NEWTOPIC));
		Transaction t = new Transaction(o);
		CreateTopicRequest req = new CreateTopicRequest(
				         new NodeInfo("12345", "10.69.40.111", 
						 "tiia", 9070),
						 new NodeInfo("23147", "10.69.40.222", 
								 "tiia2", 9072), top.getID(),
					     PubSubConstants.CREATETOPICFLAG_NEWTOPIC,
					     t.getID());
		logger.trace("Invoking onDeliverRequest....");
		pubsubmngr.onDeliverRequest(req);
		logger.trace("Invoking onForwardingRequest....");
		pubsubmngr.onForwardingRequest(req);
	}
	
	@Test
	public void PubSubManager_OnIncomingTransferTopic_AlreadyExists(){
		logger.trace("Entering test 14...");
		CreateTopicOperation o = new CreateTopicOperation("Plants",
				                                          new Subscriber("tiia", topic),
				                                          new Event(PubSubConstants.EVENT_NEWTOPIC));
		Transaction t = new Transaction(o);
		CreateTopicRequest req = new CreateTopicRequest(
				         new NodeInfo("12345", "10.69.40.111", 
						 "tiia", 9070),
						 new NodeInfo("23147", "10.69.40.222", 
								 "tiia2", 9072), "Plants",
					     PubSubConstants.CREATETOPICFLAG_TRANSFERTOPIC,
					     t.getID());
		logger.trace("Invoking onDeliverRequest....");
		pubsubmngr.onDeliverRequest(req);
		logger.trace("Invoking onForwardingRequest....");
		pubsubmngr.onForwardingRequest(req);
	}
	
	@Test
	public void PubSubManager_OnIncomingTransferTopic_DoesntExists(){
		logger.trace("Entering test 15...");
		Topic top = new Topic("Software developement");
		CreateTopicOperation o = new CreateTopicOperation(top.getID(),
				                                          new Subscriber("tiia", top),
				                                          new Event(PubSubConstants.EVENT_NEWTOPIC));
		Transaction t = new Transaction(o);
		CreateTopicRequest req = new CreateTopicRequest(
				         new NodeInfo("12345", "10.69.40.111", 
						 "tiia", 9070),
						 new NodeInfo("23147", "10.69.40.222", 
								 "tiia2", 9072), top.getID(),
					     PubSubConstants.CREATETOPICFLAG_TRANSFERTOPIC,
					     t.getID());
		logger.trace("Invoking onDeliverRequest....");
		pubsubmngr.onDeliverRequest(req);
		logger.trace("Invoking onForwardingRequest....");
		pubsubmngr.onForwardingRequest(req);
		pubsubmngr.DEBUG_showTopics();
	}
}
