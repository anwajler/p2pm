package pl.edu.pjwstk.mteam.jcsync.core.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncConstans;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncCreateCollectionMethod;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncMethod;

/**
 * Message holds indication of collection creating. 
 * Is used after positive response for the 
 * {@link JCSyncCreateCollectionRequest JCSyncCreateCollectionRequest }
 * @author Piotr Bucior
 * @version 1.0
 */
public class JCSyncCreateCollectionIndication extends JCSyncMessage {

    /**
     * all details needs to create collection
     */
    private JCSyncCreateCollectionMethod creationDetails;
    /**
     * transaction ID assigned the request of create this collection
     */
    private int transID_req;
    /**
     * if its <tt>TRUE</tt> then this message is transferred to all nodes in the layer.
     * if <tt>FALSE</tt> then the message is transferred only for this node, 
     * which sends request to create collection.
     */
    private boolean global = true;

    /**
     * create new message with given params. Not used currently.
     * @param src object that represents information about the source of this 
     * message.{@link pl.edu.pjwstk.mteam.core.NodeInfo NodeInfo} 
     * as a used object type.
     * @param dst object that represents information about the destination of this 
     * message.{@link pl.edu.pjwstk.mteam.core.NodeInfo NodeInfo} 
     * as a used object type.
     * @param collID collection identifier
     * @param transID current transaction
     * @param method that object holds all information required to create collection
     * @param publisher publisher name
     * @param oldTransID transaction ID assigned the request of create this collection
     */
    public JCSyncCreateCollectionIndication(Object src, Object dst, String collID, int transID, JCSyncCreateCollectionMethod method, String publisher, int oldTransID) {
        super(src, dst, collID, transID, JCSyncConstans.JCSYNC_CREATE_COLLECTION_INDICATION, publisher);
        this.creationDetails = method;
        this.transID_req = oldTransID;
    }

    /**
     * create new message with given params. Constructor used while decoding 
     * essage from byte array. 
     * @param src object that represents information about the source of this 
     * message.{@link pl.edu.pjwstk.mteam.core.NodeInfo NodeInfo} 
     * as a used object type.
     * @param dst object that represents information about the destination of this 
     * message.{@link pl.edu.pjwstk.mteam.core.NodeInfo NodeInfo} 
     * as a used object type.
     * @param collID collection identifier
     * @param transID current transaction
     * @param publisher publisher name
     * @param oldTransID transaction ID assigned the request of create this collection
     */
    public JCSyncCreateCollectionIndication(Object src, Object dst, String collID, int transID, String publisher, int oldTrans) {
        super(src, dst, collID, transID, JCSyncConstans.JCSYNC_CREATE_COLLECTION_INDICATION, publisher);
        this.transID_req = oldTrans;
    }

    /**
     * new message by the given params, used by the core algorith.
     * @param req delivered request to create collection
     * @param transID current transaction
     * @param publisher publisher name
     * @param oldTransID transaction ID assigned the request of create this collection
     */
    public JCSyncCreateCollectionIndication(JCSyncCreateCollectionRequest req, int transID, String publisher, int oldTrans) {
        super(new Object(), new Object(), req.getCollectionID(), transID, JCSyncConstans.JCSYNC_CREATE_COLLECTION_INDICATION, publisher);
        this.creationDetails = (JCSyncCreateCollectionMethod) req.getDetailedMethod();
        this.transID_req = oldTrans;
    }

    /**
     * called when message will be create to send to node, who want to subscribe collection
     * @param method object holds all information required to create collection
     * @param transID current transaction
     * @param publisher publisher name
     * @param oldTransID transaction ID assigned the request of create this collection
     * @param collID collection identifier
     * @param isGlobal determines that the message is prepared for one node - is set to <tt>FALSE</tt>
     */
    public JCSyncCreateCollectionIndication(JCSyncCreateCollectionMethod method, int transID, String publisher, int oldTrans, String collID, boolean isGlobal) {
        super(new Object(), new Object(), collID, transID, JCSyncConstans.JCSYNC_CREATE_COLLECTION_INDICATION, publisher);
        this.creationDetails = method;
        this.transID_req = oldTrans;
        this.global = isGlobal;
    }

    /**
     * checks that the message is prepared for all/one node <tt>TRUE/FALSE</tt>
     */
    public boolean isGlobal() {
        return this.global;
    }

    @Override
    public int decodeBody(byte[] data, long offset) {
        int headerLenght = 0;
        int lastAvailablebytesToRead = 0;
        ByteArrayInputStream istream = new ByteArrayInputStream(data);
        DataInputStream dtstr = new DataInputStream(istream);
        ObjectInputStream ostr_ = null;
        try {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> data lenght: " + data.length);
            headerLenght += offset;
            istream.skip(offset);
            lastAvailablebytesToRead = dtstr.available();
            ostr_ = new ObjectInputStream(dtstr);
            this.publisher = ostr_.readUTF();
            headerLenght += (lastAvailablebytesToRead - dtstr.available());
            lastAvailablebytesToRead = dtstr.available();
            this.transID_req = ostr_.readInt();
            headerLenght += (lastAvailablebytesToRead - dtstr.available());
            lastAvailablebytesToRead = dtstr.available();
            this.global = ostr_.readBoolean();
            headerLenght += (lastAvailablebytesToRead - dtstr.available());
            lastAvailablebytesToRead = dtstr.available();
            this.creationDetails = (JCSyncCreateCollectionMethod) ostr_.readObject();
            headerLenght += (lastAvailablebytesToRead - dtstr.available());
            lastAvailablebytesToRead = dtstr.available();
//            ostr_.readBoolean();
//            headerLenght+=(lastAvailablebytesToRead-dtstr.available());
//            lastAvailablebytesToRead = dtstr.available();
//            if(lastAvailablebytesToRead>0){
//                dtstr.read(additionalData, headerLenght, lastAvailablebytesToRead);
//            }
//            if(lastAvailablebytesToRead==0) return 0;
//            ostr_ = new ObjectInputStream(dtstr);
//            headerLenght+=(lastAvailablebytesToRead-dtstr.available());
//
//            // method body here
//
//            lastAvailablebytesToRead = dtstr.available();
//            headerLenght+=(lastAvailablebytesToRead-dtstr.available());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (ostr_ != null) {
                    ostr_.close();
                }
            } catch (IOException ex) {
            }
            return headerLenght;
        }
    }

    @Override
    public byte[] encodeBody() {
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        DataOutputStream dtstr = new DataOutputStream(ostream);
        ObjectOutputStream ostr = null;
        try {
            ostr = new ObjectOutputStream(dtstr);
            ostr.writeUTF(this.publisher);
            ostr.writeInt(this.transID_req);
            ostr.writeBoolean(this.global);
            ostr.writeObject(this.creationDetails);
//                    ostr.writeBoolean(global);
            //if(this.additionalData!=null)ostr.write(additionalData);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return ostream.toByteArray();
    }

    @Override
    public String toString() {
        sb = new StringBuilder();
        sb.append(
                "\n->Type: " + JCSyncConstans.messages.get(getMessageType()));
        sb.append("\n->Details:");
        sb.append("\n" + this.creationDetails);
        sb.append("\n-->REQ_TRANS_ID: " + this.transID_req);
        sb.append("\n-->IS_GLOBAL: " + this.global);
        sb.append("\n->CollectionID: " + getCollectionID());
        sb.append("\n->Transactio ID: " + getTransactionID());
        sb.append("\n->Source: " + getSourceInfo());
        sb.append("\n->Destination: " + getDestinationInfo());
        return sb.toString();
    }

    @Override
    public JCSyncMethod getDetailedMethod() {
        return this.creationDetails;
    }

    @Override
    public short getDetailedMethodID() {
        return getMessageType();
    }

    /**
     * returns transaction ID assigned the request of create this collection
     */
    public int getTransactionIdOfRequest() {
        return this.transID_req;
    }
}
