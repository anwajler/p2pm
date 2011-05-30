/**
 * 
 */
package pl.edu.pjwstk.net.proto;

import pl.edu.pjwstk.types.ExtendedBitSet;

/**
 * Enumerator describes supported protocols.
 * @author Robert Strzelecki robert.strzelecki@codearch.eu
 *
 */
public enum SupportedProtocols {
	UDP("UDP", 17),
	TCP("TCP", 6),
    TCP_TLS("TCP_TLS", 0),
    TCP_SSL("TCP_SSL",0 );
	//private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SupportedProtocols.class);
	/**
	 * Name of the protocol
	 */
	private final String name;

	private final ExtendedBitSet protocolCodeEBS;
	
	private final boolean active; 
	/**
	 * @param name - name of the protocol
	 */
	SupportedProtocols(String name, int protocolCode){
		this.name = name;
		protocolCodeEBS = new ExtendedBitSet("",32,false,true);
		if (protocolCode != 0) {
			ExtendedBitSet tmpEBS = new ExtendedBitSet(Integer.toBinaryString(protocolCode),8,false,true);
			protocolCodeEBS.set(0, tmpEBS);
		}
		active = (protocolCode != 0);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
        @Override
	public String toString(){
		return this.name;
	}
    
    public ExtendedBitSet getProtocolCode(){
    	return protocolCodeEBS;
    }
    
    public boolean isActive(){
    	return this.active;
    }
}
