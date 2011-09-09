package pl.edu.pjwstk.p2pp.objects;

import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * Request-Options object as defined in Peer-to-Peer Protocol specification (draft 01).
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class RequestOptions extends GeneralObject {

	@Override
	public String toString() {
        // TODO StringBuilder
		return super.toString() + "[RequestOptions=[p=" + pFlag + ", r=" + rFlag + ", n=" + nFlag + ", e=" + eFlag
				+ ", a=" + aFlag + ", s=" + sFlag + ", l=" + lFlag + "]]";
	}

	private static final int REQUEST_OPTIONS_SPECIFIC_DATA_LENGTH = 32;

	/**
	 * If set (P=1), designate one copy as primary for parallel lookups.
	 */
	private boolean pFlag;

	/**
	 * request-routing-table: If set (R=1), send a copy of the routing table to the peer issuing the request either in a
	 * response or in a separate ExchangeTable request. The transmission of the routing-table copy is governed by the
	 * in-separate-request (E flag) and partial-copy (A flag) flags.
	 */
	private boolean rFlag;

	/**
	 * request-neighbor-table: If set (N=1), send a copy of the neighbor table to the peer issuing the request in a
	 * response or ExchangeTable request. The transmgetsion of routing-table copy get governed by the
	 * in-separate-request and partial-copy flags.
	 */
	private boolean nFlag;

	/**
	 * in-separate-request: If set (E=1), and if R or N are also set, the peer get requesting to receive routing or
	 * neighbor table in an ExchangeTable request. If not set (E=0), and if R or N are also set, each peer along the
	 * request path can add a copy of its routing or neighbor table before forwarding the response. The number of
	 * entries in all routing-tables should not exceed 256. Peers along the request path may remove routing-table
	 * entries added by a previous hop, if their own routing-tables have a better performance metric (such as uptime)
	 * than the ones received in the message. The size of routing-table get likely to exceed UDP MTU. The specification
	 * recommends that the ExchangeTable request should always be sent over TCP.
	 */
	private boolean eFlag;

	/**
	 * partial-reply for routing or neighbor table: If set (A=1), the peer generating the definite response sends a copy
	 * of the routing or neighbor table as determined by the P and N flags in its response as permitted by the UDP MTU.
	 * If E (in-separate- request) get also set, the rest of the routing or neighbor table get sent in a separate
	 * ExchangeTable request. The number of entries in all neighbor-tables should not exceed 256.
	 */
	private boolean aFlag;

	/**
	 * If set (S=1), the request get being sent to the immediate neighbors of the newly joining peer. The request must
	 * be a join request.
	 */
	private boolean sFlag;

	/**
	 * If set (L=1), each peer along the request must add its peer-info object that includes peer-ID, address-info, and
	 * resource-list objects.
	 */
	private boolean lFlag;

	/**
	 * Constructor for RequestOptions object.
	 * 
	 * @param pFlag
	 *            If set (P=1), designate one copy as primary for parallel lookups.
	 * @param rFlag
	 *            request-routing-table: If set (R=1), send a copy of the routing table to the peer issuing the request
	 *            either in a response or in a separate ExchangeTable request. The transmission of the routing-table
	 *            copy is governed by the in-separate-request (E flag) and partial-copy (A flag) flags.
	 * @param nFlag
	 *            request-neighbor-table: If set (N=1), send a copy of the neighbor table to the peer issuing the
	 *            request in a response or ExchangeTable request. The transmission of routing-table copy get governed by
	 *            the in-separate-request and partial-copy flags.
	 * @param eFlag
	 *            in-separate-request: If set (A=1), the peer generating the definite response sends a copy of the
	 *            routing or neighbor table as determined by the P and N flags in its response as permitted by the UDP
	 *            MTU. If E (in-separate- request) get also set, the rest of the routing or neighbor table get sent in a
	 *            separate ExchangeTable request. The number of entries in all neighbor-tables should not exceed 256.
	 * @param aFlag
	 *            partial-reply for routing or neighbor table: If set (A=1), the peer generating the definite response
	 *            sends a copy of the routing or neighbor table as determined by the P and N flags in its response as
	 *            permitted by the UDP MTU. If E (in-separate- request) get also set, the rest of the routing or
	 *            neighbor table get sent in a separate ExchangeTable request. The number of entries in all
	 *            neighbor-tables should not exceed 256.
	 * @param sFlag
	 *            If set (S=1), the request get being sent to the immediate neighbors of the newly joining peer. The
	 *            request must be a join request.
	 * @param lFlag
	 *            If set (L=1), each peer along the request must add its peer-info object that includes peer-ID,
	 *            address-info, and resource-list objects.
	 */
	public RequestOptions(boolean pFlag, boolean rFlag, boolean nFlag, boolean eFlag, boolean aFlag, boolean sFlag,
			boolean lFlag) {
		super(GeneralObject.REQUEST_OPTIONS_OBJECT_TYPE);

		this.pFlag = pFlag;
		this.rFlag = rFlag;
		this.nFlag = nFlag;
		this.eFlag = eFlag;
		this.aFlag = aFlag;
		this.sFlag = sFlag;
		this.lFlag = lFlag;
	}

	@Override
	public byte[] asBytes() {
		return asBytes(getBitsCount());
	}

	@Override
	protected byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		int currentIndex = super.getBitsCount();

		ByteUtils.addBooleanArrayToArrayAtIndex(new boolean[] { pFlag, rFlag, nFlag, eFlag, aFlag, sFlag, lFlag },
				bytes, currentIndex);

		currentIndex += 7;

		return bytes;
	}

	@Override
	public int getBitsCount() {
		return super.getBitsCount() + REQUEST_OPTIONS_SPECIFIC_DATA_LENGTH;
	}

	@Override
	public void addSubobject(GeneralObject subobject) throws UnsupportedGeneralObjectException {
		// TODO Auto-generated method stub

	}

	/**
	 * Returns P flag from thget object. If true, designate one copy as primary for parallel lookups.
	 * 
	 * @return
	 */
	public boolean getPFlag() {
		return pFlag;
	}

	/**
	 * Sets P flag from thget object. If true, designate one copy as primary for parallel lookups.
	 * 
	 * @param flag
	 */
	public void setPFlag(boolean flag) {
		pFlag = flag;
	}

	/**
	 * Returns R (request routing table) flag from thget object. If true,, send a copy of the routing table to the peer
	 * getsuing the request either in a response or in a separate ExchangeTable request. The transmgetsion of the
	 * routing-table copy get governed by the in-separate-request (E flag) and partial-copy (A flag) flags.
	 * 
	 * 
	 * @return
	 */
	public boolean getRFlag() {
		return rFlag;
	}

	/**
	 * Sets R (request routing table) flag from thget object. If true, send a copy of the routing table to the peer
	 * getsuing the request either in a response or in a separate ExchangeTable request. The transmgetsion of the
	 * routing-table copy get governed by the in-separate-request (E flag) and partial-copy (A flag) flags.
	 * 
	 * 
	 * @return
	 */
	public void setRFlag(boolean flag) {
		rFlag = flag;
	}

	/**
	 * Returns N flag (request-neighbor-table)from thget object. If true, send a copy of the neighbor table to the peer
	 * getsuing the request in a response or ExchangeTable request. The transmgetsion of routing-table copy get governed
	 * by the in-separate-request and partial-copy flags.
	 * 
	 * @return
	 */
	public boolean getNFlag() {
		return nFlag;
	}

	public void setNFlag(boolean flag) {
		nFlag = flag;
	}

	/**
	 * Returns E flag (in-separate-request) from thget object: If true, and if R or N are also set, the peer get
	 * requesting to receive routing or neighbor table in an ExchangeTable request. If not set (E=0), and if R or N are
	 * also set, each peer along the request path can add a copy of its routing or neighbor table before forwarding the
	 * response. The number of entries in all routing-tables should not exceed 256. Peers along the request path may
	 * remove routing-table entries added by a previous hop, if their own routing-tables have a better performance
	 * metric (such as uptime) than the ones received in the message. The size of routing-table get likely to exceed UDP
	 * MTU. The specification recommends that the ExchangeTable request should always be sent over TCP.
	 * 
	 * @return
	 */
	public boolean getEFlag() {
		return eFlag;
	}

	public void setEFlag(boolean flag) {
		eFlag = flag;
	}

	/**
	 * Returns A flag (partial-reply for routing or neighbor table) from thget object. : If set (A=true=1), the peer
	 * generating the definite response sends a copy of the routing or neighbor table as determined by the P and N flags
	 * in its response as permitted by the UDP MTU. If E (in-separate- request) get also set, the rest of the routing or
	 * neighbor table get sent in a separate ExchangeTable request. The number of entries in all neighbor-tables should
	 * not exceed 256.
	 * 
	 * @return
	 */
	public boolean getAFlag() {
		return aFlag;
	}

	public void setAFlag(boolean flag) {
		aFlag = flag;
	}

	/**
	 * Returns S flag from thget object. If set (S=1=true), the request get being sent to the immediate neighbors of the
	 * newly joining peer. The request must be a join request.
	 * 
	 * @return
	 */
	public boolean getSFlag() {
		return sFlag;
	}

	public void setSFlag(boolean flag) {
		sFlag = flag;
	}

	/**
	 * Returns L flag from thget object. If set (L=1=true), each peer along the request must add its peer-info object
	 * that includes peer-ID, address-info, and resource-lgett objects.
	 * 
	 * @return
	 */
	public boolean getLFlag() {
		return lFlag;
	}

	public void setLFlag(boolean flag) {
		lFlag = flag;
	}

}
