/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncWriteMethod;

/**
 *
 * @author pb
 */
public class JCSyncInvokeMethodIndication extends JCSyncMessage {

    private JCSyncWriteMethod methodDetails;
    private int oldTransID;
    private Long operationID;

    public JCSyncInvokeMethodIndication(Object src, Object dst, String collID, int transID, String pbl, int oldTransID) {
        super(src, dst, collID, transID, JCSyncConstans.JCSYNC_INVOWE_WRITE_METHOD_INDICATION, pbl);
        this.oldTransID = oldTransID;
    }
//not used
//    public JCSyncInvokeMethodIndication(Object src, Object dst, String collID, int transID, JCSyncWriteMethod method, String pbl, int oldTransID) {
//        super(src, dst, collID, transID, JCSyncConstans.JCSYNC_INVOWE_WRITE_METHOD_INDICATION, pbl);
//        this.methodDetails = method;
//        this.oldTransID = oldTransID;
//    }

    public JCSyncInvokeMethodIndication(JCSyncInvokeMethodRequest req, int transID, String publisher, int oldTrans) {
        super(new Object(), new Object(), req.getCollectionID(), transID, JCSyncConstans.JCSYNC_INVOWE_WRITE_METHOD_INDICATION, publisher);
        this.methodDetails = (JCSyncWriteMethod) req.getDetailedMethod();
        this.oldTransID = oldTrans;
    }

    @Override
    protected void finalize() throws Throwable {
        this.methodDetails = null;
        super.finalize();
    }
    public void setOperationID(Long l){
        this.operationID = l;
    }
    public Long getOperationID(){
        return this.operationID;
    }

    @Override
    public int decodeBody(byte[] data, long offset) {
        int headerLenght = 0;
        int lastAvailablebytesToRead = 0;
        ByteArrayInputStream istream = new ByteArrayInputStream(data);
        DataInputStream dtstr = new DataInputStream(istream);
        ObjectInputStream ostr_ = null;
        try {
            headerLenght += offset;
            istream.skip(offset);
            lastAvailablebytesToRead = dtstr.available();
            ostr_ = new ObjectInputStream(dtstr);
            this.publisher = ostr_.readUTF();
            headerLenght += (lastAvailablebytesToRead - dtstr.available());
            lastAvailablebytesToRead = dtstr.available();
            this.oldTransID = ostr_.readInt();
            headerLenght += (lastAvailablebytesToRead - dtstr.available());
            lastAvailablebytesToRead = dtstr.available();
            this.operationID = ostr_.readLong();
            headerLenght += (lastAvailablebytesToRead - dtstr.available());
            lastAvailablebytesToRead = dtstr.available();
            this.methodDetails = (JCSyncWriteMethod) ostr_.readObject();
            headerLenght += (lastAvailablebytesToRead - dtstr.available());
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
            ostr.writeUTF(publisher);
            ostr.writeInt(this.oldTransID);
            ostr.writeLong(this.operationID);
            ostr.writeObject(this.methodDetails);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return ostream.toByteArray();
    }

    @Override
    public String toString() {
        try {
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
        } finally {
            sb = null;
        }
    }

    @Override
    public JCSyncMethod getDetailedMethod() {
        return this.methodDetails;
    }

    @Override
    public short getDetailedMethodID() {
        return getMessageType();
    }

    public int getTransactionID_() {
        return this.oldTransID;
    }

    public String toSimpleString() {
        try {
            sb = new StringBuilder();
            sb.append(
                    JCSyncConstans.messages.get(getMessageType())
                    + "\n->CollectionID: " + getCollectionID()
                    + "\n->Transactio ID: " + getTransactionID()
                    + "\n->Source: " + getSourceInfo());
            return sb.toString();
        } finally {
            sb = null;
        }
    }
}
