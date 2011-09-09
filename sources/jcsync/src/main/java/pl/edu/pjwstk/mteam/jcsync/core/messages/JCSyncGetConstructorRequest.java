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

/**
 *
 * @author pb
 */
public class JCSyncGetConstructorRequest  extends JCSyncMessage {

    public JCSyncGetConstructorRequest(Object src, Object dst, String collID, int transID, String pbl) {
        super(src, dst, collID, transID, JCSyncConstans.JCSYNC_GET_CONSTRUCTOR_REQUEST, pbl);

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
            //ostr_ = new ObjectInputStream(dtstr);
            this.publisher = dtstr.readUTF();
            headerLenght += (lastAvailablebytesToRead - dtstr.available());
            lastAvailablebytesToRead = dtstr.available();
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
          ByteArrayOutputStream ostream = new ByteArrayOutputStream();
		DataOutputStream dtstr_ = new DataOutputStream(ostream);
                ObjectOutputStream ostr =null;
            byte [] retVal = null;
        try {
            //ostr = new ObjectOutputStream(dtstr_);
            dtstr_.writeUTF(publisher);
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
                + "\n->CollectionID: " + getCollectionID()
                + "\n->Transactio ID: " + getTransactionID()
                + "\n->Source: " + getSourceInfo()
                + "\n->Destination: " + getDestinationInfo());
        return sb.toString();
    }

    @Override
    public short getDetailedMethodID() {
        return getMessageType();
    }

    @Override
    public JCSyncMethod getDetailedMethod() {
        return null;
    }
}



