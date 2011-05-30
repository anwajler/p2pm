/**
 * 
 */
package pl.edu.pjwstk.net.state.UDP;

import pl.edu.pjwstk.net.state.ProtocolStateMachine;
import pl.edu.pjwstk.net.state.ProtocolState;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.state.UDP
 */
public class UDPStateMachine implements ProtocolState {

	public boolean ChangeState(ProtocolStateMachine newState) {
		return false;
	}

}
