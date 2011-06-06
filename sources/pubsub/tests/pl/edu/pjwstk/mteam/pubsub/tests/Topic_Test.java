package pl.edu.pjwstk.mteam.pubsub.tests;

import pl.edu.pjwstk.mteam.pubsub.logging.Logger;
import org.junit.*;

import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.core.Subscriber;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;

public class Topic_Test{
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
		Subscriber subscriber1 = new Subscriber("ziuta", topic);
		topic.setOwner(subscriber1);
	}
	
	@Test
	public void Topic_SetParent_Remove_Test(){
		logger.trace("Entering test 1.........");
		NodeInfo parent = new NodeInfo("parent1Id", "10.29.45.7", "parent1", 4000);
		topic.setParent(parent);
		topic.DEBUG_showParent();
		Topic.DEBUG_showParents();
		parent = new NodeInfo("parent2Id", "10.29.45.25", "parent2", 5000);
		topic.setParent(parent);
		topic.DEBUG_showParent();
		Topic.DEBUG_showParents();
	}
	
	@Test
	public void Topic_SetParent_DontRemove_Test(){
		logger.trace("Entering test 2.........");
		NodeInfo parent = new NodeInfo("parent1Id", "10.29.45.7", "parent1", 4000);
		topic.setParent(parent);
		topic1.setParent(parent);
		topic.DEBUG_showParent();
		Topic.DEBUG_showParents();
		parent = new NodeInfo("parent2Id", "10.29.45.25", "parent2", 5000);
		topic.setParent(parent);
		topic.DEBUG_showParent();
		Topic.DEBUG_showParents();
		topic1.setParent(parent);
		topic1.DEBUG_showParent();
		Topic.DEBUG_showParents();
	}
	
	@Test
	public void Topic_AddSubscriber_Test(){
		logger.trace("Entering test 3.........");
		NodeInfo child = new NodeInfo("child1Id", "10.29.45.7", "child1", 4000);
		NodeInfo child1 = new NodeInfo("child2Id", "10.29.45.25", "child2", 6000);
		topic.addSubscriber(child);
		topic1.addSubscriber(child);
		topic1.addSubscriber(child1);
		topic.DEBUG_showChildren();
		topic1.DEBUG_showChildren();
		Topic.DEBUG_showAllChildren();
	}
	
	@Test
	public void Topic_RemoveSubscriber_Test(){
		logger.trace("Entering test 4.........");
		NodeInfo child = new NodeInfo("child1Id", "10.29.45.7", "child1", 4000);
		NodeInfo child1 = new NodeInfo("child2Id", "10.29.45.25", "child2", 6000);
		topic.addSubscriber(child);
		topic1.addSubscriber(child);
		topic1.addSubscriber(child1);
		topic.DEBUG_showChildren();
		topic1.DEBUG_showChildren();
		Topic.DEBUG_showAllChildren();
		logger.trace("And now trying to remove children....");
		topic1.removeSubscriber(child1.getID());
		topic.removeSubscriber(child.getID());
		Topic.DEBUG_showAllChildren();	
		logger.trace("Checking the romove procedure results...");
		topic1.removeSubscriber(child.getID());
		topic.DEBUG_showChildren();
		topic1.DEBUG_showChildren();
	}
	
	@Test
	public void Topic_ResetParent_Test(){
		logger.trace("Entering test 5.........");
		topic.DEBUG_showParent();
		Topic.DEBUG_showParents();
		logger.trace("Reset parent for topic '"+topic.getID()+"'.....");
		topic.setParent(null);
		topic.DEBUG_showParent();
		Topic.DEBUG_showParents();
	}
	
}
