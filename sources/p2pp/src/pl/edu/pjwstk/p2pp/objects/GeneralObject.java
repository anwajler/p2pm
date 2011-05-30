package pl.edu.pjwstk.p2pp.objects;

import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * Abstract class for objects used in Messages in Peer-to-Peer Protocol.
 * 
 * @see P2PPMessage
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public abstract class GeneralObject {

	/**
	 * Redefined standard Java toString() method to show what does GeneralObject consist of.
	 */
	@Override
	public String toString() {
        StringBuilder strb = new StringBuilder("header=[type=");
        strb.append(type);
        strb.append("; AB=");
        strb.append(ByteUtils.booleanArrayToBinaryString(ab));
        strb.append("; reserved=");
        strb.append(ByteUtils.booleanArrayToBinaryString(RESERVED_FIELD));
        strb.append("; length=");
        strb.append(length);
        strb.append("] value=");
		return strb.toString();
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	/** Constant for PeerInfo object type. */
	public static final byte PEER_INFO_OBJECT_TYPE = 0x00;
	/** Constant for PeerID object type. */
	public static final byte PEER_ID_OBJECT_TYPE = 0x01;
	/** Constant for AddressInfo object type. */
	public static final byte ADDRESS_INFO_OBJECT_TYPE = 0x02;
	/** Constant for UnhashedID object type. */
	public static final byte UNHASHED_ID_OBJECT_TYPE = 0x03;
	/** Constant for Uptime object type. */
	public static final byte UPTIME_OBJECT_TYPE = 0x04;
	/** Constant for P2POptions object type. */
	public static final byte P2P_OPTIONS_OBJECT_TYPE = 0x05;
	/** Constant for RequestOptions object type. */
	public static final byte REQUEST_OPTIONS_OBJECT_TYPE = 0x06;
	/** Constant for Diagnostics object type. */
	public static final byte DIAGNOSTICS_OPTIONS_OBJECT_TYPE = 0x07;
	/** Constant for RoutingTable object type. */
	public static final byte ROUTING_TABLE_OBJECT_TYPE = 0x08;
	/** Constant for NeighborTable object type. */
	public static final byte NEIGHBOR_TABLE_OBJECT_TYPE = 0x09;
	/** Constant for PLookup object type. */
	public static final byte P_LOOKUP_OBJECT_TYPE = 0x0A;
	/** Constant for ResourceID object type. */
	public static final byte RESOURCE_ID_OBJECT_TYPE = 0x0B;
	/** Constant for RLookup object type. */
	public static final byte R_LOOKUP_OBJECT_TYPE = 0x0C;
	/** Constant for ResourceObject object type. */
	public static final byte RESOURCE_OBJECT_OBJECT_TYPE = 0x0D;
	/** Constant for Expires object type. */
	public static final byte EXPIRES_OBJECT_TYPE = 0x0E;
	/** Constant for Owner object type. */
	public static final byte OWNER_OBJECT_TYPE = 0x0F;
	/** Constant for Certificate Sign Request object type. */
	public static final byte CERTIFICATE_SIGN_REQUEST_OBJECT_TYPE = 0x10;
	/** Constant for X509Certificate object type. */
	public static final byte X509_CERTIFICATE_OBJECT_TYPE = 0x11;
	/** Constant for X509Signature object type. */
	public static final byte X509_CER7_SIGNATURE_OBJECT_TYPE = 0x12;
	/** Constant for TimeWindow object type. */
	public static final byte TIME_WINDOW_OBJECT_TYPE = 0x13;
	/** Constant for Connections object type. */
	public static final byte CONNECTIONS_OBJECT_TYPE = 0x14;
	/** Constant for NodeResourceUtilization object type. */
	public static final byte NODE_RESOURCE_UTILIZATION_OBJECT_TYPE = 0x15;
	/** Constant for MessagesReceived object type. */
	public static final byte MESSAGES_RECEIVED_OBJECT_TYPE = 0x16;
	/** Constant for ASNumber object type. */
	public static final byte AS_NUMBER_OBJECT_TYPE = 0x17;
	/** Constant for Error object type. */
	public static final byte ERROR_OBJECT_TYPE = 0x18;
	/** Constant for ResourceObjectValue object type. */
	public static final byte RESOURCE_OBJECT_VALUE_OBJECT_TYPE = 0x19;
        public static final byte SOCIAL_PATH_OBJECT_TYPE = 0x1A;
        public static final byte PATH_ID_OBJECT_TYPE = 0x1B;

	/** TODO proabably deleteLength of the header of resource object. */
	// private static final short RESOURCE_OBJECT_HEADER_LENGTH = 26;
	/** Length of the type field from header. */
	@SuppressWarnings("unused")
	private static final short HEADER_TYPE_FIELD_LENGTH = 8;

	/** Index of first bit of length field in header. */
	private static final int LENGTH_FIELD_INDEX = 16;

	/** Index of first bit of AB field in header. */
	private static final int AB_FIELD_START_INDEX = 8;

	/** Index of first bit of reserved field in header. */
	private static final int RESERVED_FIELD_INDEX = 10;

	private static final boolean[] RESERVED_FIELD = new boolean[] { false, false, false, false, false, false, false,
			false };

	/** An IANA-assigned identifier for the type of the object. */
	private byte type = 0;

	/** Field of header with mandatory or ignore meaning. */
	private boolean[] ab = new boolean[] { false, false };

	/** Length (in bytes) of this object. */
	private short length;

	/**
	 * Constructor for resource-object of given type.
	 * 
	 * @param type
	 *            Type of resource-object that will be constructed. There are constants in this class for this.
	 */
	public GeneralObject(byte type) {
		this.type = type;
	}

	/**
	 * Returns length (in bits) of this object. Every subclass MUST use super.getBitsCount() for getting the length of a
	 * super-class. Then adds own length and returns that number.
	 * 
	 * @return Length (in bits) of this object.
	 */
	public int getBitsCount() {
		return 32;
	}

	/**
	 * Sets AB field in header.
	 * 
	 * @param ab
	 *            Value of header. Must be two bits long.
	 */
	public void setAb(boolean[] ab) {
		this.ab = ab;
	}

	/**
	 * Sets length of this object.
	 * 
	 * @param length
	 */
	public void setLength(short length) {
		this.length = length;
	}

	/**
	 * Returns length (in bytes) of this object.
	 * 
	 * @return
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Returns byte representation of this object.
	 * 
	 * @param bitsCount
	 *            Number of bits that this byte representation will have to contain.
	 */
	protected byte[] asBytes(int bitsCount) {

		// Calculates number of bytes for storing given number of bits.
		int bytesCount = bitsCount / 8;
		byte[] bytes = new byte[bytesCount];

		// sets length after the header
		length = (short) (bytesCount - 4);

		bytes[0] = type;

		ByteUtils.addBooleanArrayToArrayAtIndex(ab, bytes, AB_FIELD_START_INDEX);
		ByteUtils.addBooleanArrayToArrayAtIndex(RESERVED_FIELD, bytes, RESERVED_FIELD_INDEX);

		ByteUtils.addShortToArrayAtBitIndex(length, bytes, LENGTH_FIELD_INDEX);

		return bytes;
	}

	/**
	 * Returns byte representation of this object.
	 * 
	 * @return Byte representation.
	 */
	public abstract byte[] asBytes();

	/**
	 * Adds given general object to this object.
	 * 
	 * @param subobject
	 *            GeneralObject to be added to this object.
	 * @throws UnsupportedGeneralObjectException
	 *             Throws when this object can't handle given subobject.
	 */
	public abstract void addSubobject(GeneralObject subobject) throws UnsupportedGeneralObjectException;
}
