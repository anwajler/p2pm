/**
 * Contains objects implementing publish-subscribe topology layer.
 */
package pl.edu.pjwstk.mteam.pubsub.topology;

import pl.edu.pjwstk.mteam.pubsub.core.CoreAlgorithm;

/**
 * Abstract class describing publish-subscribe topology.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public abstract class TopologyManager implements TopologyMessageInterface{
	/**
	 * Publish-Subscribe Manager, this topology is associated with.
	 */
	protected CoreAlgorithm pubsubManager;
	
	/**
	 * Creates Topology Manager object.
	 * @param pubsubMngr Publish-Subscribe Manager, this topology is associated with.
	 */
	protected TopologyManager(CoreAlgorithm pubsubMngr){
		pubsubManager = pubsubMngr;
		pubsubMngr.setTopology(this);
	}
}
