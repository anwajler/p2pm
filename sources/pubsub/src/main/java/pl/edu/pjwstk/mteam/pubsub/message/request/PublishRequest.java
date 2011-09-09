package pl.edu.pjwstk.mteam.pubsub.message.request;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.User;
import pl.edu.pjwstk.mteam.pubsub.logging.Logger;

/**
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class PublishRequest extends PubSubRequest{
	private static Logger logger = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.message.request.PublishRequest");
	
	private byte eventType;
	private User publisher;
	private byte[] message;

	public PublishRequest(){
		super(new NodeInfo("127.0.0.1", 0), new NodeInfo("127.0.0.1", 0), "", PubSubConstants.MSG_PUBLISH, 0);
		eventType = PubSubConstants.EVENT_CUSTOM;
	}
	
	/**
	 * 
	 * @param src
	 * @param dest
	 * @param topicId
	 * @param transaction
	 * @param event Acceptable values are:<p>
	 *              <li>{@link PubSubConstants#EVENT_CUSTOM},
	 *              <li>{@link PubSubConstants#EVENT_MODIFYAC},
	 *              <li>{@link PubSubConstants#EVENT_REMOVETOPIC},
	 * @param msg For {@link PubSubConstants#EVENT_REMOVETOPIC} may be <code>null</code>.
	 *            For {@link PubSubConstants#EVENT_MODIFYAC} must be encoded new AC rules.
	 */
	public PublishRequest(int transaction, NodeInfo src, NodeInfo dest, 
			              String topicId, byte event, byte[] msg, User publ){
		super(src, dest, topicId, PubSubConstants.MSG_PUBLISH, transaction);
		eventType = event;
		publisher = publ;
		message = msg;
	}

	public byte getEventType(){
		return eventType;
	}
	
	public String getPublisher(){
		return publisher.getNodeInfo().getName();
	}
	
	public byte[] getMessage(){
		return message;
	}
	
	/**
	 * Prepares create topic request for sending.
	 * @return Bytes to send.
	 */
	public byte[] encode(){
		ByteArrayOutputStream ostr = new ByteArrayOutputStream();
		DataOutputStream dtstr = new DataOutputStream(ostr);
		
		try {
			//writing header inherited from PubSubRequest object
			byte[] header = super.encode();
			dtstr.write(header);
			dtstr.writeByte(eventType);
			dtstr.writeInt(publisher.getNodeInfo().getName().length());
			dtstr.write(publisher.getNodeInfo().getName().getBytes());
			if(message != null){
				dtstr.writeInt(message.length);
				dtstr.write(message);
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
			eventType = dtstr.readByte();
			int pubNameLength = dtstr.readInt();
			byte[] pubName = new byte[pubNameLength];
			dtstr.read(pubName);
			publisher = new User(new String(pubName));
			message = new byte[dtstr.readInt()];
			dtstr.read(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
