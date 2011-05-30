package pl.edu.pjwstk.p2pp.objects;

import pl.edu.pjwstk.p2pp.util.Arrays;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * Object that contains info about address.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class AddressInfo extends GeneralObject {

	@Override
	public String toString() {
        // TODO StringBuilder
		return super.toString() + "[AddressInfo=[num=" + numberOfICECandidates + ", "
				+ ByteUtils.booleanArrayToBinaryString(new boolean[] { rFlag }) + ", reserved="
				+ ByteUtils.booleanArrayToBinaryString(reserved) + ", ipVer="
				+ ByteUtils.booleanArrayToBinaryString(ipVersion) + ", foundation=" + foundation + ", componendID="
				+ componentID + ", priority=" + priority + ", tt=" + ByteUtils.booleanArrayToBinaryString(tt) + ", tt="
				+ ByteUtils.booleanArrayToBinaryString(ht) + ", port=" + port + ", address="
				+ ByteUtils.bytesToStringIP(address) + "]]";
	}

	/** Constant representing IP v4 protocol in AddressInfo object. */
	public static final boolean[] IP_V4 = new boolean[] { false, true, false, false };
	/** Constant representing IP v6 protocol used in AddressInfo object. */
	public static final boolean[] IP_V6 = new boolean[] { false, true, true, false };
	/**
	 * Constant representing UDP transport type of address used in AddressInfo object.
	 */
	public static final boolean[] UDP_TRANSPORT_TYPE = new boolean[4];
	/**
	 * Constant representing TCP transport type of address used in AddressInfo object.
	 */
	public static final boolean[] TCP_TRANSPORT_TYPE = new boolean[] { false, false, false, true };

	/** Constant representing host address type of the peer as defined in ICE. */
	public static final boolean[] HOST_ADDRESS_TYPE = new boolean[4];
	/**
	 * Constant representing server reflexive address type of the peer as defined in ICE.
	 */
	public static final boolean[] SERVER_REFLEXIVE_ADDRESS_TYPE = new boolean[] { false, false, false, true };
	/**
	 * Constant representing peer reflexive address type of the peer as defined in ICE.
	 */
	public static final boolean[] PEER_REFLEXIVE_ADDRESS_TYPE = new boolean[] { false, false, true, false };
	/**
	 * Constant representing relayed candidate address type of the peer as defined in ICE.
	 */
	public static final boolean[] RELAYED_CANDIDATE_ADDRESS_TYPE = new boolean[] { false, false, true, true };

	/** Length (in bytes) of static part of address info. */
	private static final int ADDRESS_INFO_STATIC_SPECIFIC_DATA_LENGTH = 88;

	/**
	 * TODO Spec says that this is 4 bits in place but in other it says that it has eight bits.
	 */
	private byte numberOfICECandidates;
	/**
	 * If set, rel-addr and rel-port are included as defined in ICE.
	 */
	private boolean rFlag;

	private boolean[] reserved = new boolean[] { false, false, false };

	/**
	 * The IP version number, 4 or 6.
	 */
	private boolean[] ipVersion;
	/**
	 * The foundation field as defined by ICE. Note that the length of this field is only 8-bits as compared to 64- bits
	 * as defined by ICE specification.
	 */
	private byte foundation;

	/**
	 * The component-ID field as defined by ICE.
	 */
	private byte componentID;

	/**
	 * The priority of the address obtained through ICE.
	 */
	private int priority;

	/**
	 * The transport type of the address. One of UDP (0000), or TCP (0001).
	 */
	private boolean[] tt;

	/**
	 * The address type of the peer as defined in ICE [7]. One of host (0000), server reflexive (0001), peer reflexive
	 * (0010), or relayed candidate (0011).
	 */
	private boolean[] ht;

	/**
	 * The port on which this peer listens for requests.
	 */
	private short port;

	/**
	 * The IP address of the peer. Its length depends on the IP-Ver field.
	 */
	private byte[] address;

	/**
	 * Constructor for AddressInfo. TODO There's a problem with NumberOfICECandidates (spec says it has 4 or 8 bits
	 * lenght).
	 * 
	 * @param numberOfICECandidates
	 * @param rFlag
	 *            If set, rel-addr and rel-port are included as defined in ICE
	 * @param ipVersion
	 *            There are constants in this class for this.
	 * @param foundation
	 *            The foundation field as defined by ICE. Note that the length of this field is only 8-bits as compared
	 *            to 64- bits as defined by ICE specification.
	 * @param componentID
	 *            The component-ID field as defined by ICE.
	 * @param priority
	 *            The priority of the address obtained through ICE.
	 * @param tt
	 *            The transport type of the address. One of UDP (0000), or TCP (0001). (constants are in this class).
	 * @param ht
	 *            The address type of the peer as defined in ICE [7]. One of host (0000), server reflexive (0001), peer
	 *            reflexive (0010), or relayed candidate (0011).(constants are in this class).
	 * 
	 * @param port
	 *            The port on which this peer listens for requests.
	 * @param address
	 *            The IP address of the peer. Its length depends on the ipVersion field.
	 */
	public AddressInfo(byte numberOfICECandidates, boolean rFlag, boolean[] ipVersion, byte foundation,
			byte componentID, int priority, boolean[] tt, boolean[] ht, int port, byte[] address) {
		super(GeneralObject.ADDRESS_INFO_OBJECT_TYPE);

		this.numberOfICECandidates = numberOfICECandidates;
		this.rFlag = rFlag;
		this.ipVersion = ipVersion;
		this.foundation = foundation;
		this.componentID = componentID;
		this.priority = priority;
		this.tt = tt;
		this.ht = ht;
		this.port = (short) port;
		this.address = address;
	}

	@Override
	public byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		int currentBitIndex = super.getBitsCount();

		ByteUtils.addByteToArrayAtBitIndex(numberOfICECandidates, bytes, currentBitIndex);
		currentBitIndex += 8;
		ByteUtils.addBooleanArrayToArrayAtIndex(new boolean[] { rFlag }, bytes, currentBitIndex);
		currentBitIndex += 1;
		ByteUtils.addBooleanArrayToArrayAtIndex(reserved, bytes, currentBitIndex);
		currentBitIndex += reserved.length;
		ByteUtils.addBooleanArrayToArrayAtIndex(ipVersion, bytes, currentBitIndex);
		currentBitIndex += ipVersion.length;
		ByteUtils.addByteToArrayAtBitIndex(foundation, bytes, currentBitIndex);
		currentBitIndex += 8;
		ByteUtils.addByteToArrayAtBitIndex(componentID, bytes, currentBitIndex);
		currentBitIndex += 8;
		ByteUtils.addIntToArrayAtBitIndex(priority, bytes, currentBitIndex);
		currentBitIndex += 32;
		ByteUtils.addBooleanArrayToArrayAtIndex(tt, bytes, currentBitIndex);
		currentBitIndex += 4;
		ByteUtils.addBooleanArrayToArrayAtIndex(ht, bytes, currentBitIndex);
		currentBitIndex += 4;
		ByteUtils.addShortToArrayAtBitIndex(port, bytes, currentBitIndex);
		currentBitIndex += 16;
		ByteUtils.addByteArrayToArrayAtBitIndex(address, bytes, currentBitIndex);

		return bytes;
	}

	@Override
	public int getBitsCount() {
		// TODO I don't know if Num field is 8 or 4 bits long.
		return super.getBitsCount() + ADDRESS_INFO_STATIC_SPECIFIC_DATA_LENGTH + (address.length * 8);
	}

	@Override
	public byte[] asBytes() {
		int bitsCount = getBitsCount();
		return asBytes(bitsCount);
	}

	@Override
	public void addSubobject(GeneralObject subobject) throws UnsupportedGeneralObjectException {
		throw new UnsupportedGeneralObjectException("AddressInfo can't handle any inner objects.");
	}

	/**
	 * IPv4 or v6 address in this object.
	 * 
	 * @return
	 */
	public byte[] getAddress() {
		return address;
	}

	/**
	 * Returns port stored in this object.
	 * 
	 * @return
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Returns address included in this object. Address is IP v4 or v6, so will and will be returned as
	 * "xxx.xxx.xxx.xxx" (v4) or "xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx". Null is returned when there's no
	 * address in this object or address length is not 4 or 16 (so address is not a proper one).
	 * 
	 * @return Address in text form.
	 */
	public String getAddressAsString() {
		if (address == null || (address.length != 4 && address.length != 16)) {
			return null;
		} else {
			int size = address.length;
			StringBuffer buffer = new StringBuffer();
			if (size == 4) {
				for (int i = 0; i < 4; i++) {
					buffer.append(address[i] & 0xFF);
					if (i < 3) {
						buffer.append(".");
					}
				}
			} else {
				// TODO add IPv6 address
				for (int i = 0; i < 16; i++) {
					buffer.append(address[i] & 0xFF);
					if (i < 15) {
						buffer.append(":");
					}
				}
			}
			return buffer.toString();
		}
	}

	public byte getNumberOfICECandidates() {
		return numberOfICECandidates;
	}

	public void setNumberOfICECandidates(byte numberOfICECandidates) {
		this.numberOfICECandidates = numberOfICECandidates;
	}

	public boolean isRFlag() {
		return rFlag;
	}

	public void setRFlag(boolean flag) {
		rFlag = flag;
	}

	public boolean[] getReserved() {
		return reserved;
	}

	public void setReserved(boolean[] reserved) {
		this.reserved = reserved;
	}

	public boolean[] getIpVersion() {
		return ipVersion;
	}

	public byte getFoundation() {
		return foundation;
	}

	public void setFoundation(byte foundation) {
		this.foundation = foundation;
	}

	public byte getComponentID() {
		return componentID;
	}

	public void setComponentID(byte componentID) {
		this.componentID = componentID;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * Returns the transport type of the address. One of UDP (0000), or TCP (0001).
	 * 
	 * @return
	 */
	public boolean[] getTt() {
		return tt;
	}

	/**
	 * Sets the transport type of the address. One of UDP (0000), or TCP (0001).
	 * 
	 * @param tt
	 */
	public void setTt(boolean[] tt) {
		this.tt = tt;
	}

	/**
	 * Gets the address type of the peer as defined in ICE. One of host (0000), server reflexive (0001), peer reflexive
	 * (0010), or relayed candidate (0011).
	 * 
	 * @return
	 */
	public boolean[] getHt() {
		return ht;
	}

	/**
	 * Sets the address type of the peer as defined in ICE. One of host (0000), server reflexive (0001), peer reflexive
	 * (0010), or relayed candidate (0011).
	 * 
	 * @param ht
	 */
	public void setHt(boolean[] ht) {
		this.ht = ht;
	}

	public void setPort(short port) {
		this.port = port;
	}

	/**
	 * Sets
	 * 
	 * @param address
	 */
	public void setAddress(byte[] address) {
		this.address = address;
		if (address.length == 4)
			this.ipVersion = IP_V4;
		else if (address.length == 16) {
			this.ipVersion = IP_V6;
		}
	}

	/**
	 * Returns address type (HT in AddressInfo) as integer. Returns -1 if given HT is not compatible with constant
	 * values of AddressInfo
	 * 
	 * @return
	 */
	public static int addressTypeToInteger(boolean[] ht) {
		int asInt = -1;
		if (Arrays.equals(ht, SERVER_REFLEXIVE_ADDRESS_TYPE)) {
			asInt = 1;
		} else if (Arrays.equals(ht, HOST_ADDRESS_TYPE)) {
			asInt = 0;
		} else if (Arrays.equals(ht, PEER_REFLEXIVE_ADDRESS_TYPE)) {
			asInt = 2;
		} else if (Arrays.equals(ht, RELAYED_CANDIDATE_ADDRESS_TYPE)) {
			asInt = 3;
		}
		return asInt;
	}

	/**
	 * TODO Returns address type (as defined in AddressInfo class) for given integer. Returns null if integer is not
	 * compatible with address types defined in this class.
	 * 
	 * @param integer
	 * @return
	 */
	public static boolean[] integerToAddressType(int integer) {
		boolean[] asArray = new boolean[4];
		switch (integer) {
		case 0:
			// doesn't do anything because array is filled with zeroes and we want this
			break;
		case 1:
			asArray[3] = true;
			break;
		case 2:
			asArray[2] = true;
			break;
		case 3:
			asArray[3] = true;
			asArray[2] = true;
			break;
		default:
			asArray = null;
		}
		return asArray;
	}
}
