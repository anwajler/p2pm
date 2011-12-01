package pl.edu.pjwstk.mteam.jcsync.collections;

import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncMethod;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncWriteMethod;

/**
 *
 * @author Piotr Bucior
 */
public interface CollectionMethodInvokingLogic {
    /**
	 * Method must be called before calling any 'read' methods on the collection.
	 * @param method the method which will be invoked on implements collection
	 */
	Object readOperation(JCSyncMethod method);
        /**
         * todo hide methods:
         * readOp.., writeOp... , invokeMethod, 
         */

	/**
	 * Method must be called before calling any 'write' methods on the collection.
	 * @param method the method which will be invoked on implements collection
	 */
	Object writeOperation(JCSyncWriteMethod method) throws Exception;
}
