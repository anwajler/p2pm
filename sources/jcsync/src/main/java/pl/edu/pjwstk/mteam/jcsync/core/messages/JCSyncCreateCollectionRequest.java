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
 */
public class JCSyncCreateCollectionRequest extends JCSyncMessage {
    private JCSyncCreateCollectionMethod creationDetails;
    
    public JCSyncCreateCollectionRequest(Object src, Object dst, String collID, int transID, String publisher){
        super(src, dst, collID, transID, JCSyncConstans.JCSYNC_CREATE_COLLECTION_REQUEST, publisher);

    }
    public JCSyncCreateCollectionRequest(Object src, Object dst, String collID, int transID, JCSyncCreateCollectionMethod method, String publisher){
        super(src, dst, collID, transID, JCSyncConstans.JCSYNC_CREATE_COLLECTION_REQUEST, publisher);
        this.creationDetails = method;
    }


    @Override
    public int decodeBody(byte[] data, long offset) {
        int headerLenght = 0;
        int lastAvailablebytesToRead = 0;
        ByteArrayInputStream istream = new ByteArrayInputStream(data);
        DataInputStream dtstr = new DataInputStream(istream);
        ObjectInputStream ostr_ = null;
        try {
            headerLenght+=offset;
            istream.skip(offset);
            lastAvailablebytesToRead = dtstr.available();
            ostr_ = new ObjectInputStream(dtstr);
            this.publisher = ostr_.readUTF();            
            headerLenght+=(lastAvailablebytesToRead-dtstr.available());
            lastAvailablebytesToRead = dtstr.available();
            this.creationDetails = (JCSyncCreateCollectionMethod) ostr_.readObject();
            headerLenght+=(lastAvailablebytesToRead-dtstr.available());
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
                    ostr.writeObject(this.creationDetails);
                    
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ostream.toByteArray();
    }
    @Override
    public String toString(){
        sb = new StringBuilder();
            sb.append(
                "\n->Type: " + JCSyncConstans.messages.get(getMessageType())
                +"\n->Details:"
                +"\n"+this.creationDetails
                + "\n->CollectionID: " + getCollectionID()
                + "\n->Transactio ID: " + getTransactionID()
                + "\n->Source: " + getSourceInfo()
                + "\n->Destination: " + getDestinationInfo());
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
}
