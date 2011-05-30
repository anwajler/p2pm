/**
 * 
 */
package pl.edu.pjwstk.net.state;


/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.state
 */
public interface ProtocolState {
	public boolean ChangeState(ProtocolStateMachine newState);
}
