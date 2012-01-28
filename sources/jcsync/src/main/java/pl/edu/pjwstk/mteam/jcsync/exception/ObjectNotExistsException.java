package pl.edu.pjwstk.mteam.jcsync.exception;

/**
 *Throws when the application is trying to do some stuff on the shared object
 * or (Topic) but the object doesn't exists in the overlay.
 * @author Piotr Bucior
 */
public class ObjectNotExistsException extends Exception {

    private static final ObjectNotExistsException instance = new ObjectNotExistsException();

    private ObjectNotExistsException() {
        super("Object doesn't exists in the network layer.");
    }

    public static ObjectNotExistsException instance() {
        return instance;
    }
}
