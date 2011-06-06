package pl.edu.pjwstk.mteam.pubsub.tests;

import org.junit.*;

import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.core.Subscriber;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.logging.Logger;

public class Subscriber_Test {
	static Logger logger = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.tests");
	static Topic topic;	
	static Topic topic1;
	static Topic topic2;

	static{
		topic = new Topic("Software Developement");
		Subscriber subscriber = new Subscriber("paulina", topic);
		topic.setOwner(subscriber);
		
		topic1 = new Topic("Plants");
		topic2 = new Topic("Palaeontology");
	}
	
	@Test
	public void test_newSubscriber_nameTopic(){
		Subscriber s = new Subscriber("testUser", topic);
		s.addTopic(topic1);
		s.DEBUG_showTopics();
	}
	
	@Test
	public void test_newSubscriber_topicNodeInfo(){
		NodeInfo ninfo = new NodeInfo("uid2", "10.25.31.40", "testUser2", 7000);
		Subscriber s = new Subscriber(topic, ninfo);
		s.addTopic(topic1);
		s.DEBUG_showTopics();
	}
	
	@Test
	public void test_Subscriber_romoveWhenNoSuchTopic(){
		NodeInfo ninfo = new NodeInfo("uid2", "10.25.31.40", "testUser2", 7000);
		Subscriber s = new Subscriber(topic, ninfo);
		s.addTopic(topic1);
		s.removeTopic(topic2);
		s.DEBUG_showTopics();
		s.removeTopic(topic);
		s.DEBUG_showTopics();
	}
	
	@Test
	public void test_Subscriber_encodingParsing(){
		NodeInfo ninfo = new NodeInfo("12345", "10.25.31.40", "paulina", 7000);
		Subscriber s = new Subscriber(topic, ninfo);
		logger.trace("Original subscriber: "+s);
		byte[] encs = s.encode();
		Subscriber parsed = new Subscriber(encs);
		logger.trace("Parsed subscriber: "+parsed);
	}
}
