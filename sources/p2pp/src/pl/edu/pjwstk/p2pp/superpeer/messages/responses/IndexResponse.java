package pl.edu.pjwstk.p2pp.superpeer.messages.responses;

import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.responses.Response;
import pl.edu.pjwstk.p2pp.objects.*;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

import java.util.Vector;

public class IndexResponse extends Response {

	/**
	 * PeerInfo object describing a PeerInfo that has sent this response.
	 */
	private PeerInfo responseOriginatorPeerInfo;

	/**
	 * Creates empty {@link IndexResponse}.
	 */
	public IndexResponse() {
	}

	/**
	 *
	 * @param protocolVersion
	 * @param messageType
	 * @param isAcknowledgment
	 * @param isSentByPeer
	 * @param isRecursive
	 * @param responseCode
	 * @param ttl
	 * @param transactionID
	 *            May be null and filled later with {@link #setTransactionID(byte[])} method.
	 * @param sourceID
	 * @param responseID
	 * @param isOverReliable
	 * @param isEncrypted
	 * @param responseOriginatorPeerInfo
	 */
	public IndexResponse(boolean[] protocolVersion, boolean[] messageType, boolean isAcknowledgment,
			boolean isSentByPeer, boolean isRecursive, boolean[] responseCode, byte ttl, byte[] transactionID,
			byte[] sourceID, byte[] responseID, boolean isOverReliable, boolean isEncrypted,
			PeerInfo responseOriginatorPeerInfo) {
		super(protocolVersion, messageType, isAcknowledgment, isSentByPeer, isRecursive, responseCode,
				P2PPMessage.INDEX_MESSAGE_TYPE, ttl, transactionID, sourceID, responseID, isOverReliable, isEncrypted);

		this.responseOriginatorPeerInfo = responseOriginatorPeerInfo;
	}

	@Override
	public void addObject(GeneralObject object) throws UnsupportedGeneralObjectException {
		if (object instanceof PeerInfo) {
			responseOriginatorPeerInfo = (PeerInfo) object;
		} else {
			throw new UnsupportedGeneralObjectException("IndexResponse can't contain " + object.getClass().getName() + " as object.");
		}
	}

	@Override
	public PeerInfo getPeerInfo() {
		return responseOriginatorPeerInfo;
	}

	@Override
	public boolean verify() {
		boolean result = true;

		if (responseOriginatorPeerInfo == null) {
			result = false;
		} else {
			// must contain peerID
			PeerID peerID = responseOriginatorPeerInfo.getPeerID();
			if (peerID == null) {
				result = false;
			} else {
				if (peerID.getPeerIDBytes() == null) {
					result = false;
				}
			}
			// not checked if peerID isn't here
			if (result) {
				// must contain unshadedID
				UnhashedID unhashedID = responseOriginatorPeerInfo.getUnhashedID();
				if (unhashedID == null) {
					result = false;
				} else {
					if (unhashedID.getUnhashedIDValue() == null) {
						result = false;
					}
				}
			}
			// not checked if PeerID or UnhashedID isn't here
			if (result) {
				// has to contain at least one AddressInfo
				Vector<AddressInfo> addressInfos = responseOriginatorPeerInfo.getAddressInfos();
				if (addressInfos == null) {
					result = false;
				} else if (addressInfos.size() <= 0) {
					result = false;
				}
			}
		}

		return result;
	}

	@Override
	public byte[] asBytes() {
		return asBytes(getBitsCount());
	}

	@Override
	public int getBitsCount() {
		return super.getBitsCount() + responseOriginatorPeerInfo.getBitsCount();
	}

	@Override
	protected byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		int currentIndex = super.getBitsCount();

		ByteUtils.addByteArrayToArrayAtByteIndex(responseOriginatorPeerInfo.asBytes(), bytes, currentIndex / 8);

		return bytes;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("IndexResponse=[message=[" + super.toString() + "], header=[" + getHeader() +
                "], responseOriginatorPeerInfo=[");
		if (responseOriginatorPeerInfo != null) {
			builder.append(responseOriginatorPeerInfo.toString());
		} else {
			builder.append("null");
		}
		builder.append("]]");
		return builder.toString();
	}

}