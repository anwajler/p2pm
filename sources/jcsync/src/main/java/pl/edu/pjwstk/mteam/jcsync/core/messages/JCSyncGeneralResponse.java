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
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncConstans;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncMethod;

/**
 *
 * @author pb
 */
public class JCSyncGeneralResponse extends JCSyncMessage {

    private short respCode;
    private short methodDetails;

    public JCSyncGeneralResponse(Object src, Object dst, String collID, int transID, short respCode, short details, String publisher) {
        super(src, dst, collID, transID, JCSyncConstans.JCSYNC_GENERIC_RESPONSE, publisher);
        this.respCode = respCode;
        this.methodDetails = details;
    }

    @Override
    public int decodeBody(byte[] data, long offset) {
        int headerLenght = 0;
        int lastAvailablebytesToRead = 0;
        istream = new ByteArrayInputStream(data);
        dtstr = new DataInputStream(istream);
        try {
            headerLenght += offset;
            istream.skip(offset);
            lastAvailablebytesToRead = dtstr.available();
            //ostr_ = new ObjectInputStream(distr);
            this.publisher = (String) dtstr.readUTF();

            headerLenght += (lastAvailablebytesToRead - dtstr.available());
            lastAvailablebytesToRead = dtstr.available();
            this.methodDetails = dtstr.readShort();
            headerLenght += (lastAvailablebytesToRead - dtstr.available());
            lastAvailablebytesToRead = dtstr.available();
            this.respCode = dtstr.readShort();
            headerLenght += (lastAvailablebytesToRead - dtstr.available());
//            if(lastAvailablebytesToRead==0) return 0;
//            ostr_ = new ObjectInputStream(distr);
//            headerLenght+=(lastAvailablebytesToRead-distr.available());
//
//            // method body here
//
//            lastAvailablebytesToRead = distr.available();
//            headerLenght+=(lastAvailablebytesToRead-distr.available());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (ostr_ != null) {
                    ostr_.close();
                }
                istream = null;
                dtstr = null;
                ostr_ = null;
            } catch (IOException ex) {
            }
            return headerLenght;
        }
    }
    byte[] msgToByte;

    @Override
    public byte[] encodeBody() {
        msgToByte = null;
        ostream = new ByteArrayOutputStream();
        dtstr_ = new DataOutputStream(ostream);
        try {
            //oistr = new ObjectOutputStream(distr);
            dtstr_.writeUTF(this.publisher);
            dtstr_.writeShort(this.methodDetails);
            dtstr_.writeShort(respCode);
            msgToByte = ostream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                dtstr_.close();

            } catch (IOException ex) {
            }
        }
        return msgToByte;
    }
    @Override
    public String toString() {
        try{
        sb = new StringBuilder();
        sb.append(
                "\n->JCSyncResponse, code: " + respCode
                + "\n->Type: " + JCSyncConstans.messages.get(getMessageType())
                + "\n->Details:"
                + "\n-->RESP_FOR: " + JCSyncConstans.messages.get(this.methodDetails)
                + "\n-->RESP_CODE: " + JCSyncConstans.messages.get(this.respCode)
                + "\n->CollectionID: " + getCollectionID()
                + "\n->Transactio ID: " + getTransactionID()
                + "\n->Source: " + getSourceInfo()
                + "\n->Destination: " + getDestinationInfo());
        return sb.toString();}finally{
            sb = null;
        }
    }

    @Override
    public short getDetailedMethodID() {
        return this.methodDetails;
    }

    @Override
    public JCSyncMethod getDetailedMethod() {
        return null;
    }
}
