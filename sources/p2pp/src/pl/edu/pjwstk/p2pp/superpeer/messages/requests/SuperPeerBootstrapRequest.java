package pl.edu.pjwstk.p2pp.superpeer.messages.requests;

import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.requests.Request;
import pl.edu.pjwstk.p2pp.objects.*;
import pl.edu.pjwstk.p2pp.superpeer.messages.responses.SuperPeerBootstrapResponse;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

import java.util.Collection;

public class SuperPeerBootstrapRequest extends Request {

    private PeerInfo originatorPeerInfo;


    public SuperPeerBootstrapRequest() {}

    public SuperPeerBootstrapRequest(boolean[] protocolVersion, boolean isAcknowledgment, boolean isSentByPeer, boolean isRecursive, byte ttl,
                                     byte[] transactionID, boolean isOverReliable, boolean isEncrypted, PeerInfo peerInfo) {
        super(protocolVersion, isAcknowledgment, isSentByPeer, isRecursive, P2PPMessage.BOOTSTRAP_MESSAGE_TYPE, ttl,
                transactionID, new byte[4], isOverReliable, isEncrypted);
        this.originatorPeerInfo = peerInfo;
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
        return null;
    }

    @Override
    public void addObject(GeneralObject object) throws UnsupportedGeneralObjectException {
        if (object instanceof PeerInfo) {
            if (this.originatorPeerInfo == null) {
                this.originatorPeerInfo = (PeerInfo) object;
            } else {
                throw new UnsupportedGeneralObjectException("BootstrapRequest can't handle more objects.");
            }
        } else {
            throw new UnsupportedGeneralObjectException("BootstrapRequest can't handle " + object.getClass().getName() + " objects.");
        }
    }

    public SuperPeerBootstrapResponse createResponse(boolean[] responseCode, PeerInfo bootstrapPeerInfo, P2POptions options, byte[] peerIDValue) {
        PeerInfo bootstrapPeerInfoCopy = new PeerInfo(bootstrapPeerInfo.getPeerID(), bootstrapPeerInfo.getUptime(), bootstrapPeerInfo.getAddressInfos(),
                bootstrapPeerInfo.getUnhashedID());
        PeerInfo originatorPeerInfoCopy = new PeerInfo(new PeerID(peerIDValue), null, originatorPeerInfo.getAddressInfos(), originatorPeerInfo.getUnhashedID());
        SuperPeerBootstrapResponse response = new SuperPeerBootstrapResponse(protocolVersion, P2PPMessage.RESPONSE_ACK_MESSAGE_TYPE,
                false, byPeer, recursive, responseCode, ttl, transactionID, overReliable, encrypted, bootstrapPeerInfoCopy, originatorPeerInfoCopy, options);
        this.originatorPeerInfo.getPeerID().setPeerIDBytes(peerIDValue);
        response.setReceiverPort(senderPort);
        response.setReceiverAddress(senderAddress);
        return response;
    }

    @Override
    public PeerInfo getPeerInfo() {
        return this.originatorPeerInfo;
    }

    @Override
    public boolean verify() {
        boolean result = true;

        if (sourceID == null || sourceID.length != 4) {
            result = false;
        }
        else if (this.originatorPeerInfo == null) {
            result = false;
        } else {
            PeerID peerID = this.originatorPeerInfo.getPeerID();
            if (peerID == null) {
                result = false;
            } else {
                if (peerID.getPeerIDBytes() == null) {
                    result = false;
                }
            }

            if (result) {
                UnhashedID unhashedID = this.originatorPeerInfo.getUnhashedID();
                if (unhashedID == null) {
                    result = false;
                } else {
                    if (unhashedID.getUnhashedIDValue() == null) {
                        result = false;
                    }
                }
            }

            if (result) {
                Collection<AddressInfo> addressInfos = this.originatorPeerInfo.getAddressInfos();
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
        StringBuilder builder = new StringBuilder("BootstrapRequest=[message=[").append(super.toString()).append("], header=[").append(getHeader()).
                append("], request originator=[");
        if (this.originatorPeerInfo != null) {
            builder.append(this.originatorPeerInfo.toString());
            builder.append("]]");
        } else {
            builder.append(this.originatorPeerInfo);
            builder.append("]]");
        }
        return builder.toString();
    }
}