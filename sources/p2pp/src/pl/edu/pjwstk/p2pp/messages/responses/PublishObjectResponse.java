package pl.edu.pjwstk.p2pp.messages.responses;

import java.util.Vector;

import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.requests.PublishObjectRequest;
import pl.edu.pjwstk.p2pp.objects.AddressInfo;
import pl.edu.pjwstk.p2pp.objects.Expires;
import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.PeerID;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.UnhashedID;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * Response to {@link PublishObjectRequest}, as defined in P2PP specification (draft 01).
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class PublishObjectResponse extends Response {

	/**
	 * PeerInfo object describing a PeerInfo that has sent this response.
	 */
	private PeerInfo responseOriginatorPeerInfo;

	/**
	 * Expires object describing a time (in seconds) after which a resource has to be re-published.
	 */
	private Expires expires;

	/**
	 * Creates empty {@link PublishObjectResponse}.
	 */
	public PublishObjectResponse() {
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
	 * @param expires
	 */
	public PublishObjectResponse(boolean[] protocolVersion, boolean[] messageType, boolean isAcknowledgment,
			boolean isSentByPeer, boolean isRecursive, boolean[] responseCode, byte ttl, byte[] transactionID,
			byte[] sourceID, byte[] responseID, boolean isOverReliable, boolean isEncrypted,
			PeerInfo responseOriginatorPeerInfo, Expires expires) {
		super(protocolVersion, messageType, isAcknowledgment, isSentByPeer, isRecursive, responseCode,
				P2PPMessage.PUBLISH_MESSAGE_TYPE, ttl, transactionID, sourceID, responseID, isOverReliable, isEncrypted);

		this.responseOriginatorPeerInfo = responseOriginatorPeerInfo;
		this.expires = expires;
	}

	@Override
	public void addObject(GeneralObject object) throws UnsupportedGeneralObjectException {
		if (object instanceof PeerInfo) {
			responseOriginatorPeerInfo = (PeerInfo) object;
		} else if (object instanceof Expires) {
			expires = (Expires) object;
		} else {
			throw new UnsupportedGeneralObjectException("PublishObjectResponse can't contain "
					+ object.getClass().getName() + " as object.");
		}
	}

	@Override
	public PeerInfo getPeerInfo() {
		return responseOriginatorPeerInfo;
	}

	public Expires getExpires() {
		return expires;
	}

	public void setExpires(Expires expires) {
		this.expires = expires;
	}

	@Override
	public boolean verify() {
		boolean result = true;

		if (responseOriginatorPeerInfo == null || expires == null) {
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
		return super.getBitsCount() + responseOriginatorPeerInfo.getBitsCount() + expires.getBitsCount();
	}

	@Override
	protected byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		int currentIndex = super.getBitsCount();

		ByteUtils.addByteArrayToArrayAtByteIndex(responseOriginatorPeerInfo.asBytes(), bytes, currentIndex / 8);
		currentIndex += responseOriginatorPeerInfo.getBitsCount();

		ByteUtils.addByteArrayToArrayAtByteIndex(expires.asBytes(), bytes, currentIndex / 8);
		// currentIndex += expires.getBitsCount();

		return bytes;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("PublishObjectResponse=[message=[" + super.toString() + "], header=["
				+ getHeader() + "], responseOriginatorPeerInfo=[");
		if (responseOriginatorPeerInfo != null) {
			builder.append(responseOriginatorPeerInfo.toString());
		} else {
			builder.append(responseOriginatorPeerInfo);
		}
		builder.append("], expires=[");
		if (expires != null) {
			builder.append(expires.toString());
		} else {
			builder.append(expires);
		}
		return builder.toString() + "]]";
	}

}
