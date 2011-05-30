package pl.edu.pjwstk.p2pp.socialcircle.messages.requests;

import pl.edu.pjwstk.p2pp.messages.requests.Request;
import java.util.Vector;

import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.responses.Response;
import pl.edu.pjwstk.p2pp.objects.AddressInfo;
import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.P2POptions;
import pl.edu.pjwstk.p2pp.objects.PeerID;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.RequestOptions;
import pl.edu.pjwstk.p2pp.objects.UnhashedID;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;
import pl.edu.pjwstk.p2pp.socialcircle.messages.responses.SocialLookupNeighbourResponse;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * Class wrapping up data of socialcircle bootstrap request
 * 
 * @author Konrad Adamczyk conrad.adamczyk@gmail.com
 * 
 */
public class SocialLookupNeighbourRequest extends Request {

    private PeerInfo originatorPeerInfo;
    private PeerInfo physicalNeighbour;

    /**
     * Empty constructor used for creating this object but filling it with data later.
     */
    public SocialLookupNeighbourRequest() {
    }

    /**
     * Constructor for Bootstrap request. Source ID is not given as argument because it's constant and uses only 4 bytes
     * and each of them is 0.
     *
     * @param protocolVersion
     * @param isAcknowledgment
     * @param isSentByPeer
     * @param isRecursive
     * @param ttl
     * @param transactionID
     * @param isOverReliable
     * @param isEncrypted
     * @param peerInfo
     */
    public SocialLookupNeighbourRequest(boolean[] protocolVersion, boolean isAcknowledgment,  boolean isSentByPeer,
             boolean isRecursive,  byte ttl, byte[] transactionID,  boolean isOverReliable,
             boolean isEncrypted,  PeerInfo peerInfo,  PeerInfo physicalNeighbour) {
        super(protocolVersion, isAcknowledgment, isSentByPeer, isRecursive, P2PPMessage.LOOKUP_NEIGHBOUR_MESSAGE_TYPE, ttl,
                transactionID, new byte[4], isOverReliable, isEncrypted);
        this.originatorPeerInfo = new PeerInfo(peerInfo.getPeerID(), peerInfo.getUptime(), peerInfo.getAddressInfos(), peerInfo.getUnhashedID());
        this.physicalNeighbour = new PeerInfo(physicalNeighbour.getPeerID(), physicalNeighbour.getUptime(), physicalNeighbour.getAddressInfos(),
                physicalNeighbour.getUnhashedID());
    }

    @Override
    public byte[] asBytes() {
        return asBytes(getBitsCount());
    }

    @Override
    protected byte[] asBytes( int bitsCount) {
        byte[] bytes = super.asBytes(bitsCount);

        int currentIndex = super.getBitsCount();

        ByteUtils.addByteArrayToArrayAtByteIndex(this.originatorPeerInfo.asBytes(), bytes, currentIndex / 8);
        currentIndex += this.originatorPeerInfo.getBitsCount();

        ByteUtils.addByteArrayToArrayAtByteIndex(this.physicalNeighbour.asBytes(), bytes, currentIndex / 8);

        return bytes;
    }

    @Override
    public int getBitsCount() {
        return super.getBitsCount() + this.originatorPeerInfo.getBitsCount() + this.physicalNeighbour.getBitsCount();
    }

    @Override
    public RequestOptions getRequestOptions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addObject( GeneralObject object) throws UnsupportedGeneralObjectException {
        if (object instanceof PeerInfo) {
            if (this.originatorPeerInfo == null) {
                this.originatorPeerInfo = (PeerInfo) object;
            } else if (this.physicalNeighbour == null) {
                this.physicalNeighbour = (PeerInfo) object;
            } else {
                throw new UnsupportedGeneralObjectException("SocialLookupNeighbourRequest can't handle more objects.");
            }
        } else {
            throw new UnsupportedGeneralObjectException("SocialLookupNeighbourRequest can't handle " + object.getClass().getName() + " objects.");
        }
    }

    /**
     * Creates response for this request.
     *
     * @param responseCode
     *            Response code as boolean array. There are constants for this in {@link Response} class.0
     * @param ownPeerInfo
     * @param resourcesList
     *            Resource object to be part of response. May be null if response has to be without resource object (for
     *            instance when response code is NOT FOUND) or if returned object will be filled later.
     * @return
     */
    public SocialLookupNeighbourResponse createResponse(boolean[] responseCode, P2POptions options, PeerInfo ownPeerInfo, PeerInfo physicalNeighbour) {

        SocialLookupNeighbourResponse response = new SocialLookupNeighbourResponse(protocolVersion, P2PPMessage.RESPONSE_ACK_MESSAGE_TYPE,
                false, byPeer, recursive, responseCode, ttl, transactionID, overReliable, encrypted,
                ownPeerInfo, physicalNeighbour, options);
        response.setResponseID(ownPeerInfo.getPeerID().getPeerIDBytes());
        response.setTransactionID(transactionID);
        response.setReceiverPort(senderPort);
        response.setReceiverAddress(senderAddress);

        return response;
    }

    /**
     * Returns PeerInfo included in this request.
     *
     * @return
     */
    @Override
    public PeerInfo getPeerInfo() {
        return originatorPeerInfo;
    }

    public PeerInfo getPhysicalNeighbour() {
        return this.physicalNeighbour;
    }

    @Override
    public boolean verify() {
        boolean result = true;

        // has to contain sourceID being four bytes long
        if (sourceID == null || sourceID.length != 4) {
            result = false;
        } // has to contain PeerInfo object
        else if (originatorPeerInfo == null || this.physicalNeighbour == null) {
            result = false;
        } else {
            // must contain peerID
            PeerID peerID = originatorPeerInfo.getPeerID();
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
                UnhashedID unhashedID = originatorPeerInfo.getUnhashedID();
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
                Vector<AddressInfo> addressInfos = originatorPeerInfo.getAddressInfos();
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
        StringBuilder builder = new StringBuilder("SocialLookupNeighbourRequest=[message=[");
        builder.append(super.toString());
        builder.append("], header=[");
        builder.append(getHeader());
        builder.append("], request originator=[");
        if (originatorPeerInfo != null) {
            builder.append(originatorPeerInfo.toString());
            builder.append("]]");
        } else {
            builder.append(originatorPeerInfo.toString());
            builder.append("]]");
        }

        builder.append("], physicalNeighbour=[");
        if (this.physicalNeighbour != null) {
            builder.append(this.physicalNeighbour.toString());
            builder.append("]]");
        } else {
            builder.append(this.physicalNeighbour.toString());
            builder.append("]]");
        }

        return builder.toString();
    }
}
