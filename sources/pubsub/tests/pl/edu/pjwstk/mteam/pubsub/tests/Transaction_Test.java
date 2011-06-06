package pl.edu.pjwstk.mteam.pubsub.tests;

import org.junit.*;

import pl.edu.pjwstk.mteam.pubsub.core.Event;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.Subscriber;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.core.Transaction;
import pl.edu.pjwstk.mteam.pubsub.logging.Logger;
import pl.edu.pjwstk.mteam.pubsub.operation.CreateTopicOperation;

public class Transaction_Test {
	static Logger logger = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.tests");
	
	static Topic topic;
	static Subscriber owner;
	
	static{
		topic = new Topic("Plants");
		owner = new Subscriber("owner", topic);
	}
	
	public void generateTransaction(){
		Event e = new Event(PubSubConstants.EVENT_ALL);
		CreateTopicOperation o = new CreateTopicOperation(topic.getID(),
				                                          owner,
				                                          e);
		Transaction t = new Transaction(o);
		logger.trace("New transaction id = "+t.getID());
	}
	
	@Test
	public void generateSeveralIDs_Test(){
		for(int i=0; i<5; i++){
			generateTransaction();
		}
	}

}
