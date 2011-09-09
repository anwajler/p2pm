/**
 * 
 */
package pl.edu.pjwstk.mteam.pubsub.message.response;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import pl.edu.pjwstk.mteam.core.NodeInfo;

import pl.edu.pjwstk.mteam.pubsub.accesscontrol.AccessControlRules;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;


/**
 * Class representing standard response for publish-subscribe request.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class StandardResponse extends PubSubResponse{
	/**
	 * Used only for OK response to subscribe request.
	 */
	private AccessControlRules acRules;
	
	/**
	 * Creates new response with default parameters. Used only by PubSubMessage class.
	 */
	public StandardResponse(){
		super(new NodeInfo("127.0.0.1", 0), new NodeInfo("127.0.0.1", 0), "");
		setType(PubSubConstants.MSG_STDRESPONSE);
	}
	
	/**
	 * Creates new response with specified parameters.
	 * @param transId Transaction this response is associated with.
	 * @param respCode Response code.
	 * @param src Message sender.
	 * @param dest Message destination.
	 * @param topicID ID of the topic this response is associated with.
	 */
	public StandardResponse(int transId, int respCode, NodeInfo src, NodeInfo dest, String topicID){
		super(src, dest, topicID);
		transactionId = transId;
		responseCode = respCode;
		setType(PubSubConstants.MSG_STDRESPONSE);
	}
	
	/**
	 * Creates new OK response for subscribe request.
	 * @param transId Transaction this response is associated with.
	 * @param src Message sender.
	 * @param dest Message destination.
	 * @param topicID ID of the topic this response is associated with.
	 * @param ac AC rules for the topic
	 */
	public StandardResponse(int transId, NodeInfo src, NodeInfo dest, 
			                String topicID, AccessControlRules ac){
		super(src, dest, topicID);
		transactionId = transId;
		responseCode = PubSubConstants.RESP_SUCCESS;
		setType(PubSubConstants.MSG_STDRESPONSE);
		acRules = ac;
	}
	
	/**
	 * 
	 * @return When this message is 200 response for subscribe request - the value is
	 *         AC rule set defined for the specified topic. Otherwise it is <code>null
	 *         </code> 
	 */
	public AccessControlRules getAccessControlRules(){
		return acRules;
	}
	
	/**
	 * Prepares response for sending.
	 * @return Bytes to send.
	 */
	public byte[] encode(){
		ByteArrayOutputStream ostr = new ByteArrayOutputStream();
		DataOutputStream dtstr = new DataOutputStream(ostr);
		
		try {
			//writing header inherited from PubSubRequest object
			byte[] header = super.encode();
			dtstr.write(header);
			if(acRules != null){
				byte[] encodedRules = acRules.encode();
				dtstr.writeInt(encodedRules.length);
				//writing access control rule set 
				dtstr.write(encodedRules);
			}
			else{
				dtstr.writeInt(0);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ostr.toByteArray();
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
		
		try {
			istream.skip(offset);
			super.parse(stream, offset);
			istream.skip(super.getByteLength());
			int acLength = dtstr.readInt();
			if(acLength >0){
				byte[] encAC = new byte[acLength];
				dtstr.read(encAC);
				acRules = new AccessControlRules(encAC);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
