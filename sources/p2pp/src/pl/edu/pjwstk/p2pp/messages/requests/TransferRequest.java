package pl.edu.pjwstk.p2pp.messages.requests;

import java.util.ArrayList;
import java.util.List;

import pl.edu.pjwstk.p2pp.P2PPManager;
import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.responses.Response;
import pl.edu.pjwstk.p2pp.messages.responses.TransferResponse;
import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.RequestOptions;
import pl.edu.pjwstk.p2pp.objects.ResourceObject;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * Transfer request as defined in P2PP specification (draft 01).
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class TransferRequest extends Request {

	/**
	 * PeerInfo object describing a peer that transfers a resource object.
	 */
	private PeerInfo transferringPeerInfo;

	/**
	 * Vector of resource objects contained in this message.
	 */
	private List<ResourceObject> resourceObjects = new ArrayList<ResourceObject>();

	/**
	 * Creates empty TransferRequest. Useful when filling a message with data received from socket.
	 */
	public TransferRequest() {

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
	 * @param peerInfo
	 * @param resourceObject
	 */
	public TransferRequest(boolean[] protocolVersion, boolean isAcknowledgment, boolean isSentByPeer,
			boolean isRecursive, byte ttl, byte[] transactionID, byte[] sourceID, boolean isOverReliable,
			boolean isEncrypted, PeerInfo peerInfo, ResourceObject resourceObject) {
		super(protocolVersion, isAcknowledgment, isSentByPeer, isRecursive, P2PPMessage.TRANSFER_MESSAGE_TYPE, ttl,
				transactionID, sourceID, isOverReliable, isEncrypted);

		this.transferringPeerInfo = peerInfo;
		this.resourceObjects.add(resourceObject);
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
	 * @param peerInfo
	 * @param resourceObjects
	 */
	public TransferRequest(boolean[] protocolVersion, boolean isAcknowledgment, boolean isSentByPeer,
			boolean isRecursive, byte ttl, byte[] transactionID, byte[] sourceID, boolean isOverReliable,
			boolean isEncrypted, PeerInfo peerInfo, List<ResourceObject> resourceObjects) {
		super(protocolVersion, isAcknowledgment, isSentByPeer, isRecursive, P2PPMessage.TRANSFER_MESSAGE_TYPE, ttl,
				transactionID, sourceID, isOverReliable, isEncrypted);

		this.transferringPeerInfo = peerInfo;
		this.resourceObjects = resourceObjects;
	}

	@Override
	public RequestOptions getRequestOptions() {
		return null;
	}

	@Override
	public void addObject(GeneralObject object) throws UnsupportedGeneralObjectException {
		if (object instanceof PeerInfo) {
			transferringPeerInfo = (PeerInfo) object;
		} else if (object instanceof ResourceObject) {
			resourceObjects.add((ResourceObject) object);
		} else {
			throw new UnsupportedGeneralObjectException("TransferRequest can't contain an object of "
					+ object.getClass().getName() + " type.");
		}
	}

	/**
	 * Returns a list of ResourceObjects being part of this message.
	 * 
	 * @return
	 */
	public List<ResourceObject> getResourceObjects() {
		return resourceObjects;
	}

	public void setResourceObjects(List<ResourceObject> resourceObjects) {
		this.resourceObjects = resourceObjects;
	}

	@Override
	public PeerInfo getPeerInfo() {
		return transferringPeerInfo;
	}

	@Override
	public byte[] asBytes() {
		return asBytes(getBitsCount());
	}

	@Override
	public int getBitsCount() {
		int additionalBits = 0;
		if (resourceObjects != null) {
			for (ResourceObject currentResource : resourceObjects) {
				additionalBits += currentResource.getBitsCount();
			}
		}
		return super.getBitsCount() + additionalBits + transferringPeerInfo.getBitsCount();
	}

	@Override
	protected byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		int currentIndex = super.getBitsCount();
		ByteUtils.addByteArrayToArrayAtBitIndex(transferringPeerInfo.asBytes(), bytes, currentIndex);
		currentIndex += transferringPeerInfo.getBitsCount();

		for (ResourceObject currentResource : resourceObjects) {
			ByteUtils.addByteArrayToArrayAtBitIndex(currentResource.asBytes(), bytes, currentIndex);
			currentIndex += currentResource.getBitsCount();
		}

		return bytes;
	}

	@Override
	public boolean verify() {
		if (transferringPeerInfo == null) {
			return false;
		} else {
			if (resourceObjects == null) {
				return false;
			} else {
				if (resourceObjects.size() <= 0) {
					return false;
				} else {
					// TODO probably some checking if ResourceObject is properly formed
					return true;
				}
			}
		}
	}

	/**
	 * Creates TransferResponse for this request.
	 * 
	 * @param responseCode
	 *            Response code. There are constants for this in {@link Response} class.
	 * @param responsePeerInfo
	 *            PeerInfo of a peer that created this message.
	 * @return
	 */
	public TransferResponse createResponse(boolean[] responseCode, byte[] responseID, PeerInfo responsePeerInfo) {
		TransferResponse response = new TransferResponse(P2PPManager.CURRENT_PROTOCOL_VERSION, false, isByPeer(),
				isRecursive(), responseCode, ttl, transactionID, sourceID, responseID, isOverReliable(), isEncrypted(),
				responsePeerInfo);
		return response;
	}
}
