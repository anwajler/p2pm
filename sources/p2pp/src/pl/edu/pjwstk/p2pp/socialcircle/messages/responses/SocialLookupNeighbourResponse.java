package pl.edu.pjwstk.p2pp.socialcircle.messages.responses;

import pl.edu.pjwstk.p2pp.messages.responses.Response;
import java.util.Vector;

import pl.edu.pjwstk.p2pp.objects.AddressInfo;
import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.P2POptions;
import pl.edu.pjwstk.p2pp.objects.PeerID;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;
import pl.edu.pjwstk.p2pp.util.ByteUtils;


public class SocialLookupNeighbourResponse extends Response {

    /**
     * PeerInfo of response originator.
     */
    private PeerInfo peerInfo;
    private PeerInfo physicalNeighbour;
    @SuppressWarnings("unused")
	private P2POptions options;


    public SocialLookupNeighbourResponse() {
    }

   
    public SocialLookupNeighbourResponse(boolean[] protocolVersion, boolean[] messageType, boolean isAcknowledgment, boolean isSentByPeer, boolean isRecursive,
             boolean[] responseCode, byte ttl, byte[] transactionID, byte[] sourceID, byte[] responseID, boolean isOverReliable, boolean isEncrypted,
             PeerInfo ownPeerInfo, PeerInfo physicalNeighbour) {
        super(protocolVersion, messageType, isAcknowledgment, isSentByPeer, isRecursive, responseCode, LOOKUP_NEIGHBOUR_MESSAGE_TYPE, ttl, transactionID, sourceID,
                responseID, isOverReliable, isEncrypted);
        this.peerInfo = new PeerInfo(ownPeerInfo.getPeerID(), ownPeerInfo.getUptime(), ownPeerInfo.getAddressInfos(), ownPeerInfo.getUnhashedID());
        this.physicalNeighbour = new PeerInfo(physicalNeighbour.getPeerID(), physicalNeighbour.getUptime(), physicalNeighbour.getAddressInfos(),
                physicalNeighbour.getUnhashedID());
    }

    public SocialLookupNeighbourResponse(boolean[] protocolVersion, boolean[] messageType, boolean isAcknowledgment, boolean isSentByPeer, boolean isRecursive,
            boolean[] responseCode, byte ttl, byte[] transactionID, boolean isOverReliable, boolean isEncrypted, PeerInfo peerInfo, PeerInfo physicalNeighbour,
            P2POptions options) {
        super(protocolVersion, messageType, isAcknowledgment, isSentByPeer, isRecursive, responseCode, LOOKUP_NEIGHBOUR_MESSAGE_TYPE, ttl, transactionID,
                new byte[4], new byte[]{0, 0, 0, 1}, isOverReliable, isEncrypted);

        this.peerInfo = new PeerInfo(peerInfo.getPeerID(), peerInfo.getUptime(), peerInfo.getAddressInfos(), peerInfo.getUnhashedID());
        this.physicalNeighbour = new PeerInfo(physicalNeighbour.getPeerID(), physicalNeighbour.getUptime(), physicalNeighbour.getAddressInfos(),
                physicalNeighbour.getUnhashedID());
        this.options = options;
    }

    @Override
    public void addObject(GeneralObject object) throws UnsupportedGeneralObjectException {
        if (object instanceof PeerInfo) {
            if (this.peerInfo == null) {
                this.peerInfo = (PeerInfo) object;
            } else if (this.physicalNeighbour == null) {
                this.physicalNeighbour = (PeerInfo) object;
            } else {
                throw new UnsupportedGeneralObjectException("SocialLookupNeighbourResponse can't handle more objects.");
            }
        } else {
            throw new UnsupportedGeneralObjectException("SocialLookupNeighbourResponse can't handle " + object.getClass().getName() + " objects.");
        }
    }

    @Override
    public byte[] asBytes() {
        return asBytes(getBitsCount());
    }

    @Override
    protected byte[] asBytes( int bitsCount) {
        byte[] bytes = super.asBytes(bitsCount);

        int currentIndex = super.getBitsCount();

        ByteUtils.addByteArrayToArrayAtByteIndex(this.peerInfo.asBytes(), bytes, currentIndex / 8);
        currentIndex += this.peerInfo.getBitsCount();

        ByteUtils.addByteArrayToArrayAtByteIndex(this.physicalNeighbour.asBytes(), bytes, currentIndex / 8);

        return bytes;
    }

    @Override
    public int getBitsCount() {
        return super.getBitsCount() + this.peerInfo.getBitsCount() + this.physicalNeighbour.getBitsCount();
    }

    /**
     * Returns PeerInfo object of response originator.
     *
     * @return
     */
    @Override
    public PeerInfo getPeerInfo() {
        return this.peerInfo;
    }

    public PeerInfo getPhysicalNeighbour() {
        return this.physicalNeighbour;
    }

    @Override
    public boolean verify() {
        boolean result = true;

        if (this.peerInfo == null || this.physicalNeighbour == null) {
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
            
            // not checked if PeerID or UnhashedID isn't here
            if (result) {
                // has to contain at least one AddressInfo
                Vector<AddressInfo> addressInfos = this.peerInfo.getAddressInfos();
                if (addressInfos == null) {
                    result = false;
                } else if (addressInfos.isEmpty()) {
                    result = false;
                }
            }

            /*if (result && physicalNeighbour != null) {
                // has to contain at least one AddressInfo
                Vector<AddressInfo> addressInfos = this.physicalNeighbour.getAddressInfos();
                if (addressInfos == null) {
                    result = false;
                } else if (addressInfos.size() <= 0) {
                    result = false;
                }
            }*/
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("SocialLookupNeighbourResponse=[message=[");
        builder.append(super.toString());
        builder.append("header=[");
        builder.append(getHeader());
        builder.append("], peerInfo=[");
        if (this.peerInfo != null) {
            builder.append(this.peerInfo.toString());
        } else {
            builder.append(this.peerInfo);
        }

        builder.append("], physicalNeighbour=[");
        if (this.physicalNeighbour != null) {
            builder.append(this.physicalNeighbour.toString());
            builder.append("]]");
        } else {
            builder.append(this.physicalNeighbour);
            builder.append("]]");
        }

        return builder.toString();
    }
}
