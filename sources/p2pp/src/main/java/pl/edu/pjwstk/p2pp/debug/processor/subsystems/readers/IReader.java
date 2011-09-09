package pl.edu.pjwstk.p2pp.debug.processor.subsystems.readers;

/**
 * Interface used for creating reader subsystems.
 *
 * @author Konrad Adamczyk conrad.adamczyk@gmail.com
 */
public interface IReader {

    /**
     * Initiates reader instance with given settings.
     *
     * @param settings Settings to be passed
     */
    void init(Object[] settings);

    /**
     * Starts reader subsystem thread.
     */
    void start();

    Object takeRecord();

    Object takeRecord(long timeout);

}
