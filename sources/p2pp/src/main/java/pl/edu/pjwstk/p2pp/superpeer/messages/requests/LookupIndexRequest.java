package pl.edu.pjwstk.p2pp.superpeer.messages.requests;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.requests.Request;
import pl.edu.pjwstk.p2pp.objects.*;
import pl.edu.pjwstk.p2pp.superpeer.messages.responses.LookupIndexResponse;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

import java.util.Vector;

public class LookupIndexRequest extends Request {

    private static Logger logger = Logger.getLogger(LookupIndexRequest.class);

	/**
	 * Request options object included in this message. Optional.
	 */
	private RequestOptions requestOptions;

	/**
	 * PeerInfo object included in this message.
	 */
	private PeerInfo peerInfo;

	/**
	 * RLookup (Resource lookup object included in this message).
	 */
	private RLookup resourceLookup;

	/**
	 * Default constructor. Used for creating empty messages to be filled later.
	 */
	public LookupIndexRequest() {
	}

	/**
	 * Creates lookup object request.
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
	 * @param options
	 *            May be null.
	 * @param peerInfo
	 *            Can't be null.
	 * @param resourceLookup
	 *            Can't be null.
	 */
	public LookupIndexRequest(boolean[] protocolVersion, boolean isAcknowledgment, boolean isSentByPeer,
			boolean isRecursive, byte ttl, byte[] transactionID, byte[] sourceID, boolean isOverReliable,
			boolean isEncrypted, RequestOptions options, PeerInfo peerInfo, RLookup resourceLookup) {
		super(protocolVersion, isAcknowledgment, isSentByPeer, isRecursive, Request.LOOKUP_INDEX_MESSAGE_TYPE, ttl,
				transactionID, sourceID, isOverReliable, isEncrypted);
		this.requestOptions = options;
		this.peerInfo = peerInfo;
		this.resourceLookup = resourceLookup;
	}

	@Override
	public RequestOptions getRequestOptions() {
		return requestOptions;
	}

	@Override
	public void addObject(GeneralObject object) throws UnsupportedGeneralObjectException {
		if (object instanceof RequestOptions) {
			requestOptions = (RequestOptions) object;
		} else if (object instanceof PeerInfo) {
			peerInfo = (PeerInfo) object;
		} else if (object instanceof RLookup) {
			resourceLookup = (RLookup) object;
		} else {
			throw new UnsupportedGeneralObjectException("LookupIndex can't contain object of " + object.getClass().getName() + " type.");
		}

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
		ByteUtils.addByteArrayToArrayAtByteIndex(resourceLookup.asBytes(), bytes, index / 8);
		// index += resourceLookup.getBitsCount();

		return bytes;
	}

	@Override
	public int getBitsCount() {
		int additionalBits = 0;
		if (requestOptions != null) {
			additionalBits += requestOptions.getBitsCount();
		}
		return super.getBitsCount() + additionalBits + peerInfo.getBitsCount() + resourceLookup.getBitsCount();
	}

	/**
	 * Returns RLookup object included in this message.
	 *
	 * @return
	 */
	public RLookup getResourceLookup() {
		return resourceLookup;
	}

	public void setResourceLookup(RLookup resourceLookup) {
		this.resourceLookup = resourceLookup;
	}

	/**
	 * Returns PeerInfo object being a part of this message.
	 *
	 * @return
	 */
        @Override
	public PeerInfo getPeerInfo() {
		return peerInfo;
	}

	@Override
	public boolean verify() {
		boolean result = true;

		if (peerInfo == null || resourceLookup == null) {
			result = false;
		} else {
			// must contain peerID
			PeerID peerID = peerInfo.getPeerID();
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
				UnhashedID unhashedID = peerInfo.getUnhashedID();
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
				Vector<AddressInfo> addressInfos = peerInfo.getAddressInfos();
				if (addressInfos == null) {
					result = false;
				} else if (addressInfos.size() <= 0) {
					result = false;
				}
			}
			// not checked if PeerID, UnhashedID or address infos isn't here
			if (result) {
				ResourceID resourceID = resourceLookup.getResourceID();
				if (resourceID == null) {
					result = false;
				} else {
					if (resourceID.getResourceID() == null) {
						result = false;
					}
				}
			}
		}

		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("LookupIndexRequest=[message=[" + super.toString() + "], header=["
				+ getHeader() + "], peerInfo=[");
		if (peerInfo != null) {
			builder.append(peerInfo.toString());
		} else {
			builder.append(peerInfo);
		}
		builder.append("], requestOptions=[");
		if (requestOptions != null) {
			builder.append(requestOptions.toString());
		} else {
			builder.append(requestOptions);
		}
		builder.append("], resourceLookup=[");
		if (resourceLookup != null) {
			builder.append(resourceLookup.toString());
		} else {
			builder.append(resourceLookup);
		}
		return builder.toString() + "]]";
	}

	/**
	 * Creates response for this request.
	 *
	 * @param responseCode
	 *            Response code as boolean array. There are constants for this in {@link pl.edu.pjwstk.p2pp.messages.responses.Response} class.0
	 * @param ownPeerInfo PeerInfo of a response-originator
	 *            Resource object to be part of response. May be null if response has to be without resource object (for
	 *            instance when response code is NOT FOUND) or if returned object will be filled later.
	 * @return response
	 */
	public LookupIndexResponse createResponse(boolean[] responseCode, PeerInfo ownPeerInfo, PeerInfo resourceKeeperPpeerInfo) {

        if (logger.isTraceEnabled()) {
            logger.trace("Creating response for LookupIndexRequest[transactionID=" + ByteUtils.byteArrayToHexString(transactionID) + "]");
        }

		LookupIndexResponse response = new LookupIndexResponse(P2PPMessage.P2PP_PROTOCOL_VERSION_1,
				P2PPMessage.RESPONSE_ACK_MESSAGE_TYPE, false, true, false, responseCode, (byte) 255, null, sourceID,
                ownPeerInfo.getPeerID().getPeerIDBytes(), overReliable, encrypted, ownPeerInfo, resourceKeeperPpeerInfo);
		response.setResponseID(ownPeerInfo.getPeerID().getPeerIDBytes());
		response.setTransactionID(transactionID);
		response.setReceiverPort(senderPort);
		response.setReceiverAddress(senderAddress);

		return response;
	}
}