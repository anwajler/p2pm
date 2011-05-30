package pl.edu.pjwstk.p2pp.messages.responses;

import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * RemoveObjectResponse as defined by P2PP specification (draft 01).
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class RemoveObjectResponse extends Response {

	private PeerInfo peerInfo;

	/**
	 * Constructor for empty response. Convinient for filling this object later.
	 */
	public RemoveObjectResponse() {
		super();
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
	 * @param sourceID
	 * @param responseID
	 * @param isOverReliable
	 * @param isEncrypted
	 * @param peerInfo
	 */
	public RemoveObjectResponse(boolean[] protocolVersion, boolean[] messageType, boolean isAcknowledgment,
			boolean isSentByPeer, boolean isRecursive, boolean[] responseCode, byte ttl, byte[] transactionID,
			byte[] sourceID, byte[] responseID, boolean isOverReliable, boolean isEncrypted, PeerInfo peerInfo) {
		super(protocolVersion, messageType, isAcknowledgment, isSentByPeer, isRecursive, responseCode,
				P2PPMessage.REMOVE_OBJECT_MESSAGE_TYPE, ttl, transactionID, sourceID, responseID, isOverReliable,
				isEncrypted);
		this.peerInfo = peerInfo;
	}

	@Override
	public void addObject(GeneralObject object) throws UnsupportedGeneralObjectException {

		if (object instanceof PeerInfo) {
			peerInfo = (PeerInfo) object;
		} else {
			throw new UnsupportedGeneralObjectException("RemoveObjectResponse can't contain "
					+ object.getClass().getName() + " as subobject.");
		}
	}

	@Override
	public PeerInfo getPeerInfo() {
		return peerInfo;
	}

	@Override
	public boolean verify() {
		if (peerInfo == null) {
			return false;
		}
		// TODO more handling

		return true;
	}

	@Override
	public byte[] asBytes() {
		return asBytes(getBitsCount());
	}

	@Override
	protected byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		int index = super.getBitsCount();

		ByteUtils.addByteArrayToArrayAtByteIndex(peerInfo.asBytes(), bytes, index / 8);
		index += peerInfo.getBitsCount();

		return bytes;
	}

	@Override
	public int getBitsCount() {
		return super.getBitsCount() + peerInfo.getBitsCount();
	}

}
