package pl.edu.pjwstk.p2pp.messages.requests;

import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.responses.HopsExceededResponse;
import pl.edu.pjwstk.p2pp.messages.responses.NextHopResponse;
import pl.edu.pjwstk.p2pp.messages.responses.Response;
import pl.edu.pjwstk.p2pp.objects.PeerID;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.RequestOptions;

/**
 * <p>
 * Class describing request as defined in P2PP protocol (draft 01).
 * </p>
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public abstract class Request extends P2PPMessage {

	/** TODO Number of bits used for request-specific data. */
	private static final int REQUEST_SPECIFIC_DATA_SIZE = 0;

	/**
	 * Creates request.
	 * 
	 * @param protocolVersion
	 * @param isAcknowledgment
	 * @param isSentByPeer
	 * @param isRecursive
	 * @param requestType
	 * @param ttl
	 * @param transactionID
	 * @param sourceID
	 * @param isOverReliable
	 * @param isEncrypted
	 */
	public Request(boolean[] protocolVersion, boolean isAcknowledgment, boolean isSentByPeer, boolean isRecursive,
			byte requestType, byte ttl, byte[] transactionID, byte[] sourceID, boolean isOverReliable,
			boolean isEncrypted) {
		super(protocolVersion, P2PPMessage.REQUEST_MESSAGE_TYPE, isAcknowledgment, isSentByPeer, isRecursive,
				new boolean[9], requestType, ttl, transactionID, sourceID, null, isOverReliable, isEncrypted);
	}

	/**
	 * Empty constructor. Just for creating object that could be filled with data later.
	 */
	public Request() {

	}

	@Override
	protected byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		// TODO Add Request-specific data.
		// int startIndex = super.getBitsCount();

		return bytes;
	}

	@Override
	public int getBitsCount() {
		return super.getBitsCount() + REQUEST_SPECIFIC_DATA_SIZE;
	}

	/**
	 * Returns RequestOptions object from this request. If there's no such an object, null is returned.
	 * 
	 * @return RequestOptions object from this request.
	 * @see RequestOptions
	 */
	public abstract RequestOptions getRequestOptions();

	/**
	 * Creates "TTL Hops Exceeded" response for this message.
	 * 
	 * @param responseIDToBeAdded
	 *            PeerID to be included in response as responseID.
     *
     * @return HopsExceededResponse
	 */
	public HopsExceededResponse createTTLHopsExceededResponse(PeerID responseIDToBeAdded) {
		return new HopsExceededResponse(protocolVersion,
				P2PPMessage.RESPONSE_ACK_MESSAGE_TYPE, false, isByPeer(), isRecursive(),
				Response.RESPONSE_CODE_TTL_HOPS_EXCEEDED_BITS_ARRAY, requestOrResponseType, (byte) 0, transactionID,
				sourceID, responseIDToBeAdded.getPeerIDBytes(), isOverReliable(), isEncrypted());
	}

	/**
	 * Creates "Next hop" response for this message.
	 * 
	 * @param ownPeerInfo
	 * @param nextHop
	 * @return
	 */
	public Response createNextHopResponse(PeerInfo ownPeerInfo, PeerInfo nextHop) {
		NextHopResponse response = new NextHopResponse(protocolVersion, isByPeer(), isRecursive(),
				requestOrResponseType, ttl, transactionID, sourceID, ownPeerInfo.getPeerID().getPeerIDBytes(),
				isOverReliable(), isEncrypted(), ownPeerInfo, nextHop);
		response.setResponseID(ownPeerInfo.getPeerID().getPeerIDBytes());
		return response;
	}
}
