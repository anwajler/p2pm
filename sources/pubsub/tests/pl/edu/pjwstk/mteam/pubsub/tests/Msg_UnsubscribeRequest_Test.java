package pl.edu.pjwstk.mteam.pubsub.tests;

import pl.edu.pjwstk.mteam.pubsub.logging.Logger;
import org.junit.Test;

import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.algorithm.implementation.DefaultAlgorithmConfigurator;
import pl.edu.pjwstk.mteam.pubsub.message.PubSubMessage;
import pl.edu.pjwstk.mteam.pubsub.message.request.UnsubscribeRequest;


public class Msg_UnsubscribeRequest_Test {
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
	public void test_Unsubscribe_IPv4(){
		UnsubscribeRequest req = new UnsubscribeRequest(
				 7000,
				 new NodeInfo("peer id", "10.69.40.111", 
				 "tiia", 9070),
				 new NodeInfo("peer id2", "10.69.40.222", 
				 "tiia2", 9072), "Software developement");
		
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
		messageTestLogger.trace("\nPublish-subscribe unsubscribe message data:"+"\n"+
                                "\tTransaction id = "+((UnsubscribeRequest)received).getTransactionID());
		
	}
}
