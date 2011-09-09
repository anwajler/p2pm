package pl.edu.pjwstk.p2pp.ice;

import pl.edu.pjwstk.p2pp.objects.AddressInfo;

/**
 * Interface for objects that want to know of communication with STUN server.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public interface STUNCommunicationListener {

	/**
	 * Method invoked when there was an error during communication with STUN server.
	 * 
	 * @param errorCode
	 *            Code of an error. Defined in STUNService object.
	 */
	public void onError(int errorCode);

	/**
	 * Method invoked when server-reflexive (as defined in P2PP and STUN specification) address was determined.
	 * 
	 * @param addressInfo
	 *            AddressInfo object containing information about server reflexive address determined during
	 *            communication with STUN server.
	 */
	public void onAddressDetermined(AddressInfo addressInfo);

}
