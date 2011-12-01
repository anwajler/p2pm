package pl.edu.pjwstk.mteam.jcsync.collections;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncMethod;

/**
 * Collection listener, below you can see how to get information about invoked method:
 * <pre>
 * {@code 
 * 
 *      // collection ID
 *      String collectionID = collection.getcollectionID().getID();
        
 *      // if invoked method is a "WRITE" type            
        if(details.getType()==JCSyncMethod.WRITE_OPERATION){
 * 
 *          //the name of invoked method
            String methodName = ((JCSyncWriteMethod)details).getGenericMethodName();
 * 
 *          //arguments delivered to invoke method
            Parameter[] params = ((JCSyncWriteMethod)details).getParameters();
 * 
 *          //the results of invoked method
            Object returnedValue = retVal;
	}
 * }
 * </pre>
 * @author Piotr Bucior
 * @version 1.0
 */
public interface JCSyncCollectionStateListener {

	/**
	 * Called when invoked method was initialized by local user
	 * @param collection collection on which method was invoked
	 * @param details set of update information
         * @param retVal the result of update (returned value from invoked method)
	 */
	public void onRemoteStateUpdated(JCSyncAbstractCollection collection, JCSyncMethod details, Object retVal);
        /**
	 * Called when invoked method was delivered from PUB-SUB layer
	 * @param collection collection on which method was invoked
	 * @param details set of update information
         * @param retVal the result of update (returned value from invoked method)
	 */
        public void onLocalStateUpdated(JCSyncAbstractCollection collection, JCSyncMethod details, Object retVal);

}