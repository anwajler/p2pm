package pl.edu.pjwstk.p2pp.socialcircle;

import java.math.BigInteger;
import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;
import pl.edu.pjwstk.p2pp.util.ByteUtils;


public class SocialPath extends GeneralObject implements Comparable<SocialPath> {

    private PathID pathId;

    private PeerInfo endpointA;
    private PeerInfo endpointB;

    private PeerInfo nextA;
    private PeerInfo nextB;

    
    public SocialPath() {
        super(GeneralObject.SOCIAL_PATH_OBJECT_TYPE);
    }

    public SocialPath(PathID pathId) {
        super(GeneralObject.SOCIAL_PATH_OBJECT_TYPE);
        this.pathId = new PathID(pathId.getPathIDValue());
    }

    public SocialPath(PathID pathId, PeerInfo endpointA, PeerInfo endpointB, PeerInfo nextA, PeerInfo nextB) {
        super(GeneralObject.SOCIAL_PATH_OBJECT_TYPE);

        this.pathId = new PathID(pathId.getPathIDValue());

        this.endpointA = new PeerInfo(endpointA.getPeerID(), endpointA.getUptime(), endpointA.getAddressInfos(), endpointA.getUnhashedID());
        this.endpointB = new PeerInfo(endpointB.getPeerID(), endpointB.getUptime(), endpointB.getAddressInfos(), endpointB.getUnhashedID());

        this.nextA = new PeerInfo(nextA.getPeerID(), nextA.getUptime(), nextA.getAddressInfos(), nextA.getUnhashedID());
        this.nextB = new PeerInfo(nextB.getPeerID(), nextB.getUptime(), nextB.getAddressInfos(), nextB.getUnhashedID());

    }

    public PathID getPathId() {
        return this.pathId;
    }

    public PeerInfo getEndpointA() {
        return this.endpointA;
    }

    public PeerInfo getEndpointB() {
        return this.endpointB;
    }

    public PeerInfo getNextA() {
        return this.nextA;
    }

    public PeerInfo getNextB() {
        return this.nextB;
    }

    public static BigInteger calculateDistance(PeerInfo first, PeerInfo second, boolean isPhysical) {
        if (isPhysical) {
            return BigInteger.ZERO;
        } else {
            BigInteger firstIdBI = new BigInteger(1, first.getUnhashedID().asBytes());
            BigInteger secondIdBI = new BigInteger(1, second.getUnhashedID().asBytes());
            return firstIdBI.xor(secondIdBI);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("SocialPath=[pathId=");
        sb.append(this.pathId);
        sb.append("; endpointA=");
        sb.append(this.endpointA);
        sb.append("; endpointB=");
        sb.append(this.endpointB);
        sb.append("; nextA=");
        sb.append(this.nextA);
        sb.append("; nextB=");
        sb.append(this.nextB);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public byte[] asBytes() {
            return asBytes(getBitsCount());
    }

    @Override
    protected byte[] asBytes(int bitsCount) {
            byte[] bytes = super.asBytes(bitsCount);

            int byteIndex = super.getBitsCount() / 8;

            ByteUtils.addByteArrayToArrayAtBitIndex(pathId.asBytes(), bytes, byteIndex * 8);
            byteIndex += pathId.getBitsCount() / 8;
            ByteUtils.addByteArrayToArrayAtBitIndex(endpointA.asBytes(), bytes, byteIndex * 8);
            byteIndex += endpointA.getBitsCount() / 8;
            ByteUtils.addByteArrayToArrayAtBitIndex(endpointB.asBytes(), bytes, byteIndex * 8);
            byteIndex += endpointB.getBitsCount() / 8;
            ByteUtils.addByteArrayToArrayAtBitIndex(nextA.asBytes(), bytes, byteIndex * 8);
            byteIndex += nextA.getBitsCount() / 8;
            ByteUtils.addByteArrayToArrayAtBitIndex(nextB.asBytes(), bytes, byteIndex * 8);
            byteIndex += nextB.getBitsCount() / 8;

            return bytes;
    }

    @Override
    public int getBitsCount() {
            return super.getBitsCount()  + pathId.getBitsCount() + endpointA.getBitsCount() + endpointB.getBitsCount() + nextA.getBitsCount() + nextB.getBitsCount();
    }

    @Override
    public void addSubobject(GeneralObject subobject) throws UnsupportedGeneralObjectException {
            if (subobject instanceof PathID){
                if (null == this.pathId) {
                    this.pathId = (PathID) subobject;
                }
            }
            else if (subobject instanceof PeerInfo) {
                if (null == this.endpointA) {
                    this.endpointA = (PeerInfo) subobject;
                } else if (null == this.endpointB) {
                    this.endpointB = (PeerInfo) subobject;
                } else if (null == this.nextA) {
                    this.nextA = (PeerInfo) subobject;
                } else if (null == this.nextB) {
                    this.nextB = (PeerInfo) subobject;
                } else {
                    throw new UnsupportedGeneralObjectException("SocialPath can't handle more objects.");
                }
            } else {
                    throw new UnsupportedGeneralObjectException("SocialPath can't contain "+subobject.getClass().getName()+" as subobject.");
            }
    }

    public int compareTo(SocialPath path) {

        if (this.endpointA.getPeerID().equals(path.getEndpointA().getPeerID()) && this.endpointB.getPeerID().equals(path.getEndpointB().getPeerID())) {
            boolean nextAEqual = false;

            if (null == this.nextA.getPeerID()) {
                if (null == path.getNextA().getPeerID()) nextAEqual = true;
            } else if (this.nextA.getPeerID().equals(path.getNextA().getPeerID())) {
                nextAEqual = true;
            }

            if (null == this.nextB.getPeerID()) {
                if (null == path.getNextA().getPeerID() && nextAEqual) return 0;
            } else if (this.nextB.getPeerID().equals(path.getNextB().getPeerID())){
                if (nextAEqual) return 0;
            }
        }

        return 1;
    }
    
}
