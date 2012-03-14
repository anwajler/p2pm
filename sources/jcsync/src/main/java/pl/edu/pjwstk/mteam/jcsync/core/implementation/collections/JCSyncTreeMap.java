package pl.edu.pjwstk.mteam.jcsync.core.implementation.collections;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncAbstractSharedObject;

/**
 * A <tt>TreeMap</tt> implementation.
 * @author Piotr Bucior
 * @serial
 */
public class JCSyncTreeMap<K,V> extends TreeMap<K,V> implements JCsyncNucleusInterface,Serializable{
private Object shared_object = null;

    @Deprecated
    public JCSyncTreeMap(SortedMap<K, ? extends V> m) {
        super(m);        
    }
    
    @Deprecated
    public JCSyncTreeMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    public JCSyncTreeMap(Comparator<? super K> comparator) {
        super(comparator);
    }

    public JCSyncTreeMap() {
        super();
    }

    @Override
    public Serializable getNucleus() {
        return (TreeMap) this;
    }
    
    @Override
    public Object invoke(String methodName, Class[] argTypes, Object[] argValues, boolean local) {
        methodName = methodName + '_';
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
        final TreeMap<K,V> other = (TreeMap<K,V>) obj;
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
    /**
     * Writes this object to the stream as a super class.
     */
    private void writeObject(ObjectOutputStream ostr) throws IOException {
        TreeMap m = (TreeMap) this;
        ostr.writeObject(m);
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

    
    @Override
    public Object clone() {
        TreeMap retVal = new TreeMap(this);
        return retVal;
    }

    @Override
    public Comparator<? super K> comparator() {
        return super.comparator();
    }

    @Override
    public boolean containsKey(Object key) {
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
    public K firstKey() {
        return super.firstKey();
    }

    @Override
    public V get(Object key) {
        return super.get(key);
    }

    @Override
    public SortedMap<K, V> headMap(K toKey) {
        return super.headMap(toKey);
    }

    @Override
    public Set<K> keySet() {
        return super.keySet();
    }

    @Override
    public K lastKey() {
        return super.lastKey();
    }

    @Override
    public Entry<K, V> lowerEntry(K key) {
        return super.lowerEntry(key);
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        return super.pollLastEntry();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        super.putAll(map);
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

    public V remove_(K key) {
        return super.remove(key);
    }
    
    

    @Override
    public int size() {
        return super.size();
    }

    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        return super.subMap(fromKey, toKey);
    }

    @Override
    public SortedMap<K, V> tailMap(K fromKey) {
        return super.tailMap(fromKey);
    }

    @Override
    public Collection<V> values() {
        return super.values();
    }

    @Override
    public V put(K key, V value) {
        Object retVal = null;
        try {
            Class[] aT = {Object.class,Object.class};
            Serializable[] aV = {(Serializable) key,(Serializable) value};
            retVal = ((SharedCollectionObject) shared_object).publishWriteOperation("put", aT, aV);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException(ex.getMessage());
        }
        if(retVal!=null)
        return (V)retVal;
        else return null;
    }

    public V put_(K key, V value) {
        return super.put(key, value);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public String toString() {
        return super.toString();
    }
    
    
}
