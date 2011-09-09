package pl.edu.pjwstk.p2pp.objects;

import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * Uptime object as defined in P2PP (draft 01). The uptime of this peer in number of seconds.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class Uptime extends GeneralObject {

	@Override
	public String toString() {
		return super.toString() + "[Uptime=" + uptime + "]";
	}

	private static final int UPTIME_SPECIFIC_DATA_LENGTH = 4 * 8;

	private int uptime;

	/**
	 * Constructor for uptime object.
	 * 
	 * @param uptime
	 *            Uptime.
	 */
	public Uptime(int uptime) {
		super(GeneralObject.UPTIME_OBJECT_TYPE);

		this.uptime = uptime;
	}

	@Override
	protected byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		int firstBitIndex = super.getBitsCount();

		ByteUtils.addIntToArrayAtBitIndex(uptime, bytes, firstBitIndex);

		return bytes;
	}

	@Override
	public int getBitsCount() {
		return super.getBitsCount() + UPTIME_SPECIFIC_DATA_LENGTH;
	}

	@Override
	public byte[] asBytes() {
		return asBytes(getBitsCount());
	}

	@Override
	public void addSubobject(GeneralObject subobject) throws UnsupportedGeneralObjectException {
		// TODO Auto-generated method stub

	}

}
