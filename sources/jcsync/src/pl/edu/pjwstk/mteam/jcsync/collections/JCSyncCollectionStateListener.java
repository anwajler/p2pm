package pl.edu.pjwstk.mteam.jcsync.collections;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncMethod;

/**
 * @author Piotr Bucior
 * @version 1.0
 */
public interface JCSyncCollectionStateListener {

	/**
	 * 
	 * @param collectionID
	 * @param method
	 */
	public void onRemoteStateUpdated(JCSyncAbstractCollection collection, JCSyncMethod details);
        public void onLocalStateUpdated(JCSyncAbstractCollection collection, JCSyncMethod details);

}