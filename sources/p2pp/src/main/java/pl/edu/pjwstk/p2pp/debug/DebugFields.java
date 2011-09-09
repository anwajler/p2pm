package pl.edu.pjwstk.p2pp.debug;

import java.util.Hashtable;

/**
 * Provides a list of fields defining order in debug information array.
 *
 * @see pl.edu.pjwstk.p2pp.debug.processor.DebugWriter
 * @see pl.edu.pjwstk.p2pp.messages.Message#getDebugInformation()
 * @see pl.edu.pjwstk.p2pp.messages.P2PPMessage#getDebugInformation() 
 *
 * @author Konrad Adamczyk conrad.adamczyk@gmail.com
 */

public class DebugFields {

    /**
     * Indication whether message was sent or received.
     */
    public static final byte SENT_OR_RECEIVED = 0;

    /**
     * Name of the class message is an instance of.
     */
    public static final byte MESSAGE_CLASS = 1;

    /**
     * Timestamp created with message's sending or reception.
     */
    public static final byte TIMESTAMP = 2;

    /**
     * {@link pl.edu.pjwstk.p2pp.messages.Message#overReliable}
     */
    public static final byte OVER_RELIABLE = 3;

    /**
     * {@link pl.edu.pjwstk.p2pp.messages.Message#encrypted}
     */
    public static final byte ENCRYPTED = 4;

    /**
     * {@link pl.edu.pjwstk.p2pp.messages.Message#senderAddress}
     */
    public static final byte SENDER_ADDRESS = 5;

    /**
     * {@link pl.edu.pjwstk.p2pp.messages.Message#senderPort}
     */
    public static final byte SENDER_PORT = 6;

    /**
     * {@link pl.edu.pjwstk.p2pp.messages.Message#receiverAddress}
     */
    public static final byte RECEIVER_ADDRESS = 7;

    /**
     * {@link pl.edu.pjwstk.p2pp.messages.Message#receiverPort}
     */
    public static final byte RECEIVER_PORT = 8;

    /**
     * {@link pl.edu.pjwstk.p2pp.messages.Message#overReliable}
     * {@link pl.edu.pjwstk.p2pp.messages.Message#encrypted}
     * {@link pl.edu.pjwstk.p2pp.util.P2PPUtils#protocolNameForPreferences(boolean, boolean)}
     */
    public static final byte PROTOCOL_NAME = 9;

    /**
     * {@link pl.edu.pjwstk.p2pp.messages.P2PPMessage#protocolVersion}
     */
    public static final byte PROTOCOL_VERSION = 10;

    /**
     * {@link pl.edu.pjwstk.p2pp.messages.P2PPMessage#messageType}
     */
    public static final byte MESSAGE_TYPE = 11;

    /**
     * {@link pl.edu.pjwstk.p2pp.messages.P2PPMessage#acknowledgment}
     * {@link pl.edu.pjwstk.p2pp.messages.P2PPMessage#byPeer}
     * {@link pl.edu.pjwstk.p2pp.messages.P2PPMessage#recursive}
     */
    public static final byte ACK_BYPEER_RECURSIVE = 12;

    /**
     * {@link pl.edu.pjwstk.p2pp.messages.P2PPMessage#reservedOrResponseCode}
     */
    public static final byte RESERVED_RESPONSE_CODE = 13;

    /**
     * {@link pl.edu.pjwstk.p2pp.messages.P2PPMessage#requestOrResponseType}
     */
    public static final byte REQUEST_RESPONSE_TYPE = 14;

    /**
     * {@link pl.edu.pjwstk.p2pp.messages.P2PPMessage#ttl}
     */
    public static final byte TTL = 15;

    /**
     * {@link pl.edu.pjwstk.p2pp.messages.P2PPMessage#transactionID}
     */
    public static final byte TRANSACTION_ID = 16;

    /**
     * {@link pl.edu.pjwstk.p2pp.messages.P2PPMessage#messageLength}
     */
    public static final byte MESSAGE_LENGTH = 17;

    /**
     * {@link pl.edu.pjwstk.p2pp.messages.P2PPMessage#sourceID}
     */
    public static final byte SOURCE_ID = 18;

    /**
     * {@link pl.edu.pjwstk.p2pp.messages.P2PPMessage#responseID}
     */
    public static final byte RESPONSE_ID = 19;

    /**
     * Unhashed ID of the peer logging message.
     * {@link pl.edu.pjwstk.p2pp.objects.UnhashedID}
     */
    public static final byte UNHASHED_ID = 20;

    /**
     * Indicates each field's type.
     */
    public static final Hashtable<Byte,Class<?>> FIELD_TYPES = new Hashtable<Byte,Class<?>>()
    {
		private static final long serialVersionUID = -3527716542274633707L;
		{
            try {
                Class classString = Class.forName("java.lang.String");
                Class classTimestamp = Class.forName("java.sql.Timestamp");
                Class classBoolean = Class.forName("java.lang.Boolean");
                Class classInteger = Class.forName("java.lang.Integer");
                Class classByte = Class.forName("java.lang.Byte");
                Class classLong = Class.forName("java.lang.Long");
                put(SENT_OR_RECEIVED, classString);
                put(MESSAGE_CLASS, classString);
                put(TIMESTAMP, classTimestamp);
                put(OVER_RELIABLE, classBoolean);
                put(ENCRYPTED, classBoolean);
                put(SENDER_ADDRESS, classString);
                put(SENDER_PORT, classInteger);
                put(RECEIVER_ADDRESS, classString);
                put(RECEIVER_PORT, classInteger);
                put(PROTOCOL_NAME, classString);
                put(PROTOCOL_VERSION, classString);
                put(MESSAGE_TYPE, classString);
                put(ACK_BYPEER_RECURSIVE, classString);
                put(RESERVED_RESPONSE_CODE, classString);
                put(REQUEST_RESPONSE_TYPE, classByte);
                put(TTL, classByte);
                put(TRANSACTION_ID, classString);
                put(MESSAGE_LENGTH, classLong);
                put(SOURCE_ID, classString);
                put(RESPONSE_ID, classString);
                put(UNHASHED_ID, classString);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    };
}
