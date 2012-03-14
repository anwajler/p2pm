package pl.edu.pjwstk.mteam.jcsync.core.implementation.collections;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncAbstractSharedObject;

/**
 * <tt>HashMap</tt> implementation.
 * @author Piotr Bucior
 * @serial
 */
public class JCSyncHashMap<K, V> extends HashMap<K, V> implements JCsyncNucleusInterface {

    private Object shared_object = null;

    public JCSyncHashMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    public JCSyncHashMap() {
        super();
        this.shared_object = null;
    }

    public JCSyncHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public JCSyncHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    @Override
    public void clear() {
        try {
            ((SharedCollectionObject) shared_object).publishWriteOperation("clear", null, null);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    private void clear_() {
        super.clear();
    }

    /**
     * Clone this JCSyncHashMap to clean java.util.hashMap
     * @return 
     */
    @Override
    public Object clone() {
        HashMap retVal = new HashMap(super.size());         
                retVal.putAll(this);
        return retVal;
    }

    @Override
    public boolean containsKey(Object key) {
        //we dont want to send any 'read' operation via overlay
        return super.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return super.containsValue(value);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {   
        return super.entrySet();
    }

    @Override
    public V get(Object key) {
        return super.get(key);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        return super.keySet();
    }

    @Override
    public V put(K key, V value) {
        @SuppressWarnings("unchecked")
        V retVal = null;
        try {
            Class[] aT = {Object.class,Object.class};
            Serializable[] aV = {(Serializable) key,(Serializable) value};
            
            retVal = (V) ((SharedCollectionObject) shared_object).publishWriteOperation("put", aT, aV);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException(ex.getMessage());
        }
        return retVal;
    }

    private V put_(K key, V value) {
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        super.putAll(m);
//        try {
//            Class[] aT = {Map.class};
//            Serializable[] aV = {(Serializable) m};
//            ((SharedCollectionObject) shared_object).publishWriteOperation("putAll", aT, aV);
//        } catch (Exception ex) {
//            throw new IllegalArgumentException(ex.getMessage());
//        }
    }

    private void putAll_(Map<? extends K, ? extends V> m) {
        super.putAll(m);
    }

    @Override
    public V remove(Object key) {
        V retVal = null;
        try {
            Class[] aT = {Object.class};
            Serializable[] aV = {(Serializable) key};
            retVal = (V) ((SharedCollectionObject) shared_object).publishWriteOperation("remove", aT, aV);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
        return retVal;
    }

    private V remove_(K key) {
        return super.remove(key);
    }

    @Override
    public int size() {
        return super.size();
    }

    @Override
    public Collection<V> values() {
        return super.values();
    }

    @Override
    public Serializable getNucleus() {
        return (HashMap) this;
    }

    @Override
    public Object invoke(String methodName, Class[] argTypes, Object[] argValues, boolean local) {
        methodName = methodName + '_';
        Method[] allMethods = getClass().getDeclaredMethods();
        Object retVal = null;
        Method m = null;
        try {
            if (argTypes != null && argTypes.length > 0) {
                m = getClass().getDeclaredMethod(methodName, argTypes);
                m.setAccessible(true);
                retVal = m.invoke(this, argValues);
            } else {
                m = getClass().getDeclaredMethod(methodName);
                m.setAccessible(true);
                retVal = m.invoke(this);
            }

        } catch (Exception e) {
            retVal = e;
        }
        return retVal;
    }

    @Override
    public void objectCtreated(JCSyncAbstractSharedObject object) {
        this.shared_object = (SharedCollectionObject) object;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JCSyncHashMap<K, V> other = (JCSyncHashMap<K, V>) obj;
        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
    /**
     * Writes this object to the stream as a super class.
     */
    private void writeObject(ObjectOutputStream ostr) throws IOException {
        HashMap m = (HashMap) this;
        ostr.writeObject(m);
        //else  ostr.write(0);
    }
//    private void readObject(ObjectInputStream oistr) throws IOException {
//        try {
//            this = (JCSyncHashMap<K, V>) oistr.readObject();
//        } catch (ClassNotFoundException ex) {
//            Logger.getLogger(JCSyncHashMap.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
}
