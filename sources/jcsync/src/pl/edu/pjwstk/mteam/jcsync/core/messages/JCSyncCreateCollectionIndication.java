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
 *
 * @author Piotr Bucior
 * @version 1.0
 */
public class JCSyncCreateCollectionIndication extends JCSyncMessage{
    
private JCSyncCreateCollectionMethod creationDetails;

/**
 * transaction ID from request
 */
private int transID_;
private boolean global = true;
//private byte [] additionalData = null;

    public JCSyncCreateCollectionIndication(Object src, Object dst, String collID, int transID, JCSyncCreateCollectionMethod method, String publisher, int oldTransID){
        super(src, dst, collID, transID, JCSyncConstans.JCSYNC_CREATE_COLLECTION_INDICATION,publisher);
        this.creationDetails = method;
        this.transID_ = oldTransID;
    }
    public JCSyncCreateCollectionIndication(Object src, Object dst, String collID, int transID,String publisher, int oldTrans){
        super(src, dst, collID, transID, JCSyncConstans.JCSYNC_CREATE_COLLECTION_INDICATION,publisher);
        this.transID_ = oldTrans;
    }
    public JCSyncCreateCollectionIndication(JCSyncCreateCollectionRequest req, int transID, String publisher, int oldTrans){
        super(new Object(),new Object(),req.getCollectionID(), transID, JCSyncConstans.JCSYNC_CREATE_COLLECTION_INDICATION, publisher);
        this.creationDetails = (JCSyncCreateCollectionMethod) req.getDetailedMethod();
        this.transID_ = oldTrans;
    }
    public JCSyncCreateCollectionIndication(JCSyncCreateCollectionMethod constructorDetails, int transID, String publisher, int oldTrans,String collectionID, boolean isGlobal){
        super(new Object(),new Object(),collectionID, transID, JCSyncConstans.JCSYNC_CREATE_COLLECTION_INDICATION, publisher);
        this.creationDetails = constructorDetails;
        this.transID_ = oldTrans;
        this.global = isGlobal;
    }
    public boolean isGlobal(){
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
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> data lenght: "+data.length);
            headerLenght+=offset;
            istream.skip(offset);
            lastAvailablebytesToRead = dtstr.available();
            ostr_ = new ObjectInputStream(dtstr);
            this.publisher = ostr_.readUTF();
            headerLenght+=(lastAvailablebytesToRead-dtstr.available());
            lastAvailablebytesToRead = dtstr.available();
            this.transID_ = ostr_.readInt();
            headerLenght+=(lastAvailablebytesToRead-dtstr.available());
            lastAvailablebytesToRead = dtstr.available();
            this.global = ostr_.readBoolean();
            headerLenght+=(lastAvailablebytesToRead-dtstr.available());
            lastAvailablebytesToRead = dtstr.available();
            this.creationDetails = (JCSyncCreateCollectionMethod) ostr_.readObject();
            headerLenght+=(lastAvailablebytesToRead-dtstr.available());
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
        }
        finally{
            try {
                if(ostr_!=null)
                ostr_.close();
            } catch (IOException ex) {
            }
            return headerLenght;
        }
    }

    @Override
    public byte[] encodeBody() {
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
		DataOutputStream dtstr = new DataOutputStream(ostream);
                ObjectOutputStream ostr =null;
		try {
                    ostr = new ObjectOutputStream(dtstr);
                    ostr.writeUTF(this.publisher);
                    ostr.writeInt(this.transID_);
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
    public String toString(){
        sb = new StringBuilder();
            sb.append(
                "\n->Type: " + JCSyncConstans.messages.get(getMessageType()));
                sb.append("\n->Details:");
                sb.append("\n"+this.creationDetails);
                sb.append("\n-->REQ_TRANS_ID: "+this.transID_);
                sb.append("\n-->IS_GLOBAL: "+this.global);
                sb.append( "\n->CollectionID: " + getCollectionID());
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
    public int getTransactionID_(){
        return this.transID_;
    }

}
