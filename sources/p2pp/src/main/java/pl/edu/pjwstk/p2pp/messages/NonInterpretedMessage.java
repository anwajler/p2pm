package pl.edu.pjwstk.p2pp.messages;

import pl.edu.pjwstk.p2pp.debug.DebugFields;
import pl.edu.pjwstk.p2pp.debug.DebugInformation;

/**
 * @author Robert
 *
 */
public class NonInterpretedMessage extends Message {
	
	private byte[] messageBody;
	public NonInterpretedMessage() {
		super();
	}

	public NonInterpretedMessage(boolean isEncrypted, boolean isOverReliable) {
		super(isEncrypted, isOverReliable);
	}

	public NonInterpretedMessage(String receiverAddress, int receiverPort,
			String senderAddress, int senderPort, boolean isOverReliable,
			boolean isEncrypted) {
		super(receiverAddress, receiverPort, senderAddress, senderPort,
				isOverReliable, isEncrypted);
	}

	public NonInterpretedMessage(String receiverAddress, int receiverPort,
			String senderAddress, int senderPort, boolean isOverReliable,
			boolean isEncrypted, byte[] messageBody) {
		super(receiverAddress, receiverPort, senderAddress, senderPort,
				isOverReliable, isEncrypted);
		this.messageBody = messageBody; 
	}

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

	/**
	 * @param messageBody the messageBody to set
	 */
	public void setMessageBody(byte[] messageBody) {
		this.messageBody = messageBody;
	}

	/**
	 * @return the messageBody
	 */
	public byte[] getMessageBody() {
		return messageBody;
	}

}
