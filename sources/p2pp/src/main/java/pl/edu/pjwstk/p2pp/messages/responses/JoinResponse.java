package pl.edu.pjwstk.p2pp.messages.responses;

import java.util.Vector;

import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.objects.Expires;
import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.NeighborTable;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.RoutingTable;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * Class wrapping data of a response for join request. TODO implement asBytes().
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class JoinResponse extends Response {

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("JoinResponse=[message=[" + super.toString() + "header=["
				+ getHeader() + "], admittingPeerInfo=[");
		if (admittingPeerInfo != null) {
			builder.append(admittingPeerInfo.toString());
		} else {
			builder.append(admittingPeerInfo);
		}
		builder.append("], expires=[");
		if (expires != null) {
			builder.append(expires.toString());
		} else {
			builder.append(expires);
		}
		builder.append("], routingTable=[");
		if (routingTable != null) {
			builder.append(routingTable.toString());
		} else {
			builder.append(routingTable);
		}
		builder.append("], neighborTable=[");
		if (neighborTable != null) {
			builder.append(neighborTable.toString());
		} else {
			builder.append(neighborTable);
		}
		builder.append("], peerInfos=[");
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

	/** TODO is this really PeerInfo of admitting peer? */
	private PeerInfo admittingPeerInfo;

	/**
	 * time before which the JP should send a keep-alive message.
	 */
	private Expires expires;

	/**
	 * Routing table.
	 */
	private RoutingTable routingTable;

	/**
	 * Neighbor table.
	 */
	private NeighborTable neighborTable;

	/**
	 * Multiple PeerInfo objects as defined in 8.4 of P2PP specification (draft 01). TODO what's this exactly? Whose
	 * PeerInfo?
	 */
	private Vector<PeerInfo> peerInfos = new Vector<PeerInfo>();

	/**
	 * Empty constructor. Useful for creating message piece-by-piece when reading from sockets.
	 */
	public JoinResponse() {

	}

	/**
	 * Constructor of JoinResponse P2PP message.
	 * 
	 * @param protocolVersion
	 * @param messageType
	 * @param isAcknowledgment
	 * @param isSentByPeer
	 * @param isRecursive
	 * @param responseCode
	 * @param ttl
	 * @param transactionID
	 * @param sourceID
	 * @param responseID
	 * @param isOverReliable
	 * @param isEncrypted
	 * @param admittingPeerInfo
	 *            TODO is this really PeerInfo of admitting peer?
	 * @param expires
	 * @param routingTable
	 * @param neighborTable
	 */
	public JoinResponse(boolean[] protocolVersion, boolean[] messageType, boolean isAcknowledgment,
			boolean isSentByPeer, boolean isRecursive, boolean[] responseCode, byte ttl, byte[] transactionID,
			byte[] sourceID, byte[] responseID, boolean isOverReliable, boolean isEncrypted,
			PeerInfo admittingPeerInfo, Expires expires, RoutingTable routingTable, NeighborTable neighborTable) {

		// TODO check if this is OK
		super(protocolVersion, messageType, isAcknowledgment, isSentByPeer, isRecursive, responseCode,
				P2PPMessage.JOIN_MESSAGE_TYPE, ttl, transactionID, sourceID, responseID, isOverReliable, isEncrypted);
		this.admittingPeerInfo = admittingPeerInfo;
		this.expires = expires;
		this.routingTable = routingTable;
		this.neighborTable = neighborTable;
	}

	@Override
	public byte[] asBytes() {
		return asBytes(getBitsCount());
	}

	@Override
	protected byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		int currentIndex = super.getBitsCount();
		ByteUtils.addByteArrayToArrayAtByteIndex(admittingPeerInfo.asBytes(), bytes, currentIndex / 8);

		currentIndex += admittingPeerInfo.getBitsCount();
		for (int i = 0; i < peerInfos.size(); i++) {
			PeerInfo currentPeerInfo = (PeerInfo) peerInfos.get(i);
			ByteUtils.addByteArrayToArrayAtByteIndex(currentPeerInfo.asBytes(), bytes, currentIndex / 8);
			currentIndex += currentPeerInfo.getBitsCount();
		}

		ByteUtils.addByteArrayToArrayAtByteIndex(expires.asBytes(), bytes, currentIndex / 8);

		currentIndex += expires.getBitsCount();

		if (routingTable != null) {
			ByteUtils.addByteArrayToArrayAtByteIndex(routingTable.asBytes(), bytes, currentIndex / 8);
			currentIndex += routingTable.getBitsCount();
		}
		if (neighborTable != null) {
			ByteUtils.addByteArrayToArrayAtByteIndex(neighborTable.asBytes(), bytes, currentIndex / 8);
			currentIndex += neighborTable.getBitsCount();
		}

		return bytes;
	}

	@Override
	public int getBitsCount() {
		// TODO synchronize getting of bitsCount?
		int additionalBits = 0;
		if (admittingPeerInfo != null)
			additionalBits += admittingPeerInfo.getBitsCount();
		if (expires != null)
			additionalBits += expires.getBitsCount();
		if (routingTable != null)
			additionalBits += routingTable.getBitsCount();
		if (neighborTable != null)
			additionalBits += neighborTable.getBitsCount();
		int size = peerInfos.size();
		for (int i = 0; i < size; i++) {
			additionalBits += ((PeerInfo) peerInfos.get(i)).getBitsCount();
		}

		return super.getBitsCount() + additionalBits;
	}

	@Override
	public void addObject(GeneralObject object) throws UnsupportedGeneralObjectException {
		if (object instanceof PeerInfo) {
			PeerInfo info = (PeerInfo) object;
			if (admittingPeerInfo == null) {
				admittingPeerInfo = info;
			} else {
				peerInfos.add(info);
			}
		} else if (object instanceof Expires) {
			expires = (Expires) object;
		} else if (object instanceof RoutingTable) {
			routingTable = (RoutingTable) object;
		} else if (object instanceof NeighborTable) {
			neighborTable = (NeighborTable) object;
		} else {
			throw new UnsupportedGeneralObjectException("JoinResponse can't handle " + object.getClass().getName()
					+ ".");
		}
	}

	@Override
	public PeerInfo getPeerInfo() {
		return admittingPeerInfo;
	}

	public PeerInfo getAdmittingPeerInfo() {
		return admittingPeerInfo;
	}

	public void setAdmittingPeerInfo(PeerInfo admittingPeerInfo) {
		this.admittingPeerInfo = admittingPeerInfo;
	}

	/**
	 * Returns object describing after how many seconds without message, peer will be considered dead.
	 * 
	 * @return
	 */
	public Expires getExpires() {
		return expires;
	}

	/**
	 * Sets object describing after how many seconds without message, peer will be considered dead.
	 * 
	 * @return
	 */
	public void setExpires(Expires expires) {
		this.expires = expires;
	}

	public RoutingTable getRoutingTable() {
		return routingTable;
	}

	public void setRoutingTable(RoutingTable routingTable) {
		this.routingTable = routingTable;
	}

	public NeighborTable getNeighborTable() {
		return neighborTable;
	}

	public void setNeighborTable(NeighborTable neighborTable) {
		this.neighborTable = neighborTable;
	}

	public Vector<PeerInfo> getPeerInfos() {
		return peerInfos;
	}

	public void setPeerInfos(Vector<PeerInfo> peerInfos) {
		this.peerInfos = peerInfos;
	}

	@Override
	public boolean verify() {
		boolean result = true;

		if (admittingPeerInfo == null) {
			result = false;
		}
		// TODO nothing more? I think that all peer infos should be checked...

		return result;
	}

}
