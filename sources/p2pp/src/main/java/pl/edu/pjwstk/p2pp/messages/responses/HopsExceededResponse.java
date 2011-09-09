package pl.edu.pjwstk.p2pp.messages.responses;

import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;

/**
 * Class representing a "TTL Hops exceeded response.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class HopsExceededResponse extends Response {

	/**
	 * Creates {@link HopsExceededResponse} object.
	 * 
	 * @param protocolVersion
	 * @param messageType
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
	public HopsExceededResponse(boolean[] protocolVersion, boolean[] messageType, boolean isAcknowledgment,
			boolean isSentByPeer, boolean isRecursive, boolean[] responseCode, byte requestOrResponseType, byte ttl,
			byte[] transactionID, byte[] sourceID, byte[] responseID, boolean isOverReliable, boolean isEncrypted) {
		super(protocolVersion, messageType, isAcknowledgment, isSentByPeer, isRecursive, responseCode,
				requestOrResponseType, ttl, transactionID, sourceID, responseID, isOverReliable, isEncrypted);
	}

	@Override
	public void addObject(GeneralObject object) throws UnsupportedGeneralObjectException {
		throw new UnsupportedGeneralObjectException("HopsExceededResponse can't contain " + object.getClass().getName()
				+ " as a subobject.");
	}

	@Override
	public byte[] asBytes() {
		return super.asBytes(getBitsCount());
	}

	@Override
	public PeerInfo getPeerInfo() {
		return null;
	}

	@Override
	public boolean verify() {
		// TODO really?
		return true;
	}

}
