package pl.edu.pjwstk.net.proto;

/**
 * Interface describes common protocol properties and extensions. 
 * @author Robert Strzelecki robert.strzelecki@codearch.eu
 *
 */
public interface ProtocolControl {
	/**
	 * Determines is protocol is reliable or not. 
	 * @return true - reliable/false - unreliable
	 */
	public boolean isReliable();
	
	/**
	 * Determines is protocol worker has state machine. This future is only for unreliable protocols.
	 * @return true - exists/false - not exists
	 */
	public boolean isMessageStateMachine();
	
	/**
	 * Determines is worker is ready for work
	 * @return true - ready/false - waiting or initializing connection
	 */
	
	public boolean isWorkerReady();
	
}
