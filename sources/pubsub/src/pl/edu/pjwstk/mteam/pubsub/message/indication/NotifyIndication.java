package pl.edu.pjwstk.mteam.pubsub.message.indication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.User;

/**
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class NotifyIndication extends PubSubIndication {
	int length;
	byte eventType;
	User publisher;
	boolean isHistorical;
	byte[] message;
        int operationID;
	
	public NotifyIndication(){
		super(new NodeInfo("127.0.0.1", 0), new NodeInfo("127.0.0.1", 0), "", PubSubConstants.MSG_NOTIFY);
	}
	
	/**
	 * 
	 * @param src
	 * @param dest
	 * @param topicId
     * @param event Acceptable values are:<p>
	 *              <li>{@link PubSubConstants#EVENT_CUSTOM},
	 *              <li>{@link PubSubConstants#EVENT_MODIFYAC},
	 *              <li>{@link PubSubConstants#EVENT_REMOVETOPIC},
	 * @param msg For {@link PubSubConstants#EVENT_REMOVETOPIC} may be <code>null</code>.
	 *            For {@link PubSubConstants#EVENT_MODIFYAC} must be encoded new AC rules.
	 * @param historical Value indicating, whether this event is new or historical
	 * @param publ Event publisher
	 */
	public NotifyIndication(NodeInfo src, NodeInfo dest, String topicId,
			                byte event, byte[] msg, boolean historical, User publ, int operationID){
		super(src, dest, topicId, PubSubConstants.MSG_NOTIFY);
		eventType = event;
		publisher = publ;
		message = msg;
		isHistorical = historical;
                this.operationID = operationID;
	}
	
	/**
	 * @return Byte length of message content.
	 */
	protected int getByteLength(){
		return super.getByteLength()+length;
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
	
	public boolean isHistorical(){
		return isHistorical;
	}
        public int getOperationID(){
            return this.operationID;
        }
	
	/**
	 * Prepares create topic request for sending.
	 * @return Bytes to send.
	 */
	public byte[] encode(){
		ByteArrayOutputStream ostr = new ByteArrayOutputStream();
		DataOutputStream dtstr = new DataOutputStream(ostr);
		
		try {
			//writing header inherited from PubSubIndication object
			byte[] header = super.encode();
			dtstr.write(header);
			dtstr.writeBoolean(isHistorical);
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
                        dtstr.writeInt(this.operationID);
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
			isHistorical = dtstr.readBoolean();
			length += 1;
			eventType = dtstr.readByte();
			length += 1;
			int pubNameLength = dtstr.readInt();
			length += 4;
			byte[] pubName = new byte[pubNameLength];
			dtstr.read(pubName);
			length += pubNameLength;
			publisher = new User(new String(pubName));
			message = new byte[dtstr.readInt()];
			length += 4;
			dtstr.read(message);
			length += message.length;
                        this.operationID = dtstr.readInt();
                        length+=4;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("EventType: ").append(this.eventType);
        sb.append(", Publisher: ").append(this.publisher.getNodeInfo().getName());
        sb.append(", opID: ").append(this.operationID);
        sb.append(", message: ");
        sb.append(new String(this.message));
        return sb.toString();
    }


}
