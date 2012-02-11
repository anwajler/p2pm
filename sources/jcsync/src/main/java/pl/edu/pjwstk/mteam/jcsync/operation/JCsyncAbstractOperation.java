package pl.edu.pjwstk.mteam.jcsync.operation;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import static pl.edu.pjwstk.mteam.jcsync.operation.RegisteredOperations.*;

/**
 * Contains all interested information about published operations.
 * @author Piotr Bucior
 */
public class JCsyncAbstractOperation implements Comparable {

    /**
     * Object identifier that operation refers to.
     */
    protected String objectID = null;
    /**
     * Operation type.
     * @see RegisteredOperations
     */
    protected short operationType = -1;
    /**
     * Additional data associated with current operation, such as method invocation 
     * return value delivered from other node. 
     */
    protected byte[] details = null;
    /**
     * Used to transport {@link MethodCarrier MethodCarrier}.
     */
    protected Serializable details_=null;
    /**
     * Request identifier of current operation.
     */
    protected long reqestID = -1;
    protected final String publisher;
    /**
     * Creates new instance of class with given arguments.
     * @param objectID shared object identifier.
     * @param operationType operation type.
     */
    protected JCsyncAbstractOperation(String objectID, short operationType, String publisher) {
        this.objectID = objectID;
        this.operationType = operationType;
        this.publisher = publisher;
    }

    /**
     * Creates new instance wit given arguments.
     * @param objectID shared object identifier.
     * @param operationType operation type.
     * @param details additional data related with this operation.
     */
    public JCsyncAbstractOperation(String objectID, short operationType, byte details[], String publisher) {
        this.objectID = objectID;
        this.operationType = operationType;
        this.details = details;
        this.publisher = publisher;
    }
    /**
     * Creates new instance wit given arguments.
     * @param objectID shared object identifier.
     * @param operationType operation type.
     * @param details additional data related with this operation.
     */
    public JCsyncAbstractOperation(String objectID, short operationType, Serializable details, String publisher) {
        this.objectID = objectID;
        this.operationType = operationType;
        this.details_ = details;
        this.publisher = publisher;
    }

    /**
     * Returns new <tt>OP_REQ_CREATE_SHARED_OBJECT</tt> operation related to 
     * shared object with given <tt>objectName</tt>
     * @param objectName object identifier associated with the operation
     * @return <tt>OP_REQ_CREATE_SHARED_OBJECT</tt> operation
     * @see RegisteredOperations
     */
    public static JCsyncAbstractOperation get_OP_REQ_CREATE_SHARED_OBJECT(String objectName, String publisher) {
        return new JCsyncAbstractOperation(objectName, OP_REQ_CREATE_SHARED_OBJECT, publisher);
    }

    /**
     * Returns new <tt>OP_REQ_SUBSCRIBE</tt> operation related to 
     * shared object with given <tt>objectName</tt>
     * @param objectName object identifier associated with the operation
     * @return <tt>OP_REQ_SUBSCRIBE</tt> operation
     * @see RegisteredOperations
     */
    public static JCsyncAbstractOperation get_OP_REQ_SUBSCRIBE(String objectName, String publisher) {
        return new JCsyncAbstractOperation(objectName, OP_REQ_SUBSCRIBE, publisher);
    }

    /**
     * Returns new <tt>OP_REQ_UNSUBSCRIBE</tt> operation related to 
     * shared object with given <tt>objectName</tt>
     * @param objectName object identifier associated with the operation
     * @return <tt>OP_REQ_UNSUBSCRIBE</tt> operation
     * @see RegisteredOperations
     */
    public static JCsyncAbstractOperation get_OP_REQ_UNSUBSCRIBE(String objectName, String publisher) {
        return new JCsyncAbstractOperation(objectName, OP_REQ_UNSUBSCRIBE,  publisher);
    }

    /**
     * Returns new <tt>OP_REQ_REMOVE</tt> operation related to 
     * shared object with given <tt>objectName</tt>
     * @param objectName object identifier associated with the operation
     * @return <tt>OP_REQ_REMOVE</tt> operation
     * @see RegisteredOperations
     */
    public static JCsyncAbstractOperation get_OP_REQ_REMOVE(String objectName, String publisher) {
        return new JCsyncAbstractOperation(objectName, OP_REQ_REMOVE,publisher);
    }

    /**
     * Returns new <tt>OP_REQ_TRANSFER_OBJECT</tt> operation related to 
     * shared object with given <tt>objectName</tt>
     * @param objectName object identifier associated with the operation
     * @return <tt>OP_REQ_TRANSFER_OBJECT</tt> operation
     * @see RegisteredOperations
     */
    public static JCsyncAbstractOperation get_OP_REQ_TRANSFER_OBJECT(String objectName, String publisher) {
        return new JCsyncAbstractOperation(objectName, OP_REQ_TRANSFER_OBJECT,publisher);
    }

    /**
     * Returns new <tt>OP_IND_TRANSFER_OBJECT</tt> operation related to 
     * shared object with given <tt>objectName</tt>
     * @param objectName object identifier associated with the operation
     * @return <tt>OP_IND_TRANSFER_OBJECT</tt> operation
     * @see RegisteredOperations
     */
    public static JCsyncAbstractOperation get_OP_IND_TRANSFER_OBJECT(String objectName, byte[] details, String publisher) {
        return new JCsyncAbstractOperation(objectName, OP_IND_TRANSFER_OBJECT, details,publisher);
    }

    /**
     * Returns new <tt>OP_REQ_LOCK_APPLY</tt> operation related to 
     * shared object with given <tt>objectName</tt>
     * @param objectName object identifier associated with the operation
     * @return <tt>OP_REQ_LOCK_APPLY</tt> operation
     * @see RegisteredOperations
     */
    public static JCsyncAbstractOperation get_OP_REQ_LOCK_APPLY(String objectName, String publisher) {
        return new JCsyncAbstractOperation(objectName, OP_REQ_LOCK_APPLY,publisher);
    }

    /**
     * Returns new <tt>OP_IND_LOCK_APPLY,</tt> operation related to 
     * shared object with given <tt>objectName</tt>
     * @param objectName object identifier associated with the operation
     * @return <tt>OP_IND_LOCK_APPLY,</tt> operation
     * @see RegisteredOperations
     */
    public static JCsyncAbstractOperation get_OP_IND_LOCK_APPLY(String objectName, byte[] details, String publisher) {
        return new JCsyncAbstractOperation(objectName, OP_IND_LOCK_APPLY, details,publisher);
    }

    /**
     * Returns new <tt>OP_REQ_LOCK_RELEASE,</tt> operation related to 
     * shared object with given <tt>objectName</tt>
     * @param objectName object identifier associated with the operation
     * @return <tt>OP_REQ_LOCK_RELEASE,</tt> operation
     * @see RegisteredOperations
     */
    public static JCsyncAbstractOperation get_OP_REQ_LOCK_RELEASE(String objectName, String publisher) {
        return new JCsyncAbstractOperation(objectName, OP_REQ_LOCK_RELEASE,publisher);
    }

    /**
     * Returns new <tt>OP_IND_LOCK_RELEASE,</tt> operation related to 
     * shared object with given <tt>objectName</tt>
     * @param objectName object identifier associated with the operation
     * @return <tt>OP_IND_LOCK_RELEASE,</tt> operation
     * @see RegisteredOperations
     */
    public static JCsyncAbstractOperation get_OP_IND_LOCK_RELEASE(String objectName, byte[] details, String publisher) {
        return new JCsyncAbstractOperation(objectName, OP_IND_LOCK_RELEASE, details,publisher);
    }

    /**
     * Returns new <tt>OP_REQ_READ_METHOD,</tt> operation related to 
     * shared object with given <tt>objectName</tt>
     * @param objectName object identifier associated with the operation
     * @return <tt>OP_REQ_READ_METHOD,</tt> operation
     * @see RegisteredOperations
     */
    public static JCsyncAbstractOperation get_OP_REQ_READ_METHOD(String objectName, byte[] details, String publisher) {
        return new JCsyncAbstractOperation(objectName, OP_REQ_READ_METHOD, details,publisher);
    }

    /**
     * Returns new <tt>OP_REQ_WRITE_METHOD,</tt> operation related to 
     * shared object with given <tt>objectName</tt>
     * @param objectName object identifier associated with the operation
     * @return <tt>OP_REQ_WRITE_METHOD,</tt> operation
     * @see RegisteredOperations
     */
    public static JCsyncAbstractOperation get_OP_REQ_WRITE_METHOD(String objectName, byte[] details, String publisher) {
        return new JCsyncAbstractOperation(objectName, OP_REQ_WRITE_METHOD, details,publisher);
    }

    /**
     * Returns new <tt>OP_IND_READ_METHOD,</tt> operation related to 
     * shared object with given <tt>objectName</tt>
     * @param objectName object identifier associated with the operation
     * @return <tt>OP_IND_READ_METHOD,</tt> operation
     * @see RegisteredOperations
     */
    public static JCsyncAbstractOperation get_OP_IND_READ_METHOD(String objectName, byte[] details, String publisher) {
        return new JCsyncAbstractOperation(objectName, OP_IND_READ_METHOD, details,publisher);
    }

    /**
     * Returns new <tt>OP_IND_WRITE_METHOD,</tt> operation related to 
     * shared object with given <tt>objectName</tt>
     * @param objectName object identifier associated with the operation
     * @return <tt>OP_IND_WRITE_METHOD,</tt> operation
     * @see RegisteredOperations
     */
    public static JCsyncAbstractOperation get_OP_IND_WRITE_METHOD(String objectName, byte[] details, String publisher) {
        return new JCsyncAbstractOperation(objectName, OP_IND_WRITE_METHOD, details,publisher);
    }

    /**
     * Returns new operation depending on the <tt>type</tt>
     * @param type operation type
     * @param name object identifier
     * @return new operation associated with given arguments.
     */
    public static JCsyncAbstractOperation getByType(short type, String name,String publisher) { 
        //checks whether the operation is registered - it prevents duplication of types
        if(!RegisteredOperations.OP_NAMES.containsKey(type)) 
            throw new IllegalArgumentException("Operation with given type: "+type+" is not registered.");
        return new JCsyncAbstractOperation(name, type,publisher);
    }

    /**
     * Returns new operation depending on the <tt>type</tt>
     * @param type operation type
     * @param name object identifier
     * @param details additional details that will be passed with this operations
     * @return new operation associated with given arguments
     */
    public static JCsyncAbstractOperation getByType(short type, String name, byte[] details,String publisher) {
        //checks whether the operation is registered - it prevents duplication of types
        if(!RegisteredOperations.OP_NAMES.containsKey(type)) 
            throw new IllegalArgumentException("Operation with given type: "+type+" is not registered.");
        return new JCsyncAbstractOperation(name, type, details,publisher);
    }
    /**
     * Returns new operation depending on the <tt>type</tt>
     * @param type operation type
     * @param name object identifier
     * @param details additional details that will be passed with this operations
     * @return new operation associated with given arguments
     */
    public static JCsyncAbstractOperation getByType(short type, String name, Serializable details,String publisher) {
        JCsyncAbstractOperation o = new JCsyncAbstractOperation(name, type, publisher);
        o.details_ = details;
        return o;
        
    }

    /**
     * Returns details associated with current operation.
     * @return associated details as byte array or null 
     */
    public byte[] getDetails() {
        return this.details;
    }

    /**
     * Returns object identifier for current operation.
     * @return object identifier as <tt>String</tt> value
     */
    public String getObjectID() {
        return this.objectID;
    }

    /**
     * Returns the type of this operation.
     * @return operation type as <tt>short</tt> value
     * @see RegisteredOperations
     */
    public short getOperationType() {
        return this.operationType;
    }

    /**
     * Returns request identifier associated with this operation.
     * @return request identifier as <tt>long</tt> value
     */
    public long getReqestID() {
        return reqestID;
    }
    public String getPublisher(){
        return this.publisher;
    }
    /**
     * Sets request identifier for current operation.
     * @param reqestID request identifier that current operation will be 
     * related for.
     */
    public void setReqestID(long reqestID) {
        this.reqestID = reqestID;
    }
    
    /**
     * Returns necessary information to invoke delivered method as 
     * <tt>MethodCarrier</tt> related with current operation.
     * @return method specification to invoke or <tt>null</t>
     */
    public MethodCarrier getMethodCarrier(){
        if(this.details_!=null) return (MethodCarrier) this.details_;
        else return null;
    }
    /**
     * Sets <tt>MethodCarrier</tt>.
     * @param mc <tt>MethodCarrier</tt> that will be related with current operation.
     */
    public void setMethodCarrier(MethodCarrier mc){
        this.details_ = mc;
    }    

    @Override
    public int compareTo(Object o_) {
        JCsyncAbstractOperation o = (JCsyncAbstractOperation) o_;
        if (this.objectID.compareTo(o.getObjectID()) == 0) {
            if (this.operationType == o.getOperationType() 
                    && this.reqestID == o.getReqestID()) {
                return 0;
            }
        }
        return -1;
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + (this.objectID != null ? this.objectID.hashCode() : 0);
        hash = 47 * hash + this.operationType;
        hash = 47 * hash + (int) (this.reqestID ^ (this.reqestID >>> 32));
        return hash;
    }
    
    @Override
    public boolean equals(Object o_) {
        JCsyncAbstractOperation o = (JCsyncAbstractOperation) o_;
        if (this.objectID.compareTo(o.getObjectID()) == 0) {
            if (this.operationType == o.getOperationType() 
                    && this.reqestID == o.getReqestID()) {
                if(this.publisher.compareTo(o.publisher)==0)
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.objectID);
        sb.append('@');
        sb.append('[');
        sb.append(this.operationType);
        sb.append(']');
        sb.append('-');
        sb.append(OP_NAMES.get(this.operationType));
        sb.append('-');
        sb.append(this.reqestID);
        sb.append('-');
        if(this.details_!=null && this.details_ instanceof MethodCarrier)
        sb.append(((MethodCarrier)this.details_).toString());
        
        return sb.toString();
    }
    private ByteArrayOutputStream ostream;
    private DataOutputStream dostr;
    private byte[] msgToByte;

    /**
     * Returns an byte array representation of current operation.
     * @return current operation as byte array.
     */
    public byte[] toByteArray() {
        ostream = new ByteArrayOutputStream();
        dostr = new DataOutputStream(ostream);
        try {
            dostr.writeShort(this.operationType);
            dostr.writeUTF(this.objectID);
            dostr.writeLong(this.reqestID);
            dostr.writeUTF(this.publisher);
            if(this.details_==null){
            if (this.details != null && details.length > 0) {
                dostr.writeInt(this.details.length);
                ostream.write(this.details);
            }
            }else{
                dostr.writeInt(-1);
                ObjectOutputStream oos = new ObjectOutputStream(dostr);
                oos.writeObject(this.details_);
            }
            msgToByte = null;
            msgToByte = ostream.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                ostream.close();
                ostream = null;
            } catch (IOException ex) {
            }
        }
        return msgToByte;
    }

    /**
     * Encode operation from given byte array.
     * @param stream byte array that contains operation to encode.
     * @return instance of encoded operation from byte array.
     */
    public static JCsyncAbstractOperation encode(byte[] stream) {
        ByteArrayInputStream istream;
        DataInputStream distr;
        JCsyncAbstractOperation retval = null;
        istream = new ByteArrayInputStream(stream);
        distr = new DataInputStream(istream);
        short operationType = -1;
        String objectID = null;
        String publisher = "";
        long reqID = -1;
        byte[] details = null;
        Serializable details_ = null;
        try {
            operationType = distr.readShort();
            objectID = distr.readUTF();
            reqID = distr.readLong();
            publisher = distr.readUTF();
            if (distr.available() > 0) {
                int detailsLenght = distr.readInt();
                if(detailsLenght == -1){
                    ObjectInputStream ois = new ObjectInputStream(distr);
                    details_ = (Serializable) ois.readObject();
                }else{
                    details = new byte[detailsLenght];
                    distr.read(details);
                }
            }
            if(details_ == null)
            retval = new JCsyncAbstractOperation(objectID, operationType, details,publisher);
            else
            retval = new JCsyncAbstractOperation(objectID, operationType, details_,publisher);            
            retval.setReqestID(reqID);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (distr != null) {
                    distr.close();
                }
                istream = null;
                distr = null;
            } catch (IOException ex) {
            }
        }

        return retval;
    }

    /**
     * Dynamically changing the operation type from indication to request.
     * @return a copy of current operation with changed type to request. 
     */
    public JCsyncAbstractOperation changeTypeToRequest() {
        JCsyncAbstractOperation o = new JCsyncAbstractOperation(this.objectID, (short) (this.operationType -128),this.details,this.publisher);
        o.details_ = this.details_;
        o.reqestID = this.reqestID;
        return o;
    }

    /**
     * Dynamically change operation type from request to indication.
     * @return a copy of current operation with changed type to indication.
     */
    public JCsyncAbstractOperation changeTypeToIndication() {
        JCsyncAbstractOperation o = new JCsyncAbstractOperation(this.objectID, (short) (this.operationType +128),this.details,this.publisher);
        o.details_ = this.details_;
        o.reqestID = this.reqestID;
        return o;       
    }
    
}
