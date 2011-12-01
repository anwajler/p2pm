package pl.edu.pjwstk.mteam.jcsync.collections.implementation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import pl.edu.pjwstk.mteam.jcsync.collections.JCSyncCollectionStateListener;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncCreateCollectionMethod;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncMethod;
import java.util.ArrayList;
import pl.edu.pjwstk.mteam.jcsync.collections.CollectionMethodInvokingLogic;
import pl.edu.pjwstk.mteam.jcsync.collections.JCSyncAbstractCollection;
import pl.edu.pjwstk.mteam.jcsync.collections.SOLogic;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncWriteMethod;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.Parameter;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;

/**
 * This class is a templeate to write your own implementation of synchronized Collection.
 * <p>
 * Lets see the full code of this template:
 * <pre>
 * {@code 
 
 * }
 * </pre>
 * 
 * @param <K> the {@linkplain pl.edu.pjwstk.mteam.jcsync.collections.SOLogic SOLogic} extended type of keys maintained in this collection
 * @param <V> the {@linkplain pl.edu.pjwstk.mteam.jcsync.collections.SOLogic SOLogic} extended extended type of mapped values
 * @author Piotr Bucior
 * @version 1.0
 */
public class JCSyncClearCollectionTemplate<K extends SOLogic, V extends SOLogic> /*EDIT extends some implementation of collection*/ 
            implements JCSyncAbstractCollection,CollectionMethodInvokingLogic {

    /**
     * collection manager
     */
    private final static AbstractCollectionsManager cM = AbstractCollectionsManager.getInstance();
    /**
     * collection identifier name in the PUB-SUB layer
     */
    private final String collectionID_;
    //EDIT change type to your collection
    /**
     * Core collection object e.g.: 
     * <p>
     * {@code private final static Class declaredClass = HashMap.class}
     */
    private final Object coreCollection;
    //EDIT change declared class to your one, eg HashMap.class
    /**
     * Declared collection class e.g.:
     * <p> {@code private final static Class declaredClass = HashMap.class}
     */
    private final static Class declaredClass = Object.class;
    
    /**
     * collection identifier in the PUB-SUB layer
     */
    private final Object collectionIdentifier;
    /**
     * true if the collection is properly initialised
     */
    private boolean isInitialised = false;
    /**
     * list of the collection listeners
     */
    private final ArrayList<JCSyncCollectionStateListener> listeners = new ArrayList<JCSyncCollectionStateListener>(5);
    /**
     * details of used constructor to make instance of this collection
     */
    private JCSyncCreateCollectionMethod constructorDetails = null;

    /**
     * Constructs an empty collection instance.
     * 
     * <p><b>REMEMBER: </b>This constructor is used by the internal algorithm to create specific collection, to create new collection check
     * {@linkplain pl.edu.pjwstk.mteam.jcsync.collections.implementation.AbstractCollectionsManager#requestCreateCollection(Class collectionClass, String collectionID,EventInvoker.InvokerType type, Parameter... param) requestCreateCollection}
     * @param collectionID collection identifier
     * @param collectionConstructor reflective object which represents collection constructor 
     * @param params contructor arguments
     * @throws InstantiationException throws when the new instance of the collection cannot be instantiated with this constructor
     * @throws IllegalArgumentException throws when the new instance of the collection cannot be instantiated with this arguments
     * @throws IllegalAccessException throws when the application can not reflectively create new instance of collection
     * @throws InvocationTargetException throws by an invoked constructor
     */
    protected JCSyncClearCollectionTemplate(Topic collectionID, Constructor collectionConstructor, Object... params) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        this.collectionID_ = collectionID.getID();
        this.collectionIdentifier = collectionID;
        this.coreCollection = (Object) collectionConstructor.newInstance(params);
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
     * @param key 
     * @return 
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
     * @param value
     * @return  
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
     * @param map 
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
    

    /**
     * =====================================================
     *          Below code should as is
     * =====================================================
     */

    /**
     * {@inheritDoc} 
     */
    @Override
    public Object readOperation(JCSyncMethod method) {
        return null;
    }
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public int size() {
        //EDIT implement it in your class implementation
        return 0;
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public Object writeOperation(JCSyncWriteMethod method) throws Exception {
        try {
            return cM.requestOperation(method, collectionID_);
        } finally {
        }
    }

    /**
     * 
     */
    protected void unlockRead() {
    }

    /**
     * 
     */
    protected void unlockWrite() {
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public pl.edu.pjwstk.mteam.pubsub.core.Topic getcollectionID() {
        return (Topic) collectionIdentifier;
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public AbstractCollectionsManager getCollectionManager() {
        return cM;
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public Constructor getConstructor(String genericName) {
        //EDIT implement it in your class
        throw new UnsupportedOperationException("Unimplemented");
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public Method getMethod(String genericName) {
        //EDIT implement it in your class
        throw new UnsupportedOperationException("Unimplemented");
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public Class getDeclaredClass() {
        return declaredClass;
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public Object invokeMethod(Method m,long operationID, boolean localOperation, Object... params) throws Exception {
        try {
        return m.invoke(this.coreCollection, params);
        }
        finally{

        }
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public void addStateListener(JCSyncCollectionStateListener lst) {
        if (!this.listeners.contains(lst)) {
            this.listeners.add(lst);
        }
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public boolean removeStateListener(JCSyncCollectionStateListener lst) {
        return this.listeners.remove(lst);
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public ArrayList<JCSyncCollectionStateListener> getListeners() {
        ArrayList<JCSyncCollectionStateListener> r = new ArrayList<JCSyncCollectionStateListener>();
        r.addAll(this.listeners);
        return r;
    }
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public JCSyncCreateCollectionMethod getConstructorDetails() {
        return this.constructorDetails;
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public byte[] serialize() {
        //EDIT implement it in your class
        throw new UnsupportedOperationException("Unimplemented");
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public void deserialize(byte[] raw_data) {
        //EDIT implement it in your class
        throw new UnsupportedOperationException("Unimplemented");
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public long getCurrentOperationID() {
        //EDIT implement in your class
        throw new UnsupportedOperationException("Unimplemented");
    }
}
