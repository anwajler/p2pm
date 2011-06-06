package pl.edu.pjwstk.mteam.core;

/**
 * Object providing node with pub-sub functionalities.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public abstract class AbstractCoreAlgorithm{
	/**
	 * Node this pub-sub manager is associated with.
	 */
	private Node p2pnode;
	
	protected AbstractCustomizableAlgorithm algorithm;
	
	protected AbstractCoreAlgorithm(Node n){
		p2pnode = n;
		p2pnode.setPubSubCoreAlgorithm(this);
	}
	
	public Node getNode(){
		return p2pnode;
	}
	
	public AbstractCustomizableAlgorithm getCustomizableAlgorithm(){
		return algorithm;
	}
}
