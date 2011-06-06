package pl.edu.pjwstk.mteam.jcsync.collections;

/**
 * @author Piotr Bucior
 * @version 1.0
 */
abstract class CollectionLocker {

	public CollectionLocker(){

	}

	public void finalize() throws Throwable {

	}
	/**
	 * 
	 * @param collectionID
	 */
	protected abstract void unlock(pl.edu.pjwstk.mteam.pubsub.core.Topic collectionID);

	protected abstract void lock();
}//end CollectionLocker