package pl.edu.pjwstk.mteam.jcsync.collections;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import pl.edu.pjwstk.mteam.jcsync.collections.implementation.AbstractCollectionsManager;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncCreateCollectionMethod;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncMethod;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncWriteMethod;

/**
 * @author Piotr Bucior
 * @version 1.0
 */
public interface JCSyncAbstractCollection {
	/**
	 * 
	 * @param method
	 */
	public Object readOperation(JCSyncMethod method);
        /**
         * todo hide methods:
         * readOp.., writeOp... , invokeMethod, 
         */

	/**
	 * 
	 * @param method
	 */
	public Object writeOperation(JCSyncWriteMethod method) throws Exception;

	public pl.edu.pjwstk.mteam.pubsub.core.Topic getcollectionID();

        public AbstractCollectionsManager getCollectionManager();
        public Constructor getConstructor(String genericName);
        public Method getMethod(String genericName);
        public Class getDeclaredClass();
        public Object invokeMethod(Method m,long operationID,boolean localOperation, Object ... params)throws Exception;
        public void addStateListener(JCSyncCollectionStateListener lst);
        public boolean removeStateListener(JCSyncCollectionStateListener lst);
        public ArrayList<JCSyncCollectionStateListener> getListeners();
        public int size();

        //public void setConstructorMethod(JCSyncCreateCollectionMethod method);
        public JCSyncCreateCollectionMethod getConstructorDetails();
        public byte[] serialize();
        public void deserialize(byte [] raw_data);
        public long getCurrentOperationID();        
}