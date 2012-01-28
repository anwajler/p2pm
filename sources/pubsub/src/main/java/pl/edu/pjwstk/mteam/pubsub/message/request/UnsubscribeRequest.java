package pl.edu.pjwstk.mteam.pubsub.message.request;

import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;

/* 
 * Currently there are no specific fields. Subclass was created to provide structure
 * for possible future extensions.
 */

/**
 * Class representing unsubscribe request.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class UnsubscribeRequest extends PubSubRequest{
	
	/**
	 * Creates new unsubscribe request with default parameters. Used only by PubSubMessage 
	 * class.
	 */
	public UnsubscribeRequest(){
		super(new NodeInfo("127.0.0.1", 0), new NodeInfo("127.0.0.1", 0), "", PubSubConstants.MSG_UNSUBSCRIBE, 0);
	}
	
	/**
	 * Creates new unsubscribe request with specified parameters.
	 * @param transId Transaction this response is associated with.
	 * @param src Message sender.
	 * @param dest Message destination.
	 * @param topicID ID of the topic this response is associated with.
	 */
	public UnsubscribeRequest(int transId, NodeInfo src, NodeInfo dest, String topicID){
		super(src, dest, topicID, PubSubConstants.MSG_UNSUBSCRIBE, transId);
		transactionId = transId;
	}
	
	/**
	 * Prepares unsubscribe request for sending.
	 * @return Bytes to send.
	 */
	public byte[] encode(){
		return super.encode();
	}
	
	/**
	 * Parses type-dependent message contents.
	 * @param stream Received byte buffer.
	 * @param offset Number of bytes reserved for headers (they will be skipped while 
	 * 				 parsing).
	 */
	public void parse(byte[] stream, int offset){
		super.parse(stream, offset);
	}
        @Override
        public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("UnsubscribeRequest");
        sb.append(", topicID: ");
        sb.append(getTopicID());
        sb.append(", transID: ");
        sb.append(getTransactionID());
        sb.append(", sourceName: ");
        sb.append(getSourceInfo().getName());
        return sb.toString();
    }
}
