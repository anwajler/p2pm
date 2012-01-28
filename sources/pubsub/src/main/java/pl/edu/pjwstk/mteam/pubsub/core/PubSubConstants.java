package pl.edu.pjwstk.mteam.pubsub.core;

import java.util.HashMap;

/**
 * Class containing some predefined variables used by publish-subscribe.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class PubSubConstants {

    public static final byte OPERATION_CREATETOPIC = 1;
    public static final byte OPERATION_SUBSCRIBE = 2;
    public static final byte OPERATION_UNSUBSCRIBE = 3;
    public static final byte OPERATION_PUBLISH = 4;
    public static final byte OPERATION_NOTIFY = 5;
    public static final byte OPERATION_KEEPALIVE = 6;
    public static final byte OPERATION_CACHE_UPDATE = 7;
    public static final HashMap<Short, String> STR_OPERATION = new HashMap<Short, String>(10) {

        @Override
        public String get(Object key) {
            if(containsKey(key))
            return super.get(key);
            else return "["+key+"]-no-named_operation";
            
        }
    };
    public static final byte EVENT_REMOVETOPIC = 0;
    public static final byte EVENT_MODIFYAC = 1;
    public static final byte EVENT_CUSTOM = 2;
    public static final byte EVENT_ALL = 3;
    public static final byte EVENT_NEWTOPIC = 4;
    public static final byte EVENT_TRANSFERTOPIC = 5;
    public static final String[] STR_EVENT_ = {"REMOVE TOPIC", "MODIFY AC", "CUSTOM", "ALL",
        "NEW TOPIC", "TRANSFER TOPIC"};
    public static final HashMap<Short, String> STR_EVENT = new HashMap<Short, String>(10){

        @Override
        public String get(Object key) {
            if(containsKey(key))
            return super.get(key);
            else return "["+key+"]-no-named_event";
            
        }
    };

    static {
        STR_OPERATION.put((short) 0, "");
        STR_OPERATION.put((short) OPERATION_CREATETOPIC,"["+OPERATION_CREATETOPIC +"]-OPERATION_CREATETOPIC");
        STR_OPERATION.put((short) OPERATION_SUBSCRIBE, "["+OPERATION_SUBSCRIBE +"]-OPERATION_SUBSCRIBE");
        STR_OPERATION.put((short) OPERATION_UNSUBSCRIBE, "["+OPERATION_UNSUBSCRIBE +"]-OPERATION_UNSUBSCRIBE");
        STR_OPERATION.put((short) OPERATION_PUBLISH, "["+ OPERATION_PUBLISH+"]-OPERATION_PUBLISH");
        STR_OPERATION.put((short) OPERATION_NOTIFY, "["+OPERATION_NOTIFY +"]-OPERATION_NOTIFY");
        STR_OPERATION.put((short) OPERATION_KEEPALIVE, "["+ OPERATION_KEEPALIVE+"]-OPERATION_KEEPALIVE");
        STR_OPERATION.put((short) OPERATION_CACHE_UPDATE, "["+ OPERATION_CACHE_UPDATE+"]-OPERATION_CACHE_UPDATE");

        STR_EVENT.put((short) EVENT_REMOVETOPIC, "["+ EVENT_REMOVETOPIC+"]-EVENT_REMOVETOPIC");
        STR_EVENT.put((short) EVENT_MODIFYAC, "["+ EVENT_MODIFYAC+"]-EVENT_MODIFYAC");
        STR_EVENT.put((short) EVENT_CUSTOM, "["+ EVENT_CUSTOM+"]-EVENT_CUSTOM");
        STR_EVENT.put((short) EVENT_ALL, "["+ EVENT_ALL+"]-EVENT_ALL");
        STR_EVENT.put((short) EVENT_NEWTOPIC, "["+ EVENT_NEWTOPIC+"]-EVENT_NEWTOPIC");
        STR_EVENT.put((short) EVENT_TRANSFERTOPIC,"["+ EVENT_TRANSFERTOPIC+"]-EVENT_TRANSFERTOPIC");
    }
    public static final byte CREATETOPICFLAG_NEWTOPIC = 0;
    public static final byte CREATETOPICFLAG_TRANSFERTOPIC = 1;
    public static final byte MSG_STDRESPONSE = 0;
    public static final byte MSG_CREATETOPIC = OPERATION_CREATETOPIC;
    public static final byte MSG_SUBSCRIBE = OPERATION_SUBSCRIBE;
    public static final byte MSG_UNSUBSCRIBE = OPERATION_UNSUBSCRIBE;
    public static final byte MSG_PUBLISH = OPERATION_PUBLISH;
    public static final byte MSG_NOTIFY = OPERATION_NOTIFY;
    public static final byte MSG_KEEPALIVE = OPERATION_KEEPALIVE;
    public static final byte MSG_MAINTENANCE_CACHE_UPDATE = 7;
    /**
     * Node doesn't want to receive topic history after successful subscription
     */
    public static final int HISTORY_NONE = -2;
    /**
     * Node wants to receive whole topic history after successful subscription
     */
    public static final int HISTORY_ALL = -1;
    public static final int RESP_SUCCESS = 200;
    public static final int RESP_FORBIDDEN = 403;
    public static final int RESP_DOESNOTEXIST = 404;
    public static final int RESP_ALREADYEXISTS = 409;
    //maintenance
    public static byte MAINTENANCE_NEW_NODE_CONNECTED = 70;
    public static byte MAINTENANCE_DELETE_NODE = 71;
}
