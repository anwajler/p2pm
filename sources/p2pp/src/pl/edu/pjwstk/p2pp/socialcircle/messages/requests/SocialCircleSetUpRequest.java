package pl.edu.pjwstk.p2pp.socialcircle.messages.requests;

import java.util.List;

import pl.edu.pjwstk.p2pp.messages.requests.Request;

import java.util.Vector;

import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.responses.Response;
import pl.edu.pjwstk.p2pp.objects.AddressInfo;
import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.PeerID;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.RequestOptions;
import pl.edu.pjwstk.p2pp.objects.UnhashedID;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;
import pl.edu.pjwstk.p2pp.socialcircle.messages.responses.SocialCircleSetUpResponse;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * @author Konrad Adamczyk conrad.adamczyk@gmail.com
 */
public class SocialCircleSetUpRequest extends Request {

    private PeerInfo originatorPeerInfo;

    /**
     * Empty constructor used for creating this object but filling it with data later.
     */
    public SocialCircleSetUpRequest() {
    }


    public SocialCircleSetUpRequest(boolean[] protocolVersion, boolean isAcknowledgment, boolean isSentByPeer,
                                    boolean isRecursive, byte ttl, byte[] transactionID, byte[] sourceID, boolean isOverReliable,
                                    boolean isEncrypted, PeerInfo peerInfo) {
        super(protocolVersion, isAcknowledgment, isSentByPeer, isRecursive, SET_UP_MESSAGE_TYPE, ttl,
                transactionID, sourceID, isOverReliable, isEncrypted);
        this.originatorPeerInfo = new PeerInfo(peerInfo.getPeerID(), peerInfo.getUptime(), peerInfo.getAddressInfos(), peerInfo.getUnhashedID());
    }

    @Override
    public byte[] asBytes() {
        return asBytes(getBitsCount());
    }

    @Override
    protected byte[] asBytes(int bitsCount) {
        byte[] bytes = super.asBytes(bitsCount);

        int currentIndex = super.getBitsCount();

        ByteUtils.addByteArrayToArrayAtByteIndex(this.originatorPeerInfo.asBytes(), bytes, currentIndex / 8);

        return bytes;
    }

    @Override
    public int getBitsCount() {
        return super.getBitsCount() + this.originatorPeerInfo.getBitsCount();
    }

    @Override
    public RequestOptions getRequestOptions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addObject(GeneralObject object) throws UnsupportedGeneralObjectException {
        if (object instanceof PeerInfo) {
            if (this.originatorPeerInfo == null) {
                this.originatorPeerInfo = (PeerInfo) object;
            } else {
                throw new UnsupportedGeneralObjectException("SocialLookupNeighbourRequest can't handle more objects.");
            }
        } else {
            throw new UnsupportedGeneralObjectException("SocialLookupNeighbourRequest can't handle " + object.getClass().getName() + " objects.");
        }
    }

    public SocialCircleSetUpResponse createResponse(PeerInfo ownPeerInfo, List<PeerInfo> vset) {

        SocialCircleSetUpResponse response = new SocialCircleSetUpResponse(P2PPMessage.P2PP_PROTOCOL_VERSION_1,
                P2PPMessage.RESPONSE_ACK_MESSAGE_TYPE, false, true, false, Response.RESPONSE_CODE_OK_BITS_ARRAY,
                (byte) 255, null, sourceID, ownPeerInfo.getPeerID().getPeerIDBytes(), overReliable, encrypted,
                ownPeerInfo, vset);
        response.setResponseID(ownPeerInfo.getPeerID().getPeerIDBytes());
        response.setTransactionID(transactionID);
        response.setReceiverPort(senderPort);
        response.setReceiverAddress(senderAddress);

        return response;
    }

    /**
     * Returns PeerInfo included in this request.
     *
     * @return originatorPeerInfo
     */
    @Override
    public PeerInfo getPeerInfo() {
        return originatorPeerInfo;
    }

    @Override
    public boolean verify() {
        boolean result = true;

        // has to contain sourceID being four bytes long
        //if (sourceID == null || sourceID.length != 4) {
        if (sourceID == null) {
            //    System.out.println("::0:"+sourceID+":"+sourceID.length);
            result = false;
        } // has to contain PeerInfo object
        else if (originatorPeerInfo == null) {
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
        StringBuilder builder = new StringBuilder("SocialCircleSetUpRequest=[message=[");
        builder.append(super.toString());
        builder.append("], header=[");
        builder.append(getHeader());
        builder.append("], request originator=[");
        if (originatorPeerInfo != null) {
            builder.append(originatorPeerInfo.toString());
            builder.append("]]");
        } else {
            builder.append("null]]");
        }

        return builder.toString();
    }
}
