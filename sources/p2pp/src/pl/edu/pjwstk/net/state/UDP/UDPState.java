/**
 * 
 */
package pl.edu.pjwstk.net.state.UDP;

import java.util.Arrays;
import java.util.Vector;

import pl.edu.pjwstk.net.state.ProtocolStateMachine;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.state.UDP
 */
public enum UDPState implements ProtocolStateMachine{
	FAILURE,
	TERMINATED,
	WAIT_RESP(new UDPState[]{UDPState.TERMINATED,UDPState.FAILURE}),
	TRANS_MSG(new UDPState[]{UDPState.TERMINATED,UDPState.FAILURE}),
	INITIAL(new UDPState[]{UDPState.TRANS_MSG}),
	;
	private Vector<ProtocolStateMachine> allowedStates = new Vector<ProtocolStateMachine>();
	private boolean isFinish;
	
	UDPState(){
		setFinish(true);
	}

	UDPState(ProtocolStateMachine[] validStates){
		if (validStates != null && validStates.length > 0) {
			setFinish(false);
            this.allowedStates.addAll(Arrays.asList(validStates));
		} else {
			setFinish(true);
		}
	}
	/* (non-Javadoc)
	 * @see pl.edu.pjwstk.net.state.ProtocolState#isValid(pl.edu.pjwstk.net.state.ProtocolState)
	 */
	public boolean isValid(ProtocolStateMachine newState) {
		for (ProtocolStateMachine currentState : allowedStates){
			if (currentState == newState) {
				return true;
			}
		}
		return false;
	}
	/**
	 * @param isFinish the isFinish to set
	 */
	private void setFinish(boolean isFinish) {
		this.isFinish = isFinish;
	}
	/**
	 * @return the isFinish
	 */
	public boolean isFinish() {
		return isFinish;
	}
}
