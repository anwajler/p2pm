package pl.edu.pjwstk.p2pp.messages.responses;

import java.util.Vector;

import pl.edu.pjwstk.p2pp.objects.Certificate;
import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.P2POptions;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * Class for responses for enroll request.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class EnrollResponse extends Response {

	private PeerInfo peerInfo;
	private Certificate certificate;
	private P2POptions options;
	/** Vector with PeerInfo objects containing information about peers. */
	private Vector<PeerInfo> peers = new Vector<PeerInfo>();

	/**
	 * Empty constructor that doesn't fill any fields of this object. Useful for creating empty message that could be
	 * filled with data later (for instance, we read bytes from stream and fill fields one by one).
	 */
	public EnrollResponse() {

	}

	/**
	 * 
	 * @param protocolVersion
	 * @param sourceID
	 * @param isAcknowledgment
	 * @param isSentByPeer
	 * @param isRecursive
	 * @param peerInfo
	 * @param certificate
	 * @param options
	 */
	public EnrollResponse(boolean[] protocolVersion, boolean isAcknowledgment, boolean isSentByPeer,
			boolean isRecursive, boolean[] responseCode, byte ttl, byte[] transactionID, byte[] sourceID,
			byte[] responseID, PeerInfo peerInfo, Certificate certificate, P2POptions options) {
		/*
		 * super(protocolVersion, isAcknowledgment, isSentByPeer, isRecursive, responseCode,
		 * P2PPMessage.ENROLL_MESSAGE_TYPE, ttl, transactionID, sourceID, responseID);
		 */
		this.peerInfo = peerInfo;
		this.certificate = certificate;
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

		ByteUtils.addByteArrayToArrayAtBitIndex(peerInfo.asBytes(), bytes, currentIndex);
		currentIndex += peerInfo.getBitsCount();
		ByteUtils.addByteArrayToArrayAtBitIndex(certificate.asBytes(), bytes, currentIndex);
		currentIndex += certificate.getBitsCount();
		if (options != null) {
			ByteUtils.addByteArrayToArrayAtBitIndex(options.asBytes(), bytes, currentIndex);
			currentIndex += options.getBitsCount();
		}

		if (peers.size() > 0) {
			int peersSize = peers.size();
			for (int i = 0; i < peersSize; i++) {
				PeerInfo currentInfo = (PeerInfo) peers.get(i);
				ByteUtils.addByteArrayToArrayAtBitIndex(currentInfo.asBytes(), bytes, currentIndex);
				currentIndex += currentInfo.getBitsCount();
			}
		}

		return bytes;
	}

	@Override
	public int getBitsCount() {
		int peersSize = peers.size();
		int peersBitsCount = 0;
		for (int i = 0; i < peersSize; i++) {
			peersBitsCount += ((PeerInfo) peers.get(i)).getBitsCount();
		}

		return super.getBitsCount() + peerInfo.getBitsCount() + certificate.getBitsCount() + options.getBitsCount()
				+ peersSize;
	}

	@Override
	public void addObject(GeneralObject object) throws UnsupportedGeneralObjectException {
		// TODO Auto-generated method stub

	}

	@Override
	public PeerInfo getPeerInfo() {
		return peerInfo;
	}

	@Override
	public boolean verify() {
		// TODO Auto-generated method stub
		return false;
	}

}
