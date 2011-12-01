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
 * Generic JCSync message pattern, contains only general information about the 
 * message, like source, destination, collection ID, transaction ID.
 * @author Piotr Bucior
 */
public abstract class JCSyncMessage {
     private Object sourceInfo;
     private Object destinationInfo;
     private String collectionID;
     private int transactionID;
     private short messageType = JCSyncConstans.JCSYNC_GENERIC_MESSAGE;
     /**
      * The publisher identifier
      */
     protected String publisher;

     /**
      * blank constructor 
      */
     public JCSyncMessage(){
         this(new Object(),new Object(),"",-1,JCSyncConstans.JCSYNC_GENERIC_MESSAGE,"...");
     }
     /**
      * creates new message with given arguments
      * @param src object that represents information about the source of this 
      * message.{@link pl.edu.pjwstk.mteam.core.NodeInfo NodeInfo} 
      * as a used object type.
      * @param dst object that represents information about the destination of this 
      * message.{@link pl.edu.pjwstk.mteam.core.NodeInfo NodeInfo} 
      * as a used object type.
      * @param collID collection identifier
      * @param transID current transaction
      * @param msgType message type as short, take a look at 
      * {@link pl.edu.pjwstk.mteam.jcsync.core.JCSyncConstans JCSyncConstans}
      * @param publisherName publisher name
      */
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

     
    /**
     * returns the source information contained in this message
     */
    public Object getSourceInfo(){
         return this.sourceInfo;
     }
     /**
      * return the destination information contained in this message
      */
     public Object getDestinationInfo(){
         return this.destinationInfo;
     }
     /**
      * returns publisher name 
      */
     public String getPublisher(){
         return publisher;
     }
     /**
      * set the publisher name 
      * @param pbl publisher name
      */
     public void setPublisher(String pbl){
         this.publisher = pbl;
     }
     /**
      * returns the collection identifier
      */
     public String getCollectionID(){
         return this.collectionID;
     }
     /**
      * returns transaction, which is assigned to this message
      */
     public int getTransactionID(){
         return this.transactionID;
     }
     /**
      * returns message type
      */
     public short getMessageType(){
         return this.messageType;
     }
     /**
      * sets the destination information of this message
      */
     public void setDestinationInfo(Object dest){
         this.destinationInfo = dest;
     }
     /**
      * setting up the source information of this message
      */
     public void setSourceInfo(Object src){
         this.sourceInfo = src;
     }
     /**
      * setting up collection ID
      */
     protected void setCollectionID(String collID){
         this.collectionID = collID;
     }
     /**
      * setting up transaction identifier
      */
     public void setTransactionID(int transID){
         this.transactionID = transID;
     }
     /**
      * setting up the message type, see 
      * {@link pl.edu.pjwstk.mteam.jcsync.core.JCSyncConstans JCSyncConstans}
      */
     protected void setMessageType(short messageType){
         this.messageType = messageType;
     }
     
     /**
      * encodes body to byte array
      * @return encoded body of this message as a byte array
      */
     public abstract byte [] encodeBody();
     /**
      * uses do decode header/body of the message
      */
     protected ByteArrayInputStream istream;
     /**
      * uses do decode header/body of the message
      */
     protected DataInputStream dtstr;
     /**
      * uses do decode header/body of the message
      */
     protected ObjectInputStream ostr_ ;
     /**
      * decodes header of the message from given byte array
      * @param data message as a byte array to decode
      * @param offset start position to decode header
      * @return returns count of decoded bytes + <tt> offset </tt>
      */
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
     /**
      * uses do encode header/body of the message
      */
     protected ByteArrayOutputStream ostream;
    /**
     * uses do encode header/body of the message
     */
    protected DataOutputStream dtstr_;
    /**
     * uses do encode header/body of the message
     */
    protected ObjectOutputStream ostr;
    /**
     * encode header of the message to byte array
     * @return message header as a byte array
     */
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
    /**
      * decodes body of the message from given byte array
      * @param data message as a byte array to decode
      * @param offset start position to decode header
      * @return depends on implementation
      */
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
    /**
     * returns contaned <tt>JCSyncMethod</tt>
     * @return contained method or null if the message does not conatin any <tt>JCSyncMethod</tt> 
     */
    public abstract JCSyncMethod getDetailedMethod();
    /**
     * returns method identifier specified in 
     * {@link pl.edu.pjwstk.mteam.jcsync.core.JCSyncConstans JCSyncConstans}
     */
    public abstract short getDetailedMethodID();

}
