package pl.edu.pjwstk.mteam.pubsub.tests;

import pl.edu.pjwstk.mteam.pubsub.logging.Logger;
import org.junit.Test;

import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.algorithm.implementation.DefaultAlgorithmConfigurator;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.User;
import pl.edu.pjwstk.mteam.pubsub.message.PubSubMessage;
import pl.edu.pjwstk.mteam.pubsub.message.indication.NotifyIndication;

public class Msg_NotifyIndication_Test {
	static Logger messageTestLogger;
	
	static{
		messageTestLogger = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.tests");
		
		/*chooseAlgorithm will register appropriate messages for the algorithm,
		 * so it has to be called here
		 */
		DefaultAlgorithmConfigurator alg = new DefaultAlgorithmConfigurator();
		alg.chooseAlgorithm("whatever");
	}
	
	@Test
	public void test_Notify_removeTopic(){
		/*
		 * In case of AC modification message contains byte representation of new AC 
		 * rules (ac.encode() method may be used)
		 */
		NotifyIndication req = new NotifyIndication(
				 new NodeInfo("peer id", "10.69.40.111", 
				 "tiia", 9070),
				 new NodeInfo("peer id2", "10.69.40.222", 
				 "tiia2", 9072), "Software developement",
				 PubSubConstants.EVENT_REMOVETOPIC, null, true,
				 new User("tiia"));
		
		byte[] msgbytes = req.encode();
		
		PubSubMessage received = PubSubMessage.parseMessage(msgbytes);
		messageTestLogger.debug("\nGeneral message data:\n"+
		                        "\tIP version = "+received.getIPVersion()+"\n"+
		                        "\tSource IP = "+received.getSourceInfo().getIP()+"\n"+
		                        "\tSource port = "+received.getSourceInfo().getPort()+"\n"+
		                        "\tDestination IP = "+received.getDestinationInfo().getIP()+"\n"+
		                        "\tDestination port = "+received.getDestinationInfo().getPort());
		
		messageTestLogger.debug("\nPublish-subscribe general message data:"+
		                        "\tMessage type = "+received.getType()+"\n"+
		                        "\tSource user name = "+received.getSourceInfo().getName()+"\n"+
		                        "\tSource peer id = "+received.getSourceInfo().getID()+"\n"+
		                        "\tDestination user name = "+received.getDestinationInfo().getName()+"\n"+
		                        "\tDestination peer id = "+received.getDestinationInfo().getID()+"\n"+
		                        "\tTopic id = "+received.getTopicID());
		messageTestLogger.debug("\nPublish-subscribe notify message data:"+"\n"+
				                "\tIs Historical = "+((NotifyIndication)received).isHistorical()+"\n"+
                                "\tEvent type = "+PubSubConstants.STR_EVENT[((NotifyIndication)received).getEventType()]+"\n"+
                                "\tPublisher's name = "+((NotifyIndication)received).getPublisher()+"\n"+
                                "\tMessage = "+new String(((NotifyIndication)received).getMessage()));
	}
	
	@Test
	public void test_Notify_custom(){
		String eventMessage = "Test message";
		NotifyIndication req = new NotifyIndication(
				 new NodeInfo("peer id", "10.69.40.111", 
				 "tiia", 9070),
				 new NodeInfo("peer id2", "10.69.40.222", 
				 "tiia2", 9072), "Software developement",
				 PubSubConstants.EVENT_CUSTOM, eventMessage.getBytes(), false,
				 new User("ziuta"));
		
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
		messageTestLogger.trace("\nPublish-subscribe notify message data:"+"\n"+
				                "\tIs Historical = "+((NotifyIndication)received).isHistorical()+"\n"+
                                "\tEvent type = "+PubSubConstants.STR_EVENT[((NotifyIndication)received).getEventType()]+"\n"+
                                "\tPublisher's name = "+((NotifyIndication)received).getPublisher()+"\n"+
                                "\tMessage = "+new String(((NotifyIndication)received).getMessage()));
	}
}
