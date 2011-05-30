package pl.edu.pjwstk.net.state;

public interface ProtocolStateMachine {
	boolean isValid(ProtocolStateMachine newState);
}
