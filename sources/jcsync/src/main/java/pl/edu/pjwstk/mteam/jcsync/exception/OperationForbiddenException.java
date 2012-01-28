
package pl.edu.pjwstk.mteam.jcsync.exception;

/**
 * Throws then called operation is not allowed for the operation publisher.
 * @author Piotr Bucior
 */
public class OperationForbiddenException extends Exception{
    
    private static final OperationForbiddenException instance = new OperationForbiddenException();
    private OperationForbiddenException(){
        super("Operation forbidden");
    }
    public static OperationForbiddenException instance(){
        return instance;
    }
}
