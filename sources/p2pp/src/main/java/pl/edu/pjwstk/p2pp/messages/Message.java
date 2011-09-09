package pl.edu.pjwstk.p2pp.messages;

import pl.edu.pjwstk.p2pp.debug.DebugInformation;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

/**
 * Abstract message of any protocol that may contain any type of data. It also contains information about an
 * originator's and recipient's address and their ports. The last thing is if the message was received or will be send
 * over reliable transport (TCP is one example) and if it was received or has to be send encrypted. Not all the members
 * have to be used simultaneusly. It depends on subclasses.
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 *
 */
public abstract class Message {

	@Override
	public String toString() {

        StringBuilder strb = new StringBuilder("Message=[senderAddress=");
        strb.append(senderAddress);
        strb.append("; senderPort=");
        strb.append(senderPort);
        strb.append("; receiverAddress=");
        strb.append(receiverAddress);
        strb.append("; receiverPort=");
        strb.append(receiverPort);
        strb.append("; protocol=");
        strb.append(P2PPUtils.protocolNameForPreferences(overReliable, encrypted));
        strb.append("]");

		return strb.toString();
	}

	/**
	 * True if this message was received or will be send over reliable transport. False otherwise.
	 */
	protected boolean overReliable;

	/**
	 * True if this message was received or will be send over encrypted transport. False otherwise.
	 */
	protected boolean encrypted;

	/**
	 * Address of originator of this message. In form of "xxx.xxx.xxx.xxx" or DNS name.
	 */
	protected String senderAddress;
	/**
	 * Port of originator of this message.
	 */
	protected int senderPort;

	/**
	 * Address of recipient of this message. In form of "xxx.xxx.xxx.xxx" or DNS name.
	 */
	protected String receiverAddress;
	/**
	 * Port of recipient of this message.
	 */
	protected int receiverPort;

	/**
	 * Default constructor. Doesn't initialize any fields.
	 */
	public Message() {}

	/**
	 * Creates message with information about if it was received/will be send over reliable transport (TCP is an
	 * example) and if it has to be send or was received encrypted.
	 *
	 * @param isEncrypted
	 * @param isOverReliable
	 */
	public Message(boolean isEncrypted, boolean isOverReliable) {
		this.encrypted = isEncrypted;
		this.overReliable = isOverReliable;
	}

	/**
	 *
	 * @param receiverAddress
	 *            Address of receiver of this message in IP or DNS format.
	 * @param receiverPort
	 * @param senderAddress
	 *            Address of sender of this message in IP or DNS format.
	 * @param senderPort
	 * @param isOverReliable
	 * @param isEncrypted
	 */
	public Message(String receiverAddress, int receiverPort, String senderAddress, int senderPort,
			boolean isOverReliable, boolean isEncrypted) {
		this.receiverAddress = receiverAddress;
		this.receiverPort = receiverPort;
		this.encrypted = isEncrypted;
		this.senderAddress = senderAddress;
		this.senderPort = senderPort;
		this.overReliable = isOverReliable;
	}

	/**
	 * Returns true if this message was received/has to be send over reliable transport. False otherwise.
	 *
	 * @return
	 */
	public boolean isOverReliable() {
		return overReliable;
	}

	/**
	 * Sets a parameter that says if this message was received/has to be send over reliable transport.
	 *
	 * @param overReliable
	 */
	public void setOverReliable(boolean overReliable) {
		this.overReliable = overReliable;
	}

	/**
	 * Returns true if this message was received/has to be send encrypted.
	 *
	 * @return
	 */
	public boolean isEncrypted() {
		return encrypted;
	}

	/**
	 * Sets if this message was receiver/has to be send encrypted.
	 *
	 * @param encrypted
	 */
	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}

	/**
	 * Gets address (in DNS or IP format) of the sender of this message.
	 *
	 * @return
	 */
	public String getSenderAddress() {
		return senderAddress;
	}

	/**
	 * Sets address (in DNS or IP format) of the sender of this message.
	 *
	 * @param senderAddress
	 */
	public void setSenderAddress(String senderAddress) {
		this.senderAddress = senderAddress;
	}

	/**
	 * Gets port number of sender of this message.
	 *
	 * @return
	 */
	public int getSenderPort() {
		return senderPort;
	}

	/**
	 * Sets port number of sender of this message.
	 *
	 * @param senderPort
	 */
	public void setSenderPort(int senderPort) {
		this.senderPort = senderPort;
	}

	/**
	 * Returns address (in DNS or IP format) of receiver of this message. If this message was received, it's local
	 * address.
	 *
	 * @return
	 */
	public String getReceiverAddress() {
		return receiverAddress;
	}

	/**
	 * Sets receiver (in DNS or IP format) of this message.
	 *
	 * @param receiverAddress
	 */
	public void setReceiverAddress(String receiverAddress) {
		this.receiverAddress = receiverAddress;
	}

	/**
	 * Returns port of the receiver of this message.
	 *
	 * @return
	 */
	public int getReceiverPort() {
		return receiverPort;
	}

	public void setReceiverPort(int receiverPort) {
		this.receiverPort = receiverPort;
	}

	/**
	 * Sets receiver address. Address has to be IPv4 or IPv6, so length of address array MUST be 4 or 16. Otherwise
	 * nothing happens.
	 *
	 * @param address
	 */
	public void setReceiverAddress(byte[] address) {
		if (address.length != 4) {
			if (address.length != 6) {
				return;
			}
		}
		StringBuffer buffer = new StringBuffer();
		int size = address.length;
		for (int i = 0; i < size; i++) {
            String byteStr = "" + (address[i] & 0xFF);
			buffer.append(byteStr);
			if (i < size - 1) {
				buffer.append(".");
			}
		}
		receiverAddress = buffer.toString();
	}

	/**
	 * Returns sender address as bytes. If address is in DNS form, DNS lookup is made.
	 *
	 * @return
	 */
	public byte[] getSenderAddressAsBytes() {
		// FIXME now only xxx.xxx.xxx.xxx is handled.
		byte[] addressBytes = new byte[4];
		int addressIndex = 0;
		for (int i = 0; i < 3; i++) {
			int tempIndex = senderAddress.indexOf('.', addressIndex);
			int current = Integer.parseInt(senderAddress.substring(addressIndex, tempIndex));
			addressBytes[i] = (byte) current;
			addressIndex = tempIndex + 1;
		}
		addressBytes[3] = (byte) Integer.parseInt(senderAddress.substring(addressIndex));
		return addressBytes;
	}

	/**
	 * Method that creates byte representation of the message. It must be implemented in subclasses.
	 *
	 * @return Byte representation of this message.
	 */
	public abstract byte[] asBytes();

    /**
     * Returns debug information being an array of Strings which order is defined by
     * {@link pl.edu.pjwstk.p2pp.debug.DebugFields}.
     *
     * @return
     */
    public abstract DebugInformation getDebugInformation();

}
