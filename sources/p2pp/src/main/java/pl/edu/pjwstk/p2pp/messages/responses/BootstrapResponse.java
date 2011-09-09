package pl.edu.pjwstk.p2pp.messages.responses;

import java.util.Vector;

import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.objects.AddressInfo;
import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.P2POptions;
import pl.edu.pjwstk.p2pp.objects.PeerID;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.UnhashedID;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * Class wrapping data of a response for bootstrap request. TODO Add optional PeerInfo object and extensions.
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 */
public class BootstrapResponse extends Response {

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("BootstrapResponse=[message=[" + super.toString() + "], header=["
                + getHeader() + "], bootstrapPeerInfo=");
        if (bootstrapPeerInfo != null) {
            builder.append(bootstrapPeerInfo.toString());
        } else {
            builder.append(bootstrapPeerInfo);
        }
        builder.append(", originatorPeerInfo=");
        if (originatorPeerInfo != null) {
            builder.append(originatorPeerInfo.toString());
        } else {
            builder.append(originatorPeerInfo);
        }
        builder.append(", p2poptions=");
        if (options != null) {
            builder.append(options.toString());
        } else {
            builder.append(options);
        }
        builder.append(", peerInfos=[");
        for (int i = 0; i < peerInfos.size(); i++) {
            PeerInfo currentPeerInfo = peerInfos.get(i);
            builder.append(currentPeerInfo.toString());
            if (i != peerInfos.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append("]]");
        return builder.toString();
    }

    /**
     * PeerInfo of bootstrap server that generated this response.
     */
    private PeerInfo bootstrapPeerInfo;

    /**
     * PeerInfo of originator of the transaction that this response is part of.
     */
    private PeerInfo originatorPeerInfo;

    /**
     * P2POptions object included in this response.
     */
    private P2POptions options;

    /**
     * Vector consisting of PeerInfo objects.
     */
    private Vector<PeerInfo> peerInfos = new Vector<PeerInfo>();

    /**
     * Empty constructor for creating empty messages and filling it later.
     */
    public BootstrapResponse() {

    }

    /**
     * Creates bootstrap response.
     *
     * @param protocolVersion
     * @param messageType        Message type (response, responseACK). Constants for this are in P2PPMessage class. As this is
     *                           response, it may be only response or responseACK.
     * @param isAcknowledgment
     * @param isSentByPeer
     * @param isRecursive
     * @param responseCode
     * @param ttl
     * @param transactionID
     * @param isOverReliable
     * @param isEncrypted
     * @param bootstrapPeerInfo
     * @param originatorPeerInfo
     * @param options
     */
    public BootstrapResponse(boolean[] protocolVersion, boolean[] messageType, boolean isAcknowledgment,
                             boolean isSentByPeer, boolean isRecursive, boolean[] responseCode, byte ttl, byte[] transactionID,
                             boolean isOverReliable, boolean isEncrypted, PeerInfo bootstrapPeerInfo, PeerInfo originatorPeerInfo,
                             P2POptions options) {
        super(protocolVersion, messageType, isAcknowledgment, isSentByPeer, isRecursive, responseCode,
                P2PPMessage.BOOTSTRAP_MESSAGE_TYPE, ttl, transactionID, new byte[4], new byte[]{0, 0, 0, 1},
                isOverReliable, isEncrypted);

        this.bootstrapPeerInfo = bootstrapPeerInfo;
        this.originatorPeerInfo = originatorPeerInfo;
        this.options = options;
    }

    @Override
    public byte[] asBytes() {
        return asBytes(getBitsCount());
    }

    @Override
    protected byte[] asBytes(int bitsCount) {
        byte[] bytes = super.asBytes(bitsCount);

        int currentIndex = super.getBitsCount();

        ByteUtils.addByteArrayToArrayAtByteIndex(bootstrapPeerInfo.asBytes(), bytes, currentIndex / 8);
        currentIndex += bootstrapPeerInfo.getBitsCount();
        ByteUtils.addByteArrayToArrayAtByteIndex(originatorPeerInfo.asBytes(), bytes, currentIndex / 8);
        currentIndex += originatorPeerInfo.getBitsCount();
        ByteUtils.addByteArrayToArrayAtByteIndex(options.asBytes(), bytes, currentIndex / 8);
        currentIndex += options.getBitsCount();
        int size = peerInfos.size();
        for (int i = 0; i < size; i++) {
            PeerInfo current = (PeerInfo) peerInfos.get(i);
            ByteUtils.addByteArrayToArrayAtByteIndex(current.asBytes(), bytes, currentIndex / 8);
            currentIndex += current.getBitsCount();
        }

        return bytes;
    }

    @Override
    public int getBitsCount() {
        int additionalLength = 0;
        int size = peerInfos.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                additionalLength += ((PeerInfo) peerInfos.get(i)).getBitsCount();
            }
        }
        return super.getBitsCount() + bootstrapPeerInfo.getBitsCount() + originatorPeerInfo.getBitsCount()
                + options.getBitsCount() + additionalLength;
    }

    @Override
    public void addObject(GeneralObject object) {
        if (object instanceof PeerInfo) {
            if (bootstrapPeerInfo == null) {
                bootstrapPeerInfo = (PeerInfo) object;
            } else if (originatorPeerInfo == null) {
                originatorPeerInfo = (PeerInfo) object;
            } else {
                peerInfos.add((PeerInfo) object);
            }
        } else if (object instanceof P2POptions) {
            options = (P2POptions) object;
        }
    }

    /**
     * Returns PeerInfo object of the bootstrap server that created this response.
     *
     * @return
     */
    public PeerInfo getBootstrapPeerInfo() {
        return bootstrapPeerInfo;
    }

    /**
     * Returns PeerInfo object of a node that initiated the transaction that this response is part of.
     *
     * @return
     */
    public PeerInfo getOriginatorPeerInfo() {
        return originatorPeerInfo;
    }

    /**
     * Returns P2POptions object included in this response.
     *
     * @return
     */
    public P2POptions getP2POptions() {
        return options;
    }

    /**
     * Sets P2POptions object being part of this message.
     *
     * @param options
     */
    public void setP2POptions(P2POptions options) {
        this.options = options;
    }

    /**
     * Adds peer info to a list of peer infos.
     *
     * @param peerInfo
     */
    public void addPeerInfo(PeerInfo peerInfo) {
        peerInfos.add(peerInfo);
    }

    /**
     * Returns Vector of PeerInfo objects of this response. Those PeerInfos are describing peers in the overlay.
     *
     * @return
     */
    public Vector<PeerInfo> getPeersInOverlay() {
        return peerInfos;
    }

    @Override
    public PeerInfo getPeerInfo() {
        return bootstrapPeerInfo;
    }

    @Override
    public boolean verify() {
        boolean result = true;

        if (bootstrapPeerInfo == null || originatorPeerInfo == null || options == null) {
            result = false;
        } else {

            // must contain peerID
            PeerID peerID = bootstrapPeerInfo.getPeerID();
            if (peerID == null) {
                result = false;
            } else {
                if (peerID.getPeerIDBytes() == null) {
                    result = false;
                }
            }

            // not checked if PeerID isn't here
            if (result) {
                // has to contain at least one AddressInfo
                Vector<AddressInfo> addressInfos = bootstrapPeerInfo.getAddressInfos();
                if (addressInfos == null) {
                    result = false;
                } else if (addressInfos.size() <= 0) {
                    result = false;
                }
            }

            // must contain peerID
            peerID = originatorPeerInfo.getPeerID();
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

            // P2POptions object must contain overlayID
			if (options.getOverlayID() == null) {
				result = false;
			}
		}

		return result;
	}

}
