package pl.edu.pjwstk.mteam.jcsync.core.implementation.collections;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncAbstractSharedObject;

/**
 * A subclass of <tt>ArrayList<E></tt> which provides synchronisation mechanism 
 * over the network layer.
 * 
 * @author Piotr Bucior
 * @serial 
 */
public class JCSyncArrayList<E> extends ArrayList<E> implements JCsyncNucleusInterface{
   
private Object shared_object = null;
    
    
    public JCSyncArrayList(Collection<? extends E> c) {
        super(c);
    }

    public JCSyncArrayList() {
        super();
    }

    public JCSyncArrayList(int initialCapacity) {
        super(initialCapacity);
    }
    
    @Override
    public boolean add(E e) {
        Boolean retVal = false;
        try {
            Class[] aT = {Object.class};
            Serializable[] aV = {(Serializable) e};
            retVal = (Boolean)((SharedCollectionObject) shared_object).publishWriteOperation("add", aT, aV);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException(ex.getMessage());
        }
        return retVal.booleanValue();
    }
    
    private boolean add_(E e) {
        return super.add(e);
    }

    @Override
    public void add(int index, E element) {
        try {
            Class[] aT = {int.class,Object.class};
            Serializable[] aV = {index,(Serializable) element};
            ((SharedCollectionObject) shared_object).publishWriteOperation("add", aT, aV);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException(ex.getMessage());
        }
    }
    
    private void add_(int index, E element) {
        super.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        Boolean retVal = false;
        try {
            Class[] aT = {Collection.class};
            Serializable[] aV = {(Serializable) c};
            retVal = (Boolean)((SharedCollectionObject) shared_object).publishWriteOperation("addAll", aT, aV);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException(ex.getMessage());
        }
        return retVal.booleanValue();
    }
    private boolean addAll_(Collection<? extends E> c) {
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        Boolean retVal = false;
        try {
            Class[] aT = {int.class,Collection.class};
            Serializable[] aV = {index,(Serializable) c};
            retVal = (Boolean)((SharedCollectionObject) shared_object).publishWriteOperation("addAll", aT, aV);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException(ex.getMessage());
        }
        return retVal.booleanValue();
    }
    
    private boolean addAll_(int index, Collection<? extends E> c) {
        return super.addAll(index, c);
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
        ArrayList retVal = new ArrayList(this.size());
        retVal.addAll(this);
        return retVal;
    }

    @Override
    public boolean contains(Object o) {
        return super.contains(o);
    }

    @Override
    public void ensureCapacity(int minCapacity) {
        super.ensureCapacity(minCapacity);
//        try {
//            Class[] aT = {Integer.class};
//            Serializable[] aV = {minCapacity};
//            ((SharedCollectionObject) shared_object).publishWriteOperation("ensureCapacity", aT, aV);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            throw new IllegalArgumentException(ex.getMessage());
//        }
    }
    
    private void ensureCapacity_(int minCapacity) {
        super.ensureCapacity(minCapacity);
    }

    @Override
    public E get(int index) {
        return super.get(index);
    }

    @Override
    public int indexOf(Object o) {
        return super.indexOf(o);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public int lastIndexOf(Object o) {
        return super.lastIndexOf(o);
    }

    @Override
    public E remove(int index) {
        E retVal = null;
        try {
            Class[] aT = {int.class};
            Serializable[] aV = {index};
            retVal = (E) ((SharedCollectionObject) shared_object).publishWriteOperation("remove", aT, aV);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
        return retVal;
    }

    private E remove_(int index) {
        return super.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        Boolean retVal = false;
        try {
            Class[] aT = {Object.class};
            Serializable[] aV = {(Serializable)o};
            retVal = (Boolean)((SharedCollectionObject) shared_object).publishWriteOperation("remove", aT, aV);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException(ex.getMessage());
        }
        return retVal.booleanValue();
    }
    private boolean remove_(Object o) {
        return super.remove(o);
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        try {
            Class[] aT = {int.class,int.class};
            Serializable[] aV = {fromIndex,toIndex};
            ((SharedCollectionObject) shared_object).publishWriteOperation("removeRange", aT, aV);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }
    
    private void removeRange_(int fromIndex, int toIndex) {
        super.removeRange(fromIndex, toIndex);
    }

    @Override
    public E set(int index, E element) {
        E retVal = null;
        try {
            Class[] aT = {int.class,Object.class};
            Serializable[] aV = {index,(Serializable)element};
            retVal = (E) ((SharedCollectionObject) shared_object).publishWriteOperation("set", aT, aV);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
        return retVal;
    }
    private E set_(int index, E element) {
        return super.set(index, element);
    }

    @Override
    public int size() {
        return super.size();
    }

    @Override
    public Object[] toArray() {
        return super.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return super.toArray(a);
    }

    @Override
    public void trimToSize() {
        super.trimToSize();
    }
    


    //--------------------------------------------------
    //JCSync code
    //--------------------------------------------------

    @Override
    public Serializable getNucleus() {
        return (ArrayList) this;
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
        final JCSyncArrayList<E> other = (JCSyncArrayList<E>) obj;
        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
    /**
     * Writes this object to the stream as a super class.<br>
     * <pre>
     * ArrayList m = (ArrayList) this;
       ostr.writeObject(m);
     * </pre>
     */
    private void writeObject(ObjectOutputStream ostr) throws IOException {
        ArrayList m = (ArrayList) this;
        ostr.writeObject(m);
        //else  ostr.write(0);
    }
    
}
