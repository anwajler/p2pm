package pl.edu.pjwstk.mteam.pubsub.message.indication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;

/**
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class KeepAliveIndication extends PubSubIndication {

        int transID;
        /*
         * The type of keep alive indication
         */
        byte keepAliveType;
        /*
         * Normal KeepAliveIndication
         */
        public static final byte KEEP_ALIVE_NORMAL = 0;
        /*
         * Used when the parent fails and we resend KeepAlive to grand-parent node
         */
        public static final byte KEEP_ALIVE_PARENT_FAILS = 1;

        public static byte ROOT_FAILS = 3;

        protected NodeInfo failedNode = new NodeInfo();
        
        public KeepAliveIndication(NodeInfo src, NodeInfo dest, String topicId) {
            super(src, dest, topicId, PubSubConstants.MSG_KEEPALIVE);
            this.transID = -1;
        }

        public KeepAliveIndication(NodeInfo src, NodeInfo dest, String topicId, int transID) {
            super(src, dest, topicId, PubSubConstants.MSG_KEEPALIVE);
            this.transID = transID;
        }
        public KeepAliveIndication(){
            super(new NodeInfo("127.0.0.1", 0), new NodeInfo("127.0.0.1", 0), "", PubSubConstants.MSG_KEEPALIVE);
        }

        public void setTransactionID(int id){
            this.transID = id;
        }
        public int getTransactionID(){
            return this.transID;
        }        
        public byte getKeepAliveType(){
            return this.keepAliveType;
        }
        public void setFailedNode(NodeInfo fail){
            this.failedNode = fail;
        }
        public NodeInfo getFailedNode(){
            return this.failedNode;
        }
    @Override
	public byte[] encode() {
        ByteArrayOutputStream ostr = new ByteArrayOutputStream();
        DataOutputStream dtstr = new DataOutputStream(ostr);
        ObjectOutputStream ostr_ = null;
        try {
            //writing header inherited from PubSubRequest object
            byte[] header = super.encode();
            dtstr.write(header);
            dtstr.writeInt(this.transID);
            dtstr.writeByte(this.keepAliveType);
            if(this.keepAliveType == KEEP_ALIVE_PARENT_FAILS){
                ostr_ = new ObjectOutputStream(ostr);
                ostr_.writeObject(this.failedNode);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
//        finally{
//            try {
//                ostr.close();
//            } catch (IOException ex) {
//               //ignore
//            }
//        }

        return ostr.toByteArray();
    }

    /**
     * Parses type-dependent message contents.
     * @param stream Received byte buffer.
     * @param offset Number of bytes reserved for headers (they will be skipped while
     * 				 parsing).
     */
    @Override
    public void parse(byte[] stream, int offset) {
        ByteArrayInputStream istream = new ByteArrayInputStream(stream);
        DataInputStream dtstr = new DataInputStream(istream);
        ObjectInputStream ostr_ = null;

        try {
            istream.skip(offset);
            //super.parse(stream, offset);
            //istream.skip(super.getByteLength());
            this.transID = dtstr.readInt();
            this.keepAliveType = dtstr.readByte();
            if(this.keepAliveType == KEEP_ALIVE_PARENT_FAILS){
                ostr_ = new ObjectInputStream(dtstr);
                this.failedNode = (NodeInfo) ostr_.readObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        finally {
//            try {
//                ostr_.close();
//            } catch (IOException ex) {
//                //ignore
//            }
//        }
    }

    public void setKeepAliveType(byte type) {
        this.keepAliveType = type;
    }
    }

