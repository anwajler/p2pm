package pl.edu.pjwstk.p2pp.messages.indications;

import java.util.ArrayList;
import java.util.List;

import pl.edu.pjwstk.p2pp.messages.Indication;
import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.ResourceObject;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

public class LeaveIndication extends Indication {

    private PeerInfo leavingPeerInfo;

    private List<ResourceObject> resources = new ArrayList<ResourceObject>();

    /**
     * @param protocolVersion
     * @param isSentByPeer
     * @param isRecursive
     * @param ttl
     * @param transactionID
     * @param sourceID
     * @param isOverReliable
     * @param isEncrypted
     * @param leavingPeerInfo
     * @param resources
     */
    public LeaveIndication(boolean[] protocolVersion, boolean isSentByPeer, boolean isRecursive, byte ttl,
                           byte[] transactionID, byte[] sourceID, boolean isOverReliable, boolean isEncrypted,
                           PeerInfo leavingPeerInfo, List<ResourceObject> resources) {

        super(protocolVersion, isSentByPeer, isRecursive, P2PPMessage.LEAVE_MESSAGE_TYPE, ttl, transactionID, sourceID,
                null, isOverReliable, isEncrypted);

        this.resources = (resources != null) ? resources : new ArrayList<ResourceObject>();
        this.leavingPeerInfo = leavingPeerInfo;
    }

    /**
     * Creates empty LeaveIndication.
     */
    public LeaveIndication() {
        super();
    }

    @Override
    public boolean verify() {
        /*if (resources.size() <= 0) {
              return false;
          } else {*/
        if (leavingPeerInfo == null) {
            return false;
        } else {
            // TODO add handling of no PeerID etc.
        }
        //}
        return true;
    }

    @Override
    public void addObject(GeneralObject object) throws UnsupportedGeneralObjectException {
        if (object instanceof ResourceObject) {
            resources.add((ResourceObject) object);
        } else if (object instanceof PeerInfo) {
            leavingPeerInfo = (PeerInfo) object;
        } else {
            throw new UnsupportedGeneralObjectException("LeaveIndication can't contain " + object.getClass().getName() + " object.");
        }

    }

    @Override
    public PeerInfo getPeerInfo() {
        return leavingPeerInfo;
    }

    @Override
    public byte[] asBytes() {
        return asBytes(getBitsCount());
    }

    @Override
    protected byte[] asBytes(int bitsCount) {
        byte[] bytes = super.asBytes(bitsCount);

        int currentBitIndex = super.getBitsCount();

        ByteUtils.addByteArrayToArrayAtByteIndex(leavingPeerInfo.asBytes(), bytes, currentBitIndex / 8);
        currentBitIndex += leavingPeerInfo.getBitsCount();

        for (ResourceObject currentResource : resources) {
            ByteUtils.addByteArrayToArrayAtByteIndex(currentResource.asBytes(), bytes, currentBitIndex / 8);
            currentBitIndex += currentResource.getBitsCount();
        }

        return bytes;
    }

    @Override
    public int getBitsCount() {
        int additionalBits = 0;
        for (ResourceObject currentResource : resources) {
            additionalBits += currentResource.getBitsCount();
        }
        return super.getBitsCount() + leavingPeerInfo.getBitsCount() + additionalBits;
    }

    /**
     * Adds resource object to this message.
     *
     * @param resource
     */
    public void addResourceObject(ResourceObject resource) {
        resources.add(resource);
    }

    public List<ResourceObject> getResources() {
        return this.resources;
    }

}
