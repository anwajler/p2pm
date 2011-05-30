package pl.edu.pjwstk.p2pp.superpeer.messages.indications;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.p2pp.messages.Indication;
import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.objects.*;
import pl.edu.pjwstk.p2pp.superpeer.messages.requests.LookupIndexRequest;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

import java.util.Vector;

public class IndexPeerIndication extends Indication {

    @SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(LookupIndexRequest.class);

    /**
     * PeerInfo object included in this message.
     */
    private PeerInfo peerInfo;

    /**
     * Default constructor. Used for creating empty messages to be filled later.
     */
    public IndexPeerIndication() {
    }

    /**
     * Creates lookup object request.
     *
     * @param protocolVersion
     * @param isSentByPeer
     * @param isRecursive
     * @param ttl
     * @param transactionID
     * @param isOverReliable
     * @param isEncrypted
     * @param peerInfo Can't be null.
     */
    public IndexPeerIndication(boolean[] protocolVersion, boolean isSentByPeer, boolean isRecursive, byte ttl, byte[] transactionID,
                     boolean isOverReliable, boolean isEncrypted, PeerInfo peerInfo) {
        super(protocolVersion, isSentByPeer, isRecursive, P2PPMessage.INDEX_PEER_MESSAGE_TYPE, ttl, transactionID, new byte[4], null, isOverReliable,
                isEncrypted);
        this.peerInfo = peerInfo;
    }

    @Override
    public void addObject(GeneralObject object) throws UnsupportedGeneralObjectException {
        if (object instanceof PeerInfo) {
            if (this.peerInfo == null) {
                this.peerInfo = (PeerInfo) object;
            } else {
                throw new UnsupportedGeneralObjectException("IndexPeerIndication can't contain more objects of " + object.getClass().getName() + " type.");
            }
        } else {
            throw new UnsupportedGeneralObjectException("IndexPeerIndication can't contain object of " + object.getClass().getName() + " type.");
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

        ByteUtils.addByteArrayToArrayAtByteIndex(this.peerInfo.asBytes(), bytes, index / 8);

        return bytes;
    }

    @Override
    public int getBitsCount() {
        return super.getBitsCount() + this.peerInfo.getBitsCount();
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

        if (this.peerInfo == null) {
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
        StringBuilder builder = new StringBuilder("IndexPeerIndication=[message=[" + super.toString() + "], header=["
                + getHeader() + "], peerInfo=[");
        if (this.peerInfo != null) {
            builder.append(this.peerInfo.toString());
        } else {
            builder.append("null");
        }
        return builder.toString() + "]]";
    }
}