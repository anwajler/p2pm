package pl.edu.pjwstk.p2pp.messages.responses;

import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * TransferResponse as defined in P2PP specification (draft 01).
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class TransferResponse extends Response {

	private PeerInfo responseCreaterPeerInfo;

	/**
	 * Creates empty TransferResponse.
	 */
	public TransferResponse() {

	}

	/**
	 * 
	 * @param protocolVersion
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
	public TransferResponse(boolean[] protocolVersion, boolean isAcknowledgment, boolean isSentByPeer,
			boolean isRecursive, boolean[] responseCode, byte ttl, byte[] transactionID, byte[] sourceID,
			byte[] responseID, boolean isOverReliable, boolean isEncrypted, PeerInfo peerInfo) {
		super(protocolVersion, P2PPMessage.RESPONSE_MESSAGE_TYPE, isAcknowledgment, isSentByPeer, isRecursive,
				responseCode, P2PPMessage.TRANSFER_MESSAGE_TYPE, (byte) 255, transactionID, sourceID, responseID,
				isOverReliable, isEncrypted);

		this.responseCreaterPeerInfo = peerInfo;

	}

	@Override
	public void addObject(GeneralObject object) throws UnsupportedGeneralObjectException {
		if (object instanceof PeerInfo) {
			responseCreaterPeerInfo = (PeerInfo) object;
		} else {
			throw new UnsupportedGeneralObjectException("TransferResponse can't contain " + object.getClass().getName()
					+ " object as subobject.");
		}
	}

	@Override
	public PeerInfo getPeerInfo() {
		return responseCreaterPeerInfo;
	}

	@Override
	public boolean verify() {
		if (responseCreaterPeerInfo == null) {
			return false;
		} else {
			// TODO more verification
			return true;
		}
	}

	@Override
	public byte[] asBytes() {
		return asBytes(getBitsCount());
	}

	@Override
	protected byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		int currentIndex = super.getBitsCount();
		ByteUtils.addByteArrayToArrayAtBitIndex(responseCreaterPeerInfo.asBytes(), bytes, currentIndex);
		// currentIndex += responseCreaterPeerInfo.getBitsCount();

		return bytes;
	}

	@Override
	public int getBitsCount() {
		return super.getBitsCount() + responseCreaterPeerInfo.getBitsCount();
	}

}
