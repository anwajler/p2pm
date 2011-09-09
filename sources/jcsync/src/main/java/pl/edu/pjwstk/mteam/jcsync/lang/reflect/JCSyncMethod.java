package pl.edu.pjwstk.mteam.jcsync.lang.reflect;
import java.io.Serializable;

/**
 * @author Piotr Bucior
 * @version 1.0
 */
public abstract class JCSyncMethod implements Serializable{
    String collectionID ="";
    public static final byte CREATE_COLLECTION = 1;
    public static final byte WRITE_OPERATION = 2;
    public static final byte READ_OPERATION = 4;
    byte methodType = 0;

    public JCSyncMethod(){        
    }
    
    public JCSyncMethod(String collID, byte opType) {
        this.collectionID = collID;
        this.methodType = opType;
    }

    public String getCollectionID(){
        return this.collectionID;
    }
    public byte getType(){
        return this.methodType;
    }
}//end JCSyncMethod
