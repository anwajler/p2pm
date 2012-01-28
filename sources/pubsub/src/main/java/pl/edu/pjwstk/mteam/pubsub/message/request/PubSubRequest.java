package pl.edu.pjwstk.mteam.pubsub.message.request;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.message.PubSubMessage;

/*Message format:
 * 
 * | 	transaction	   | 
 * |	    id	       | 
 * |--------   --------|
 * 		  4 bytes		
 * 
 */
/**
 * Abstract object representing publish-subscribe request.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public abstract class PubSubRequest extends PubSubMessage {

    /**
     * Transaction this request is associated with.
     */
    protected int transactionId;
    /**
     * Byte length of this part of message.
     */
    private int length;

    public PubSubRequest(NodeInfo src, NodeInfo dest, String topicId, int type, int transaction) {
        super(src, dest, topicId);
        setType(type);
        transactionId = transaction;
    }

    /**
     * @return ID of the transaction this message is associated with.
     */
    public int getTransactionID() {
        return transactionId;
    }

    /**
     * @return Byte length of message content.
     */
    protected int getByteLength() {
        return length;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("PubSubRequest type: ");
        sb.append(getType());
        sb.append(", topicID: ");
        sb.append(getTopicID());
        sb.append(", transID: ");
        sb.append(getTransactionID());
        sb.append(", sourceName: ");
        sb.append(getSourceInfo().getName());
        return sb.toString();
    }
    private ByteArrayOutputStream ostream;
    private DataOutputStream dtstr;
    private byte [] msgToByte;
    /**
     * Prepares response for sending.
     * @return Bytes to send.
     */
    public byte[] encode() {
        ostream = new ByteArrayOutputStream();
        dtstr = new DataOutputStream(ostream);
        msgToByte = null;
        //encoding standard header
        msgToByte = super.encode();
        try {
            //writing contents stored in Message and PubSubMessage classes
            ostream.write(msgToByte);

            //writing request specific message contents
            //writing transaction id (int32)
            dtstr.writeInt(transactionId);
            msgToByte = null;
            msgToByte = ostream.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                ostream.close();
                ostream = null;
                dtstr = null;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return msgToByte;
    }
    private ByteArrayInputStream istream;
    private DataInputStream distr;
    /**
     * Parses type-dependent message contents.
     * @param stream Received byte buffer.
     * @param offset Number of bytes reserved for headers (they will be skipped while
     * 				 parsing).
     */
    public void parse(byte[] stream, int offset) {
        istream = new ByteArrayInputStream(stream);
        distr = new DataInputStream(istream);

        //skipping bytes used for standard header
        istream.skip(offset);
        try {
            //reading transaction id
            transactionId = distr.readInt();
            length += 4;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                istream.close();
                istream = null;
                distr = null;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
