package pl.edu.pjwstk.p2pp.messages;

public abstract class Indication extends P2PPMessage {

	/**
	 * Creates Indication.
	 * 
	 * @param protocolVersion
	 * @param isSentByPeer
	 * @param isRecursive
	 * @param requestOrResponseType
	 * @param ttl
	 * @param transactionID
	 * @param sourceID
	 * @param responseID
	 * @param isOverReliable
	 * @param isEncrypted
	 */
	public Indication(boolean[] protocolVersion, boolean isSentByPeer, boolean isRecursive, byte requestOrResponseType,
			byte ttl, byte[] transactionID, byte[] sourceID, byte[] responseID, boolean isOverReliable,
			boolean isEncrypted) {
		super(protocolVersion, P2PPMessage.INDICATION_MESSAGE_TYPE, false, isSentByPeer, isRecursive, new boolean[9],
				requestOrResponseType, ttl, transactionID, sourceID, responseID, isOverReliable, isEncrypted);
	}

	/**
	 * Creates empty indication.
	 */
	public Indication() {
		super();
	}
}
