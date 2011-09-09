package pl.edu.pjwstk.mteam.jcsync.core.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncConstans;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncMethod;

/**
 *
 * @author Piotr Bucior
 */
public abstract class JCSyncMessage {
     private Object sourceInfo;
     private Object destinationInfo;
     private String collectionID;
     private int transactionID;
     private short messageType = JCSyncConstans.JCSYNC_GENERIC_MESSAGE;
     protected String publisher;

     public JCSyncMessage(){
         this(new Object(),new Object(),"",-1,JCSyncConstans.JCSYNC_GENERIC_MESSAGE,"...");
     }
     public JCSyncMessage(Object src, Object dst, String collID, int transID, short msgType, String publisherName){
         this.sourceInfo =src;
         this.destinationInfo = dst;
         this.collectionID = collID;
         this.transactionID = transID;
         this.messageType = msgType;
         this.publisher = publisherName;
     }

    @Override
    protected void finalize() throws Throwable {
        this.sourceInfo = null;
        this.destinationInfo = null;
        this.collectionID = null;
        this.publisher = null;
        this.sb = null;
        super.finalize();
    }

     
     public Object getSourceInfo(){
         return this.sourceInfo;
     }
     public Object getDestinationInfo(){
         return this.destinationInfo;
     }
     public String getPublisher(){
         return publisher;
     }
     public void setPublisher(String pbl){
         this.publisher = pbl;
     }
     public String getCollectionID(){
         return this.collectionID;
     }
     public int getTransactionID(){
         return this.transactionID;
     }
     public short getMessageType(){
         return this.messageType;
     }
     public void setDestinationInfo(Object dest){
         this.destinationInfo = dest;
     }
     public void setSourceInfo(Object src){
         this.sourceInfo = src;
     }
     protected void setCollectionID(String collID){
         this.collectionID = collID;
     }
     public void setTransactionID(int transID){
         this.transactionID = transID;
     }
     protected void setMessageType(short messageType){
         this.messageType = messageType;
     }
     
     public abstract byte [] encodeBody();
     protected ByteArrayInputStream istream;
     protected DataInputStream dtstr;
     protected ObjectInputStream ostr_ ;
     public int decodeHeader(byte[] data, long offset) {
        int headerLenght = 0;
        int lastAvailablebytesToRead = 0;
        istream = new ByteArrayInputStream(data);
        dtstr = new DataInputStream(istream);
        ostr_ = null;
        try {
            //TODO check headerLenght computing
            headerLenght+=offset;
            istream.skip(offset);
            ostr_ = new ObjectInputStream(dtstr);
            lastAvailablebytesToRead = dtstr.available();
            setMessageType(ostr_.readShort());
            headerLenght+=(lastAvailablebytesToRead-dtstr.available());
            lastAvailablebytesToRead = dtstr.available();
            setSourceInfo(ostr_.readObject());
            headerLenght+=(lastAvailablebytesToRead-dtstr.available());
            lastAvailablebytesToRead = dtstr.available();
            setDestinationInfo(ostr_.readObject());
            headerLenght+=(lastAvailablebytesToRead-dtstr.available());
            lastAvailablebytesToRead = dtstr.available();
            setCollectionID(ostr_.readUTF());
            headerLenght+=(lastAvailablebytesToRead-dtstr.available());
            lastAvailablebytesToRead = dtstr.available();
            setTransactionID(ostr_.readInt());
            headerLenght+=(lastAvailablebytesToRead-dtstr.available());
            lastAvailablebytesToRead = dtstr.available();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            try {
                ostr_.close();
                ostr_ = null;
                istream = null;
                dtstr = null;
            } catch (IOException ex) {
            }
            return headerLenght;
        }
    }
    protected ByteArrayOutputStream ostream;
    protected DataOutputStream dtstr_;
    protected ObjectOutputStream ostr;
    public byte[] encodeHeader() {
        ostream = new ByteArrayOutputStream();
        dtstr_ = new DataOutputStream(ostream);
        ostr = null;
        try {
            ostr = new ObjectOutputStream(dtstr_);
            ostr.writeShort(getMessageType());
            ostr.writeObject(getSourceInfo());
            ostr.writeObject(getDestinationInfo());
            ostr.writeObject(getCollectionID());
            ostr.writeObject(getTransactionID());
            return ostream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                ostr.close();
                ostr = null;
                dtstr_ = null;
                ostream = null;
            } catch (IOException ex) {
            }
        }
    }
     public abstract int decodeBody(byte [] data, long offset);
    StringBuilder sb;
    @Override
     public String toString(){
         sb = new StringBuilder();
            sb.append("\n---JCSyncMessage---"+
                "\nType: " + JCSyncConstans.messages.get(this.messageType)
                + "\nCollectionID: " + getCollectionID()
                + "\nSource: " + getSourceInfo()
                + "\nDestination: " + getDestinationInfo());
        return sb.toString();
     }
    public abstract JCSyncMethod getDetailedMethod();
    public abstract short getDetailedMethodID();

}
