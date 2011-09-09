package pl.edu.pjwstk.p2pp.messages.requests;

import java.util.Vector;

import pl.edu.pjwstk.p2pp.messages.Acknowledgment;
import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.responses.JoinResponse;
import pl.edu.pjwstk.p2pp.objects.AddressInfo;
import pl.edu.pjwstk.p2pp.objects.Expires;
import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.NeighborTable;
import pl.edu.pjwstk.p2pp.objects.PeerID;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.RequestOptions;
import pl.edu.pjwstk.p2pp.objects.RoutingTable;
import pl.edu.pjwstk.p2pp.objects.UnhashedID;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class JoinRequest extends Request {

	/**
	 * PeerInfo object included in this request.
	 */
	private PeerInfo peerInfo;

	/**
	 * RequestOptions included in this object.
	 */
	private RequestOptions options;

	/**
	 * Default constructor for join request message.
	 */
	public JoinRequest() {

	}

	/**
	 * Creates JoinRequest object.
	 * 
	 * @param protocolVersion
	 * @param isSentByPeer
	 * @param isRecursive
	 * @param ttl
	 * @param transactionID
	 *            May be null, but has to be filled before sending.
	 * @param sourceID
	 * @param isOverReliable
	 * @param isEncrypted
	 * @param requestOptions
	 *            Optional object in this message, so may be null.
	 * @param peerInfo
	 */
	public JoinRequest(boolean[] protocolVersion, boolean isSentByPeer, boolean isRecursive, byte ttl,
			byte[] transactionID, byte[] sourceID, boolean isOverReliable, boolean isEncrypted,
			RequestOptions requestOptions, PeerInfo peerInfo) {
		// TODO is false OK?
		super(protocolVersion, false, isSentByPeer, isRecursive, P2PPMessage.JOIN_MESSAGE_TYPE, ttl, transactionID,
				sourceID, isOverReliable, isEncrypted);
		this.peerInfo = peerInfo;
		this.options = requestOptions;
	}

	@Override
	public byte[] asBytes() {
		return asBytes(getBitsCount());
	}

	@Override
	protected byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		int currentIndex = super.getBitsCount();

		if (options != null) {
			ByteUtils.addByteArrayToArrayAtByteIndex(options.asBytes(), bytes, currentIndex / 8);
			currentIndex += options.getBitsCount();
		}
		ByteUtils.addByteArrayToArrayAtByteIndex(peerInfo.asBytes(), bytes, currentIndex / 8);
		// currentIndex += peerInfo.getBitsCount();
		return bytes;
	}

	@Override
	public int getBitsCount() {
		int additionalBitsCount = 0;
		if (options != null) {
			additionalBitsCount += options.getBitsCount();
		}
		return super.getBitsCount() + additionalBitsCount + peerInfo.getBitsCount();
	}

	@Override
	public RequestOptions getRequestOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addObject(GeneralObject object) throws UnsupportedGeneralObjectException {
		if (object instanceof PeerInfo) {
			peerInfo = (PeerInfo) object;
		} else if (object instanceof RequestOptions) {
			options = (RequestOptions) object;
		} else {
			throw new UnsupportedGeneralObjectException("JoinRequest can't contain " + object.getClass().toString());
		}
	}

	@Override
	public Acknowledgment createACK(byte[] ownPeerID) {

		return null;
	}

	@Override
	public PeerInfo getPeerInfo() {
		return peerInfo;
	}

	/**
	 * Creates {@link JoinResponse} object for this request. Response will contain the same protocolVersion, A, P, R
	 * flags, requestCode, transactionID, sourceID, over reliable and encrypted as this request. ResponseID is filled
	 * with PeerID value of ownPeerInfo. Expires can't be null. Routing and neighbor tables may be null.
	 * 
	 * @param responseCode
	 * @param ownPeerInfo
	 * @param expires
	 *            Describes a time after which a peer will be considered dead, if not sending any messages.Can't be
	 *            null.
	 * @param routingTable
	 *            May be null.
	 * @param neighborTable
	 *            May be null.
	 */
	public JoinResponse createResponse(boolean[] responseCode, PeerInfo ownPeerInfo, Expires expires,
			RoutingTable routingTable, NeighborTable neighborTable) {
		JoinResponse response = new JoinResponse(protocolVersion, P2PPMessage.RESPONSE_MESSAGE_TYPE,
				isAcknowledgment(), isByPeer(), isRecursive(), responseCode, (byte) 255, transactionID, sourceID,
				ownPeerInfo.getPeerID().getPeerIDBytes(), isOverReliable(), isEncrypted(), ownPeerInfo, expires,
				routingTable, neighborTable);
		return response;
	}

	@Override
	public boolean verify() {
		boolean result = true;

		// must contain peerinfo
		if (peerInfo == null) {
			result = false;
		} else {
			// must contain peerID
			PeerID peerID = peerInfo.getPeerID();
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
				UnhashedID unhashedID = peerInfo.getUnhashedID();
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
				Vector<AddressInfo> addressInfos = peerInfo.getAddressInfos();
				if (addressInfos == null) {
					result = false;
				} else if (addressInfos.size() <= 0) {
					result = false;
				}
			}

			// TODO something more?
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("JoinRequest=[message=[" + super.toString() + " header=["
				+ getHeader() + "], peerInfo=[");
		if (peerInfo != null) {
			builder.append(peerInfo.toString());
		} else {
			builder.append(peerInfo);
		}
		builder.append("], options=[");
		if (options != null) {
			builder.append(options.toString());
		} else {
			builder.append(options);
		}
		return builder.toString() + "]]";
	}
}
