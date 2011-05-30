package pl.edu.pjwstk.p2pp.superpeer.messages.requests;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.requests.Request;
import pl.edu.pjwstk.p2pp.objects.*;
import pl.edu.pjwstk.p2pp.superpeer.messages.responses.LookupPeerIndexResponse;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

import java.util.Vector;

public class LookupPeerIndexRequest extends Request {

    private static Logger logger = Logger.getLogger(LookupPeerIndexRequest.class);

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
	private PeerID peerID;

	/**
	 * Default constructor. Used for creating empty messages to be filled later.
	 */
	public LookupPeerIndexRequest() {
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
	 * @param peerID
	 *            Can't be null.
	 */
	public LookupPeerIndexRequest(boolean[] protocolVersion, boolean isAcknowledgment, boolean isSentByPeer,
			boolean isRecursive, byte ttl, byte[] transactionID, byte[] sourceID, boolean isOverReliable,
			boolean isEncrypted, RequestOptions options, PeerInfo peerInfo, PeerID peerID) {
		super(protocolVersion, isAcknowledgment, isSentByPeer, isRecursive, Request.LOOKUP_PEER_INDEX_MESSAGE_TYPE, ttl,
				transactionID, sourceID, isOverReliable, isEncrypted);
		this.requestOptions = options;
		this.peerInfo = peerInfo;
		this.peerID = peerID;
	}

    @Override
	public RequestOptions getRequestOptions() {
		return requestOptions;
	}

	@Override
	public void addObject(GeneralObject object) throws UnsupportedGeneralObjectException {
		if (object instanceof RequestOptions) {
			this.requestOptions = (RequestOptions) object;
		} else if (object instanceof PeerInfo) {
			this.peerInfo = (PeerInfo) object;
		} else if (object instanceof PeerID) {
			this.peerID = (PeerID) object;
		} else {
			throw new UnsupportedGeneralObjectException("LookupPeerIndexRequest can't contain object of " + object.getClass().getName() + " type.");
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

		if (this.requestOptions != null) {
			ByteUtils.addByteArrayToArrayAtByteIndex(this.requestOptions.asBytes(), bytes, index / 8);
			index += this.requestOptions.getBitsCount();
		}
		ByteUtils.addByteArrayToArrayAtByteIndex(this.peerInfo.asBytes(), bytes, index / 8);
		index += this.peerInfo.getBitsCount();
		ByteUtils.addByteArrayToArrayAtByteIndex(this.peerID.asBytes(), bytes, index / 8);

		return bytes;
	}

	@Override
	public int getBitsCount() {
		int additionalBits = 0;
		if (this.requestOptions != null) {
			additionalBits += this.requestOptions.getBitsCount();
		}
		return super.getBitsCount() + additionalBits + this.peerInfo.getBitsCount() + this.peerID.getBitsCount();
	}

	/**
	 * Returns PeerID object included in this message.
	 *
	 * @return peerID
	 */
	public PeerID getPeerID() {
		return this.peerID;
	}

	public void setPeerID(PeerID peerID) {
		this.peerID = peerID;
	}

	/**
	 * Returns PeerInfo object being a part of this message.
	 *
	 * @return PeerInfo
	 */
        @Override
	public PeerInfo getPeerInfo() {
		return this.peerInfo;
	}

	@Override
	public boolean verify() {
		boolean result = true;

		if (this.peerInfo == null || this.peerID == null) {
			result = false;
		} else {
			// must contain peerID
			PeerID peerID = this.peerInfo.getPeerID();
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
				UnhashedID unhashedID = this.peerInfo.getUnhashedID();
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
				Vector<AddressInfo> addressInfos = this.peerInfo.getAddressInfos();
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
	public String toString() {
		StringBuilder builder = new StringBuilder("LookupPeerIndexRequest=[message=[" + super.toString() + "], header=["
				+ getHeader() + "], peerInfo=[");
		if (this.peerInfo != null) {
			builder.append(this.peerInfo.toString());
		} else {
			builder.append("null");
		}
		builder.append("], requestOptions=[");
		if (this.requestOptions != null) {
			builder.append(this.requestOptions.toString());
		} else {
			builder.append("null");
		}
		builder.append("], peerID=[");
		if (this.peerID != null) {
			builder.append(this.peerID.toString());
		} else {
			builder.append("null");
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
     * @param lookedUpPeerInfo PeerInfo
	 * @return response
	 */
	public LookupPeerIndexResponse createResponse(boolean[] responseCode, PeerInfo ownPeerInfo, PeerInfo lookedUpPeerInfo) {

        if (logger.isTraceEnabled()) {
            logger.trace("Creating response for LookupIndexRequest[transactionID=" + ByteUtils.byteArrayToHexString(transactionID) + "]");
        }

		LookupPeerIndexResponse response = new LookupPeerIndexResponse(P2PPMessage.P2PP_PROTOCOL_VERSION_1,
				P2PPMessage.RESPONSE_ACK_MESSAGE_TYPE, false, true, false, responseCode, (byte) 255, null, sourceID,
                ownPeerInfo.getPeerID().getPeerIDBytes(), overReliable, encrypted, ownPeerInfo, lookedUpPeerInfo);
		response.setResponseID(ownPeerInfo.getPeerID().getPeerIDBytes());
		response.setTransactionID(transactionID);
		response.setReceiverPort(senderPort);
		response.setReceiverAddress(senderAddress);

		return response;
	}
}