package pl.edu.pjwstk.mteam.pubsub.message.indication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.message.PubSubMessage;

/* 
 * Currently there are no specific fields. Subclass was created to provide structure
 * for possible future extensions.
 */

/**
 * Abstract object representing publish-subscribe indication.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public abstract class PubSubIndication extends PubSubMessage{
	/**
	 * Byte length of this part of message.
	 */
	private int length;
	
	public PubSubIndication(NodeInfo src, NodeInfo dest, String topicId, short type){
		super(src, dest, topicId);
		setType(type);
	}
	
	/**
	 * @return Byte length of message content.
	 */
	protected int getByteLength(){
		return length;
	}
	
	/**
	 * Prepares response for sending.
	 * @return Bytes to send.
	 */
	public byte[] encode(){
		ByteArrayOutputStream ostream = new ByteArrayOutputStream();
		
		//encoding standard header
		byte[] header = super.encode();
		try {
			//writing contents stored in Message and PubSubMessage classes
			ostream.write(header);

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
		length =0;
	}
}
