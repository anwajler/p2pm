package pl.edu.pjwstk.mteam.jcsync.collections.implementation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import pl.edu.pjwstk.mteam.jcsync.collections.JCSyncCollectionStateListener;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncCreateCollectionMethod;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncMethod;
import java.util.ArrayList;
import pl.edu.pjwstk.mteam.jcsync.collections.JCSyncAbstractCollection;
import pl.edu.pjwstk.mteam.jcsync.collections.SOLogic;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncWriteMethod;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.Parameter;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;

/**
 * @author Piotr Bucior
 * @version 1.0
 */
public class JCSyncClearCollectionTemplate<K extends SOLogic, V extends SOLogic> /*TODO extends some implementation of collection*/ implements JCSyncAbstractCollection {

    private final static AbstractCollectionsManager cM = AbstractCollectionsManager.getInstance();
    //TODO change type to your collection
    private final Object collection;
    //TODO change declared class to your one, eg HashMap.class
    private final static Class declaredClass = Object.class;
    private final String collectionID_;
    private final Object collectionIdentifier;
    private boolean isInitialised = false;
    private final ArrayList<JCSyncCollectionStateListener> listeners = new ArrayList<JCSyncCollectionStateListener>(5);
    private JCSyncCreateCollectionMethod constructorDetails = null;

    /**
     * Constructs an empty collection
     * @param id collection identifier in P2P overlay.
     */
    protected JCSyncClearCollectionTemplate(Topic collectionID, Constructor collectionConstructor, Object... params) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        this.collectionID_ = collectionID.getID();
        this.collectionIdentifier = collectionID;
        this.collection = (Object) collectionConstructor.newInstance(params);
        this.isInitialised = true;
    }

    /**
     * ====================================================================
     *      See example below how to write read/write method for JCsync
     * ====================================================================
     */

    /**
     * Example method - type "write"
     */
    public void clear() {
        try {
            Method m = declaredClass.getDeclaredMethod("clear");
            JCSyncWriteMethod wM = new JCSyncWriteMethod(this.collectionID_, declaredClass, m.getName());
        } catch (Exception e) {
            throw new IllegalArgumentException("An error while processing write method: " + e.getMessage());
        }
    }

    /**
     * Example method - type "read"
     */
    public boolean containsKey(Object key) {
        try {
            this.readOperation(null);
            return true; // ==> return this.collection.containsKey(key);
        } finally {
            this.unlockRead();
        }
    }

    /**
     * Example method - type "read"
     */
    public boolean containsValue(Object value) {
        try {
            this.readOperation(null);
            return true; // ==> return this.collection.containsValue(value);
        } finally {
            this.unlockRead();
        }
    }

    /**
     * Example method 'write' (from java.util.HashMap)
     * @param key
     * @param value
     * @return
     */
    public Object put(Object key, Object value) {
        try {
            Method m = declaredClass.getDeclaredMethod("put", Object.class, Object.class);
            //Parameter [] prm = Parameter.getParams(key,value);
            JCSyncWriteMethod wM = new JCSyncWriteMethod(this.collectionID_, declaredClass, m.getName(), new Parameter(Object.class, key), new Parameter(Object.class, value));
            return this.writeOperation(wM);
        } catch (Exception e) {
            throw new IllegalArgumentException("An error while processing write method: " + e.getMessage());
        }
    }

    /**
     * Other example method 'write' (from java.util.HashMap)
     * @param key
     * @param value
     * @return
     */
    public void putAll(Map map) {
        try {
            Method m = declaredClass.getDeclaredMethod("putAll", Map.class);
            //Parameter [] prm = Parameter.getParams(key,value);
            JCSyncWriteMethod wM = new JCSyncWriteMethod(this.collectionID_, declaredClass, m.getName(), new Parameter(Map.class, map));
        } catch (Exception e) {
            throw new IllegalArgumentException("An error while processing write method: " + e.getMessage());
        }
    }

    @Override
    public int size() {
        return 0; // ==> return this.collection.size();
    }

    /**
     * =====================================================
     *          Below code should as is
     * =====================================================
     */

    /**
     *
     * @param method
     */
    public Object readOperation(JCSyncMethod method) {
        return null;
    }

    /**
     *
     * @param method
     */
    public Object writeOperation(JCSyncWriteMethod method) throws Exception {
        try {
            return cM.requestOperation(method, collectionID_);
        } finally {
        }
    }

    protected void unlockRead() {
    }

    protected void unlockWrite() {
    }

    public pl.edu.pjwstk.mteam.pubsub.core.Topic getcollectionID() {
        return (Topic) collectionIdentifier;
    }

    public AbstractCollectionsManager getCollectionManager() {
        return cM;
    }

    public Constructor getConstructor(String genericName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Method getMethod(String genericName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Class getDeclaredClass() {
        return declaredClass;
    }

    public Object invokeMethod(Method m,long operationID, boolean localOperation, Object... params) throws Exception {
        try {
        return m.invoke(this.collection, params);
        }
        finally{

        }
    }

    public void addStateListener(JCSyncCollectionStateListener lst) {
        if (!this.listeners.contains(lst)) {
            this.listeners.add(lst);
        }
    }

    public boolean removeStateListener(JCSyncCollectionStateListener lst) {
        return this.listeners.remove(lst);
    }

    public ArrayList<JCSyncCollectionStateListener> getListeners() {
        ArrayList<JCSyncCollectionStateListener> r = new ArrayList<JCSyncCollectionStateListener>();
        r.addAll(this.listeners);
        return r;
    }

    public void setConstructorMethod(JCSyncCreateCollectionMethod method) {
        this.constructorDetails = method;
    }

    public JCSyncCreateCollectionMethod getConstructorDetails() {
        return this.constructorDetails;
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
}
