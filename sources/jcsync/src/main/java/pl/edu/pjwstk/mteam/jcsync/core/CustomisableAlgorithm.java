package pl.edu.pjwstk.mteam.jcsync.core;

import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncMessage;
import pl.edu.pjwstk.mteam.jcsync.core.pubsubwrapper.JCSyncMessageCarrier;

/**
 * @author Piotr Bucior
 * @version 1.0
 */
public abstract class CustomisableAlgorithm extends AbstractCoreAlgorithm implements AlgorithmInterface {

    protected JCSyncCoreAlgorithm core = null;

    public CustomisableAlgorithm() {
    }

    public void finalize() throws Throwable {
    }

    protected JCSyncCoreAlgorithm getCoreAlgorithm() {
        return this.core;
    }

    public void setCoreAlgorithm(JCSyncCoreAlgorithm alg) {
        this.core = alg;
    }
}//end CustomisableAlgorithm
