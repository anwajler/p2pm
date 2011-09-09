package pl.edu.pjwstk.p2pp.superpeer.messages.responses;

import pl.edu.pjwstk.p2pp.messages.responses.Response;
import pl.edu.pjwstk.p2pp.objects.*;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

public class LookupIndexResponse extends Response {

	private PeerInfo originatorPeerInfo;
    private PeerInfo resourceKeeperPeerInfo;

	public LookupIndexResponse() {}

	public LookupIndexResponse(boolean[] protocolVersion, boolean[] messageType, boolean isAcknowledgment, boolean isSentByPeer, boolean isRecursive,
                               boolean[] responseCode, byte ttl, byte[] transactionID, byte[] sourceID, byte[] responseID, boolean isOverReliable,
                               boolean isEncrypted, PeerInfo originatorPeerInfo, PeerInfo resourceKeeperPeerInfo) {
		super(protocolVersion, messageType, isAcknowledgment, isSentByPeer, isRecursive, responseCode, LOOKUP_INDEX_MESSAGE_TYPE, ttl, transactionID,
                sourceID, responseID, isOverReliable, isEncrypted);
		this.originatorPeerInfo = originatorPeerInfo;
        this.resourceKeeperPeerInfo = resourceKeeperPeerInfo;
	}

	@Override
	public void addObject(GeneralObject object) throws UnsupportedGeneralObjectException {
		if (object instanceof PeerInfo) {
            if (this.originatorPeerInfo == null) {
			    this.originatorPeerInfo = (PeerInfo) object;
            } else if (this.resourceKeeperPeerInfo == null) {
                this.resourceKeeperPeerInfo = (PeerInfo) object;
            } else {
                throw new UnsupportedGeneralObjectException("LookupIndexResponse can't contain more than two PeerInfo objects");
            }
		} else {
			throw new UnsupportedGeneralObjectException("LookupIndexResponse can't contain " + object.getClass().getName() + " object.");
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
        ByteUtils.addByteArrayToArrayAtByteIndex(this.resourceKeeperPeerInfo.asBytes(), bytes, currentIndex / 8);

		return bytes;
	}

	@Override
	public int getBitsCount() {
		return super.getBitsCount() + this.originatorPeerInfo.getBitsCount() + this.resourceKeeperPeerInfo.getBitsCount();
	}

	public PeerInfo getOriginatorPeerInfo() {
		return this.originatorPeerInfo;
	}

    public PeerInfo getResourceKeeperPeerInfo() {
        return this.resourceKeeperPeerInfo;
    }

    public PeerInfo getPeerInfo() {
        return getResourceKeeperPeerInfo();
    }

	@Override
	public boolean verify() {
		return this.originatorPeerInfo != null && this.resourceKeeperPeerInfo != null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("LookupIndexResponse=[message=[" + super.toString() + "header=[" + getHeader() + "], originatorPeerInfo=[");
		if (this.originatorPeerInfo != null) {
			builder.append(this.originatorPeerInfo.toString());
		} else {
			builder.append(this.originatorPeerInfo);
		}
		builder.append("], resourceKeeperPeerInfo=[");
        if (this.resourceKeeperPeerInfo != null) {
            builder.append(this.resourceKeeperPeerInfo.toString());
        } else {
            builder.append(this.resourceKeeperPeerInfo);
        }
        builder.append("]]");


		return builder.toString();
	}
}