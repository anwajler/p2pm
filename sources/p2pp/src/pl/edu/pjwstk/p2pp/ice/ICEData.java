package pl.edu.pjwstk.p2pp.ice;

import pl.edu.pjwstk.p2pp.objects.AddressInfo;

/**
 * Object containing data gathered by ICE protocol.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class ICEData {

	/**
	 * Constant for no NAT.
	 */
	public static final int NO_NAT_NAT_TYPE = 0;

	/**
	 * Constant for full-cone NAT.
	 */
	public static final int FULL_CONE_NAT_TYPE = 1;

	// TODO add more NAT types

	/**
	 * NAT type discovered by ICE.
	 */
	private int natType;

	/**
	 * Server reflexive address. TODO I'm not sure if that's the best object because it is from P2PP. Maybe in ICE it
	 * contains some additional info or something.
	 */
	private AddressInfo serverReflexiveAddress;

	/**
	 * Returns NAT type. NAT type is described by a constant of this class (for example
	 * 
	 * @return
	 */
	public int getNatType() {
		return natType;
	}

	public void setNatType(int natType) {
		this.natType = natType;
	}

	/**
	 * Returns server reflexive address. TODO I'm not sure if that's the best object because it is from P2PP. Maybe in
	 * ICE it contains some additional info or something.
	 */
	public AddressInfo getServerReflexiveAddress() {
		return serverReflexiveAddress;
	}

	public void setServerReflexiveAddress(AddressInfo serverReflexiveAddress) {
		this.serverReflexiveAddress = serverReflexiveAddress;
	}

}
