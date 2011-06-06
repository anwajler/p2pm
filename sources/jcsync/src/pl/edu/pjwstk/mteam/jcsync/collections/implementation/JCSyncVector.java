package pl.edu.pjwstk.mteam.jcsync.collections.implementation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Vector;
import pl.edu.pjwstk.mteam.jcsync.collections.JCSyncCollectionStateListener;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncCreateCollectionMethod;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncMethod;
import pl.edu.pjwstk.mteam.jcsync.collections.JCSyncAbstractCollection;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncWriteMethod;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.Parameter;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;

/**
 * @author Piotr Bucior
 * @version 1.0
 */
    public class JCSyncVector extends java.util.Vector implements JCSyncAbstractCollection {
        private final static AbstractCollectionsManager cM = AbstractCollectionsManager.getInstance();
        private final Vector collection;
        private final String collectionID_;
        private final Topic collectionID;
        private boolean isInitialised = false;
        private static final Class declaredClass = Vector.class;
        private JCSyncCreateCollectionMethod constructorDetails = null;
        
	protected JCSyncVector(Topic collectionID, Constructor collectionConstructor, Object ... params) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
            this.collectionID_ = collectionID.getID();
            this.collectionID =  collectionID;
            this.collection = (Vector) collectionConstructor.newInstance(params);
        }

	public void finalize() throws Throwable {
		super.finalize();
	}
	/**
	 * 
	 * @param method
	 */
	public Object  readOperation(JCSyncMethod method){
            return null;
	}

	/**
	 * 
	 * @param method
	 */
	public Object writeOperation(JCSyncWriteMethod method){
            return null;
	}

	public pl.edu.pjwstk.mteam.pubsub.core.Topic getcollectionID(){
		return null;
	}

    public AbstractCollectionsManager getCollectionManager() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Constructor getConstructor(String genericName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Method getMethod(String genericName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Parameter[] getParams(Object... params) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Class getDeclaredClass() {
        return declaredClass;
    }

    public Object invokeMethod(Method m,long operationID, boolean localOperation, Object... params) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addStateListener(JCSyncCollectionStateListener lst) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeStateListener(JCSyncCollectionStateListener lst) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ArrayList<JCSyncCollectionStateListener> getListeners() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setConstructorMethod(JCSyncCreateCollectionMethod method) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public JCSyncCreateCollectionMethod getConstructorDetails() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public byte[] serialize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void deserialize(byte[] raw_data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getCurrentOperationID() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}//end JCSyncVector