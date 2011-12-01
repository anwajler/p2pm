package pl.edu.pjwstk.mteam.jcsync.lang.reflect;
import java.io.Serializable;

/**
 * Abstract JCSyncMethod, contains only basic information about the method.
 * @author Piotr Bucior
 * @version 1.0
 */
public abstract class JCSyncMethod implements Serializable{
    String collectionID ="";
    /**
     * determines, that the method is a <tt>CREATE_COLLECTION</tt> type.
     */
    public static final byte CREATE_COLLECTION = 1;
    /**
     * determines, that the method is a <tt>WRITE_OPERATION</tt> type.
     */
    public static final byte WRITE_OPERATION = 2;
    /**
     * determines, that the method is a <tt>READ_OPERATION/tt> type.
     */
    public static final byte READ_OPERATION = 4;
    byte methodType = 0;

    /**
     * blank constructor
     */
    public JCSyncMethod(){        
    }
    
    /**
     * creates new method by given params.
     * @param collID collection ID, which the method is assigned
     * @param opType method type
     */
    public JCSyncMethod(String collID, byte opType) {
        this.collectionID = collID;
        this.methodType = opType;
    }

    /**
     * returns collection ID
     */
    public String getCollectionID(){
        return this.collectionID;
    }
    /**
     * returns the type of the method
     */
    public byte getType(){
        return this.methodType;
    }
}//end JCSyncMethod
