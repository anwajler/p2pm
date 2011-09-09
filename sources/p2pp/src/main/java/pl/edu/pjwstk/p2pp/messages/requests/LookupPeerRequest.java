package pl.edu.pjwstk.p2pp.messages.requests;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.responses.LookupPeerResponse;
import pl.edu.pjwstk.p2pp.objects.*;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

public class LookupPeerRequest extends Request {

    private static Logger logger = Logger.getLogger(LookupPeerRequest.class);

    /**
     * Request options object included in this message. Optional.
     */
    private RequestOptions requestOptions;

    /**
     * PeerInfo object included in this message.
     */
    private PeerInfo peerInfo;

    /**
     * Default constructor. Used for creating empty messages to be filled later.
     */
    public LookupPeerRequest() {
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
     * @param options          May be null.
     * @param peerInfo         Can't be null.
     */
    public LookupPeerRequest(boolean[] protocolVersion, boolean isAcknowledgment, boolean isSentByPeer,
                             boolean isRecursive, byte ttl, byte[] transactionID, byte[] sourceID, boolean isOverReliable,
                             boolean isEncrypted, RequestOptions options, PeerInfo peerInfo) {
        super(protocolVersion, isAcknowledgment, isSentByPeer, isRecursive, Request.LOOKUP_PEER_MESSAGE_TYPE,
                ttl, transactionID, sourceID, isOverReliable, isEncrypted);
        this.requestOptions = options;
        this.peerInfo = peerInfo;
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
            if (this.peerInfo == null) {
                this.peerInfo = (PeerInfo) object;
            } else {
                throw new UnsupportedGeneralObjectException("LookupPeerRequest can't contain more than one PeerInfo objects");
            }
        } else {
            throw new UnsupportedGeneralObjectException("LookupPeerRequest can't contain object of " + object.getClass().getName() + " type.");
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

        return bytes;
    }

    @Override
    public int getBitsCount() {
        int additionalBits = 0;
        if (this.requestOptions != null) {
            additionalBits += this.requestOptions.getBitsCount();
        }
        return super.getBitsCount() + additionalBits + this.peerInfo.getBitsCount();
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
        return this.peerInfo != null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("LookupPeerRequest=[message=[" + super.toString() + "], header=[" + getHeader() + "], peerInfo=[");
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
        return builder.toString() + "]]";
    }

    /**
     * Creates response for this request.
     *
     * @param responseCode Response code as boolean array. There are constants for this in {@link pl.edu.pjwstk.p2pp.messages.responses.Response} class.0
     * @param ownPeerInfo  PeerInfo of a response-originator
     *                     Resource object to be part of response. May be null if response has to be without resource object (for
     *                     instance when response code is NOT FOUND) or if returned object will be filled later.
     * @return response
     */
    public LookupPeerResponse createResponse(boolean[] responseCode, PeerInfo ownPeerInfo) {

        if (logger.isTraceEnabled()) {
            logger.trace("Creating response for LookupPeerRequest[transactionID=" + ByteUtils.byteArrayToHexString(transactionID) + "]");
        }

        LookupPeerResponse response = new LookupPeerResponse(P2PPMessage.P2PP_PROTOCOL_VERSION_1, P2PPMessage.RESPONSE_MESSAGE_TYPE, false, byPeer,
                recursive, responseCode, ttl, transactionID, sourceID, ownPeerInfo.getPeerID().getPeerIDBytes(), overReliable, encrypted);
        response.setResponseID(ownPeerInfo.getPeerID().getPeerIDBytes());
        response.setTransactionID(transactionID);
        response.setReceiverPort(senderPort);
        response.setReceiverAddress(senderAddress);

		return response;
	}
}