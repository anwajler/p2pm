package pl.edu.pjwstk.p2pp.superpeer.messages.responses;

import pl.edu.pjwstk.p2pp.messages.responses.Response;
import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;
import pl.edu.pjwstk.p2pp.util.ByteUtils;


public class LookupPeerIndexResponse extends Response {

	private PeerInfo originatorPeerInfo;
    private PeerInfo lookedUpPeerInfo;

	public LookupPeerIndexResponse() {}

	public LookupPeerIndexResponse(boolean[] protocolVersion, boolean[] messageType, boolean isAcknowledgment, boolean isSentByPeer, boolean isRecursive,
                               boolean[] responseCode, byte ttl, byte[] transactionID, byte[] sourceID, byte[] responseID, boolean isOverReliable,
                               boolean isEncrypted, PeerInfo originatorPeerInfo, PeerInfo lookedUpPeerInfo) {
		super(protocolVersion, messageType, isAcknowledgment, isSentByPeer, isRecursive, responseCode, LOOKUP_PEER_INDEX_MESSAGE_TYPE, ttl, transactionID,
                sourceID, responseID, isOverReliable, isEncrypted);
		this.originatorPeerInfo = originatorPeerInfo;
        this.lookedUpPeerInfo = lookedUpPeerInfo;
	}

	@Override
	public void addObject(GeneralObject object) throws UnsupportedGeneralObjectException {
		if (object instanceof PeerInfo) {
            if (this.originatorPeerInfo == null) {
			    this.originatorPeerInfo = (PeerInfo) object;
            } else if (this.lookedUpPeerInfo == null) {
                this.lookedUpPeerInfo = (PeerInfo) object;
            } else {
                throw new UnsupportedGeneralObjectException("LookupPeerIndexResponse can't contain more than two PeerInfo objects");
            }
		} else {
			throw new UnsupportedGeneralObjectException("LookupPeerIndexResponse can't contain " + object.getClass().getName() + " object.");
		}
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
        currentIndex += this.originatorPeerInfo.getBitsCount();
        ByteUtils.addByteArrayToArrayAtByteIndex(this.lookedUpPeerInfo.asBytes(), bytes, currentIndex / 8);

		return bytes;
	}

	@Override
	public int getBitsCount() {
		return super.getBitsCount() + this.originatorPeerInfo.getBitsCount() + this.lookedUpPeerInfo.getBitsCount();
	}

	public PeerInfo getOriginatorPeerInfo() {
		return this.originatorPeerInfo;
	}

    public PeerInfo getResourceKeeperPeerInfo() {
        return this.lookedUpPeerInfo;
    }

    public PeerInfo getPeerInfo() {
        return getResourceKeeperPeerInfo();
    }

	@Override
	public boolean verify() {
		return this.originatorPeerInfo != null && this.lookedUpPeerInfo != null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("LookupPeerIndexResponse=[message=[" + super.toString() + "header=[" + getHeader() + "], originatorPeerInfo=[");
		if (this.originatorPeerInfo != null) {
			builder.append(this.originatorPeerInfo.toString());
		} else {
			builder.append("null");
		}
		builder.append("], lookedUpPeerInfo=[");
        if (this.lookedUpPeerInfo != null) {
            builder.append(this.lookedUpPeerInfo.toString());
        } else {
            builder.append("null");
        }
        builder.append("]]");


		return builder.toString();
	}
}