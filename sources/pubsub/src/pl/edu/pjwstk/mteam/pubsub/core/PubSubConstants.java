package pl.edu.pjwstk.mteam.pubsub.core;

/**
 * Class containing some predefined variables used by publish-subscribe.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class PubSubConstants{
	
	public static final byte OPERATION_CREATETOPIC = 1;
	public static final byte OPERATION_SUBSCRIBE = 2;
	public static final byte OPERATION_UNSUBSCRIBE = 3; 
	public static final byte OPERATION_PUBLISH = 4;
	public static final byte OPERATION_NOTIFY = 5;
	public static final byte OPERATION_KEEPALIVE = 6;
        public static final byte OPERATION_CACHE_UPDATE = 7;
	
	public static final String[] STR_OPERATION= {"", "CREATE TOPIC", "SUBSCRIBE", 
		                                         "UNSUBSCRIBE", "PUBLISH", "NOTIFY", 
		                                         "KEEP-ALIVE"};
	
	public static final byte EVENT_REMOVETOPIC = 0;
	public static final byte EVENT_MODIFYAC = 1;
	public static final byte EVENT_CUSTOM = 2;
	public static final byte EVENT_ALL = 3; 
	public static final byte EVENT_NEWTOPIC = 4;
	public static final byte EVENT_TRANSFERTOPIC = 5;
	
	public static final String[] STR_EVENT = {"REMOVE TOPIC", "MODIFY AC", "CUSTOM", "ALL",
		                                      "NEW TOPIC", "TRANSFER TOPIC"};
	
	public static final byte CREATETOPICFLAG_NEWTOPIC = 0;
	public static final byte CREATETOPICFLAG_TRANSFERTOPIC = 1;

	public static final byte MSG_STDRESPONSE = 0;
	public static final byte MSG_CREATETOPIC = OPERATION_CREATETOPIC;
	public static final byte MSG_SUBSCRIBE = OPERATION_SUBSCRIBE;
	public static final byte MSG_UNSUBSCRIBE = OPERATION_UNSUBSCRIBE; 
	public static final byte MSG_PUBLISH = OPERATION_PUBLISH;
	public static final byte MSG_NOTIFY = OPERATION_NOTIFY;
	public static final byte MSG_KEEPALIVE = OPERATION_KEEPALIVE;
        public static final byte MSG_MAINTENANCE_CACHE_UPDATE =7;
	
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
