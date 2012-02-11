package pl.edu.pjwstk.mteam.jcsync.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.jcsync.exception.ObjectExistsException;
import pl.edu.pjwstk.mteam.jcsync.exception.ObjectNotExistsException;
import pl.edu.pjwstk.mteam.jcsync.exception.OperationForbiddenException;
import pl.edu.pjwstk.mteam.jcsync.operation.JCsyncAbstractOperation;
import pl.edu.pjwstk.mteam.jcsync.operation.MethodCarrier;
import pl.edu.pjwstk.mteam.jcsync.operation.RegisteredOperations;
import pl.edu.pjwstk.mteam.pubsub.accesscontrol.AccessControlRules;
import pl.edu.pjwstk.mteam.pubsub.core.Subscriber;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;


/**
 * Class that represents the shared object in the layer. 
 * It stores all the necessary information, such as object ID, the 
 * ConsistensyManager, nucleus object on which we make the operations that are 
 * published in the layer.
 * There is two way to get instance of this class:<br>
 * <b>1.</b> by calling the constructor in the subclass:<br>
 * <pre>
 * <code>
 * class SimpleSharedObject extends JCSyncAbstractSharedObject implements List {
 *   
 *   public SimpleSharedObject(String name, JCSyncCore core, Class consistencyManager, AccessControlRules acRules) throws ObjectExistsException, Exception {
 *       <strong>super(name, new ArrayList(), core, consistencyManager, SimpleSharedObject.class, acRules);</strong>
 *   }
 *
 *   public SimpleSharedObject(String name, JCSyncCore core, Class consistencyManager) throws ObjectExistsException, Exception {
 *       <strong>super(name, new ArrayList(), core, consistencyManager, SimpleSharedObject.class);</strong>
 *   }
 * [...]
 * }
 * </code> 
 * </pre>
 * It allows to create new object in the overlay, if the object with given name 
 * already exists then {@link ObjectExistsException ObjectExistsException} is thrown.<br>
 * <b>2.</b> or by calling static
 * {@link JCSyncAbstractSharedObject#getFromOverlay(java.lang.String, pl.edu.pjwstk.mteam.jcsync.core.JCsyncAlgorithInterface) getFromOverlay} method<br>
 * <pre>
 * <code>
 * P2PNode node;
 * JCSyncCore core;
 * [...]
 * String name = "existent_shared_object";
 * JCSyncAbstractSharedObject s2 = null;
 * <b>s2 = JCSyncAbstractSharedObject.getFromOverlay(name, core);</b>
 * [...]
 * </code>
 * Used only when the shared object is already known in the overlay.
 * </pre>
 * <p>
 * 
 * <tt>JCSyncAbstractSharedObject</tt> also provides skeleton mechanism to invoke 
 * methods on the <tt>nucleus</tt> object.
 * 
 * </p>
 * 
 * @author Piotr Bucior
 */
public abstract class JCSyncAbstractSharedObject {
    /**
     * the logger
     */
    protected final Logger log = Logger.getLogger("pl.edu.pjwstk.mteam.jcsync.core.JCSyncAbstractSharedObject");
    /**
     * object identifier
     */
    private Topic identifier = null;
    /**
     * object name
     */
    private String ID = null;
    /**
     * object on which the methods are invoked which have been caused.<br>     * 
     */
    private Serializable nucleus = null;
    /**
     * core algorithm instance related with this object
     */
    public JCsyncAlgorithInterface coreAlg = null;
    /**
     * class determines the type of Consistency Manager
     */
    private Class consistencyManager = null;
    /**
     * class determines the type of shared object
     */
    private Class sharedObjectClass = null;
    /**
     * operation identifier, it is used to sort incoming indication and invoke id in proper order
     */
    private AtomicLong operationID = new AtomicLong(0);
    /**
     * just to synchronise operationID incrementation 
     */
    protected final Object operationIdIncrementLocker = new Object();
    
    /**
     * Creates new object with default AccessControlList. 
     * First of all the topic will be created in the layer, if it exists then ObjectExistsException will be thrown.<br>
     * In the next step node will subscribe created topic. <br>
     * After that just inform JCsyncAlgorithInterface that the object was created.
     * 
     * @param name the name of new shared object
     * @param nucleus the object on which we the method are called
     * @param core the core algorithm instance
     * @param consistencyManager class of consistency manager which will hold this object
     * @param sharedObjectClass the class of shared object (the class of JCSyncAbstractSharedObject-extended object)
     * @throws ObjectExistsException if the object with this name is already created in the network 
     * @throws Exception any occurred exception
     */
    protected JCSyncAbstractSharedObject(String name, Serializable nucleus, JCSyncCore core, Class consistencyManager, Class sharedObjectClass)
            throws ObjectExistsException,
            Exception {
        if (name == null || core == null || name.length() == 0 || consistencyManager == null || sharedObjectClass == null) {
            throw new IllegalArgumentException();
        }        
        // creating default ac rules
        Topic t = new Topic(name);
        Subscriber subscriber = new Subscriber("node1", t);
        t.setOwner(subscriber);
        AccessControlLists acRules = new AccessControlLists(t);        
        // ^
        //try to create new topic 
        this.identifier = core.createSharedObject(name, true,acRules);
        if (this.identifier == null) {
            throw new Exception("Unable to create shared object");
        }
        //subscribe to the related topic
        core.subscribeSharedObject(name, true);
        this.ID = name;
        this.nucleus = nucleus;
        this.coreAlg = core;
        this.consistencyManager = consistencyManager;
        this.sharedObjectClass = sharedObjectClass;
        this.coreAlg.objectCtreated(this);
        this.coreAlg.registerConsistencyManager(name, consistencyManager);  
    }    
    /**
     * Creates new object with given AccessControlLists. 
     * First of all the topic will be created in the layer, if it exists then ObjectExistsException will be thrown.<br>
     * In the next step node will subscribe created topic. <br>
     * After that just inform JCsyncAlgorithInterface that the object was created.
     * 
     * @param name the name of new shared object
     * @param nucleus the object on which we the method are called
     * @param core the core algorithm instance
     * @param consistencyManager class of consistency manager which will hold this object
     * @param sharedObjectClass the class of shared object (the class of JCSyncAbstractSharedObject-extended object)
     * @param acRules defined access control rules
     * @throws ObjectExistsException if the object with this name is already created in the network 
     * @throws Exception 
     */
    protected JCSyncAbstractSharedObject(String name, Serializable nucleus, JCSyncCore core, Class consistencyManager, Class sharedObjectClass, AccessControlRules acRules)
            throws ObjectExistsException,
            Exception {
        if (name == null || core == null || name.length() == 0 || consistencyManager == null || sharedObjectClass == null) {
            throw new IllegalArgumentException();
        }
        this.identifier = core.createSharedObject(name, true, acRules);
        if (this.identifier == null) {
            throw new Exception("Unable to create shared object");
        }
        core.subscribeSharedObject(name, true);
        this.ID = name;
        this.nucleus = nucleus;
        this.coreAlg = core;
        this.consistencyManager = consistencyManager;
        this.sharedObjectClass = sharedObjectClass;
        this.coreAlg.objectCtreated(this);
        this.coreAlg.registerConsistencyManager(name, consistencyManager);
        
    }
    /**
     * blank constructor necessary for serialization 
     */
    protected JCSyncAbstractSharedObject() {
    }
    /**
     * Returns an instance of nucleus object.
     */
    protected Serializable getNucleus(){
        return this.nucleus;
    }    

    /**
     * Returns the ID  
     * @return identifier as String
     */
    public String getID() {
        return ID;
    }    

    /**
     * Increment and returns the next operation identifier
     * @return operation id as long
     */
    protected long nextOperationID() {
        long retVal = 0;
        synchronized (this.operationIdIncrementLocker) {
            retVal = this.operationID.incrementAndGet();
        }
        return retVal;
    }

    /**
     * Returns current operation identifier
     * @return operation id as long
     */
    public long getCurrentOperationID() {
        long retVal = 0;
        synchronized (this.operationIdIncrementLocker) {
            retVal = this.operationID.get();
        }
        return retVal;
    }

    /**
     * Return the representation if this object as byte array
     */
    protected byte[] encode() {
         ByteArrayOutputStream ostream;
        DataOutputStream dostr;
        ObjectOutputStream ostr;
        byte[] msgToByte;
        synchronized(operationIdIncrementLocker){
        ostream = new ByteArrayOutputStream();
        dostr = new DataOutputStream(ostream);
        msgToByte = null;
        try {
            //writing request specific message contents
            dostr.writeUTF(this.ID);
            dostr.writeUTF(this.sharedObjectClass.getName());
            ostr = new ObjectOutputStream(dostr);
            ostr.writeObject(this.consistencyManager);
            ostr.writeObject(this.operationID);
            if (this.nucleus != null) {
                ostr.writeObject(this.nucleus);
            }            
            msgToByte = ostream.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(ostream!=null)
                ostream.close();
                ostream = null;
                ostr = null;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        }
        return msgToByte;
    }
    /**
     * Returns the nucleus object class.
     */
    public Class getNucleusClass(){
        return this.nucleus.getClass();
    }

    /**
     * Returns object representation as byte array, which is received from the layer.
     * These  byte array created by {@link JCSyncAbstractSharedObject#encode() encode()} method.
     * @param name object identifier
     * @param core core algorithm instance which will be used to manage layer messages
     * @return object representation as byte array
     * @throws ObjectNotExistsException if the object with given name is not exist
     * @throws OperationForbiddenException if the node related with given core is not permitted to do this
     * @throws Exception
     */
    protected static byte[] getConstructorFromOverlay(String name, JCsyncAlgorithInterface core) 
            throws ObjectNotExistsException,
            OperationForbiddenException,
            Exception {
        byte[] details = null;
        details = (byte[]) core.sendMessage(JCsyncAbstractOperation.get_OP_REQ_TRANSFER_OBJECT(name,core.getNodeInfo().getName()), true);
        return details;
    }

    /**
     * Used to subscribe with existing shared object.
     * @param name object identifier
     * @param core core algorithm instance which will be related with this shared object
     * @return an instance of JCSyncSharedObject (or it extensions)
     * @throws ObjectNotExistsException if the object with given name isn't exists in the network layer
     * @throws OperationForbiddenException if the node related with given core is not permitted to do this
     * @throws Exception
     */
    public static JCSyncAbstractSharedObject getFromOverlay(String name, JCsyncAlgorithInterface core)
            throws ObjectNotExistsException,
            OperationForbiddenException,
            Exception {
        //first of all try to subscribe to the topic
        Topic t = core.subscribeSharedObject(name, true);
        if (t == null) {
            throw new Exception("Unable to get shared object from overlay");
        }
        //get the object as byte array from the network
        byte[] details = getConstructorFromOverlay(name, core);
        String receivedName = "";
        String receivedSharedObjectClassName = "";
        Class receivedConsistencyManager = null;
        Serializable receivedNucleas = null;
        ByteArrayInputStream istream;
        DataInputStream distr;
        ObjectInputStream oistr;
        istream = new ByteArrayInputStream(details);
        distr = new DataInputStream(istream);
        AtomicLong operationID;
        oistr = null;
        try {
            receivedName = distr.readUTF();
            receivedSharedObjectClassName = distr.readUTF();
            oistr = new ObjectInputStream(istream);
            receivedConsistencyManager = (Class) oistr.readObject();            
            operationID = (AtomicLong) oistr.readObject();
            Object b = oistr.readObject();
            receivedNucleas = (Serializable) b;//(Serializable) oistr.readObject();
            Class sharedObjectClass = Class.forName(receivedSharedObjectClassName);
            JCSyncAbstractSharedObject retVal = (JCSyncAbstractSharedObject) sharedObjectClass.newInstance();            
            retVal.setID(name);
            retVal.setCoreAlg(core);
            retVal.setIdentifier(t);
            retVal.setNucleus(receivedNucleas);
            retVal.setSharedObjectClass(sharedObjectClass);
            retVal.setConsistencyManager(receivedConsistencyManager);
            retVal.setCurrentOperationID(operationID.get());
            core.objectCtreated(retVal);
            core.registerConsistencyManager(retVal.getID(), retVal.consistencyManager);            
            return retVal;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (oistr != null) {
                    oistr.close();
                }
                oistr = null;
                istream = null;
                distr = null;
            } catch (IOException ex) {
                //ignore
            }
        }

    }
    
    private void setID(String ID) {
        log.trace("Receives transported shared object name set to:"+ID);
        this.ID = ID;
    }

    private void setConsistencyManager(Class consistencyManager) {
        log.trace("Receives transported shared object consistencyManager class set to:"+consistencyManager.getSimpleName());
        this.consistencyManager = consistencyManager;
    }

    private void setCoreAlg(JCsyncAlgorithInterface coreAlg) {
        this.coreAlg = coreAlg;
    }

    private void setIdentifier(Topic identifier) {
        this.identifier = identifier;
    }
    
    protected void setNucleus(Serializable nucleus) {
        log.trace("Receives transported shared object nucleus set to:"+nucleus.toString());
        this.nucleus = nucleus;
    }

    private void setSharedObjectClass(Class sharedObjectClass) {
        log.trace("Receives transported shared object sharedObjectClass set to:"+sharedObjectClass.getSimpleName());
        this.sharedObjectClass = sharedObjectClass;
    }

    /**
     * returns identifier of this shared object
     * @return layer identifier of this object as Topic 
     */
    public Topic getIdentifier() {
        return identifier;
    }

    /**
     * returns the core algorithm instance
     */
    public JCsyncAlgorithInterface getCoreAlgorith() {
        return this.coreAlg;
    }

    /**
     * called when the 'read-type' operation (method) is received from the layer and must be invoked on the nucleus object instance.
     * This method is used only by consistency manager.
     * @param methodName generic method name
     * @param argTypes an array with arguments class types
     * @param argValues an array with arguments values
     * @return the results of invoked method
     * @throws SecurityException 
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    protected Object invokeReadOperation(String methodName, Class[] argTypes, Object[] argValues)
            throws SecurityException, NoSuchMethodException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Object retVal = null;
        Method m = null;
        try {
            if (argTypes != null && argTypes.length > 0) {
                m = this.nucleus.getClass().getDeclaredMethod(methodName, argTypes);
                m.setAccessible(true);
                retVal = m.invoke(this.nucleus, argValues);
            } else {
                m = getClass().getDeclaredMethod(methodName);
                m.setAccessible(true);
                retVal = m.invoke(this.nucleus);
            }

        } catch (Exception e) {
            retVal = e;
        }
        return retVal;
    }

    /**
     * called when the 'write-type' operation (method) is received from the layer and must be invoked on the nucleus object instance.
     * This method is used only by consistency manager.
     * @param methodName generic method name
     * @param argTypes an array with arguments class types
     * @param argValues an array with arguments values
     * @return the results of invoked method
     * @throws SecurityException 
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    protected synchronized Object invokeWriteOperation(String methodName, Class[] argTypes, Object[] argValues)
            throws SecurityException, NoSuchMethodException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        this.nextOperationID();
        Object retVal = null;
        Method m = null;
        try {
            if (argTypes != null && argTypes.length > 0) {
                m = this.nucleus.getClass().getDeclaredMethod(methodName, argTypes);
                m.setAccessible(true);
                retVal = m.invoke(this.nucleus, argValues);
            } else {
                m = getClass().getDeclaredMethod(methodName);
                m.setAccessible(true);
                retVal = m.invoke(this.nucleus);
            }

        } catch (Exception e) {
            retVal = e;
        }
        return retVal;
    }

    /**
     * always invoked when the lock is required, after this method and until {@link #afterPublishReadOperation(java.lang.String, java.lang.Class[], java.lang.Object[]) afterPublishReadOperation} is called all incoming requests is passed to the buffer and waits until release. 
     * All incoming these type request is passed to the queue, and the next of 
     * it will take place when the previous one is released.
     */
    protected void beforePublishReadOperation() throws Exception {
        JCsyncAbstractOperation op = JCsyncAbstractOperation.get_OP_REQ_LOCK_APPLY(this.ID,this.coreAlg.getNodeInfo().getName());
        this.coreAlg.getConsistencyManager(this.ID).beforeRequestSend(op, true);
        this.coreAlg.sendMessage(op, false);
        this.coreAlg.getConsistencyManager(this.ID).afterRequestSend(op, true);        
    }

    /**
     * always invoked when the lock is required, after this method and until 
     * {@link #afterPublishWriteOperation(java.lang.String, java.lang.Class[], java.lang.Object[]) afterPublishWriteOperation} 
     * is called all incoming requests is passed to the buffer and waits until 
     * release. 
     * All incoming these type request is passed to the queue, and the next of 
     * it will take place when the previous one is released.
     */
    protected void beforePublishWriteOperation() throws Exception{
        JCsyncAbstractOperation op = JCsyncAbstractOperation.get_OP_REQ_LOCK_APPLY(this.ID,this.coreAlg.getNodeInfo().getName());
        this.coreAlg.getConsistencyManager(this.ID).beforeRequestSend(op, true);
        this.coreAlg.sendMessage(op, false);
        this.coreAlg.getConsistencyManager(this.ID).afterRequestSend(op, true);
    }

    /**
     * Invoked to release the locker.
     */
    
    protected void afterPublishWriteOperation() throws Exception {
        JCsyncAbstractOperation op = JCsyncAbstractOperation.get_OP_REQ_LOCK_RELEASE(this.ID,this.coreAlg.getNodeInfo().getName());
        this.coreAlg.getConsistencyManager(this.ID).beforeRequestSend(op, true);
        this.coreAlg.sendMessage(op, false);
        this.coreAlg.getConsistencyManager(this.ID).afterRequestSend(op, true);
    }

    /**
     * Invoked to release the locker.
     */
    protected void afterPublishReadOperation() throws Exception {
        JCsyncAbstractOperation op = JCsyncAbstractOperation.get_OP_REQ_LOCK_RELEASE(this.ID,this.coreAlg.getNodeInfo().getName());
        this.coreAlg.getConsistencyManager(this.ID).beforeRequestSend(op, true);
        this.coreAlg.sendMessage(op, false);
        this.coreAlg.getConsistencyManager(this.ID).afterRequestSend(op, true);
    }

    /**
     * Invoked to publish method to invoke with given arguments.
     * @param methodName generic method name
     * @param argTypes an array with arguments class types
     * @param argValues an array with arguments values
     * @return the results of invoked method or null or any occurred exception during publish
     * @throws Exception
     */
    protected Object publishWriteOperation(String methodName, Class[] argTypes, Serializable[] argValues) throws Exception{
        MethodCarrier mc = new MethodCarrier(methodName);
        mc.setArgTypes(argTypes);
        mc.setArgValues(argValues);
        JCsyncAbstractOperation op = JCsyncAbstractOperation.getByType(RegisteredOperations.OP_REQ_WRITE_METHOD, this.ID, mc,this.coreAlg.getNodeInfo().getName());
        this.coreAlg.getConsistencyManager(this.ID).beforeRequestSend(op, true);
        this.coreAlg.sendMessage(op, false);
        Object e = this.coreAlg.getConsistencyManager(this.ID).afterRequestSend(op, true);
        if(e!= null && e instanceof Exception) throw (Exception)e;
        else return e;
    }

    /**
     * Invoked to publish method to invoke with given arguments.
     * @param methodName generic method name
     * @param argTypes an array with arguments class types
     * @param argValues an array with arguments values
     * @return the results of invoked method or null or any occurred exception during publish
     * @throws Exception
     */
    protected Object publishReadOperation(String methodName, Class[] argTypes, Serializable[] argValues) throws Exception{
        MethodCarrier mc = new MethodCarrier(methodName);
        mc.setArgTypes(argTypes);
        mc.setArgValues(argValues);
        JCsyncAbstractOperation op = JCsyncAbstractOperation.getByType(RegisteredOperations.OP_REQ_READ_METHOD, this.ID, mc,this.coreAlg.getNodeInfo().getName());
        this.coreAlg.getConsistencyManager(this.ID).beforeRequestSend(op, true);
        this.coreAlg.sendMessage(op, false);
        Object e = this.coreAlg.getConsistencyManager(this.ID).afterRequestSend(op, true);
        if(e!= null && e instanceof Exception) throw (Exception)e;
        else return e;
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(ID);
        sb.append('@');
        sb.append(sharedObjectClass.getName());
        sb.append(", currentOperationID:");
        sb.append(this.operationID);
        return sb.toString();
    }

    private void setCurrentOperationID(long operationID) {
        synchronized(this.operationIdIncrementLocker){
            this.operationID.set(operationID);
        }
    }
    
}
