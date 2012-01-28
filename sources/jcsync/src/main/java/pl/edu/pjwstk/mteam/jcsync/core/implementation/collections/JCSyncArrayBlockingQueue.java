//package pl.edu.pjwstk.mteam.jcsync.core.implementation.collections;
//
//import java.io.IOException;
//import java.io.ObjectOutputStream;
//import java.io.Serializable;
//import java.lang.reflect.Method;
//import java.util.Collection;
//import java.util.Iterator;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.TimeUnit;
//import pl.edu.pjwstk.mteam.jcsync.core.JCSyncAbstractSharedObject;
//
///**
// *
// * @author Piotr Bucior
// */
//public class JCSyncArrayBlockingQueue<E> extends ArrayBlockingQueue<E> implements JCsyncNucleusInterface{
//    private Object shared_object = null;
//    public JCSyncArrayBlockingQueue(int capacity, boolean fair, Collection<? extends E> c) {
//        super(capacity, fair, c);
//    }
//
//    public JCSyncArrayBlockingQueue(int capacity, boolean fair) {
//        super(capacity, fair);
//    }
//
//    public JCSyncArrayBlockingQueue(int capacity) {
//        super(capacity);
//    }
//
//    @Override
//    public boolean add(E e) {
//        Boolean retVal = null;
//        try {
//            Class[] aT = {Object.class};
//            Serializable[] aV = {(Serializable) e};            
//            retVal = (Boolean) ((SharedCollectionObject) shared_object).publishWriteOperation("add", aT, aV);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            throw new IllegalArgumentException(ex.getMessage());
//        }
//        return retVal;        
//    }
//    
//    private boolean add_(E e){
//        return super.add(e);
//    }
//
//    @Override
//    public void clear() {
//        throw new IllegalArgumentException("Not supported");
//    }
//
//    @Override
//    public boolean contains(Object o) {
//        return super.contains(o);
//    }
//
//    @Override
//    public int drainTo(Collection<? super E> c) {
//        Integer retVal = null;
//        try {
//            Class[] aT = {Collection.class};
//            Serializable[] aV = {(Serializable) c};            
//            retVal = (Integer) ((SharedCollectionObject) shared_object).publishWriteOperation("drainTo", aT, aV);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            throw new IllegalArgumentException(ex.getMessage());
//        }
//        return retVal.intValue();  
//    }
//    
//    private int drainTo_(Collection<? super E> c) {
//        return super.drainTo(c);
//    }
//
//    @Override
//    public int drainTo(Collection<? super E> c, int maxElements) {
//        Integer retVal = null;
//        try {
//            Class[] aT = {Collection.class, int.class};
//            Serializable[] aV = {(Serializable) c,maxElements};            
//            retVal = (Integer) ((SharedCollectionObject) shared_object).publishWriteOperation("drainTo", aT, aV);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            throw new IllegalArgumentException(ex.getMessage());
//        }
//        return retVal.intValue(); 
//    }
//    
//    private int drainTo_(Collection<? super E> c, int maxElements) {
//        return super.drainTo(c, maxElements);
//    }
//
//    @Override
//    public Iterator<E> iterator() {
//        throw new IllegalArgumentException("Not supported");
//    }
//
//    @Override
//    public boolean offer(E e) {
//        return super.offer(e);
//    }
//
//    @Override
//    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
//        throw new IllegalArgumentException("Not supported");
//    }
//
//    @Override
//    public E peek() {
//        return super.peek();
//    }
//
//    @Override
//    public E poll() {
//        E retVal = null;
//        try {
//            Class[] aT = {};
//            Serializable[] aV = {};            
//            retVal = (E) ((SharedCollectionObject) shared_object).publishWriteOperation("poll", aT, aV);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            throw new IllegalArgumentException(ex.getMessage());
//        }
//        return retVal; 
//    }
//    
//    private E poll_() {
//        return super.poll();
//    }
//    
//    @Override
//    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
//        throw new IllegalArgumentException("Not supported");
////        E retVal = null;
////        try {
////            Class[] aT = {long.class, TimeUnit.class};
////            Serializable[] aV = {timeout, unit};            
////            retVal = (E) ((SharedCollectionObject) shared_object).publishWriteOperation("poll", aT, aV);
////        } catch (Exception ex) {
////            ex.printStackTrace();
////            throw new IllegalArgumentException(ex.getMessage());
////        }
////        return retVal;
//    }
//    
//    
//    private E poll_(long timeout, TimeUnit unit) throws InterruptedException {
//        return super.poll(timeout, unit);
//    }
//    
//    @Override
//    public void put(E e) throws InterruptedException {
//        try {
//            Class[] aT = {Object.class};
//            Serializable[] aV = {(Serializable)e};            
//            ((SharedCollectionObject) shared_object).publishWriteOperation("put", aT, aV);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            throw new IllegalArgumentException(ex.getMessage());
//        }
//    }
//    
//    private void put_(E e) throws InterruptedException {
//        super.put(e);
//    }
//
//    @Override
//    public int remainingCapacity() {
//        return super.remainingCapacity();
//    }
//
//    @Override
//    public boolean remove(Object o) {
//        Boolean retVal = null;
//        try {
//            Class[] aT = {Object.class};
//            Serializable[] aV = {(Serializable) o};            
//            retVal = (Boolean) ((SharedCollectionObject) shared_object).publishWriteOperation("remove", aT, aV);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            throw new IllegalArgumentException(ex.getMessage());
//        }
//        return retVal;  
//    }
//
//    private boolean remove_(Object o) {
//        return super.remove(o);
//    }
//    @Override
//    public int size() {
//        return super.size();
//    }
//
//    @Override
//    public E take() throws InterruptedException {
//        E retVal = null;
//        try {
//            Class[] aT = {};
//            Serializable[] aV = {};            
//            retVal = (E) ((SharedCollectionObject) shared_object).publishWriteOperation("take", aT, aV);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            throw new IllegalArgumentException(ex.getMessage());
//        }
//        return retVal;
//    }
//
//    private E take_() throws InterruptedException {
//        return super.take();
//    }
//    
//    @Override
//    public Object[] toArray() {
//        return super.toArray();
//    }
//
//    @Override
//    public <T> T[] toArray(T[] a) {
//        return super.toArray(a);
//    }
//
//    @Override
//    public String toString() {
//        return super.toString();
//    }
//    
//
//    @Override
//    public void objectCtreated(JCSyncAbstractSharedObject object) {
//        this.shared_object = (SharedCollectionObject) object;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final JCSyncArrayBlockingQueue<E> other = (JCSyncArrayBlockingQueue<E>) obj;
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        return super.hashCode();
//    }
//    private void writeObject(ObjectOutputStream ostr) throws IOException {
//        ArrayBlockingQueue m = (ArrayBlockingQueue) this;
//        ostr.writeObject(m);
//        //else  ostr.write(0);
//    }
//    @Override
//    public Serializable getNucleus() {
//        return (ArrayBlockingQueue) this;
//    }
//
//    @Override
//    public Object invoke(String methodName, Class[] argTypes, Object[] argValues) {
//        methodName = methodName + '_';
//        Object retVal = null;
//        Method m = null;
//        try {
//            if (argTypes != null && argTypes.length > 0) {
//                m = getClass().getDeclaredMethod(methodName, argTypes);
//                m.setAccessible(true);
//                retVal = m.invoke(this, argValues);
//            } else {
//                m = getClass().getDeclaredMethod(methodName);
//                m.setAccessible(true);
//                retVal = m.invoke(this);
//            }
//
//        } catch (Exception e) {
//            retVal = e;
//        }
//        return retVal;
//    }
//    
//    
//    
//}
