package pl.edu.pjwstk.mteam.pubsub.message.request;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.accesscontrol.AccessControlRules;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.core.User;

/**
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class CreateTopicRequest extends PubSubRequest{
	
	/**
	 * AC rules defined for this topic.
	 */
	private AccessControlRules ac;
	
	/**
	 * List of subscribers to be added after a successful topic creation.
	 */
	private Vector<User> subscribers;

	/**
	 * If the value is 0 - this message is associated with creating new topic.
	 * If it is 1 - this is only topic transfer.
	 */
	private byte subtypeFlag;
	
	/**
	 * Creates new subscribe request with default parameters. Used only by PubSubMessage 
	 * class.
	 */
	public CreateTopicRequest(){
		super(new NodeInfo("127.0.0.1", 0), new NodeInfo("127.0.0.1", 0), "", PubSubConstants.MSG_CREATETOPIC, 0);
		ac = new AccessControlRules(new Topic(""));
		subscribers = new Vector<User>();
	}
	
	public CreateTopicRequest(NodeInfo src, NodeInfo dest, String topicId, byte flag, int transaction){
		super(src, dest, topicId, PubSubConstants.MSG_CREATETOPIC, transaction);
		ac = new AccessControlRules(new Topic(topicId));
		subtypeFlag = flag;
		subscribers = new Vector<User>();
	}
	
	public CreateTopicRequest(NodeInfo src, NodeInfo dest, String topicId, AccessControlRules acRules, byte flag, int transaction) {
		super(src, dest, topicId, PubSubConstants.MSG_CREATETOPIC, transaction);
		ac = acRules;
		subtypeFlag = flag;
		subscribers = new Vector<User>();
	}
	
	public AccessControlRules getAccessRules(){
		return ac;
	}
	
	/**
	 * 
	 * @return If the value is 0 - this message is associated with creating new topic.
	 *         If it is 1 - this is only topic transfer.
	 */
	public byte getFlag(){
		return subtypeFlag;
	}
	
	public Vector<User> getSubscribers(){
		return subscribers;
	}
	
	public void addSubscriber(User user){
		subscribers.add(user);
	}
	
	public void addSubscribers(Vector<User> users){
		Iterator<User> it = users.iterator();
		while(it.hasNext()){
			subscribers.add(it.next());
		}
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
			dtstr.writeByte(subtypeFlag);
			//writing access control rule set 
			byte[] acbytes = ac.encode();
			//writing AC byte length
			dtstr.writeInt(acbytes.length);
			//writing AC object itself
			dtstr.write(acbytes);
			//writing the subscribers list length
			dtstr.writeInt(subscribers.size());
			for(int i=0; i<subscribers.size(); i++){
				byte[] encuser = subscribers.get(i).encode();
				dtstr.writeInt(encuser.length);
				dtstr.write(encuser);
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
			subtypeFlag = dtstr.readByte();
			byte[] encAC = new byte[dtstr.readInt()];
			dtstr.read(encAC);
			ac = new AccessControlRules(encAC);
			subscribers = new Vector<User>();
			//reading the subscribers list length
			int subscribersListLength = dtstr.readInt();
			for(int i=0; i<subscribersListLength; i++){
				byte[] encuser = new byte[dtstr.readInt()];
				dtstr.read(encuser);
				subscribers.add(i, new User(encuser));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
