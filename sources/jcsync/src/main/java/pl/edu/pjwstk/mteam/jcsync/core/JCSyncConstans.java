package pl.edu.pjwstk.mteam.jcsync.core;

import java.util.Hashtable;

/**
 *
 * @author Piotr Bucior
 */
public class JCSyncConstans {
    public static final short JCSYNC_GENERIC_MESSAGE = 128;             // == 100-00000
    public static final short JCSYNC_GENERIC_INDICATION = 160;          // == 100-00000
    public static final short JCSYNC_CREATE_COLLECTION_INDICATION = 161;// == 100-00001
    public static final short JCSYNC_INVOWE_WRITE_METHOD_INDICATION = 162;// == 100-00001
    public static final short JCSYNC_GENERIC_RESPONSE = 192;            // == 110-00000
    public static final short JCSYNC_GENERIC_REQUEST =  224;            // == 111-00000    
    public static final short JCSYNC_CREATE_COLLECTION_REQUEST = 225;   // == 111-00001
    public static final short JCSYNC_OPERATION_REQUEST = 226;           // == 111-00010    
    public static final short JCSYNC_INVOKE_WRITE_METHOD_REQ =  227;    // == 111-00011
    public static final short JCSYNC_GET_CONSTRUCTOR_REQUEST = 228;     // == 111-00100

    public static final short J_RESP_GENERAL_SUCCESS = 800;
    public static final short J_RESP_COLLECTION_CREATED = 801;
    public static final short J_RESP_TOPIC_CREATED = 802;
    public static final short J_ERR_GENERAL_ERROR = 900;
    public static final short J_ERR_COLLECTION_NAME_RESERVED = 901;
    public static final short J_ERR_COLLECTION_AUTH_ERROR = 902;
    public static final short J_ERR_COLLECTION_EXISTS = 903;
    public static final short J_ERR_TRANSPORT_ERROR = 904;



    public static final Hashtable<Short, String> messages = new Hashtable<Short, String>(10);

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
