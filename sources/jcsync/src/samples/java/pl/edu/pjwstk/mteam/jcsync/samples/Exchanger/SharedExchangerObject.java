package pl.edu.pjwstk.mteam.jcsync.samples.Exchanger;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import pl.edu.pjwstk.mteam.jcsync.core.AccessControlLists;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncAbstractSharedObject;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncCore;
import pl.edu.pjwstk.mteam.jcsync.core.consistencyManager.DefaultConsistencyManager;
import pl.edu.pjwstk.mteam.jcsync.core.implementation.collections.JCsyncNucleusInterface;
import pl.edu.pjwstk.mteam.jcsync.exception.ObjectExistsException;
import pl.edu.pjwstk.mteam.jcsync.operation.JCsyncAbstractOperation;
import pl.edu.pjwstk.mteam.jcsync.operation.MethodCarrier;
import pl.edu.pjwstk.mteam.jcsync.operation.RegisteredOperations;

/**
 * @author Piotr Bucior
 */
public class SharedExchangerObject extends JCSyncAbstractSharedObject{

    /**
     * Creates new instance with blank constructor.
     */
    public SharedExchangerObject(){
        
    }
    /**
     * Creates new instance with given arguments.
     * @param name the name of new shared object
     * @param nucleus the object on which we the method are called
     * @param core the core algorithm instance
     * @throws ObjectExistsException if the object with this name is already created in the network 
     * @throws Exception any occurred exception
     */
    public SharedExchangerObject(String name, JCsyncNucleusInterface nucleus, JCSyncCore core) throws ObjectExistsException, Exception{
        super(name, nucleus, core, DefaultConsistencyManager.class, SharedExchangerObject.class);
        nucleus.objectCtreated(this);
    }
    /**
     * Creates new instance with given arguments.
     * @param name the name of new shared object
     * @param nucleus the object on which we the method are called
     * @param core the core algorithm instance
     * @param acRules customised access control rules
     * @throws ObjectExistsException if the object with this name is already created in the network 
     * @throws Exception any occurred exception
     */
    public SharedExchangerObject(String name, JCsyncNucleusInterface nucleus, JCSyncCore core, AccessControlLists acRules) throws ObjectExistsException, Exception{
        super(name, nucleus, core, DefaultConsistencyManager.class, SharedExchangerObject.class,acRules);
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
    public SharedExchangerObject(String name, JCsyncNucleusInterface nucleus, JCSyncCore core, Class consistencyManager) throws ObjectExistsException, Exception{
        super(name, nucleus, core, consistencyManager, SharedExchangerObject.class);
        nucleus.objectCtreated(this);
    }
    /**
     * Creates new instance with given arguments.
     * @param name the name of new shared object
     * @param nucleus the object on which we the method are called
     * @param core the core algorithm instance
     * @param consistencyManager class of consistency manager which will hold this object
     * @param acRules customised access control rules
     * @throws ObjectExistsException if the object with this name is already created in the network 
     * @throws Exception any occurred exception
     */
    public SharedExchangerObject(String name, JCsyncNucleusInterface nucleus, JCSyncCore core, Class consistencyManager,AccessControlLists acRules) throws ObjectExistsException, Exception{
        super(name, nucleus, core, consistencyManager, SharedExchangerObject.class,acRules);
        nucleus.objectCtreated(this);
    }
    @Override
    protected Object publishReadOperation(String methodName, Class[] argTypes, Serializable[] argValues) throws Exception {
        return super.publishReadOperation(methodName, argTypes, argValues);
    }

    @Override
    protected Object publishWriteOperation(String methodName, Class[] argTypes, Serializable[] argValues) throws Exception {
        //return super.publishWriteOperation(methodName, argTypes, argValues);
        MethodCarrier mc = new MethodCarrier(methodName);
        mc.setArgTypes(argTypes);
        mc.setArgValues(argValues);
        JCsyncAbstractOperation op = JCsyncAbstractOperation.getByType(RegisteredOperations.OP_REQ_WRITE_METHOD, super.getID(), mc,this.coreAlg.getNodeInfo().getName());
        //this.coreAlg.getConsistencyManager(this.ID).beforeRequestSend(op, true);
        this.coreAlg.sendMessage(op, false);
        //Object e = this.coreAlg.getConsistencyManager(this.ID).afterRequestSend(op, true);
        //if(e!= null && e instanceof Exception) throw (Exception)e;
        //else return e;
        return null;
    }

    @Override
    protected Object invokeReadOperation(String methodName, Class[] argTypes, Object[] argValues) throws SecurityException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return ((JCsyncNucleusInterface)getNucleusObject()).invoke(methodName, argTypes, argValues, false);
    }

    @Override
    protected Object invokeWriteOperation(String methodName, Class[] argTypes, Object[] argValues, boolean local) throws SecurityException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        super.nextOperationID();
        return ((JCsyncNucleusInterface)getNucleusObject()).invoke(methodName, argTypes, argValues, local);
    }
    
    /**
     * Returns an <tt>JCSyncNucleusInterface</tt> extended object.
     * @return object associated with current shared object.
     */
    public Object getNucleusObject(){
        return super.getNucleus();
    }

    /**
     * Sets nucleus object for current shared object.
     * @param nucleus an <tt>JCSyncNucleusInterface</tt> extension.
     */
    @Override
    protected void setNucleus(Serializable nucleus) {
        super.setNucleus(nucleus);
        ((JCsyncNucleusInterface)nucleus).objectCtreated(this);
    }
    
}
