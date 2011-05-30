package pl.edu.pjwstk.p2pp.messages.responses;

import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;


public class LookupPeerResponse extends Response {

	public LookupPeerResponse() {}

	public LookupPeerResponse(boolean[] protocolVersion, boolean[] messageType, boolean isAcknowledgment, boolean isSentByPeer, boolean isRecursive,
                               boolean[] responseCode, byte ttl, byte[] transactionID, byte[] sourceID, byte[] responseID, boolean isOverReliable,
                               boolean isEncrypted) {
		super(protocolVersion, messageType, isAcknowledgment, isSentByPeer, isRecursive, responseCode, LOOKUP_PEER_MESSAGE_TYPE, ttl, transactionID,
                sourceID, responseID, isOverReliable, isEncrypted);
	}

	@Override
	public void addObject(GeneralObject object) throws UnsupportedGeneralObjectException {
		/*if (object instanceof PeerInfo) {
            if (this.originatorPeerInfo == null) {
			    this.originatorPeerInfo = (PeerInfo) object;
            } else {
                throw new UnsupportedGeneralObjectException("LookupPeerResponse can't contain more than two PeerInfo objects");
            }
		} else {*/
			throw new UnsupportedGeneralObjectException("LookupPeerResponse can't contain " + object.getClass().getName() + " object.");
		//}
	}

	@Override
	public byte[] asBytes() {
		return asBytes(getBitsCount());
	}

	@Override
	protected byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		/*int currentIndex = super.getBitsCount();

		ByteUtils.addByteArrayToArrayAtByteIndex(this.originatorPeerInfo.asBytes(), bytes, currentIndex / 8);*/

		return bytes;
	}

	@Override
	public int getBitsCount() {
		//return super.getBitsCount() + this.originatorPeerInfo.getBitsCount();
        return super.getBitsCount();
	}

	public PeerInfo getPeerInfo() {
		//return this.originatorPeerInfo;
        return null;
	}

	@Override
	public boolean verify() {
		//return this.originatorPeerInfo != null;
        return true;
	}

	@Override
	public String toString() {
		/*StringBuilder builder = new StringBuilder("LookupPeerIndexResponse=[message=[" + super.toString() + "header=[" + getHeader() + "], originatorPeerInfo=[");
		if (this.originatorPeerInfo != null) {
			builder.append(this.originatorPeerInfo.toString());
		} else {
			builder.append("null");
		}
        builder.append("]]");
		return builder.toString();*/
        return "LookupPeerResponse=[message=[" + super.toString() + "header=[" + getHeader() + "]]";
	}
}