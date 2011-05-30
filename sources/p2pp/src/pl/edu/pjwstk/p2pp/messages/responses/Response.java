package pl.edu.pjwstk.p2pp.messages.responses;

import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * TODO General response message in P2PP protocol. Intended for derivation.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 */
public abstract class Response extends P2PPMessage {

	/** Constant for "Ok" response code. */
	public static final short RESPONSE_CODE_OK = 200;
	/** Constant for "Next Hop" response code. */
	public static final short RESPONSE_CODE_NEXT_HOP = 302;
	/** Constant for "Bad request" response code. */
	public static final short RESPONSE_CODE_BAD_REQUEST = 400;
	/** Constant for "Not found"response code. */
	public static final short RESPONSE_CODE_NOT_FOUND = 404;
	/** Constant for "Error inserting object" response code. */
	public static final short RESPONSE_CODE_ERROR_INSERTING_OBJECT = 405;
	/** Constant for "Request rejected" response code. */
	public static final short RESPONSE_CODE_REQUEST_REJECTED = 406;
	/** Constant for "Join request deferred" response code. */
	public static final short RESPONSE_CODE_JOIN_REQUEST_DEFERRED = 407;
	/** Constant for "TTL hops" response code. */
	public static final short RESPONSE_CODE_TTL_HOPS_EXCEEDED = 410;
	/** Constant for "Message too large" response code. */
	public static final short RESPONSE_CODE_MESSAGE_TOO_LARGE = 413;
	/** Constant for "Timeout" response code. */
	public static final short RESPONSE_CODE_TIMEOUT = 418;

	public static final boolean[] RESPONSE_CODE_OK_BITS_ARRAY = { false, true, true, false, false, true, false, false,
			false };
	public static final boolean[] RESPONSE_CODE_NEXT_HOP_BITS_ARRAY = { true, false, false, true, false, true, true,
			true, false };
	/** Constant for "Bad request" response code. */
	public static final boolean[] RESPONSE_CODE_BAD_REQUEST_BITS_ARRAY = { true, true, false, false, true, false,
			false, false, false };
	/** Constant for "Not found"response code. */
	public static final boolean[] RESPONSE_CODE_NOT_FOUND_BITS_ARRAY = { true, true, false, false, true, false, true,
			false, false };
	/** Constant for "Error inserting object" response code. */
	public static final boolean[] RESPONSE_CODE_ERROR_INSERTING_OBJECT_BITS_ARRAY = { true, true, false, false, true,
			false, true, false, true };
	/** Constant for "Request rejected" response code. */
	public static final boolean[] RESPONSE_CODE_REQUEST_REJECTED_BITS_ARRAY = { true, true, false, false, true, false,
			true, true, false };
	/** Constant for "Join request deferred" response code. */
	public static final boolean[] RESPONSE_CODE_JOIN_REQUEST_DEFERRED_BITS_ARRAY = { true, true, false, false, true,
			false, true, true, true };
	/** Constant for "TTL hops" response code. */
	public static final boolean[] RESPONSE_CODE_TTL_HOPS_EXCEEDED_BITS_ARRAY = { true, true, false, false, true, true,
			false, true, false };
	/** Constant for "Message too large" response code. */
	public static final boolean[] RESPONSE_CODE_MESSAGE_TOO_LARGE_BITS_ARRAY = { true, true, false, false, true, true,
			true, false, true };
	/** Constant for "Timeout" response code. */
	public static final boolean[] RESPONSE_CODE_TIMEOUT_BITS_ARRAY = { true, true, false, true, false, false, false,
			true, false };

	/**
	 * Empty constructor that doesn't fill any fields of this object. Useful for creating empty message that could be
	 * filled with data later (for instance, we read bytes from stream and fill fields one by one).
	 */
	public Response() {

	}

	/**
	 * Constructor for general response message.
	 * 
	 * @param protocolVersion
	 * @param messageType
	 *            Response or responseACK. Constants for this are in P2PPMessage class.
	 * @param isAcknowledgment
	 * @param isSentByPeer
	 * @param isRecursive
	 * @param responseCode
	 * @param requestOrResponseType
	 * @param ttl
	 * @param transactionID
	 * @param sourceID
	 * @param responseID
	 * @param isOverReliable
	 * @param isEncrypted
	 */
	public Response(boolean[] protocolVersion, boolean[] messageType, boolean isAcknowledgment, boolean isSentByPeer,
			boolean isRecursive, boolean[] responseCode, byte requestOrResponseType, byte ttl, byte[] transactionID,
			byte[] sourceID, byte[] responseID, boolean isOverReliable, boolean isEncrypted) {

		super(protocolVersion, messageType, isAcknowledgment, isSentByPeer, isRecursive, responseCode,
				requestOrResponseType, ttl, transactionID, sourceID, responseID, isOverReliable, isEncrypted);

	}

	@Override
	protected byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		// probably nothing happens, because responseID is added in P2PPMessage (if necessary)
		// int currentIndex = super.getBitsCount();

		return bytes;
	}

	@Override
	public int getBitsCount() {
		return super.getBitsCount();
	}

	/**
	 * Returns response code as int.
	 * 
	 * @return
	 */
	public int getResponseCodeAsInt() {
		return ByteUtils.booleanArrayToInt(reservedOrResponseCode);
	}
}
