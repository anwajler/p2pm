package pl.edu.pjwstk.mteam.jcsync.core.pubsubwrapper;

import pl.edu.pjwstk.mteam.jcsync.core.JCSyncConstans;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Vector;
import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncCreateCollectionIndication;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncCreateCollectionRequest;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncGeneralResponse;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncGetConstructorRequest;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncInvokeMethodIndication;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncInvokeMethodRequest;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncMessage;
import pl.edu.pjwstk.mteam.pubsub.accesscontrol.AccessControlRules;
import pl.edu.pjwstk.mteam.pubsub.core.User;
import pl.edu.pjwstk.mteam.pubsub.message.request.PubSubRequest;

/**
 * @author Piotr Bucior
 * @version 1.0
 */
public class JCSyncMessageCarrier extends PubSubRequest {

    static byte b = 0;
    private short jcsyncInternalType = 0;
    private JCSyncMessage internalMessage;
    private Vector<User> subscribers = new Vector<User>();
    private AccessControlRules ac;

    private JCSyncMessageCarrier(NodeInfo src, NodeInfo dst, String collectionID, short type, int transID) {
        super(src, dst, collectionID, JCSyncConstans.JCSYNC_GENERIC_MESSAGE, transID);
        this.jcsyncInternalType = type;
    }

    public JCSyncMessageCarrier() {
        super(new NodeInfo("127.0.0.1", 0), new NodeInfo("127.0.0.1", 0), "", JCSyncConstans.JCSYNC_GENERIC_MESSAGE, 0);
        this.jcsyncInternalType = JCSyncConstans.JCSYNC_GENERIC_MESSAGE;
    }

    public JCSyncMessageCarrier(JCSyncMessage iM) {
        super((NodeInfo) iM.getSourceInfo(), (NodeInfo) iM.getDestinationInfo(), iM.getCollectionID(), JCSyncConstans.JCSYNC_GENERIC_MESSAGE, iM.getTransactionID());
        this.internalMessage = iM;
        this.jcsyncInternalType = iM.getMessageType();
    }
    public JCSyncMessageCarrier(JCSyncMessage iM, AccessControlRules ac) {
        super((NodeInfo) iM.getSourceInfo(), (NodeInfo) iM.getDestinationInfo(), iM.getCollectionID(), JCSyncConstans.JCSYNC_GENERIC_MESSAGE, iM.getTransactionID());
        this.internalMessage = iM;
        this.jcsyncInternalType = iM.getMessageType();
        this.ac = ac;
    }

    @Override
    protected void finalize() throws Throwable {
        this.internalMessage = null;
        this.subscribers = null;
        this.ac = null;
        super.finalize();
    }


    public short getMessageType() {
        return this.getEventType();
    }

    public AccessControlRules getAccessRules() {
        return ac;
    }

    public Vector<User> getSubscribers() {
        return subscribers;
    }

    public void addSubscriber(User user) {
        subscribers.add(user);
    }

    public void addSubscribers(Vector<User> users) {
        Iterator<User> it = users.iterator();
        while (it.hasNext()) {
            subscribers.add(it.next());
        }
    }
    protected ByteArrayOutputStream ostr;
    protected DataOutputStream dtstr;
    protected ObjectOutputStream ostr_;
    byte[] header;

    @Override
    public byte[] encode() {
        ostr = new ByteArrayOutputStream();
        dtstr = new DataOutputStream(ostr);
        ostr_ = null;
        try {
            //writing header inherited from PubSubRequest object
            header = super.encode();
            dtstr.write(header);
            dtstr.writeShort(this.jcsyncInternalType);
            header = null;
            if(ac != null){
				byte[] encodedRules = ac.encode();
				dtstr.writeInt(encodedRules.length);
				//writing access control rule set
				dtstr.write(encodedRules);
			}
			else{
				dtstr.writeInt(0);
			}
            header = this.internalMessage.encodeBody();
            dtstr.write(header);
            header = null;
            header = ostr.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally{
            try {
                ostr.close();
                dtstr = null;
                ostr = null;
                ostr_ = null;
            } catch (IOException ex) {
               //ignore
            }
        }
        
        return header;
    }
    private ByteArrayInputStream istream;
    private DataInputStream distr;
    private ObjectInputStream oistr;
    int acLength;
    /**
     * Parses type-dependent message contents.
     * @param stream Received byte buffer.
     * @param offset Number of bytes reserved for headers (they will be skipped while
     * 				 parsing).
     */
    @Override
    public void parse(byte[] stream, int offset) {
        istream = new ByteArrayInputStream(stream);
        distr = new DataInputStream(istream);
        oistr = null;
        try {
            istream.skip(offset);
            super.parse(stream, offset);
            offset += super.getByteLength();
            istream.skip(super.getByteLength());
            this.jcsyncInternalType = distr.readShort();
            offset += 2;
            switch (this.jcsyncInternalType) {
                case JCSyncConstans.JCSYNC_CREATE_COLLECTION_REQUEST: {
                    this.internalMessage = new JCSyncCreateCollectionRequest(getSourceInfo(), getDestinationInfo(), getTopicID(), getTransactionID(),"...");
                    //this.internalMessage.decodeBody(stream, offset);
                    break;
                }
                case JCSyncConstans.JCSYNC_INVOKE_WRITE_METHOD_REQ: {
                    this.internalMessage = new JCSyncInvokeMethodRequest(getSourceInfo(), getDestinationInfo(), getTopicID(), getTransactionID(),"...");
                    //this.internalMessage.decodeBody(stream, offset);
                    break;
                }
                case JCSyncConstans.JCSYNC_CREATE_COLLECTION_INDICATION:{
                    this.internalMessage = new JCSyncCreateCollectionIndication(getSourceInfo(), getDestinationInfo(), getTopicID(), getTransactionID(),"...",0);
                    //this.internalMessage.decodeBody(stream, offset);
                    break;
                }
                case JCSyncConstans.JCSYNC_GENERIC_RESPONSE:{
                    this.internalMessage = new JCSyncGeneralResponse(getSourceInfo(), getDestinationInfo(), getTopicID(), getTransactionID(),(short)1,(short)1,"...");
                    //this.internalMessage.decodeBody(stream, offset);
                    break;
                }
                case JCSyncConstans.JCSYNC_GET_CONSTRUCTOR_REQUEST:{
                   this.internalMessage = new JCSyncGetConstructorRequest(getSourceInfo(), getDestinationInfo(), getTopicID(), getTransactionID(),"...");
                    //this.internalMessage.decodeBody(stream, offset);
                    break;
                }
                case JCSyncConstans.JCSYNC_INVOWE_WRITE_METHOD_INDICATION:{
                   this.internalMessage = new JCSyncInvokeMethodIndication(getSourceInfo(), getDestinationInfo(), getTopicID(), getTransactionID(),"...",0);
                    //this.internalMessage.decodeBody(stream, offset);
                    break;
                }

                

                default: {
                    throw new IllegalArgumentException("Unknown message type: "+this.jcsyncInternalType);                    
                }
                

            }
            acLength = distr.readInt();
            offset += 4;
			if(acLength >0){
				byte[] encAC = new byte[acLength];
				distr.read(encAC);
				ac = new AccessControlRules(encAC);
                                offset += acLength;
			}
            this.internalMessage.decodeBody(stream, offset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                istream.close();
                istream = null;
                distr = null;
                oistr = null;
                stream = null;
            } catch (IOException ex) {
                //ignore
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n->JCSyncMessageCarrier " + "\n->CollectionID: ").append(getCollectionID()).append("\n->InternalMessage: ").append(this.internalMessage).append("\n-> source: ").append(getSourceInfo()).append("\n-> destination: ").append(getDestinationInfo());
        return sb.toString();

    }

    public short getEventType() {
        return this.jcsyncInternalType;
    }

    public String getCollectionID() {
        return this.getTopicID();
    }

    public JCSyncMessage getInternalMessage() {
        return this.internalMessage;
    }
}//end JCSyncMessageCarrier

