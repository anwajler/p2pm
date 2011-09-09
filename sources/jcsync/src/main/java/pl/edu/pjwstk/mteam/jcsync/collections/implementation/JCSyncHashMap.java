package pl.edu.pjwstk.mteam.jcsync.collections.implementation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import pl.edu.pjwstk.mteam.jcsync.collections.JCSyncCollectionStateListener;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncCreateCollectionMethod;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncMethod;
import java.util.HashMap;
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
public class JCSyncHashMap<K extends SOLogic, V extends SOLogic> extends java.util.HashMap implements JCSyncAbstractCollection {

    private final static AbstractCollectionsManager cM = AbstractCollectionsManager.getInstance();
    private HashMap collection;
    private final static Class declaredClass = HashMap.class;
    private final String collectionID_;
    private final Object collectionIdentifier;
    private boolean isInitialised = false;
    private final ArrayList<JCSyncCollectionStateListener> listeners = new ArrayList<JCSyncCollectionStateListener>(5);
    private final JCSyncCreateCollectionMethod constructorDetails;
    private volatile long operationID = 0;
    /**
     * Constructs an empty HashMap with the default initial capacity (16) and the default load factor (0.75).
     * @param id collection identifier in P2P overlay.
     */
    protected JCSyncHashMap(Topic collectionID, Constructor collectionConstructor, JCSyncCreateCollectionMethod jcsDetails, Object... params) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        this.collectionID_ = collectionID.getID();
        this.collectionIdentifier = collectionID;
        this.constructorDetails = jcsDetails;
        if (this.constructorDetails.getAdditionalData() != null) {
            deserialize(this.constructorDetails.getAdditionalData());
        }else{
            this.collection = (HashMap) collectionConstructor.newInstance(params);
        }
        this.isInitialised = true;
    }

//        public JCSyncHashMap(String id){
//            this.collection = new HashMap();
//            this.collectionID_ = id;
//            cM.requestCreateCollection(this, HashMap.class, null);
//	}
    /**
     * Constructs an empty HashMap with the specified initial capacity and the default load factor (0.75).
     * @param id collection identifier in P2P overlay.
     * @param initialCapacity initial capacity
     */
//        public JCSyncHashMap(String id, int initialCapacity){
//            this.collection = new HashMap(initialCapacity);
//            this.collectionID_ = id;
//	}
    /**
     * Constructs a new HashMap with the same mappings as the specified HashMap
     * @param id collection identifier in P2P overlay.
     * @param original the hashmap whose mappings are to be placed in this map.
     * @throws IllegalStateException - this constructor is not implemented yet.
     */
//        @Deprecated
//        public JCSyncHashMap(String id, HashMap original){
//            //this.collection = new HashMap(original);
//            //this.collectionID_ = id;
//            throw new IllegalStateException("Not implemented yet");
//	}
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
            return JCSyncHashMap.cM.requestOperation(method, collectionID_);
        } finally {
        }
    }

    protected void unlockRead() {
    }

    protected void unlockWrite() {
    }

    public HashMap test_getCollection() {
        return this.collection;
    }

    public String test_getCollectionID_() {
        return this.collectionID_;
    }

    public pl.edu.pjwstk.mteam.pubsub.core.Topic getcollectionID() {
        return (Topic) collectionIdentifier;
    }

    // ================================================
    // BELOW METHODS INHERITED FROM java.util.HashMap
    // ================================================
    /**
     * Removes all mappings from this map.
     * @see HashMap
     */
    @Override
    public void clear() {
        try {
            Method m = JCSyncHashMap.declaredClass.getDeclaredMethod("clear");
            JCSyncWriteMethod wM = new JCSyncWriteMethod(this.collectionID_, declaredClass, m.getName());
            this.writeOperation(wM);
        } catch (Exception e) {
            throw new IllegalArgumentException("An error while processing write method: " + e.getMessage());
        }
    }

    /**
     * Returns a shallow copy of this HashMap instance: the keys and values themselves are not cloned.
     * @return shallow copy of this hashmap
     */
    @Override
    @Deprecated
    public HashMap clone() {
        throw new IllegalStateException("Not allowed");
    }

    /**
     * Returns true if this map contains a mapping for the specified key.
     */
    @Override
    public boolean containsKey(Object key) {
        try {
            this.readOperation(null);
            return this.collection.containsKey(key);
        } finally {
            this.unlockRead();
        }
    }

    /**
     * Returns true if this map maps one or more keys for specified value.
     */
    @Override
    public boolean containsValue(Object value) {
        try {
            this.readOperation(null);
            return this.collection.containsValue(value);
        } finally {
            this.unlockRead();
        }
    }

    /**
     * Returns a collection view of the mappings contained in this map.
     */
    @Override
    public Set entrySet() {
        try {
            this.readOperation(null);
            return this.collection.entrySet();
        } finally {
            this.unlockRead();
        }
    }

    /**
     * @see java.util.HashMap.equals(Object o)
     */
    @Override
    public boolean equals(Object o) {
        try {
            this.readOperation(null);
            return this.collection.equals(o);
        } finally {
            this.unlockRead();
        }
    }

    /**
     * @see java.util.HashMap.finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    /**
     * @see java.util.HashMap.get(Object key)
     */
    @Override
    public Object get(Object key) {
        try {
            this.readOperation(null);
            return this.collection.get(key);
        } finally {
            this.unlockRead();
        }
    }

    /**
     * @see java.util.HashMap.hashCode()
     */
    @Override
    public int hashCode() {
        try {
            this.readOperation(null);
            return this.collection.hashCode();
        } finally {
            this.unlockRead();
        }
    }

    /**
     * @see java.util.HashMap.isEmpty()
     */
    @Override
    public boolean isEmpty() {
        try {
            this.readOperation(null);
            return this.collection.isEmpty();
        } finally {
            this.unlockRead();
        }
    }

    /**
     * @see java.util.HashMap.keySet()
     */
    @Override
    public Set keySet() {
        try {
            this.readOperation(null);
            return this.collection.keySet();
        } finally {
            this.unlockRead();
        }
    }

    @Override
    public Object put(Object key, Object value) {
        try {
            Method m = JCSyncHashMap.declaredClass.getDeclaredMethod("put", Object.class, Object.class);
            //Parameter [] prm = Parameter.getParams(key,value);
            JCSyncWriteMethod wM = new JCSyncWriteMethod(this.collectionID_, declaredClass, m.getName(), new Parameter(Object.class, key), new Parameter(Object.class, value));
            return this.writeOperation(wM);
        } catch (Exception e) {
            throw new IllegalArgumentException("An error while processing write method: " + e.getMessage());
        }
    }

    @Override
    public void putAll(Map map) {
        try {
            Method m = JCSyncHashMap.declaredClass.getDeclaredMethod("putAll", Map.class);
            //Parameter [] prm = Parameter.getParams(key,value);
            JCSyncWriteMethod wM = new JCSyncWriteMethod(this.collectionID_, declaredClass, m.getName(), new Parameter(Map.class, map));
        } catch (Exception e) {
            throw new IllegalArgumentException("An error while processing write method: " + e.getMessage());
        }
    }
    //TODO implement all below methods

    @Override
    public Object remove(Object key) {
        try {
            Method m = JCSyncHashMap.declaredClass.getDeclaredMethod("remove", Object.class);
            //Parameter [] prm = Parameter.getParams(key,value);
            JCSyncWriteMethod wM = new JCSyncWriteMethod(this.collectionID_, declaredClass, m.getName(), new Parameter(Object.class, key));
            return this.writeOperation(wM);
        } catch (Exception e) {
            throw new IllegalArgumentException("An error while processing write method: " + e.getMessage());
        }
    }

    @Override
    public Collection values() {
        try {
            this.readOperation(null);
            return this.collection.values();
        } finally {
            this.unlockRead();
        }
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

    @Override
    public Class getDeclaredClass() {
        return declaredClass;
    }

    public Object invokeMethod(Method m,long operationID, boolean localOperation, Object... params) throws Exception {
        try{
        return m.invoke(this.collection, params);
        }finally{
            this.operationID = operationID;
        }
    }

    @Override
    public int size() {
        return this.collection.size();
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

//    public void setConstructorMethod(JCSyncCreateCollectionMethod method) {
//        this.constructorDetails = method;
//    }
    public JCSyncCreateCollectionMethod getConstructorDetails() {
        return this.constructorDetails;
    }
    private byte[] raw_data;

    public byte[] serialize() {
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        //DataOutputStream dtstr = new DataOutputStream(ostream);
        ObjectOutputStream ostr = null;
        raw_data = null;
        try {
            ostr = new ObjectOutputStream(ostream);
            ostr.writeObject(this.collection);
            raw_data = ostream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                ostream.close();
            } catch (IOException ex) {
            }
            ostream = null;
            ostr = null;
        }
        return raw_data;
    }
    public final void deserialize(byte[] data) {
        ByteArrayInputStream istream = new ByteArrayInputStream(data);
        ObjectInputStream ostr_ = null;
        try {
            ostr_ = new ObjectInputStream(istream);
            this.collection = (HashMap) ostr_.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            try {
                if(ostr_!=null)
                ostr_.close();
            } catch (IOException ex) {
            }
            istream = null;
            ostr_ = null;
        }
    }

    public long getCurrentOperationID() {
        return this.operationID;
    }
}
