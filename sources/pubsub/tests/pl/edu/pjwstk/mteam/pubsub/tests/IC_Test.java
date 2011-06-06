package pl.edu.pjwstk.mteam.pubsub.tests;

import pl.edu.pjwstk.mteam.pubsub.logging.Logger;

import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.Subscriber;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.interestconditions.InterestConditions;

public class IC_Test {
	static Logger icTestLogger;
	static Topic topic;	
	static Subscriber owner;
	static Subscriber user1;
	static Subscriber user2;
	
	static{
		icTestLogger = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.tests");
		
		topic = new Topic("Software Developement");
		Subscriber subscriber = new Subscriber("paulina", topic);
		topic.setOwner(subscriber);
		icTestLogger.trace("\nCreated topic....."+topic);
		
		owner = new Subscriber("paulina", topic);
		user1 = new Subscriber("user1", topic);
		user2 = new Subscriber("user2", topic);
		
		InterestConditions conditions = new InterestConditions(topic);
		/*
		 * Rules should not allow adding limitations to MODIFYAC and REMOVETOPIC events - they are always
		 * interesting
		 */
		conditions.getRule(PubSubConstants.OPERATION_NOTIFY).addUser(PubSubConstants.EVENT_ALL, owner);
		conditions.getRule(PubSubConstants.OPERATION_NOTIFY).addUser(PubSubConstants.EVENT_REMOVETOPIC, owner);
		conditions.getRule(PubSubConstants.OPERATION_NOTIFY).addUser(PubSubConstants.EVENT_CUSTOM, user1);
		conditions.getRule(PubSubConstants.OPERATION_NOTIFY).addUser(PubSubConstants.EVENT_CUSTOM, owner);
		
		icTestLogger.trace("\nIC for the topic....."+conditions);
	}
	
/*	@Test
	public void test_IC_removeTopic(){
		Event e = new Event(PubSubConstants.EVENT_REMOVETOPIC);
		
		NotifyOperation testOperation = new NotifyOperation(topic.getID(), owner, e);
		NotifyOperation testOperation1 = new NotifyOperation(topic.getID(), user1, e);
		
		boolean result1 = topic.getInterestConditions().isInteresting(testOperation);
		boolean result2 = topic.getInterestConditions().isInteresting(testOperation1);
		
		assertTrue(result1 && result2);
	}
	
	@Test
	public void test_IC_custom(){
		Event e = new Event(PubSubConstants.EVENT_CUSTOM);
		
		NotifyOperation testOperation = new NotifyOperation(topic.getID(), owner, e);
		NotifyOperation testOperation1 = new NotifyOperation(topic.getID(), user1, e);
		NotifyOperation testOperation2 = new NotifyOperation(topic.getID(), user2, e);
		
		boolean result1 = topic.getInterestConditions().isInteresting(testOperation);
		boolean result2 = topic.getInterestConditions().isInteresting(testOperation1);
		boolean result3 = topic.getInterestConditions().isInteresting(testOperation2);
		
		assertTrue(result1 && result2 && !result3);
	}
*/
}
