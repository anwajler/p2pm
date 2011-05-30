package pl.edu.pjwstk.p2pp.messages.requests;

import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.RequestOptions;
import pl.edu.pjwstk.p2pp.objects.Signature;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * Enroll request as defined in P2PP specification (draft 01).
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class EnrollRequest extends Request {

	private Signature signature;

	private PeerInfo peerInfo;

	private RequestOptions options;

	private byte[] password;

	/**
	 * 
	 * @param protocolVersion
	 * @param sourceID
	 * @param isAcknowledgment
	 * @param isSentByPeer
	 * @param isRecursive
	 * @param signature
	 * @param peerInfo
	 * @param options
	 *            Request options. May be null.
	 */
	public EnrollRequest(boolean[] protocolVersion, boolean isAcknowledgment, boolean isSentByPeer,
			boolean isRecursive, byte ttl, byte[] transactionID, byte[] sourceID, Signature signature,
			PeerInfo peerInfo, RequestOptions options, byte[] password) {

		// TODO Is fourth argument alright?
		// super(protocolVersion, isAcknowledgment, isSentByPeer, isRecursive,
		// ENROLL_MESSAGE_TYPE, ttl, transactionID, sourceID);

		this.signature = signature;
		this.peerInfo = peerInfo;
		this.options = options;
		this.password = password;
	}

	public byte[] asBytes() {
		return asBytes(getBitsCount());
	}

	@Override
	protected byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		int currentIndex = super.getBitsCount();

		if (options != null) {
			ByteUtils.addByteArrayToArrayAtBitIndex(options.asBytes(), bytes, currentIndex);
			currentIndex += options.getBitsCount();
		}
		ByteUtils.addByteArrayToArrayAtBitIndex(peerInfo.asBytes(), bytes, currentIndex);

		currentIndex += peerInfo.getBitsCount();

		ByteUtils.addByteArrayToArrayAtBitIndex(signature.asBytes(), bytes, currentIndex);

		currentIndex += signature.getBitsCount();

		ByteUtils.addByteArrayToArrayAtBitIndex(password, bytes, currentIndex);

		return bytes;
	}

	@Override
	public int getBitsCount() {
		int bitsCount = 0;
		if (options != null)
			bitsCount += options.getBitsCount();

		return super.getBitsCount() + bitsCount + peerInfo.getBitsCount() + signature.getBitsCount()
				+ (password.length * 8);
	}

	@Override
	public RequestOptions getRequestOptions() {
		// TODO Auto-generated method stub
		return null;
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
