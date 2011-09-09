/**
 * Contains objects associated with publish-subscribe Algorithm component.
 */
package pl.edu.pjwstk.mteam.pubsub.algorithm;

/**
 * Interface defining method for choosing appropriate publish-subscribe algorithm
 * for particular overlay type and registering algorithm-compatible message formats.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public interface AlgorithmConfigurator{
	/**
	 * Value indicating Kademlia network usage.
	 */
	public final String P2P_KADEMLIA = "Kademlia";
	
	/**
	 * Invoked by Publish-Subscribe Manager. It chooses appropriate algorithm for 
	 * specified overlay type and registers appropriate message classes.
	 * @param p2pAlgorithmName Name of overlay routing algorithm. Possible values are:
	 *                         <li>{@link #P2P_KADEMLIA},
	 * @return Chosen Algorithm object.
	 */
	public CustomizableAlgorithm chooseAlgorithm(String p2pAlgorithmName);
	
}
