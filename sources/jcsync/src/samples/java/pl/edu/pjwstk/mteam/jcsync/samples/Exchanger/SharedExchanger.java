package pl.edu.pjwstk.mteam.jcsync.samples.Exchanger;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.Exchanger;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncAbstractSharedObject;
import pl.edu.pjwstk.mteam.jcsync.core.implementation.collections.JCsyncNucleusInterface;

/**
 *
 * @author Piotr Bucior
 */
public class SharedExchanger<V> extends Exchanger<V> implements JCsyncNucleusInterface{
    private Object shared_object = null;
    
    public SharedExchanger(){
        super();
    }
    
    @Override
    public V exchange(V x) throws InterruptedException {
        @SuppressWarnings("unchecked")
        V retVal = null;
        try {
            Class[] aT = {Object.class};
            Serializable[] aV = {(Serializable) x};
            
            retVal = (V) ((SharedExchangerObject) shared_object).publishWriteOperation("exchange", aT, aV);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException(ex.getMessage());
        }
        return super.exchange(x);        
    }
    public V exchange_(V x) throws InterruptedException {
        return super.exchange(x);        
    }

    @Override
    public Serializable getNucleus() {
        return this;
    }

    @Override
    public Object invoke(String methodName, Class[] argTypes, Object[] argValues, boolean local) {
        Object retVal = null;
        if(!local){
            System.out.println("method will be invoked");
        methodName = methodName + '_';
        Method[] allMethods = getClass().getDeclaredMethods();        
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
        else{
            System.out.println("method will not be invoked");
        }
        return retVal;
    }

    @Override
    public void objectCtreated(JCSyncAbstractSharedObject object) {
        this.shared_object = (SharedExchangerObject) object;
    }
    private void writeObject(ObjectOutputStream ostr) throws IOException {
        Exchanger m = (Exchanger) this;
        ostr.writeObject(m);
    }
}
