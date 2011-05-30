package pl.edu.pjwstk.p2pp.debug.processor.subsystems.interpreters;

import pl.edu.pjwstk.p2pp.debug.DebugInformation;

/**
 * Interface used for creating interpreter subsystems.
 *
 * @author Konrad Adamczyk conrad.adamczyk@gmail.com
 */
public interface IInterpreter {

    /**
     * Initiates reader instance with given settings.
     *
     * @param settings Settings to be passed
     */
    void init(Object[] settings);

    /**
     * Starts interpreter subsystem thread.
     */
    void start();

    void halt();

    void offerRequest(Object request);

    DebugInformation takeResult();

    DebugInformation takeResult(long timeout);

}
