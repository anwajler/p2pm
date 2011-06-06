package pl.edu.pjwstk.mteam.pubsub.message.response;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.message.PubSubMessage;

/*Message format:
 * 
 * | 	transaction	   | 	  response	   |
 * |	    id	       |  	    code	   |	  
 * |--------   --------|--------   --------|
 * 		  4 bytes				 4
 * 
 */

/**
 * Abstract object representing publish-subscribe response.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public abstract class PubSubResponse extends PubSubMessage{
	
	protected static final int LENGTH = 8;
	
	/**
	 * Transaction this response is associated with.
	 */
	protected int transactionId;
	/**
	 * Response code.
	 */
	protected int responseCode;
	
	public PubSubResponse(NodeInfo src, NodeInfo dest, String topicId){
		super(src, dest, topicId);
	}
	
	/**
	 * @return Response code.
	 */
	public int getResponseCode(){
		return responseCode;
	}
	
	/**
	 * @return ID of the transaction this message is associated with.
	 */
	public int getTransactionID(){
		return transactionId;
	}
	
	/**
	 * @return Byte length of objects inside response.
	 */
	protected static int getByteLength(){
		return LENGTH;
	}
	
	/**
	 * Prepares response for sending.
	 * @return Bytes to send.
	 */
	public byte[] encode(){
		ByteArrayOutputStream ostream = new ByteArrayOutputStream();
		DataOutputStream dtstr = new DataOutputStream(ostream);
		
		//encoding standard header
		byte[] header = super.encode();
		try {
			//writing contents stored in Message and PubSubMessage classes
			ostream.write(header);
			
			//writing response specific message contents
			
			//writing transaction id (int32)
			dtstr.writeInt(transactionId);
			//writing response code (int32)
			dtstr.writeInt(responseCode);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return ostream.toByteArray();
	}
	
	/**
	 * Parses type-dependent message contents.
	 * @param stream Received byte buffer.
	 * @param offset Number of bytes reserved for headers (they will be skipped while 
	 * 				 parsing).
	 */
	public void parse(byte[] stream, int offset){
		ByteArrayInputStream istream = new ByteArrayInputStream(stream);
		DataInputStream dtstr = new DataInputStream(istream);
		
		//skipping bytes used for standard header
		istream.skip(offset);
		try {
			//reading transaction id
			transactionId = dtstr.readInt();
			//reading response code
			responseCode = dtstr.readInt();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
