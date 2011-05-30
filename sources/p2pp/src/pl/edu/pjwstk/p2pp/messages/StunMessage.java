package pl.edu.pjwstk.p2pp.messages;

import pl.edu.pjwstk.p2pp.debug.DebugFields;
import pl.edu.pjwstk.p2pp.debug.DebugInformation;

public class StunMessage extends Message {

	@Override
	public byte[] asBytes() {
		// TODO Auto-generated method stub
		return null;
	}

        @Override
        public DebugInformation getDebugInformation() {
            DebugInformation debugInfo = new DebugInformation();
            debugInfo.put(DebugFields.MESSAGE_CLASS, this.getClass().getName());
            return debugInfo;
        }

}
