package pl.edu.pjwstk.mteam.jcsync.core;

import java.util.HashMap;

/**
 * Holds constant JCSync fields 
 * @author Piotr Bucior
 */
public class JCSyncConstans {
    
    /**
     * JCSYNC_GENERIC_MESSAGE = 100-00000 (128)
     * <p>informs that the message is a one of the JCSync message 
     */
    public static final short JCSYNC_GENERIC_MESSAGE = 128;             // == 100-00000
    /**
     * JCSYNC_GENERIC_INDICATION = 100-00000 (160)
     * <p>informs that the message is a one of the JCSync indication type
     */
    public static final short JCSYNC_GENERIC_INDICATION = 160;          // == 100-00000
    /**
     * JCSYNC_CREATE_COLLECTION_INDICATION = 100-00001 (161)
     * <p>informs that the message holds create collection indication details
     */
    public static final short JCSYNC_CREATE_COLLECTION_INDICATION = 161;// == 100-00001
    /**
     * JCSYNC_INVOWE_WRITE_METHOD_INDICATION = 100-00001 (162)
     * <p>informs that the message holds write method indication details
     */
    public static final short JCSYNC_INVOWE_WRITE_METHOD_INDICATION = 162;// == 100-00001
    /**
     * JCSYNC_GENERIC_RESPONSE = 110-00000 (192)
     * <p>informs that the message is a one of the JCSync response
     */
    public static final short JCSYNC_GENERIC_RESPONSE = 192;            // == 110-00000
    /**
     * JCSYNC_GENERIC_REQUEST = 111-00000 (224)
     * <p>informs that the message is a one of the JCSync request type
     */
    public static final short JCSYNC_GENERIC_REQUEST =  224;            // == 111-00000    
    /**
     * JCSYNC_CREATE_COLLECTION_REQUEST = 111-00001 (225)
     * <p>informs that the message holds create collection request details
     */
    public static final short JCSYNC_CREATE_COLLECTION_REQUEST = 225;   // == 111-00001
    /**
     * JCSYNC_OPERATION_REQUEST = 111-00010 (226)
     * <p>informs that the message holds operation request details
     */
    public static final short JCSYNC_OPERATION_REQUEST = 226;           // == 111-00010    
    /**
     * JCSYNC_INVOKE_WRITE_METHOD_REQ = 111-00011 (227)
     * <p>informs that the message is a request to invoke method
     */
    public static final short JCSYNC_INVOKE_WRITE_METHOD_REQ =  227;    // == 111-00011
    /**
     * JCSYNC_GET_CONSTRUCTOR_REQUEST = 111-00100 (228)
     * <p>informs that the message is a request to get constructor used to create collection
     */
    public static final short JCSYNC_GET_CONSTRUCTOR_REQUEST = 228;     // == 111-00100

    /**
     * J_RESP_GENERAL_SUCCESS = 11-001-000000 (800)
     * <p>informs that the message is a one of the JCSync SUCCESS response
     */
    public static final short J_RESP_GENERAL_SUCCESS = 800; // == 11-001-000000
    /**
     * J_RESP_COLLECTION_CREATED = 11-001-000001 (801)
     * <p>informs that the message is a confirmation of creating collection
     */
    public static final short J_RESP_COLLECTION_CREATED = 801; // == 11-001-000001
    /**
     * J_RESP_TOPIC_CREATED = 11-001-000010 (802)
     * <p>informs that the message is confirmation of creating topic (PUB-SUB layer)
     */
    public static final short J_RESP_TOPIC_CREATED = 802; // == 11-001-000010
    /**
     * J_ERR_GENERAL_ERROR = 11-100-00100 (900)
     * <p>informs that the message is the one of the JCSync ERROR response
     */
    public static final short J_ERR_GENERAL_ERROR = 900; // == 11-100-00100
    /**
     * J_ERR_COLLECTION_NAME_RESERVED = 11-100-00101 (901)
     * <p>informs that the name of requested collection is reserved by other collection/topic
     */
    public static final short J_ERR_COLLECTION_NAME_RESERVED = 901; // == 11-100-00101
    /**
     * J_ERR_COLLECTION_AUTH_ERROR = 11-100-00110 (902)
     * <p>informs that the user is not able to subscribe to specified collection
     */
    public static final short J_ERR_COLLECTION_AUTH_ERROR = 902; // == 11-100-00110
    /**
     * J_ERR_COLLECTION_EXISTS = 11-100-00111 (903)
     * <p>informs that the collection exists
     */
    public static final short J_ERR_COLLECTION_EXISTS = 903; // == 11-100-00111
    /**
     * J_ERR_TRANSPORT_ERROR = 11-100-01000 (904)
     * <p>informs that the transport error has occurred
     */
    public static final short J_ERR_TRANSPORT_ERROR = 904; // == 11-100-01000
    
    public static final short J_ERR_OBJECT_NOT_EXISTS = 905;



    /**
     * a map holds JCSync static fields values (as key) and its equivalent as String (as value in the map) 
     */
    public static final HashMap<Short, String> messages = new HashMap<Short, String>(10);

    static{
        messages.put(JCSYNC_GENERIC_MESSAGE, "JCSYNC_GENERIC_MESSAGE (10000000)");
        messages.put(JCSYNC_GENERIC_INDICATION, "JCSYNC_GENERIC_INDICATION (10000000)");
        messages.put(JCSYNC_GENERIC_RESPONSE, "JCSYNC_GENERIC_RESPONSE (11000000)");
        messages.put(JCSYNC_GENERIC_REQUEST, "JCSYNC_GENERIC_REQUEST (11100000)");
        messages.put(JCSYNC_CREATE_COLLECTION_REQUEST, "JCSYNC_CREATE_COLLECTION_REQUEST (11100001)");
        messages.put(JCSYNC_OPERATION_REQUEST, "JCSYNC_OPERATION_REQUEST (11100010)");
        messages.put(JCSYNC_INVOKE_WRITE_METHOD_REQ, "JCSYNC_INVOKE_WRITE_METHOD_REQ (11100011)");
        messages.put(JCSYNC_CREATE_COLLECTION_INDICATION, "JCSYNC_CREATE_COLLECTION_INDICATION (100-00001)");
        messages.put(J_RESP_GENERAL_SUCCESS , "J_RESP_GENERAL_SUCCESS (800)");
        messages.put(J_RESP_COLLECTION_CREATED, "J_RESP_COLLECTION_CREATED (801)");
        messages.put(J_RESP_TOPIC_CREATED , "J_RESP_TOPIC_CREATED (802)");
        messages.put(J_ERR_GENERAL_ERROR, "J_ERR_COLLECTION_NAME_RESERVED (900)");
        messages.put(J_ERR_COLLECTION_AUTH_ERROR, "J_ERR_COLLECTION_AUTH_ERROR (902)");
        messages.put(J_ERR_COLLECTION_NAME_RESERVED , "J_ERR_COLLECTION_NAME_RESERVED (901) ");
        messages.put(J_ERR_COLLECTION_EXISTS, "J_ERR_COLLECTION_EXISTS (903)");
        messages.put(J_ERR_TRANSPORT_ERROR, "J_ERR_TRANSPORT_ERROR (904)");
        messages.put(JCSYNC_INVOWE_WRITE_METHOD_INDICATION,"JCSYNC_INVOWE_WRITE_METHOD_INDICATION (162)");
        messages.put(JCSYNC_GET_CONSTRUCTOR_REQUEST, "JCSYNC_GET_CONSTRUCTOR_REQUEST (228)");

    }
    
}
