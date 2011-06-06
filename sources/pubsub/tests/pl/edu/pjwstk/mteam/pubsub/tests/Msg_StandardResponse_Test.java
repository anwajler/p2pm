package pl.edu.pjwstk.mteam.pubsub.tests;

import pl.edu.pjwstk.mteam.pubsub.logging.Logger;
import org.junit.* ;

import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.accesscontrol.AccessControlRules;
import pl.edu.pjwstk.mteam.pubsub.algorithm.implementation.DefaultAlgorithmConfigurator;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.Subscriber;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.message.PubSubMessage;
import pl.edu.pjwstk.mteam.pubsub.message.response.StandardResponse;

public class Msg_StandardResponse_Test {
	static Logger messageTestLogger;
	static Topic topic;
	
	static{
		messageTestLogger = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.tests");
		
		topic = new Topic("Plants");
		Subscriber towner = new Subscriber("gardener", topic);
		topic.setOwner(towner);
		
		/*chooseAlgorithm will register appropriate messages for the algorithm,
		 * so it has to be called here
		 */
		DefaultAlgorithmConfigurator alg = new DefaultAlgorithmConfigurator();
		alg.chooseAlgorithm("whatever");
	}
	
	@Test
	public void test_Response_IPv4_createTopic(){
		StandardResponse resp = new StandardResponse(7000, PubSubConstants.RESP_ALREADYEXISTS, 
				 new NodeInfo("peer id", "10.69.40.111", 
				 "tiia", 9070),
				 new NodeInfo("peer id2", "10.69.40.222", 
				 "tiia2", 9072), "Software developement");
        byte[] msg = resp.encode();
        
        PubSubMessage received = PubSubMessage.parseMessage(msg);
        
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
        
        messageTestLogger.trace("\nPublish-subscribe response message data:"+"\n"+
		                        "\tTransaction id = "+((StandardResponse)received).getTransactionID()+"\n"+
		                        "\tResponse code = "+((StandardResponse)received).getResponseCode());
	}

	@Test
	public void test_Response_IPv4_subscribe(){
		AccessControlRules ac = new AccessControlRules(topic);
		StandardResponse resp = new StandardResponse(7000, 
				 new NodeInfo("peer id", "10.69.40.111", 
				 "tiia", 9070),
				 new NodeInfo("peer id2", "10.69.40.222", 
				 "tiia2", 9072), "Software developement",
				 ac);
        byte[] msg = resp.encode();
        
        PubSubMessage received = PubSubMessage.parseMessage(msg);
        
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
        
        messageTestLogger.trace("\nPublish-subscribe response message data:"+"\n"+
		                        "\tTransaction id = "+((StandardResponse)received).getTransactionID()+"\n"+
		                        "\tResponse code = "+((StandardResponse)received).getResponseCode()+"\n"+
        			            "\tAC rules:"+((StandardResponse)received).getAccessControlRules());
	}

}
