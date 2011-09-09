package pl.edu.pjwstk.p2pp.socialcircle.messages.responses;

import pl.edu.pjwstk.p2pp.messages.responses.*;
import java.util.List;
import java.util.Vector;

import pl.edu.pjwstk.p2pp.objects.AddressInfo;
import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.PeerID;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.UnhashedID;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;
import pl.edu.pjwstk.p2pp.util.ByteUtils;


public class SocialCircleSetUpResponse extends Response {

    /**
     * PeerInfo of response originator.
     */
    private PeerInfo originatorPeerInfo;
    private Vector<PeerInfo> vset = new Vector<PeerInfo>();

    public SocialCircleSetUpResponse() {
    }

    public SocialCircleSetUpResponse(boolean[] protocolVersion, boolean[] messageType, boolean isAcknowledgment, boolean isSentByPeer, boolean isRecursive,
            boolean[] responseCode, byte ttl, byte[] transactionID, byte[] sourceID, byte[] responseID, boolean isOverReliable, boolean isEncrypted,
            PeerInfo ownPeerInfo, PeerInfo peerInfo) {
        super(protocolVersion, messageType, isAcknowledgment, isSentByPeer, isRecursive, responseCode, SET_UP_MESSAGE_TYPE, ttl, transactionID, sourceID,
                responseID, isOverReliable, isEncrypted);
        this.originatorPeerInfo = new PeerInfo(ownPeerInfo.getPeerID(), ownPeerInfo.getUptime(), ownPeerInfo.getAddressInfos(), ownPeerInfo.getUnhashedID());

        if (null != peerInfo) {
            this.vset.add(peerInfo);
        }
    }

    public SocialCircleSetUpResponse(boolean[] protocolVersion, boolean[] messageType, boolean isAcknowledgment, boolean isSentByPeer, boolean isRecursive,
            boolean[] responseCode, byte ttl, byte[] transactionID, byte[] sourceID, byte[] responseID, boolean isOverReliable, boolean isEncrypted,
            PeerInfo ownPeerInfo, List<PeerInfo> vset) {
        super(protocolVersion, messageType, isAcknowledgment, isSentByPeer, isRecursive, responseCode, SET_UP_MESSAGE_TYPE, ttl, transactionID, sourceID,
                responseID, isOverReliable, isEncrypted);
        this.originatorPeerInfo = new PeerInfo(ownPeerInfo.getPeerID(), ownPeerInfo.getUptime(), ownPeerInfo.getAddressInfos(), ownPeerInfo.getUnhashedID());

        if (vset != null) {
            // given array may contain nulls, so only non-null elements are added
            for (PeerInfo peerInfo : vset) {
                if (peerInfo != null) {
                    this.vset.add(peerInfo);
                }
            }
        }
    }

    @Override
    public void addObject(GeneralObject object) throws UnsupportedGeneralObjectException {
        if (object instanceof PeerInfo) {
            PeerInfo peerInfo = (PeerInfo) object;
            if (this.originatorPeerInfo == null) {
                this.originatorPeerInfo = peerInfo;
            } else {
                this.vset.add(peerInfo);
            }
        } else {
            throw new UnsupportedGeneralObjectException("SocialCircleSetUpResponse can't contain " + object.getClass().getName() + " object.");
        }
    }

    @Override
    public byte[] asBytes() {
        return asBytes(getBitsCount());
    }

    @Override
    protected byte[] asBytes(int bitsCount) {
        byte[] bytes = super.asBytes(bitsCount);

        int currentIndex = super.getBitsCount() /8 ;

        ByteUtils.addByteArrayToArrayAtByteIndex(this.originatorPeerInfo.asBytes(), bytes, currentIndex);
        currentIndex += this.originatorPeerInfo.getBitsCount() / 8;

        for (PeerInfo currentPeerInfo : this.vset) {
            if (currentPeerInfo != null) {
                ByteUtils.addByteArrayToArrayAtByteIndex(currentPeerInfo.asBytes(), bytes, currentIndex);
                currentIndex += currentPeerInfo.getBitsCount() / 8;
            }
        }

        return bytes;
    }

    @Override
    public int getBitsCount() {
        int additionalBits = 0;
        if (this.vset != null) {
            int vsetSize = this.vset.size();
            for (int i = 0; i < vsetSize; i++) {
                PeerInfo currentPeerInfo = this.vset.get(i);
                additionalBits += currentPeerInfo.getBitsCount();
            }

        }
        return super.getBitsCount() + this.originatorPeerInfo.getBitsCount() + additionalBits;
    }

    /**
     * Returns PeerInfo object of response originator.
     *
     * @return
     */
    @Override
    public PeerInfo getPeerInfo() {
        return this.originatorPeerInfo;
    }

    public Vector<PeerInfo> getVSet() {
        return this.vset;
    }

    @Override
    public boolean verify() {
        boolean result = true;

        if (this.originatorPeerInfo == null || this.vset == null) {
            result = false;
        } else {
            // must contain peerID
            PeerID peerID = this.originatorPeerInfo.getPeerID();
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
                UnhashedID unhashedID = this.originatorPeerInfo.getUnhashedID();
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
                Vector<AddressInfo> addressInfos = this.originatorPeerInfo.getAddressInfos();
                if (addressInfos == null) {
                    result = false;
                } else if (addressInfos.size() <= 0) {
                    result = false;
                }
            }

            if (result) {
                if (ByteUtils.booleanArrayToInt(this.reservedOrResponseCode) == Response.RESPONSE_CODE_OK) {
                    int vsetSize = this.vset.size();
                    if (vsetSize == 0) {
                        result = false;
                    } else {
                        for (int i = 0; i < vsetSize; i++) {
                            PeerInfo currentPeerInfo = this.vset.get(i);
                            PeerID currentPeerID = currentPeerInfo.getPeerID();
                            if (currentPeerID == null) {
                                result = false;
                            } else {
                                if (currentPeerID.getPeerIDBytes() == null) {
                                    result = false;
                                } else {
                                    Vector<AddressInfo> addressInfos = currentPeerInfo.getAddressInfos();
                                    if (addressInfos == null) {
                                        result = false;
                                    } else if (addressInfos.size() <= 0) {
                                        result = false;
                                    } else {
                                        UnhashedID currentUnhashedID = currentPeerInfo.getUnhashedID();
                                        if (currentUnhashedID == null) {
                                            result = false;
                                        } else {
                                            if (currentUnhashedID.getUnhashedIDValue() == null) {
                                                result = false;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("SocialCircleSetUpResponse=[message=[");
        builder.append(super.toString());
        builder.append("header=[");
        builder.append(getHeader());
        builder.append("], peerInfo=[");
        if (this.originatorPeerInfo != null) {
            builder.append(this.originatorPeerInfo.toString());
        } else {
            builder.append(this.originatorPeerInfo);
        }

        builder.append("], paths=[");
        int vsetSize = this.vset.size();
        for (int i = 0; i < vsetSize; i++) {
            PeerInfo currentPeerInfo = this.vset.get(i);
            if (currentPeerInfo != null) {
                builder.append(currentPeerInfo.toString());
                if (i != vsetSize - 1) {
                    builder.append(", ");
                }
            }
        }
        builder.append("]]");

        return builder.toString();
    }
}
