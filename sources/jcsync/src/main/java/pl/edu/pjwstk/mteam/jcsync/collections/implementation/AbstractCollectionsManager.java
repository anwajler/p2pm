package pl.edu.pjwstk.mteam.jcsync.collections.implementation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.jcsync.collections.JCSyncAbstractCollection;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncCoreAlgorithm;
import pl.edu.pjwstk.mteam.jcsync.core.concurrency.EventInvoker;
import pl.edu.pjwstk.mteam.jcsync.exception.CollectionExistException;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncCreateCollectionMethod;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncMethod;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.Parameter;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;

/**class that supports the internal operations of the collections, every attempt 
 * to do operations on collections are initialized by this class.
 * @author Piotr Bucior
 * @version 1.0
 */
public class AbstractCollectionsManager {
    
    /**
     * one, global instance of the class
     */
    private static AbstractCollectionsManager instance;
    /**
     * list of collections that have been implemented
     */
    private static final Hashtable<Class, Class> registeredCollections = new Hashtable<Class, Class>(15);
    /**
     * console logger
     */
    private static final Logger log = Logger.getLogger("pl.edu.pjwstk.mteam.jcsync.collections.implementation.AbstractCollectionsManager");
    
    static {
        registeredCollections.put(JCSyncHashMap.class, HashMap.class);
        registeredCollections.put(JCSyncVector.class, Vector.class);
    }
    /**
     * blank constructor, to get instance of the <tt>AbstractCollectionsManager</tt> see 
     * {@link #getInstance() getInstance}.
     */
    private AbstractCollectionsManager() {
    }

    /**
     * returns instance of <tt>AbstractCollectionsManager</tt>, if the global 
     * instance of this class doesn't exists yet then will be created, 
     * or returns existing instance
     * @return global instance of <tt>AbstractCollectionsManager</tt>
     */
    public static AbstractCollectionsManager getInstance() {
        if (AbstractCollectionsManager.instance == null) {
            AbstractCollectionsManager.instance = new AbstractCollectionsManager();
        }
        return AbstractCollectionsManager.instance;

    }

    /**
     * invoked when update operation is delivered from PUB-SUB layer
     * @param operation operation details
     * @param collectionID collection identifier
     * @deprecated not used now
     */
    protected void onDeliverMethod(JCSyncMethod operation, pl.edu.pjwstk.mteam.pubsub.core.Topic collectionID) {
    }

    /**
     * returns collection by the given collection ID
     * @param collectionID
     * @return collection
     * @deprecated not used
     */
    public JCSyncAbstractCollection getCollection(String collectionID) {
        return null;
    }
    /**
     * preliminary method for initiating the process of calling a method, 
     * internally invoked from the collection
     * @param operation operation details
     * @param collectionID collection identifier as <tt>String</tt>
     * @return the return value of invoked method   
     */
    public Object requestOperation(JCSyncMethod operation, String collectionID) {
        return JCSyncCoreAlgorithm.getInstance().publishWriteOperation(operation);
    }

    /**
     * returns <b>reflective</b> constructor for given <tt>collectionType</tt> aplicable for sets of arguments <tt>paramTypes</tt>
     * @param collectionType type of collection, for which will be returned constructor, e.g. for JCSyncHashMap the <tt>collectionType</tt>
     * will be <tt>java.util.HashMap.class</tt>
     * @param paramTypes sets of arguments given to get proper constructor
     * @return proper construcotr or <tt>null</tt>
     */
    protected Constructor getConstructor(Class collectionType, Class... paramTypes) {
        try {
            return collectionType.getConstructor(paramTypes);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * returns <b>reflective</b> method by given arguments
     * @param collectionType type of collection for which the method will be returned
     * @param methodName generic method name
     * @param param arguments with which the method will be called
     * @return reflective method instance of <tt>null</tt>
     */
    protected Method getCollectionMethod(Class collectionType, String methodName, Class... param) {
        try {
            return collectionType.getDeclaredMethod(methodName, param);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
        return null;

    }
    /**
     * returns original class for implemented collection <tt>collClass</tt>
     * @param collClass the class of collection for wchich will be returned original class, 
     * e.g. for <tt>JCSyncHashMap</tt> it will be <tt>java.util.HashMap</tt>
     * @return original class for implemented collection
     */
    public static Class getDeclaredClassForCollection(Class collClass){
        return registeredCollections.get(collClass);
    }
    /**
     * called when new collectior will be created. 
     * @param collectionClass implemented collection class, e.g. JCSyncHashMap.class
     * @param collectionID collection identifier (name) as <tt>String</tt> 
     * @param invokerType the type of mechanism for calling received methods (updates)
     * @param param Constructor's parameters (type and value) 
     * @see pl.edu.pjwstk.mteam.jcsync.lang.reflect.Parameter
     * @return 
     * - new instance - <b>if collection does't exists</b> in the layer
     * <p>
     * - collection <u>with all stored data</u> <b>if collection exists</b> in the layer
     * <p>
     * - or <tt>null</tt>
     * @throws CollectionExistException  throws when the collection exists in 
     * the layer and the PUB-SUB user can not subscribe to this collection.
     */
    public JCSyncAbstractCollection requestCreateCollection(Class collectionClass, String collectionID,EventInvoker.InvokerType invokerType, Parameter... param) throws CollectionExistException {
        JCSyncAbstractCollection collection = null;
        try {
            try {
                collectionClass.asSubclass(JCSyncAbstractCollection.class);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Collection class: [" + collectionClass.getName() + "] is not a subclass of JCSyncAbstractCollection!");
            }
            Class[] p = null;
            if (param != null) {
                p = new Class[param.length];
                for (int i = 0; i < p.length; i++) {
                    p[i] = param[i].type;
                }
            }
            Object[] values = null;
            if (param != null) {
                values = new Object[param.length];
                for (int i = 0; i < values.length; i++) {
                    values[i] = param[i].value;
                }
            }
            Constructor c = getConstructor(registeredCollections.get(collectionClass), p);
            JCSyncCreateCollectionMethod op =
                    new JCSyncCreateCollectionMethod(collectionID,
                    collectionClass, c.toGenericString(), param);            
            JCSyncCoreAlgorithm.getInstance().requestCreateCollection(op,true,invokerType, null);
            return JCSyncCoreAlgorithm.getInstance().getCollection(collectionID);
        } catch (IllegalArgumentException ex) {
            log.error("", ex);
        }
        return collection;
    }
    /**
     * invoked internally by JCSyncCoreAlgorthm after positive response for 
     * <tt>requestCreateCollection</tt> 
     * <p><b>do not use in the end-source</b>
     * 
     * @param method delivered from PUB-SUB layer details to make collection
     * @return created collection
     */
    public JCSyncAbstractCollection makeCollection(JCSyncCreateCollectionMethod method){
        JCSyncAbstractCollection collection = null;
        Class collectionClass = method.getCollectionClass();
        Constructor c = getConstructor(registeredCollections.get(collectionClass), method.getParamTypes());        
        log.debug("Creating new collection: "+method.getCollectionID()+" : "+method.getCollectionClass().getName());
        log.trace("\n"+method);
        try {
            //TODO make better implement below block of code
            if(collectionClass.equals(JCSyncHashMap.class)){            
                collection = new JCSyncHashMap((Topic)JCSyncCoreAlgorithm.getInstance().getCollectionIdentifier(method.getCollectionID()), c,method, method.getValues());
            }else if(collectionClass.equals(JCSyncVector.class)){
                collection = new JCSyncVector((Topic)JCSyncCoreAlgorithm.getInstance().getCollectionIdentifier(method.getCollectionID()), c, method.getValues());
            }
             } catch (InstantiationException ex) {
                log.error("", ex);
            } catch (IllegalAccessException ex) {
                log.error("", ex);
            } catch (IllegalArgumentException ex) {
                log.error("", ex);
            } catch (InvocationTargetException ex) {
                log.error("", ex);
            }
        //collection.setConstructorMethod(method);
        return collection;
    }
}//end AbstractCollectionsManager


