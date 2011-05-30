package pl.edu.pjwstk.p2pp.messages.requests;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.responses.RemoveObjectResponse;
import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.RequestOptions;
import pl.edu.pjwstk.p2pp.objects.ResourceObject;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * TODO RemoveObject request as defined in P2PP specification (draft 01).
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class RemoveObjectRequest extends Request {

    private static Logger logger = Logger.getLogger(RemoveObjectRequest.class);

	private RequestOptions requestOptions;

	private PeerInfo peerInfo;

	/**
	 * Object to be removed.
	 */
	private ResourceObject resourceObject;

	/**
	 * Creates empty request that has to be filled later.
	 */
	public RemoveObjectRequest() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * @param protocolVersion
	 * @param isAcknowledgment
	 * @param isSentByPeer
	 * @param isRecursive
	 * @param ttl
	 * @param transactionID
	 * @param sourceID
	 * @param isOverReliable
	 * @param isEncrypted
	 * @param requestOptions
	 * @param peerInfo
	 * @param resourceObject
	 */
	public RemoveObjectRequest(boolean[] protocolVersion, boolean isAcknowledgment, boolean isSentByPeer, boolean isRecursive, byte ttl,
                               byte[] transactionID, byte[] sourceID, boolean isOverReliable, boolean isEncrypted, RequestOptions requestOptions,
                               PeerInfo peerInfo, ResourceObject resourceObject) {
		super(protocolVersion, isAcknowledgment, isSentByPeer, isRecursive, P2PPMessage.REMOVE_OBJECT_MESSAGE_TYPE, ttl, transactionID, sourceID,
				isOverReliable, isEncrypted);
		this.requestOptions = requestOptions;
		this.peerInfo = peerInfo;
		this.resourceObject = resourceObject;
	}

	@Override
	public RequestOptions getRequestOptions() {
		return requestOptions;
	}

	@Override
	public void addObject(GeneralObject object) throws UnsupportedGeneralObjectException {
		if (object instanceof PeerInfo) {
			peerInfo = (PeerInfo) object;
		} else if (object instanceof ResourceObject) {
			resourceObject = (ResourceObject) object;
		} else if (object instanceof RequestOptions) {
			requestOptions = (RequestOptions) object;
		} else {
			throw new UnsupportedGeneralObjectException("RemoveObjectRequest can't contain "
					+ object.getClass().getName() + " as subobject.");
		}
	}

	@Override
	public PeerInfo getPeerInfo() {
		return peerInfo;
	}

	@Override
	public byte[] asBytes() {
		return asBytes(getBitsCount());
	}

	@Override
	protected byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		int index = super.getBitsCount();

		if (requestOptions != null) {
			ByteUtils.addByteArrayToArrayAtByteIndex(requestOptions.asBytes(), bytes, index / 8);
			index += requestOptions.getBitsCount();
		}
		ByteUtils.addByteArrayToArrayAtByteIndex(peerInfo.asBytes(), bytes, index / 8);
		index += peerInfo.getBitsCount();
		ByteUtils.addByteArrayToArrayAtByteIndex(resourceObject.asBytes(), bytes, index / 8);
		// index += resourceLookup.getBitsCount();

		return bytes;
	}

	@Override
	public boolean verify() {
		if (peerInfo == null) {
			return false;
		} else {
			// TODO more verification
		}
		if (resourceObject == null) {
			return false;
		} else {
			// TODO more verification
		}
		return true;
	}

	@Override
	public int getBitsCount() {
		int additionalBits = 0;
		if (requestOptions != null) {
			additionalBits += requestOptions.getBitsCount();
		}
		return super.getBitsCount() + peerInfo.getBitsCount() + resourceObject.getBitsCount() + additionalBits;
	}

	/**
	 * Returns resource object that is part of this message. That's the resource object that has to be removed.
	 * 
	 * @return
	 */
	public ResourceObject getResourceObject() {
		return resourceObject;
	}

	/**
	 * Creates response for this request.
	 *
	 * @param messageType
	 * @param responseCode
	 * @param ownPeerInfo
	 * @param ttl
	 * @param transactionID
	 * @param isOverReliable
	 * @param isEncrypted
	 * @return
	 */
	public RemoveObjectResponse createResponse(boolean[] messageType, boolean[] responseCode, PeerInfo ownPeerInfo,
			byte ttl, byte[] transactionID, boolean isOverReliable, boolean isEncrypted) {
		return new RemoveObjectResponse(protocolVersion, messageType, false, byPeer, recursive, responseCode, ttl, transactionID, sourceID,
                ownPeerInfo.getPeerID().getPeerIDBytes(), isOverReliable, isEncrypted, ownPeerInfo);
	}

    public RemoveObjectResponse createResponse(boolean[] responseCode, PeerInfo ownPeerInfo) {

        if (logger.isTraceEnabled()) {
            logger.trace("Creating response for RemoveObjectRequest[transactionID=" + ByteUtils.byteArrayToHexString(transactionID) + "]");
        }

        RemoveObjectResponse response = new RemoveObjectResponse(P2PPMessage.P2PP_PROTOCOL_VERSION_1, P2PPMessage.RESPONSE_ACK_MESSAGE_TYPE, false,
                true, false, responseCode, (byte) 255, null, sourceID, ownPeerInfo.getPeerID().getPeerIDBytes(), overReliable, encrypted, ownPeerInfo);
        response.setResponseID(ownPeerInfo.getPeerID().getPeerIDBytes());
        response.setTransactionID(transactionID);
        response.setReceiverPort(senderPort);
		response.setReceiverAddress(senderAddress);

		return response;
	}
}
