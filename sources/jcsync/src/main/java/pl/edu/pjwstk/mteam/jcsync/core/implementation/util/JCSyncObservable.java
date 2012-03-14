package pl.edu.pjwstk.mteam.jcsync.core.implementation.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Observable;
import java.util.Observer;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncAbstractSharedObject;
import pl.edu.pjwstk.mteam.jcsync.core.implementation.collections.JCsyncNucleusInterface;


/**
 *
 * @author Piotr Bucior
 * @serial
 */
public class JCSyncObservable extends Observable implements JCsyncNucleusInterface{
    private Object shared_object = null;
    
    public JCSyncObservable(){
        super();
    }
    @Override
    public void addObserver(Observer o) {
        super.addObserver(o);
    }
    
    private void addObserver_(Observer o) {
        super.addObserver(o);
    }

    @Override
    protected void clearChanged() {
        super.clearChanged();
    }

    @Override
    public int countObservers() {
        return super.countObservers();
    }

    @Override
    public void deleteObserver(Observer o) {
        super.deleteObserver(o);
    }

    @Override
    public void deleteObservers() {
        super.deleteObservers();
    }

    @Override
    public boolean hasChanged() {
        return super.hasChanged();
    }

    @Override
    public void notifyObservers() {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public void notifyObservers(Object arg) {
        try {
            Class[] aT = {Object.class};
            Serializable[] aV = {(Serializable) arg};
            
            ((SharedObservableObject) shared_object).publishWriteOperation("notifyObservers", aT, aV);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException(ex.getMessage());
        }
    }
    private void notifyObservers_(Object arg) {
        super.notifyObservers(arg);
    }

    @Override
    protected void setChanged() {
        super.setChanged();
    }

    @Override
    public Serializable getNucleus() {
        return this;
    }

    @Override
    public Object invoke(String methodName, Class[] argTypes, Object[] argValues, boolean local) {
        methodName = methodName + '_';
        Method[] allMethods = getClass().getDeclaredMethods();
        Object retVal = null;
        Method m = null;
        try {
            if (argTypes != null && argTypes.length > 0) {
                setChanged();
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
        this.shared_object = (SharedObservableObject) object;
    }
    /**
     * Writes this object to the stream as a super class.
     */
    private void writeObject(ObjectOutputStream ostr) throws IOException {
        Observable m = (Observable) this;
        ostr.writeObject(m);
    }
    
}
