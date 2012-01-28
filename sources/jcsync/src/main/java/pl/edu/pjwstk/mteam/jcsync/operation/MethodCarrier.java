package pl.edu.pjwstk.mteam.jcsync.operation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.apache.log4j.Logger;

/**
 * <tt>MethodCarrier</tt> is used to transport all necessary information required
 * to call published method on the other nodes.
 * @author Piotr Bucior
 * @serial
 */
public class MethodCarrier implements Serializable {
    private static final Logger log = Logger.getLogger(MethodCarrier.class);
    /**
     * The method name.
     */
    public String genericMethodName = "";
    /**
     * Called method arguments types.
     */
    public Class[] argTypes = null;
    /**
     * Called method arguments values.
     */
    public Serializable[] argValues = null;
    /**
     * Return value, which is set by the <strong>root - node</strong> of 
     * current shared object. It could be used to checks that the returned value
     * is the same in all nodes.
     */
    public Serializable retVal = null;
    /**
     * Serial number of current method. Used to sort received method in the 
     * buffer.
     */
    public volatile long operationIndex = -1;

    /**
     * Create new, blank instance of class.
     */
    public MethodCarrier() {
    }

    /**
     * Create new instance with given method name.
     * @param genericMethodName
     */
    public MethodCarrier(String genericMethodName) {
        this.genericMethodName = genericMethodName;
    }

    /**
     * Returns the types of arguments for related method.
     * @return argument types.
     */
    public Class[] getArgTypes() {
        return argTypes;
    }

    /**
     * Set arguments types.
     * @param argTypes an class array that refers arguments types for related method.
     */
    public void setArgTypes(Class[] argTypes) {
        this.argTypes = argTypes;
    }
    /**
     * Set up returned value for related method.
     * @param retVal the result of invoked method.
     */
    public void setRetVal(Serializable retVal){
        this.retVal = retVal;
    }
    /**
     * Returns returned value from invoked method.
     * @return returned value of invoked method or <tt>null</tt>
     */
    public Object getRetVal(){
        return this.retVal;
    }

    /**
     * Returns arguments values related with current method.
     * @return arguments as <tt>Serializable</tt> array or <tt>null</tt> if the 
     * method has no arguments. 
     */
    public Serializable[] getArgValues() {
        return argValues;
    }

    /**
     * Sets arguments for related method to given <tt>argValues</tt>
     * @param argValues arguments for related method.
     */
    public void setArgValues(Serializable[] argValues) {
        this.argValues = argValues;
    }

    /**
     * Returns method name.
     * @return method name as String.
     */
    public String getGenericMethodName() {
        return genericMethodName;
    }

    /**
     * Set method name by given <tt>genericMethodName</tt>
     * @param genericMethodName method name
     */
    public void setGenericMethodName(String genericMethodName) {
        this.genericMethodName = genericMethodName;
    }

    /**
     * Returns serial number of current operation.
     * @return serial number as <tt>long</tt>
     */
    public long getOperationIndex() {
        return this.operationIndex;
    }

    /**
     * Set operation serial number for current operation.
     * @param operationIndex 
     */
    public void setOperationIndex(long operationIndex) {
        if(log.isTraceEnabled()){
            log.trace("Changing operation index from: "+this.operationIndex+", to: "+operationIndex+", on: "+this.toString());
        }
        this.operationIndex = operationIndex;
    }
    /**
     * Writes object to given stream.
     * @param ostr stream to write current object.
     * @throws IOException 
     */
    private void writeObject(ObjectOutputStream ostr) throws IOException {
        ostr.writeUTF(this.genericMethodName);
        
        ostr.writeLong(this.operationIndex);
        if (this.retVal != null) {      
            ostr.writeShort(1);            
            ostr.writeObject(this.retVal);
        }else
            ostr.writeShort(-1);      
        
        if (this.argTypes != null && this.argTypes.length > 0) {
            ostr.writeInt(this.argTypes.length);
            for (int i = 0; i < argTypes.length; i++) {
                ostr.writeObject(this.argTypes[i]);
            }
        } else {
            ostr.writeInt(0);
        }

        if (this.argValues != null && this.argValues.length > 0) {
            ostr.writeInt(this.argValues.length);
            for (int i = 0; i < argValues.length; i++) {
                ostr.writeObject(this.argValues[i]);
            }
        } else {
            ostr.writeInt(0);
        }
        
        //else  ostr.write(0);
    }

    /**
     * Reads <tt>MethodCarrier</tt> object from given stream.
     * @param oistr
     * @throws IOException 
     */
    private void readObject(ObjectInputStream oistr) throws IOException {
        this.genericMethodName = oistr.readUTF();
        this.operationIndex = oistr.readLong();
        
        short zz = oistr.readShort();
        if(zz==1){
            try {
                this.retVal = (Serializable) oistr.readObject();
            } catch (Exception ex) {
                this.retVal = null;
            }
        }
        
        int z = oistr.readInt();
        if (z > 0) {
            Class[] argT = new Class[z];
            for (int i = 0; i < argT.length; i++) {
                try {
                    argT[i] = (Class) oistr.readObject();
                } catch (ClassNotFoundException ex) {
                    throw new IOException(ex.getMessage());
                }
            }
            this.argTypes = argT;
        } else {
            this.argTypes = null;
        }

        z = oistr.readInt();
        if (z > 0) {
            Serializable[] argV = new Serializable[z];
            for (int i = 0; i < argV.length; i++) {
                try {
                    argV[i] = (Serializable) oistr.readObject();
                } catch (ClassNotFoundException ex) {
                    throw new IOException(ex.getMessage());
                }
            }
            this.argValues = argV;
        } else {
            this.argValues = null;
        }
        
    }
    private ByteArrayOutputStream ostream;
    private DataOutputStream dostr;
    private ObjectOutputStream ostr;
    private byte[] msgToByte;

    /**
     * Prepares response for sending.
     * @return Bytes to send.
     * @deprecated 
     */
    @Deprecated
    public byte[] encode() {
        ostream = new ByteArrayOutputStream();
        dostr = new DataOutputStream(ostream);
        msgToByte = null;
        try {
            //writing request specific message contents
            dostr.writeUTF(this.genericMethodName);
            ostr = new ObjectOutputStream(dostr);
            if (this.argTypes != null && this.argTypes.length > 0) {
                dostr.writeInt(this.argTypes.length);
                for (int i = 0; i < argTypes.length; i++) {
                    ostr.writeObject(this.argTypes[i]);
                }
            } else {
                dostr.write(0);
            }

            if (this.argValues != null && this.argValues.length > 0) {
                dostr.writeInt(this.argValues.length);
                for (int i = 0; i < argValues.length; i++) {
                    ostr.writeObject(this.argValues[i]);
                }
            } else {
                dostr.write(0);
            }
            msgToByte = null;
            msgToByte = ostream.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                ostream.close();
                ostream = null;
                ostr = null;
            } catch (IOException ex) {
            }
        }
        return msgToByte;
    }
//    private ByteArrayInputStream istream;
//    private DataInputStream distr;
//    private ObjectInputStream oistr;
//
//    /**
//     * Parses type-dependent message contents.
//     * @param stream Received byte buffer.
//     * @param offset Number of bytes reserved for headers (they will be skipped while
//     * 				 parsing).
//     * @deprecated 
//     */
//    @Deprecated
//    public void parse(byte[] stream, int offset) {
//        istream = new ByteArrayInputStream(stream);
//        distr = new DataInputStream(istream);
//        oistr = null;
//        try {
//            //skips 4 more bytes because super class read one int in super.parse(stream, offset);
//            distr.skip(offset);
//            this.genericMethodName = distr.readUTF();
//            oistr = new ObjectInputStream(istream);
//            int z = distr.readInt();
//            if (z > 0) {
//                Class[] argT = new Class[z];
//                for (int i = 0; i < argT.length; i++) {
//                    argT[i] = (Class) oistr.readObject();
//                }
//                this.argTypes = argT;
//            } else {
//                this.argTypes = null;
//            }
//
//            z = distr.readInt();
//            if (z > 0) {
//                Serializable[] argV = new Serializable[z];
//                for (int i = 0; i < argV.length; i++) {
//                    argV[i] = (Serializable) oistr.readObject();
//                }
//                this.argValues = argV;
//            } else {
//                this.argValues = null;
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (oistr != null) {
//                    oistr.close();
//                }
//                oistr = null;
//                istream = null;
//                distr = null;
//            } catch (IOException ex) {
//            }
//        }
//    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(this.genericMethodName);
        sb.append(" -(ArgValues):[");
        if(this.argValues!=null){
        for (int i = 0; i < argTypes.length; i++) {
            sb.append(argValues[i]);
            sb.append(";");            
        }}
        sb.append("] -(retValue):");
        sb.append(retVal);
        sb.append(" -(opIndex):");
        sb.append(operationIndex);
        return sb.toString();
    }
}
