package pl.edu.pjwstk.p2pp.superpeer.messages.responses;

import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.responses.Response;
import pl.edu.pjwstk.p2pp.objects.*;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

import java.util.Collection;

public class SuperPeerBootstrapResponse extends Response {

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("BootstrapResponse=[message=[").append(super.toString()).append("], header=[").append(getHeader()).
                append("], bootstrapPeerInfo=");
		if (this.bootstrapPeerInfo != null) {
			builder.append(this.bootstrapPeerInfo.toString());
		} else {
			builder.append(this.bootstrapPeerInfo);
		}
		builder.append(", originatorPeerInfo=");
		if (this.originatorPeerInfo != null) {
			builder.append(this.originatorPeerInfo.toString());
		} else {
			builder.append(this.originatorPeerInfo);
		}
		builder.append(", p2poptions=");
		if (this.options != null) {
			builder.append(this.options.toString());
		} else {
			builder.append(this.options);
		}
		builder.append("]]");
		return builder.toString();
	}

	private PeerInfo bootstrapPeerInfo;

	private PeerInfo originatorPeerInfo;

	private P2POptions options;

	public SuperPeerBootstrapResponse() {}

	public SuperPeerBootstrapResponse(boolean[] protocolVersion, boolean[] messageType, boolean isAcknowledgment,
			boolean isSentByPeer, boolean isRecursive, boolean[] responseCode, byte ttl, byte[] transactionID,
			boolean isOverReliable, boolean isEncrypted, PeerInfo bootstrapPeerInfo, PeerInfo originatorPeerInfo,
			P2POptions options) {
		super(protocolVersion, messageType, isAcknowledgment, isSentByPeer, isRecursive, responseCode, P2PPMessage.BOOTSTRAP_MESSAGE_TYPE, ttl, transactionID,
                new byte[4], new byte[] { 0, 0, 0, 1 }, isOverReliable, isEncrypted);
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
		ByteUtils.addByteArrayToArrayAtByteIndex(this.bootstrapPeerInfo.asBytes(), bytes, currentIndex / 8);
		currentIndex += this.bootstrapPeerInfo.getBitsCount();
		ByteUtils.addByteArrayToArrayAtByteIndex(this.originatorPeerInfo.asBytes(), bytes, currentIndex / 8);
		currentIndex += this.originatorPeerInfo.getBitsCount();
		ByteUtils.addByteArrayToArrayAtByteIndex(this.options.asBytes(), bytes, currentIndex / 8);
		return bytes;
	}

	@Override
	public int getBitsCount() {
		return super.getBitsCount() + this.bootstrapPeerInfo.getBitsCount() + this.originatorPeerInfo.getBitsCount() + this.options.getBitsCount();
	}

	@Override
	public void addObject(GeneralObject object) {
		if (object instanceof PeerInfo) {
			if (this.bootstrapPeerInfo == null) {
				this.bootstrapPeerInfo = (PeerInfo) object;
			} else if (this.originatorPeerInfo == null) {
				this.originatorPeerInfo = (PeerInfo) object;
			} else {
				//throw exception
			}
		} else if (object instanceof P2POptions) {
			this.options = (P2POptions) object;
		}
	}

	public PeerInfo getBootstrapPeerInfo() {
		return this.bootstrapPeerInfo;
	}

	public PeerInfo getOriginatorPeerInfo() {
		return this.originatorPeerInfo;
	}

	public P2POptions getP2POptions() {
		return options;
	}

	public void setP2POptions(P2POptions options) {
		this.options = options;
	}

	@Override
	public PeerInfo getPeerInfo() {
		return this.bootstrapPeerInfo;
	}

	@Override
	public boolean verify() {
		boolean result = true;

		if (this.bootstrapPeerInfo == null || this.originatorPeerInfo == null || this.options == null) {
			result = false;
		} else {

			PeerID peerID = this.bootstrapPeerInfo.getPeerID();
			if (peerID == null) {
				result = false;
			} else {
				if (peerID.getPeerIDBytes() == null) {
					result = false;
				}
			}

			if (result) {
				Collection<AddressInfo> addressInfos = this.bootstrapPeerInfo.getAddressInfos();
				if (addressInfos == null) {
					result = false;
				} else if (addressInfos.size() <= 0) {
					result = false;
				}
			}

			peerID = this.originatorPeerInfo.getPeerID();
			if (peerID == null) {
				result = false;
			} else {
				if (peerID.getPeerIDBytes() == null) {
					result = false;
				}
			}

			if (result) {
				UnhashedID unhashedID = this.originatorPeerInfo.getUnhashedID();
				if (unhashedID == null) {
					result = false;
				} else {
					if (unhashedID.getUnhashedIDValue() == null) {
						result = false;
					}
				}
			}

			if (result) {
				Collection<AddressInfo> addressInfos = this.originatorPeerInfo.getAddressInfos();
				if (addressInfos == null) {
					result = false;
				} else if (addressInfos.size() <= 0) {
					result = false;
				}
			}

			if (this.options.getOverlayID() == null) {
				result = false;
			}
		}

		return result;
	}

}