package pl.edu.pjwstk.mteam.jcsync.collections.implementation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.jcsync.collections.JCSyncAbstractCollection;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncCoreAlgorithm;
import pl.edu.pjwstk.mteam.jcsync.core.concurrency.EventInvoker;
import pl.edu.pjwstk.mteam.jcsync.exception.CollectionExistException;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncCreateCollectionMethod;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncMethod;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.Parameter;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;

/**
 * @author Piotr Bucior
 * @version 1.0
 */
public class AbstractCollectionsManager {

    private static AbstractCollectionsManager instance;
    private static final Hashtable<Class, Class> registeredCollections = new Hashtable<Class, Class>(15);
    private static final Logger log = Logger.getLogger("pl.edu.pjwstk.mteam.jcsync.collections.implementation.AbstractCollectionsManager");

    static {
        registeredCollections.put(JCSyncHashMap.class, HashMap.class);
        registeredCollections.put(JCSyncVector.class, Vector.class);
    }

    private AbstractCollectionsManager() {
    }

    public static AbstractCollectionsManager getInstance() {
        if (AbstractCollectionsManager.instance == null) {
            AbstractCollectionsManager.instance = new AbstractCollectionsManager();
        }
        return AbstractCollectionsManager.instance;

    }

    /**
     *
     * @param operation
     * @param collectionID
     */
    protected void onDeliverMethod(JCSyncMethod operation, pl.edu.pjwstk.mteam.pubsub.core.Topic collectionID) {
    }

    /**
     *
     * @param collectionID
     */
    public JCSyncAbstractCollection getCollection(String collectionID) {
        return null;
    }
    /**
     *
     * @param operation
     * @param collectionID
     */
    public Object requestOperation(JCSyncMethod operation, String collectionID) {
        return JCSyncCoreAlgorithm.getInstance().publishWriteOperation(operation);
    }

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
    public static Class getDeclaredClassForCollection(Class collClass){
        return registeredCollections.get(collClass);
    }
    /**
     *
     * @param collectionClass Class bject specifying JCSync collection type, eg. JCSyncHashMap.class
     * @param collectionID Collection identifier in p2p network
     * @param param Constructor's parameters (type and value) @see pl.edu.pjwstk.mteam.jcsync.lang.reflect.Parameter
     * @return
     */
    public JCSyncAbstractCollection requestCreateCollection(Class collectionClass, String collectionID,EventInvoker.InvokerType type, Parameter... param) throws CollectionExistException {
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
            JCSyncCoreAlgorithm.getInstance().requestCreateCollection(op,true,type, null);
            return JCSyncCoreAlgorithm.getInstance().getCollection(collectionID);
        } catch (IllegalArgumentException ex) {
            log.error("", ex);
        }
        return collection;
    }
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


