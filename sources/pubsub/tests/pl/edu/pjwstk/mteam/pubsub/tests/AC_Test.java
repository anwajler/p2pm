package pl.edu.pjwstk.mteam.pubsub.tests;

import pl.edu.pjwstk.mteam.pubsub.logging.Logger;
import org.junit.* ;

import pl.edu.pjwstk.mteam.pubsub.accesscontrol.AccessControlRules;
import pl.edu.pjwstk.mteam.pubsub.core.Event;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.Subscriber;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.operation.CreateTopicOperation;
import pl.edu.pjwstk.mteam.pubsub.operation.KeepAliveOperation;
import pl.edu.pjwstk.mteam.pubsub.operation.NotifyOperation;
import pl.edu.pjwstk.mteam.pubsub.operation.PublishOperation;
import pl.edu.pjwstk.mteam.pubsub.operation.SubscribeOperation;
import static org.junit.Assert.* ;

public class AC_Test{
	static Logger acTestLogger;
	static Topic topic;	
	static Subscriber owner;
	static Subscriber user1;
	static Subscriber user2;
	
	static{
		acTestLogger = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.tests");
		
		topic = new Topic("Software Developement");
		Subscriber subscriber = new Subscriber("paulina", topic);
		topic.setOwner(subscriber);
		acTestLogger.trace("\nCreated topic....."+topic);
		
		new AccessControlRules(topic);
		acTestLogger.trace("\nAC rules for the topic....."+topic.getAccessControlRules());
		
		owner = new Subscriber("paulina", topic);
		user1 = new Subscriber("user1", topic);
		user2 = new Subscriber("user2", topic);
	}
	
	@Test
	public void test_AC_createTopic(){
		Event e = new Event(PubSubConstants.EVENT_ALL);
		
		CreateTopicOperation testOperation = new CreateTopicOperation(topic.getID(), owner, e);
		CreateTopicOperation testOperation1 = new CreateTopicOperation(topic.getID(), user1, e);
		
		boolean result1 = topic.getAccessControlRules().isAllowed(testOperation);
		boolean result2 = topic.getAccessControlRules().isAllowed(testOperation1);
		
		assertTrue(result1 && result2);
	}
	
	@Test
	public void test_AC_subscribe(){
		Event e = new Event(PubSubConstants.EVENT_ALL);
		
		SubscribeOperation testOperation = new SubscribeOperation(topic.getID(), owner, e);
		SubscribeOperation testOperation1 = new SubscribeOperation(topic.getID(), user1, e);
		
		boolean result1 = topic.getAccessControlRules().isAllowed(testOperation);
		boolean result2 = topic.getAccessControlRules().isAllowed(testOperation1);
		
		assertTrue(result1 && result2);
	}
	
	@Test
	public void test_AC_publish_removeTopic(){
		Event e = new Event(PubSubConstants.EVENT_REMOVETOPIC);
		
		PublishOperation testOperation = new PublishOperation(topic.getID(), owner, e);
		PublishOperation testOperation1 = new PublishOperation(topic.getID(), user1, e);
		
		boolean result1 = topic.getAccessControlRules().isAllowed(testOperation);
		boolean result2 = topic.getAccessControlRules().isAllowed(testOperation1);
		
		assertTrue(result1 && result2 == false);
	}
	
	@Test
	public void test_AC_publish_custom(){
		Event e = new Event(PubSubConstants.EVENT_CUSTOM);
		
		PublishOperation testOperation = new PublishOperation(topic.getID(), owner, e);
		PublishOperation testOperation1 = new PublishOperation(topic.getID(), user1, e);
		
		boolean result1 = topic.getAccessControlRules().isAllowed(testOperation);
		boolean result2 = topic.getAccessControlRules().isAllowed(testOperation1);
		
		assertTrue(result1 && result2);
	}
	
	@Test
	public void test_AC_notify_removeTopic(){
		Event e = new Event(PubSubConstants.EVENT_REMOVETOPIC);
		
		NotifyOperation testOperation = new NotifyOperation(topic.getID(), owner, e);
		NotifyOperation testOperation1 = new NotifyOperation(topic.getID(), user1, e);
		
		boolean result1 = topic.getAccessControlRules().isAllowed(testOperation);
		boolean result2 = topic.getAccessControlRules().isAllowed(testOperation1);
		
		assertTrue(result1 && result2);
	}
	
	@Test
	public void test_AC_publish_removeTopic_user1Allowed(){
		Event e = new Event(PubSubConstants.EVENT_REMOVETOPIC);
		
		PublishOperation testOperation = new PublishOperation(topic.getID(), owner, e);
		PublishOperation testOperation1 = new PublishOperation(topic.getID(), user1, e);
		PublishOperation testOperation2 = new PublishOperation(topic.getID(), user2, e);
		
		//adding user1 to default rules
		topic.getAccessControlRules().getRule(PubSubConstants.OPERATION_PUBLISH).addUser(PubSubConstants.EVENT_REMOVETOPIC, user1);
		acTestLogger.trace("New AC rules....."+topic.getAccessControlRules());
		
		boolean result1 = topic.getAccessControlRules().isAllowed(testOperation);
		boolean result2 = topic.getAccessControlRules().isAllowed(testOperation1);
		boolean result3 = topic.getAccessControlRules().isAllowed(testOperation2);
		
		assertTrue(result1 && result2 && result3 == false);
	}
	
	@Test
	public void test_AC_keepalive_onlyUser2Allowed(){
		Event e = new Event(PubSubConstants.EVENT_ALL);
		
		KeepAliveOperation testOperation = new KeepAliveOperation(topic.getID(), owner, e);
		KeepAliveOperation testOperation1 = new KeepAliveOperation(topic.getID(), user1, e);
		KeepAliveOperation testOperation2 = new KeepAliveOperation(topic.getID(), user2, e);
		
		//adding user1 to default rules
		topic.getAccessControlRules().getRule(PubSubConstants.OPERATION_KEEPALIVE).addUser(PubSubConstants.EVENT_ALL, user2);
		acTestLogger.trace("New AC rules....."+topic.getAccessControlRules());
		
		boolean result1 = topic.getAccessControlRules().isAllowed(testOperation);
		boolean result2 = topic.getAccessControlRules().isAllowed(testOperation1);
		boolean result3 = topic.getAccessControlRules().isAllowed(testOperation2);
		
		assertTrue(result1 == false && result2 == false && result3);
	}
	
	@Test
	public void test_AC_keepalive_EverybodyAllowed(){
		Event e = new Event(PubSubConstants.EVENT_ALL);
		
		KeepAliveOperation testOperation = new KeepAliveOperation(topic.getID(), owner, e);
		KeepAliveOperation testOperation1 = new KeepAliveOperation(topic.getID(), user1, e);
		KeepAliveOperation testOperation2 = new KeepAliveOperation(topic.getID(), user2, e);
		
		//adding user1 to default rules
		topic.getAccessControlRules().getRule(PubSubConstants.OPERATION_KEEPALIVE).removeUser(PubSubConstants.EVENT_ALL, user2);
		acTestLogger.trace("New AC rules....."+topic.getAccessControlRules());
		
		boolean result1 = topic.getAccessControlRules().isAllowed(testOperation);
		boolean result2 = topic.getAccessControlRules().isAllowed(testOperation1);
		boolean result3 = topic.getAccessControlRules().isAllowed(testOperation2);
		
		assertTrue(result1 && result2 && result3);
	}
}
