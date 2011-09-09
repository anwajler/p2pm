package pl.edu.pjwstk.p2pp.messages;

import pl.edu.pjwstk.p2pp.debug.DebugFields;
import pl.edu.pjwstk.p2pp.debug.DebugInformation;
import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;
import pl.edu.pjwstk.p2pp.util.Arrays;
import pl.edu.pjwstk.p2pp.util.ByteUtils;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

/**
 * <p>
 * General abstract message of P2PP. Intended for derivation. Contains general P2PP message information. Inherits from
 * Message, so that may be send or received over reliable/unreliable and encrypted/plain-text transport.
 * </p>
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * @see Message
 */
public abstract class P2PPMessage extends Message {

	public static final byte ENROLL_MESSAGE_TYPE = 0;
	public static final byte AUTHENTICATE_MESSAGE_TYPE = 1;
	public static final byte BOOTSTRAP_MESSAGE_TYPE = 2;
	public static final byte JOIN_MESSAGE_TYPE = 3;
	public static final byte LEAVE_MESSAGE_TYPE = 4;
	public static final byte KEEP_ALIVE_MESSAGE_TYPE = 5;
	public static final byte LOOKUP_PEER_MESSAGE_TYPE = 6;
	public static final byte EXCHANGE_TABLE_MESSAGE_TYPE = 7;
	public static final byte QUERY_MESSAGE_TYPE = 8;
	public static final byte PUBLISH_MESSAGE_TYPE = 9;
	public static final byte LOOKUP_OBJECT_MESSAGE_TYPE = 10;
	public static final byte REMOVE_OBJECT_MESSAGE_TYPE = 11;
	public static final byte REPLICATE_MESSAGE_TYPE = 12;
	public static final byte TRANSFER_MESSAGE_TYPE = 13;
	public static final byte TUNNEL_MESSAGE_TYPE = 14;
	public static final byte CONNECT_MESSAGE_TYPE = 15;
	public static final byte GET_DIAGNOSTICS_MESSAGE_TYPE = 16;
    public static final byte SEND_MESSAGE_MESSAGE_TYPE = 17;
    public static final byte LOOKUP_NEIGHBOUR_MESSAGE_TYPE = 20;
    public static final byte SET_UP_MESSAGE_TYPE = 21;
    public static final byte INDEX_MESSAGE_TYPE = 30;
    public static final byte INDEX_PEER_MESSAGE_TYPE = 31;
    public static final byte LOOKUP_INDEX_MESSAGE_TYPE = 32;
    public static final byte LOOKUP_PEER_INDEX_MESSAGE_TYPE = 33;

    /** Constant size of message header (without sourceID) in bits. */
	public static final int COMMON_HEADER_CONSTANT_SIZE = 160;

	/** Constant for P2PP protocol version 1. */
	public static final boolean[] P2PP_PROTOCOL_VERSION_1 = { false, true };

	/**
	 * Constant containing bits for request message from P2PP message header.
	 */
	public static final boolean[] REQUEST_MESSAGE_TYPE = { false, false };
	/**
	 * Constant containing bits for indication message from P2PP message header.
	 */
	public static final boolean[] INDICATION_MESSAGE_TYPE = { false, true };
	/**
	 * Constant containing bits for response message from P2PP message header.
	 */
	public static final boolean[] RESPONSE_MESSAGE_TYPE = { true, false };
	/**
	 * Constant containing bits for responseACK message from P2PP message header.
	 */
	public static final boolean[] RESPONSE_ACK_MESSAGE_TYPE = { true, true };

	/**
	 * Fixed field from header. Created to differentiate P2PP messages from other protocol messages such as STUN. TODO
	 * Not sure if it's the right number (maybe 0x0D should be first).
	 */
	public static final int MAGIC_COOKIE = 0x596ABF0D;

	private static final int RESERVED_OR_RESPONSE_CODE_BYTE_START_INDEX = 7;

	/** Index of first bit of request or indication type in Message header. */
	private static final int REQUEST_OR_INDICATION_TYPE_BYTE_START_INDEX = 16;

	/** Index of first bit of TTL in Message header. */
	private static final int TTL_BYTE_START_INDEX = REQUEST_OR_INDICATION_TYPE_BYTE_START_INDEX + 8;

	/** Index of first bit of Magic Cookie in Message header. */
	private static final int MAGIC_COOKIE_BYTES_START_INDEX = TTL_BYTE_START_INDEX + 8;

    private static final int SENDER_PORT_BYTES_START_INDEX = MAGIC_COOKIE_BYTES_START_INDEX + 32;

	/** Index of first bit of Transaction-ID in Message header. */
	private static final int TRANSACTION_ID_BYTES_START_INDEX = SENDER_PORT_BYTES_START_INDEX + 32;

	/** Index of first bit of Message Length in Message header. */
	private static final int MESSAGE_LENGTH_BYTES_START_INDEX = TRANSACTION_ID_BYTES_START_INDEX + 32;

	/** Index of first bit of Source-ID in Message header. */
	private static final int SOURCE_ID_BYTES_START_INDEX = MESSAGE_LENGTH_BYTES_START_INDEX + 32;

	/**
	 * Version of the protocol. It is first field in message's header.
	 */
	protected boolean[] protocolVersion = new boolean[2];

	/** Message type (request, response, indication, responseACK). */
	protected boolean[] messageType = new boolean[2];

	/**
	 * If set (=1=true), the message is an acknowledgment for the request, response, responseACK, or an indication. The
	 * A flag is never set for reliable transports.
	 */
	protected boolean acknowledgment;

	/**
	 * If set (=1=true), the message is sent by peer. Otherwise, a client sends the message.
	 */
	protected boolean byPeer;

	/**
	 * If set (=1=true), the request is sent in a recursive manner. Otherwise, it is sent in an iterative manner. The
	 * flag is not set for responses or indications.
	 */
	protected boolean recursive;

	protected boolean[] reservedOrResponseCode;

	/** The request or indication message type such as join and leave. */
	protected byte requestOrResponseType;

	/** A hop count for the number of peers this request can traverse. */
	protected byte ttl;

	/**
	 * A unique number to match responses with the originated requests. Along with source-ID, it can uniquely identify a
	 * message in the system.
	 */
	protected byte[] transactionID = new byte[4];

	/** The byte length of the message after the common header itself. */
	protected byte[] messageLength = new byte[4];

	/**
	 * The Peer-ID of the peer of client sending the request. For DHTs, it is the fixed length output of the hash
	 * function. For unstructured networks, it is a fixed length identifier. The length of this field is determined at
	 * Join. For bootstrap and authenticate requests, its length is always four bytes.
	 */
	protected byte[] sourceID;

    protected byte[] senderID;

	/**
	 * Response-ID is only included in the ACK, response, or a responseACK, and represents the peer-ID of the peer
	 * generating the response, responseACK, or ACK.
	 */
	protected byte[] responseID;

	/**
	 * Empty constructor that doesn't fill any fields of this object. Useful for creating empty message that could be
	 * filled with data later (for instance, we read bytes from stream and fill fields one by one).
	 */
	public P2PPMessage() {
		super();
	}

	/**
	 * Creates P2PP message.
	 *
	 * @param isOverReliableTransport
	 *            If true, message was received/has to be send over reliable transport.
	 * @param isEncrypted
	 *            If true, message was received/has to be send encrypted.
	 */
	public P2PPMessage(boolean isOverReliableTransport, boolean isEncrypted) {
		super(isEncrypted, isOverReliableTransport);
	}

	/**
	 * Constructor for general message in P2PP protocol.
	 *
	 * @param protocolVersion
	 *            Version of the protocol. Use constant from Message class.
	 * @param messageType
	 *            Type of message (response, request, indication, responseACK).
	 * @param isAcknowledgment
	 *            True if this Message has to be an Acknowledgment.
	 * @param isSentByPeer
	 *            True if this Message is sent by peer.
	 * @param isRecursive
	 *            True if this Message is sent in recursive manner.
	 * @param requestOrResponseType
	 *            Type of response (for instance join, enroll etc.) or request (for instance authenticate, join etc.).
	 * @param ttl
	 *            Time-to-live.
	 * @param transactionID
	 *            ID of the transaction that this message is part of.
	 * @param sourceID
	 *            SourceID represents the peer-ID of the message originator peer.
	 * @param responseID
	 *            Response-ID is only included in the ACK, response, or a responseACK, and represents the peer-ID of the
	 *            peer generating the response, responseACK, or ACK
	 * @param isOverReliable
	 *            True if this message will be send or was received over reliable transport.
	 * @param isEncrypted
	 *            True if this message was received or will be send using encryption.
	 */
	public P2PPMessage(boolean[] protocolVersion, boolean[] messageType, boolean isAcknowledgment,
			boolean isSentByPeer, boolean isRecursive, boolean[] reservedOrResponseCode, byte requestOrResponseType,
			byte ttl, byte[] transactionID, byte[] sourceID, byte[] responseID, boolean isOverReliable,
			boolean isEncrypted) {
		super(isEncrypted, isOverReliable);
		this.protocolVersion = protocolVersion;
		this.messageType = messageType;
		this.acknowledgment = isAcknowledgment;
		this.byPeer = isSentByPeer;
		this.recursive = isRecursive;
		this.reservedOrResponseCode = reservedOrResponseCode;
		this.requestOrResponseType = requestOrResponseType;
		this.ttl = ttl;
		this.transactionID = transactionID;
		this.sourceID = sourceID;
        this.senderID = sourceID;
		this.responseID = responseID;
	}

	public boolean[] getProtocolVersion() {
		return protocolVersion;
	}

	public boolean[] getReservedOrResponseCode() {
		return reservedOrResponseCode;
	}

	public byte[] getResponseID() {
		return responseID;
	}

	public void setResponseID(byte[] responseID) {
		this.responseID = responseID;
	}

	/**
	 * Sets reserved (for request) or response code (for response) part of the header. It has to be an array of 9
	 * booleans.
	 *
	 * @param reservedOrResponseCode
	 *            New reserved or response code.
	 */
	public void setReservedOrResponseCode(boolean[] reservedOrResponseCode) {
		this.reservedOrResponseCode = reservedOrResponseCode;
	}

	public void setProtocolVersion(boolean[] protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	/**
	 * Returns string representation of header.
	 *
	 * @return
	 */
	public String getHeader() {
        // TODO StringBuilder
		return "V=" + ByteUtils.booleanArrayToBinaryString(protocolVersion) + ", type="
				+ ByteUtils.booleanArrayToBinaryString(messageType) + ", APR="
				+ ByteUtils.booleanArrayToBinaryString(new boolean[] { acknowledgment, byPeer, recursive })
				+ ", reservedOrResponseCode=" + ByteUtils.booleanArrayToBinaryString(reservedOrResponseCode)
				+ ", reqType=" + requestOrResponseType + ", ttl=" + (0xFF & ttl) + ", cookie=" + MAGIC_COOKIE
				+ ", transactionID=" + ByteUtils.byteArrayToHexString(transactionID) + ", length="
				+ ByteUtils.bytesToLong(messageLength[0], messageLength[1], messageLength[2], messageLength[3])
				+ ", sourceID=" + ByteUtils.byteArrayToHexString(sourceID) + ", responseID="
				+ ByteUtils.byteArrayToHexString(responseID);
	}

	/**
	 * Returns type of message as defined in P2PP specification (draft 01). There are constants (
	 * {@link #INDICATION_MESSAGE_TYPE}, {@link #REQUEST_MESSAGE_TYPE}, {@link #RESPONSE_MESSAGE_TYPE} or
	 * {@link #RESPONSE_ACK_MESSAGE_TYPE}) in this class for this.
	 *
	 * @return
	 */
	public boolean[] getMessageType() {
		return messageType;
	}

	public void setMessageType(boolean[] messageType) {
		this.messageType = messageType;
	}

	public boolean isAcknowledgment() {
		return acknowledgment;
	}

	public void setAcknowledgment(boolean acknowledgment) {
		this.acknowledgment = acknowledgment;
	}

	public boolean isByPeer() {
		return byPeer;
	}

	public void setByPeer(boolean byPeer) {
		this.byPeer = byPeer;
	}

	/**
	 * Returns true if message was received/will be send in recursive manner. False if iterative method is used.
	 *
	 * @return
	 */
	public boolean isRecursive() {
		return recursive;
	}

	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}

	/**
	 * Returns request, response or indication type such as join or leave. There are constants (such as
	 * {@link #BOOTSTRAP_MESSAGE_TYPE} or {@link #JOIN_MESSAGE_TYPE}) for those types in this class.
	 *
	 * @return
	 */
	public byte getRequestOrResponseType() {
		return requestOrResponseType;
	}

	public void setRequestOrResponseType(byte requestOrResponseType) {
		this.requestOrResponseType = requestOrResponseType;
	}

	/**
	 * Returns TTL message field as short, so that TTL can't be below zero. TODO is this byte-to-short conversion really
	 * good?
	 *
	 * @return TTL field value.
	 */
	public short getTtl() {
		short ttlAsShort = ttl;
		if (ttlAsShort < 0) {
			ttlAsShort += 256;
		}
		return ttlAsShort;
	}

	public void setTtl(byte ttl) {
		this.ttl = ttl;
	}

    public void decreaseTtl() {
        this.ttl--;
    }

	public byte[] getTransactionID() {
		return transactionID;
	}

	/**
	 * Returns transactionID as long.
	 *
	 * @return
	 */
	public long getTransactionIDAsLong() {
		return ByteUtils.bytesToLong(transactionID[0], transactionID[1], transactionID[2], transactionID[3]);
	}

	public void setTransactionID(byte[] transactionID) {
		this.transactionID = transactionID;
	}

	/**
	 * Returns number of bytes after the common header of this message. It is returned in the same form as it is stored
	 * in this object.
	 *
	 * @return Number of bytes after the common header of this message.
	 */
	public byte[] getMessageLength() {
		return messageLength;
	}

	/**
	 * Returns number of bytes after the common header of this message. It is returned as long, because it is stored as
	 * four bytes and int can't handle all the possible values as positive.
	 *
	 * @return
	 */
	public long getMessageLengthAsLong() {
		return ByteUtils.bytesToLong(messageLength[0], messageLength[1], messageLength[2], messageLength[3]);
	}

	public void setMessageLength(byte[] messageLength) {
		this.messageLength = messageLength;
	}

	/**
	 * Returns sourceID of this message.
	 *
	 * @return
	 */
	public byte[] getSourceID() {
		return sourceID;
	}

	public void setSourceID(byte[] sourceID) {
		this.sourceID = sourceID;
	}

    public byte[] getSenderID() {
		return sourceID;
	}

	public void setSenderID(byte[] senderID) {
		this.senderID = senderID;
	}

	/**
	 * Returns this message as an array of bytes. Size of this array is determined using bitsCount argument.
	 *
	 * @param bitsCount Number of bits that this Message will consist of.
	 * @return Message as array of bytes.
	 */
	protected byte[] asBytes(int bitsCount) {

		int bytesCount = calculateBytesToStoreBits(bitsCount);

		byte[] bytes = new byte[bytesCount];
		// determines "message length" field TODO check if this is correct
		int responseIDlength = 0;
		if (responseID != null) {
			responseIDlength = responseID.length;
		}
		int length = bytesCount - sourceID.length - (COMMON_HEADER_CONSTANT_SIZE / 8) - responseIDlength;
		ByteUtils.addIntToArrayAtBitIndex(length, messageLength, 0);
		// end of determining message's length

		ByteUtils.addBooleanArrayToArrayAtIndex(protocolVersion, bytes, 0);
		ByteUtils.addBooleanArrayToArrayAtIndex(messageType, bytes, 2);
		ByteUtils.addBooleanArrayToArrayAtIndex(new boolean[] { acknowledgment }, bytes, 4);
		ByteUtils.addBooleanArrayToArrayAtIndex(new boolean[] { byPeer }, bytes, 5);
		ByteUtils.addBooleanArrayToArrayAtIndex(new boolean[] { recursive }, bytes, 6);

		if (reservedOrResponseCode != null) {
			ByteUtils.addBooleanArrayToArrayAtIndex(reservedOrResponseCode, bytes, RESERVED_OR_RESPONSE_CODE_BYTE_START_INDEX);
		}
		ByteUtils.addByteToArrayAtBitIndex(requestOrResponseType, bytes, REQUEST_OR_INDICATION_TYPE_BYTE_START_INDEX);
		ByteUtils.addByteToArrayAtBitIndex(ttl, bytes, TTL_BYTE_START_INDEX);
		ByteUtils.addIntToArrayAtBitIndex(MAGIC_COOKIE, bytes, MAGIC_COOKIE_BYTES_START_INDEX);
		ByteUtils.addIntToArrayAtBitIndex(this.senderPort, bytes, SENDER_PORT_BYTES_START_INDEX);
		ByteUtils.addByteArrayToArrayAtByteIndex(transactionID, bytes, TRANSACTION_ID_BYTES_START_INDEX / 8);
		ByteUtils.addByteArrayToArrayAtByteIndex(messageLength, bytes, MESSAGE_LENGTH_BYTES_START_INDEX / 8);
		ByteUtils.addByteArrayToArrayAtByteIndex(sourceID, bytes, SOURCE_ID_BYTES_START_INDEX / 8);
		if (responseID != null) {
			ByteUtils.addByteArrayToArrayAtBitIndex(responseID, bytes, SOURCE_ID_BYTES_START_INDEX + sourceID.length * 8);
		}

		return bytes;
	}

	/**
	 * Returns length (in bits) of this Message. Every subclass MUST use super.getBitsCount() for getting the length of
	 * a super-class. Then adds own length and returns that number.
	 *
	 * @return Length (in bits) of this Message.
	 */
	public int getBitsCount() {
		int length = COMMON_HEADER_CONSTANT_SIZE + sourceID.length * 8;

		if (responseID != null) {
			length += responseID.length * 8;
		}

		return length;
	}

	/**
	 * TODO Returns bit representation (in String) of byte.
	 *
	 * @param b
	 *            Bit to be represented by bits.
	 * @return String consisting of bit representation of byte.
	 */
	public static String byteToBitString(byte b) {

		StringBuffer buffer = new StringBuffer(8);

		for (int i = 0; i < 8; i++) {
			int mask = (int) (Math.pow(2, 8 - i - 1));

			buffer.append((b & mask) >> 8 - i - 1);
		}

		return buffer.toString();
	}

	/**
	 * Calculates number of bytes needed to store bitsCount bits. Argument has to be at least 0. For negative numbers
	 * this method always returns 0.
	 *
	 * @param bitsCount
	 *            Number of bits to be stored in bytes.
	 * @return How many bytes are needed to store bitsCount bits.
	 */
	public static int calculateBytesToStoreBits(int bitsCount) {

		if (bitsCount < 0) {
			return 0;
		}

		int bytesCount = (int) (bitsCount / 8);
		if (bitsCount % 8 > 0) {
			bytesCount++;
		}

		return bytesCount;
	}

	/**
	 * Adds general object to this message. Leaf subclasses should implement this method so that objects may be passed
	 * to a message. Objects have to be added in the same order as defined in P2PP specification.
	 *
	 * @param object
	 *            Object to be added to this message.
	 * @throws UnsupportedGeneralObjectException
	 *             Throws UnsupportedGeneralObjectException when this message can't contain given GeneralObject. This
	 *             happens when this message is not supposed to contain given object.
	 */
	public abstract void addObject(GeneralObject object) throws UnsupportedGeneralObjectException;

	/**
	 * Creates ACK for given message.
	 *
	 * @param ownPeerID
	 *            PeerID of the node that wants to create an ACK. If no response-ID was received in the message, then
	 *            the node generating an ACK will include own peer-ID as the response-ID. If the source-ID and
	 *            response-ID field are already present in the message, then the ACK message is copied back source-ID
	 *            and response-ID fields.
	 *
	 * @return
	 */
	public Acknowledgment createACK(byte[] ownPeerID) {
		Acknowledgment ack = new Acknowledgment(protocolVersion, messageType, byPeer, recursive,
				reservedOrResponseCode, requestOrResponseType, ttl, transactionID, sourceID, responseID, overReliable,
				encrypted);

		ack.setReceiverAddress(senderAddress);
		ack.setReceiverPort(senderPort);

		if (responseID == null) {
			ack.setResponseID(ownPeerID);
		}

		return ack;
	}

	/**
	 * Returns true if this message is responseACK message type as defined by P2PP specification. Returns false
	 * otherwise.
	 *
	 * @return
	 */
	public boolean isResponseACK() {
		return Arrays.equals(messageType, RESPONSE_ACK_MESSAGE_TYPE);
	}

	/**
	 * Returns true if this message is response message type as defined by P2PP specification. Returns false otherwise.
	 *
	 * @return
	 */
	public boolean isResponse() {
		return Arrays.equals(messageType, RESPONSE_MESSAGE_TYPE);
	}

	public boolean isRequest() {
		return Arrays.equals(messageType, REQUEST_MESSAGE_TYPE);
	}

	public boolean isIndication() {
		return Arrays.equals(messageType, INDICATION_MESSAGE_TYPE);
	}

	/**
	 * Returns PeerInfo object included in this message. Most messages contain PeerInfo object of message originator.
	 * This method returns it. If there's no such PeerInfo object, null is returned.
	 *
	 * @return z
	 */
	public abstract PeerInfo getPeerInfo();

	/**
	 * Verifies if this message is proper (i.e. contains all required subobjects etc.). TODO implement in P2PPMessage
	 * and direct subclasses.
	 */
	public abstract boolean verify();

    public DebugInformation getDebugInformation() {

        DebugInformation result = new DebugInformation();
        java.util.HashMap<Byte, Object> debugInfo = new java.util.HashMap<Byte, Object>();

        debugInfo.put(DebugFields.MESSAGE_CLASS, this.getClass().getSimpleName());
        debugInfo.put(DebugFields.TIMESTAMP, new java.sql.Timestamp(java.util.Calendar.getInstance().getTime().getTime()));
        debugInfo.put(DebugFields.OVER_RELIABLE, super.overReliable);
        debugInfo.put(DebugFields.ENCRYPTED, super.encrypted);
        debugInfo.put(DebugFields.SENDER_ADDRESS, super.senderAddress);
        debugInfo.put(DebugFields.SENDER_PORT, super.senderPort);
        debugInfo.put(DebugFields.RECEIVER_ADDRESS, super.receiverAddress);
        debugInfo.put(DebugFields.RECEIVER_PORT, super.receiverPort);
        debugInfo.put(DebugFields.PROTOCOL_NAME, P2PPUtils.protocolNameForPreferences(super.overReliable, super.encrypted));
        debugInfo.put(DebugFields.PROTOCOL_VERSION, ByteUtils.booleanArrayToBinaryString(protocolVersion));
        debugInfo.put(DebugFields.MESSAGE_TYPE, ByteUtils.booleanArrayToBinaryString(messageType));
        debugInfo.put(DebugFields.ACK_BYPEER_RECURSIVE, ByteUtils.booleanArrayToBinaryString(new boolean[]{acknowledgment, byPeer, recursive}));
        debugInfo.put(DebugFields.RESERVED_RESPONSE_CODE, ByteUtils.booleanArrayToBinaryString(reservedOrResponseCode));
        debugInfo.put(DebugFields.REQUEST_RESPONSE_TYPE, requestOrResponseType);
        debugInfo.put(DebugFields.TTL, ttl);
        debugInfo.put(DebugFields.TRANSACTION_ID, ByteUtils.byteArrayToHexString(transactionID));
        debugInfo.put(DebugFields.MESSAGE_LENGTH, ByteUtils.bytesToLong(messageLength[0], messageLength[1], messageLength[2], messageLength[3]));
        debugInfo.put(DebugFields.SOURCE_ID, ByteUtils.byteArrayToHexString(sourceID));
        debugInfo.put(DebugFields.RESPONSE_ID, ByteUtils.byteArrayToHexString(responseID));

        for (java.util.Map.Entry<Byte, Object> di : debugInfo.entrySet()) {
            Byte debugField = di.getKey();
            Object value = di.getValue();
            // Because receiverAddress is null (why?)
            if (null != value) {
                result.put(debugField, value.toString());
            } else {
                result.put(debugField, "");
            }
        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof P2PPMessage) && !o.getClass().isAssignableFrom(P2PPMessage.class)) return false;
        P2PPMessage oM = (P2PPMessage) o;
        return (Arrays.equals(this.transactionID, oM.getTransactionID()) && Arrays.equals(this.sourceID, oM.getSenderID()) &&
                Arrays.equals(this.senderID, oM.getSenderID()) && this.getMessageLengthAsLong()==oM.getMessageLengthAsLong());
    }

}
