package pl.edu.pjwstk.mteam.jcsync.core.pubsub;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.message.response.StandardResponse;

/**
 *
 * @author Piotr Bucior
 */

public class StandardResponse_ extends StandardResponse{
    public static final byte MSG_STDRESPONSE_ = 25;
    private long requestID = -1;
    public StandardResponse_(){
		super();
		setType(MSG_STDRESPONSE_);
	}
	
	/**
	 * Creates new response with specified parameters.
	 * @param transId Transaction this response is associated with.
	 * @param respCode Response code.
	 * @param src Message sender.
	 * @param dest Message destination.
	 * @param topicID ID of the topic this response is associated with.
	 */
	public StandardResponse_(int transId, int respCode, NodeInfo src, NodeInfo dest, String topicID, long reqID){
		super(transId, respCode, src, dest, topicID);
		this.requestID = reqID;
		setType(MSG_STDRESPONSE_);
	}
        public byte[] encode(){
		ByteArrayOutputStream ostr = new ByteArrayOutputStream();
		DataOutputStream dtstr = new DataOutputStream(ostr);
		
		try {
			//writing header inherited from PubSubRequest object
			byte[] header = super.encode();
			dtstr.write(header);
			dtstr.writeLong(this.requestID);
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
			this.requestID = dtstr.readLong();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
        public long getGlobalRequestID(){
            return this.requestID;
        }
}
