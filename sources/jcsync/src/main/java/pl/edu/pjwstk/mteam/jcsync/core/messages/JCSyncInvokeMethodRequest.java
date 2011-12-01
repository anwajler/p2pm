package pl.edu.pjwstk.mteam.jcsync.core.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncConstans;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncMethod;

/**
 *
 * @author Piotr Bucior
 */
public class JCSyncInvokeMethodRequest extends JCSyncMessage {

    private JCSyncMethod methodDetails;

    /**
     * Used when the message is decoded from the byte array
       * @param src object that represents information about the source of this 
     * message.{@link pl.edu.pjwstk.mteam.core.NodeInfo NodeInfo} 
     * as a used object type.
     * @param dst object that represents information about the destination of this 
     * message.{@link pl.edu.pjwstk.mteam.core.NodeInfo NodeInfo} 
     * as a used object type.
     * @param collID collection identifier
     * @param transID current transaction
     * @param pbl publisher name
     */
    public JCSyncInvokeMethodRequest(Object src, Object dst, String collID, int transID, String pbl) {
        super(src, dst, collID, transID, JCSyncConstans.JCSYNC_INVOKE_WRITE_METHOD_REQ, pbl);

    }

    /**
     * called when the message is prepared to send
     * @param src object that represents information about the source of this 
     * message.{@link pl.edu.pjwstk.mteam.core.NodeInfo NodeInfo} 
     * as a used object type.
     * @param dst object that represents information about the destination of this 
     * message.{@link pl.edu.pjwstk.mteam.core.NodeInfo NodeInfo} 
     * as a used object type.
     * @param collID collection identifier
     * @param transID current transaction
     * @param method object that holds all information about method to invoke
     * @param pbl publisher name     
     */
    public JCSyncInvokeMethodRequest(Object src, Object dst, String collID, int transID, JCSyncMethod method, String pbl) {
        super(src, dst, collID, transID, JCSyncConstans.JCSYNC_INVOKE_WRITE_METHOD_REQ, pbl);
        this.methodDetails = method;
    }

    @Override
    public int decodeBody(byte[] data, long offset) {
        int headerLenght = 0;
        int lastAvailablebytesToRead = 0;
        istream = new ByteArrayInputStream(data);
        dtstr = new DataInputStream(istream);
        ostr_ = null;
        try {
            headerLenght += offset;
            istream.skip(offset);
            lastAvailablebytesToRead = dtstr.available();
            ostr_ = new ObjectInputStream(dtstr);
            this.publisher = ostr_.readUTF();
            headerLenght += (lastAvailablebytesToRead - dtstr.available());
            lastAvailablebytesToRead = dtstr.available();
            this.methodDetails = (JCSyncMethod) ostr_.readObject();
            headerLenght += (lastAvailablebytesToRead - dtstr.available());
//            if(lastAvailablebytesToRead==0) return 0;
//            ostr_ = new ObjectInputStream(dtstr_);
//            headerLenght+=(lastAvailablebytesToRead-dtstr_.available());
//
//            // method body here
//
//            lastAvailablebytesToRead = dtstr_.available();
//            headerLenght+=(lastAvailablebytesToRead-dtstr_.available());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (ostr_ != null) {
                    ostr_.close();
                }
                ostr_ = null;
                istream = null;
                dtstr = null;
            } catch (IOException ex) {
            }
            return headerLenght;
        }
    }

    @Override
    public byte[] encodeBody() {
        byte [] retVal = null;
        ostream = new ByteArrayOutputStream();
		dtstr_ = new DataOutputStream(ostream);
                ostr =null;
        try {
                    ostr = new ObjectOutputStream(dtstr_);
            ostr.writeUTF(publisher);
            ostr.writeObject(this.methodDetails);
            retVal = ostream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            }
        finally{
            ostream = null;
            ostr = null;
            dtstr_ = null;
        }
        return retVal;
    }
    @Override
    public String toString() {
        sb = new StringBuilder();
        sb.append(
                "\n->Type: " + JCSyncConstans.messages.get(getMessageType())
                + "\n->Details:"
                + "\n" + this.methodDetails
                + "\n->CollectionID: " + getCollectionID()
                + "\n->Transactio ID: " + getTransactionID()
                + "\n->Source: " + getSourceInfo()
                + "\n->Destination: " + getDestinationInfo());
        return sb.toString();
    }

    @Override
    public JCSyncMethod getDetailedMethod() {
        return this.methodDetails;
    }

    @Override
    public short getDetailedMethodID() {
        return getMessageType();
    }
}


