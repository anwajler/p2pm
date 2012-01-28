
package pl.edu.pjwstk.mteam.jcsync.core.implementation.util;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncAbstractSharedObject;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncCore;
import pl.edu.pjwstk.mteam.jcsync.core.consistencyManager.DefaultConsistencyManager;
import pl.edu.pjwstk.mteam.jcsync.core.implementation.collections.JCsyncNucleusInterface;
import pl.edu.pjwstk.mteam.jcsync.core.implementation.collections.SharedCollectionObject;
import pl.edu.pjwstk.mteam.jcsync.exception.ObjectExistsException;

/**
 * An extension of {@link JCSyncAbstractSharedObject JCSyncAbstractSharedObject}
 * created for the purposes of implemented Observable mechanism.
 * @author Piotr Bucior
 */
public class SharedObservableObject extends JCSyncAbstractSharedObject{
    /**
     * Creates new instance with blank constructor.
     */
    public SharedObservableObject(){
        
    }
    /**
     * Creates new instance with given arguments.
     * @param name the name of new shared object
     * @param nucleus the object on which we the method are called
     * @param core the core algorithm instance
     * @throws ObjectExistsException if the object with this name is already created in the network 
     * @throws Exception any occurred exception
     */
    public SharedObservableObject(String name, JCsyncNucleusInterface nucleus, JCSyncCore core) throws ObjectExistsException, Exception{
        super(name, nucleus, core, DefaultConsistencyManager.class, SharedObservableObject.class);
        nucleus.objectCtreated(this);
    }
    /**
     * Creates new instance with given arguments.
     * @param name the name of new shared object
     * @param nucleus the object on which we the method are called
     * @param core the core algorithm instance
     * @param consistencyManager class of consistency manager which will hold this object
     * @throws ObjectExistsException if the object with this name is already created in the network 
     * @throws Exception any occurred exception
     */
    public SharedObservableObject(String name, JCsyncNucleusInterface nucleus, JCSyncCore core, Class consistencyManager) throws ObjectExistsException, Exception{
        super(name, nucleus, core, consistencyManager, SharedObservableObject.class);
        nucleus.objectCtreated(this);
    }
    @Override
    protected Object publishReadOperation(String methodName, Class[] argTypes, Serializable[] argValues) throws Exception {
        return super.publishReadOperation(methodName, argTypes, argValues);
    }

    @Override
    protected Object publishWriteOperation(String methodName, Class[] argTypes, Serializable[] argValues) throws Exception {
        return super.publishWriteOperation(methodName, argTypes, argValues);
    }

    @Override
    protected Object invokeReadOperation(String methodName, Class[] argTypes, Object[] argValues) throws SecurityException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return ((JCsyncNucleusInterface)getNucleusObject()).invoke(methodName, argTypes, argValues);
    }

    @Override
    protected Object invokeWriteOperation(String methodName, Class[] argTypes, Object[] argValues) throws SecurityException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        super.nextOperationID();
        return ((JCsyncNucleusInterface)getNucleusObject()).invoke(methodName, argTypes, argValues);
    }
    /**
     * Returns an <tt>JCSyncNucleusInterface</tt> extended object.
     * @return object associated with current shared object.
     */
    public Object getNucleusObject(){
        return super.getNucleus();
    }

    @Override
    protected void setNucleus(Serializable nucleus) {
        super.setNucleus(nucleus);
        ((JCsyncNucleusInterface)nucleus).objectCtreated(this);
    }
    
}
