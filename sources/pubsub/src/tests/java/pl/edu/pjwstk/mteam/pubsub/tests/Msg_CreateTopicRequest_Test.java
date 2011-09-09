package pl.edu.pjwstk.mteam.pubsub.tests;

import java.util.Iterator;
import java.util.Vector;

import pl.edu.pjwstk.mteam.pubsub.logging.Logger;
import org.junit.* ;

import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.accesscontrol.AccessControlRules;
import pl.edu.pjwstk.mteam.pubsub.algorithm.implementation.DefaultAlgorithmConfigurator;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.Subscriber;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.core.User;
import pl.edu.pjwstk.mteam.pubsub.message.PubSubMessage;
import pl.edu.pjwstk.mteam.pubsub.message.request.CreateTopicRequest;

public class Msg_CreateTopicRequest_Test {
	static Logger logger = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.tests");
	static Logger messageTestLogger;
	static Topic topic; 
	static AccessControlRules rules;
	
	static{
		messageTestLogger = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.tests");
		
		topic = new Topic("Plants");
		Subscriber towner = new Subscriber("gardener", topic);
		topic.setOwner(towner);
		messageTestLogger.trace("\nCreated topic....."+topic);
		
		rules = new AccessControlRules(topic);
		messageTestLogger.trace("\nAC rules for the topic....."+topic.getAccessControlRules());
	    
		/*chooseAlgorithm will register appropriate messages for the algorithm,
		 * so it has to be called here
		 */
		DefaultAlgorithmConfigurator alg = new DefaultAlgorithmConfigurator();
		alg.chooseAlgorithm("whatever");
	}
	
	@Test
	public void test_Message_IPv4_newTopic(){
		CreateTopicRequest req = new CreateTopicRequest(
				 new NodeInfo("peer id", "10.69.40.111", 
				 "tiia", 9070),
				 new NodeInfo("peer id2", "10.69.40.222", 
				 "tiia2", 9072), "Software developement", rules, 
				 PubSubConstants.CREATETOPICFLAG_NEWTOPIC, 7000);
		
		byte[] msgbytes = req.encode();
		
		PubSubMessage received = PubSubMessage.parseMessage(msgbytes);
		
		messageTestLogger.trace("\nGeneral message data:\n"+
		                        "\tIP version = "+received.getIPVersion()+"\n"+
		                        "\tSource IP = "+received.getSourceInfo().getIP()+"\n"+
		                        "\tSource port = "+received.getSourceInfo().getPort()+"\n"+
		                        "\tDestination IP = "+received.getDestinationInfo().getIP()+"\n"+
		                        "\tDestination port = "+received.getDestinationInfo().getPort());
		
		messageTestLogger.trace("\nPublish-subscribe general message data:"+
		                        "\tMessage type = "+received.getType()+"\n"+
		                        "\tSource user name = "+received.getSourceInfo().getName()+"\n"+
		                        "\tSource peer id = "+received.getSourceInfo().getID()+"\n"+
		                        "\tDestination user name = "+received.getDestinationInfo().getName()+"\n"+
		                        "\tDestination peer id = "+received.getDestinationInfo().getID()+"\n"+
		                        "\tTopic id = "+received.getTopicID());
		
		messageTestLogger.trace("\nPublish-subscribe create topic message data:"+"\n"+
		                        "\tTransaction id = "+((CreateTopicRequest)received).getTransactionID()+"\n"+
		                        "\tFlag: "+((CreateTopicRequest)received).getFlag()+"\n"+
		                        "\tAC: "+((CreateTopicRequest)received).getAccessRules());
	}
	
	@Test
	public void test_Message_IPv6_transferTopic(){
		CreateTopicRequest req = new CreateTopicRequest(
				 new NodeInfo("peer id",  "fe80::5efe:10.69.40.111", 
				 "tiia", 9070),
				 new NodeInfo("peer id2", "fe80::5efe:10.69.40.111", 
				 "tiia2", 9072), "Software developement", rules, 
				 PubSubConstants.CREATETOPICFLAG_TRANSFERTOPIC, 6000);
		
		byte[] msgbytes = req.encode();
		
		PubSubMessage received = PubSubMessage.parseMessage(msgbytes);
		
		messageTestLogger.trace("\nGeneral message data:\n"+
		                        "\tIP version = "+received.getIPVersion()+"\n"+
		                        "\tSource IP = "+received.getSourceInfo().getIP()+"\n"+
		                        "\tSource port = "+received.getSourceInfo().getPort()+"\n"+
		                        "\tDestination IP = "+received.getDestinationInfo().getIP()+"\n"+
		                        "\tDestination port = "+received.getDestinationInfo().getPort());
		
		messageTestLogger.trace("\nPublish-subscribe general message data:"+
		                        "\tMessage type = "+received.getType()+"\n"+
		                        "\tSource user name = "+received.getSourceInfo().getName()+"\n"+
		                        "\tSource peer id = "+received.getSourceInfo().getID()+"\n"+
		                        "\tDestination user name = "+received.getDestinationInfo().getName()+"\n"+
		                        "\tDestination peer id = "+received.getDestinationInfo().getID()+"\n"+
		                        "\tTopic id = "+received.getTopicID());
		
		messageTestLogger.trace("\nPublish-subscribe create topic message data:"+"\n"+
		                        "\tTransaction id = "+((CreateTopicRequest)received).getTransactionID()+"\n"+
		                        "\tFlag: "+((CreateTopicRequest)received).getFlag()+"\n"+
		                        "\tAC: "+((CreateTopicRequest)received).getAccessRules());
	}
	
	@Test
	public void test_Message_newTopic_ContainingSubscribers(){
		CreateTopicRequest req = new CreateTopicRequest(
				 new NodeInfo("peer id", "10.69.40.111", 
				 "tiia", 9070),
				 new NodeInfo("peer id2", "10.69.40.222", 
				 "tiia2", 9072), "Software developement", rules, 
				 PubSubConstants.CREATETOPICFLAG_NEWTOPIC, 7000);
		User usr_paulina = new User(new NodeInfo("76543", "10.69.40.111", 
				                                 "paulina", 9070));
		req.addSubscriber(usr_paulina);
		byte[] msgbytes = req.encode();
		
		PubSubMessage received = PubSubMessage.parseMessage(msgbytes);
		Vector<User> subscribers = ((CreateTopicRequest)received).getSubscribers();
		String output = "\nSubscribers to be added to the topic: \n";
		Iterator<User> it = subscribers.iterator();
		while(it.hasNext()){
			output += "- "+it.next()+"\n";
		}
		logger.trace(output);
	}
}
