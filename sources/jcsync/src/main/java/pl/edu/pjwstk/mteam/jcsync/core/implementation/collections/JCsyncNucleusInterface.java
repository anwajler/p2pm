package pl.edu.pjwstk.mteam.jcsync.core.implementation.collections;

import java.io.Serializable;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncAbstractSharedObject;

/**
 * The <tt>JCsyncNucleusInterface</tt> provides skeleton functionality of 
 * jcsync mechanism for implemented collections.
 * @author Piotr Bucior
 * @serial 
 */
public interface JCsyncNucleusInterface extends Serializable {
    
    /**
     * Returns a nucleus object associated with current shared object.<br>
     * Typically for implemented collections classes it will returns a super 
     * class of current implementation,for example in the
     * {@link JCSyncArrayList JCSyncArrayList} method looks like below:
     * <pre>
     * <code>
     * public Serializable getNucleus() {
            <strong>return (ArrayList) this;</strong>
       }
     * </code>
     * </pre>
     * 
     * @return
     */
    Serializable getNucleus();    
    
    /**
     * Allows to invoke method delivered from the overlay.
     * @param methodName method name.
     * @param argTypes arguments types.
     * @param argValues arguments values.
     * @return depends on invoked method.
     */
    //TODO this method should be more hidden
    Object invoke(String methodName, Class[]argTypes, Object [] argValues);
    
    /**
     * Informs <tt>JCSyncNucleusInterface</tt> about the associated shared object 
     * is created.
     * @param object shared object related with current <tt>JCSyncNucleusInterface</tt>
     */
    void objectCtreated(JCSyncAbstractSharedObject object);
}
