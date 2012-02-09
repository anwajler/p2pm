package pl.edu.pjwstk.p2pp.objects;

import java.util.concurrent.atomic.AtomicInteger;
import pl.edu.pjwstk.p2pp.util.Arrays;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * PeerID as defined in P2PP protocol (draft 01). It is an identifier of the node (client or peer) issuing or forwarding
 * the request. For DHTs, it is a fixed-length output of a hash function. For unstructured networks, it is also a
 * fixed-length identifier.
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 *
 */
public class PeerID extends GeneralObject {

	@Override
	public String toString() {
		return super.toString() + "[PeerID=[peerIDBytes=" + ByteUtils.byteArrayToHexString(peerIDBytes) + ", peerIDHashCode="+this.peerIDHashCode.get()+"]]";
	}

	/**
	 * Bytes containing peerID value.
	 */
	private volatile byte[] peerIDBytes;
        /**
         * Created one time hashCode for peerID - not all time when hashCode() is called.
         */
        private final AtomicInteger peerIDHashCode;

	/**
	 * Constructor for peerID object as defined in P2PP protocol (draft 01).
	 *
	 * @param peerIDBytes
	 *            Bytes of peerID.
	 */
	public PeerID(byte[] peerIDBytes) {
		super(GeneralObject.PEER_ID_OBJECT_TYPE);

		this.peerIDBytes = peerIDBytes;
                StringBuilder buffer = new StringBuilder("");
                for (byte b : this.peerIDBytes) buffer.append(b);
                this.peerIDHashCode = new AtomicInteger(buffer.toString().hashCode());
	}

	@Override
	protected byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		int byteIndex = super.getBitsCount() / 8;

		ByteUtils.addByteArrayToArrayAtByteIndex(peerIDBytes, bytes, byteIndex);

		return bytes;
	}

	@Override
	public int getBitsCount() {
		return super.getBitsCount() + peerIDBytes.length * 8;
	}

	@Override
	public byte[] asBytes() {
		return asBytes(getBitsCount());
	}

	@Override
	public void addSubobject(GeneralObject subobject) {
		// TODO Auto-generated method stub

	}

	/**
	 * Returns value of this PeerID object.
	 *
	 * @return
	 */
	public byte[] getPeerIDBytes() {
		return peerIDBytes;
	}

	/**
	 * Sets value of peerID.
	 */
	public void setPeerIDBytes(byte[] peerIDValue) {
		this.peerIDBytes = peerIDValue;
                StringBuilder buffer = new StringBuilder("");
                for (byte b : this.peerIDBytes) buffer.append(b);
                this.peerIDHashCode.set(buffer.toString().hashCode());
	}

	/**
	 * Two PeerID are considered equal if they're of the same type and if they have the same length of byte version of
	 * ID and if value of corresponding bytes in ID are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PeerID) {
			PeerID otherPeerID = (PeerID) obj;
			byte[] otherPeerIDBytes = otherPeerID.getPeerIDBytes();
			return Arrays.equals(peerIDBytes, otherPeerIDBytes);
		} else {
			return false;
		}
	}

    @Override
	public int hashCode() {
        return this.peerIDHashCode.get();
//		StringBuffer buffer = new StringBuffer("");
//        for (byte b : peerIDBytes) buffer.append(b);
//		return buffer.toString().hashCode();
	}
}
