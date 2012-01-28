package pl.edu.pjwstk.mteam.jcsync.operation;

import java.util.HashMap;

/**
 * Holds all types of used operations.
 * @author Piotr Bucior
 */
public class RegisteredOperations {

    /**
     * Used to logical verify that operation is one of jcsync operation.
     */
    public final static short OP_GENERIC_JCSYNC_OPERATION = 1024;    
    /**
     * Generic request for single -phases operations. 
     */
    public final static short OP_REQ_GENERIC = 1280;
    /**
     * Generic indication.
     */
    public final static short OP_INDICATION_GENERIC = 1536;
    //single phase requests
    /**
     * One phase generic operation. Every operations with code <b>between</b> 1344 
     * and 1408 value as a return has only response - they are single phase 
     * operations.
     */
    public final static short OP_REQ_ONE_PHASE_GENERIC = 1344;
    /**
     * Creates shared object request.
     */
    public final static short OP_REQ_CREATE_SHARED_OBJECT = 1345;
    /**
     * Subscribe request.
     */
    public final static short OP_REQ_SUBSCRIBE = 1346;
    /**
     * Unsubscribe request.
     */
    public final static short OP_REQ_UNSUBSCRIBE = 1347;
    /**
     * Remove object request.
     */
    public final static short OP_REQ_REMOVE = 1348;
    
    //biphase requests (send request -receive response - receive indication)
    //warning, check the rule: OP_REQ_{some biphase request} - OP_IND_{corresponding indication} = 128;
    /**
     * Biphase generic request. Every requested operation with types between 
     * 1408 and 1536 have 2 messages as a result of it, the response and the 
     * indication. 
     */
    public final static short OP_REQ_BIPHASE_GENERIC = 1408;
    /**
     * Used to transport shared object over the layer to requesting node.
     */
    public final static short OP_REQ_TRANSFER_OBJECT = 1409;
    /**
     * Informs that the publisher wants to get exclusive access to the shared object.
     */
    public static final short OP_REQ_LOCK_APPLY = 1410;
    /**
     * Informs that the publisher is releasing exclusive access to the shared object.
     */
    public static final short OP_REQ_LOCK_RELEASE = 1411;
    /**
     * Request to call a 'read' type method.
     */
    public static final short OP_REQ_READ_METHOD = 1412;
    /**
     * Request to call 'write' type method.
     */
    public static final short OP_REQ_WRITE_METHOD = 1413;
    //indications
    /**
     * Transfer object indication. It is sends only to the request publisher.
     */
    public final static short OP_IND_TRANSFER_OBJECT = OP_REQ_TRANSFER_OBJECT+128;
    /**
     * Lock apply indication. Sends only to the request publisher.
     */
    public static final short OP_IND_LOCK_APPLY = OP_REQ_LOCK_APPLY+128;
    /**
     * Lock release indication. Sends only to the request publisher.
     */
    public static final short OP_IND_LOCK_RELEASE = OP_REQ_LOCK_RELEASE+128;
    /**
     * 'Read' type method indication.
     */
    public static final short OP_IND_READ_METHOD = OP_REQ_READ_METHOD+128;
    /**
     * 'Write' type method indication.
     */
    public static final short OP_IND_WRITE_METHOD = OP_REQ_WRITE_METHOD+128;
    
    /**
     * Holds all registered operations. It allows to determine whether operation 
     * is already registered while we want to register the new one.
     * @see RegisteredOperations#registerOperation(short, java.lang.String) 
     */
    public static final HashMap<Short, String> OP_NAMES = new HashMap<Short, String>(){

        @Override
        public String put(Short key, String value) {
            // just checks whether the ID is already taken
            if(containsKey(key))throw new IllegalArgumentException("Operation ID already registered:"+key);
            else
            return super.put(key, value);
        }
    };
    /**
     * Register given code as new operation with given <tt>description</tt>. 
     * If given <tt>code</tt> is already registered then <tt>IllegalArgumentException</tt>
     * will throws.
     * @param code code of new operation 
     * @param description simple description for given operation code.
     */
    public static void registerOperation(short code, String description){
        OP_NAMES.put(code,description);
    }

    static {
        
        OP_NAMES.put(OP_GENERIC_JCSYNC_OPERATION, "["+ OP_GENERIC_JCSYNC_OPERATION+"] OP_GENERIC_JCSYNC_OPERATION");
        OP_NAMES.put(OP_REQ_GENERIC, "["+ OP_REQ_GENERIC+"] OP_REQ_GENERIC");
        OP_NAMES.put(OP_INDICATION_GENERIC, "["+OP_INDICATION_GENERIC +"] OP_INDICATION_GENERIC");
        
        OP_NAMES.put(OP_REQ_ONE_PHASE_GENERIC, "["+ OP_REQ_ONE_PHASE_GENERIC+"] OP_REQ_ONE_PHASE_GENERIC");        
        OP_NAMES.put(OP_REQ_TRANSFER_OBJECT,"["+ OP_REQ_TRANSFER_OBJECT+"] OP_REQ_TRANSFER_OBJECT");
        OP_NAMES.put(OP_REQ_CREATE_SHARED_OBJECT, "["+ OP_REQ_CREATE_SHARED_OBJECT+"] OP_REQ_CREATE_SHARED_OBJECT");
        OP_NAMES.put(OP_REQ_SUBSCRIBE, "["+ OP_REQ_SUBSCRIBE+"] OP_REQ_SUBSCRIBE");
        OP_NAMES.put(OP_REQ_UNSUBSCRIBE, "["+ OP_REQ_UNSUBSCRIBE+"] OP_REQ_UNSUBSCRIBE");
        OP_NAMES.put(OP_REQ_REMOVE, "["+ OP_REQ_REMOVE+"] OP_REQ_REMOVE");
        
        OP_NAMES.put(OP_REQ_BIPHASE_GENERIC, "["+ OP_REQ_BIPHASE_GENERIC+"] OP_REQ_BIPHASE_GENERIC");
        OP_NAMES.put(OP_IND_TRANSFER_OBJECT, "["+ OP_IND_TRANSFER_OBJECT+"] OP_IND_TRANSFER_OBJECT");
        OP_NAMES.put(OP_IND_LOCK_APPLY, "["+ OP_IND_LOCK_APPLY+"] OP_IND_LOCK_APPLY");
        OP_NAMES.put(OP_REQ_LOCK_APPLY, "["+ OP_REQ_LOCK_APPLY+"] OP_REQ_LOCK_APPLY");
        OP_NAMES.put(OP_REQ_LOCK_RELEASE, "["+ OP_REQ_LOCK_RELEASE+"] OP_REQ_LOCK_RELEASE");
        OP_NAMES.put(OP_IND_LOCK_RELEASE, "["+ OP_IND_LOCK_RELEASE+"] OP_IND_LOCK_RELEASE");
        
        OP_NAMES.put(OP_IND_READ_METHOD, "["+ OP_IND_READ_METHOD+"] OP_IND_READ_METHOD");
        OP_NAMES.put(OP_IND_WRITE_METHOD, "["+ OP_IND_WRITE_METHOD+"] OP_IND_WRITE_METHOD");
        OP_NAMES.put(OP_REQ_READ_METHOD, "["+ OP_REQ_READ_METHOD+"] OP_REQ_READ_METHOD");
        OP_NAMES.put(OP_REQ_WRITE_METHOD, "["+ OP_REQ_WRITE_METHOD+"] OP_REQ_WRITE_METHOD");
    }
}
