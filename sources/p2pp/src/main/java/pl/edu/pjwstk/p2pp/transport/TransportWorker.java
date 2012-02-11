package pl.edu.pjwstk.p2pp.transport;

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import pl.edu.pjwstk.net.proto.ProtocolControl;
import pl.edu.pjwstk.net.proto.ProtocolReader;
import pl.edu.pjwstk.net.proto.ProtocolWriter;
import pl.edu.pjwstk.net.proto.SendTask;
import pl.edu.pjwstk.p2pp.messages.MalformedP2PPMessageException;
import pl.edu.pjwstk.p2pp.messages.Message;
import pl.edu.pjwstk.p2pp.messages.NonInterpretedMessage;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;
import pl.edu.pjwstk.p2pp.util.AbstractMessageFactory;

public class TransportWorker<ProtocolWorkerObject extends ProtocolControl & ProtocolReader & ProtocolWriter> implements ProtocolControl{
	private final static Logger LOG = Logger.getLogger(TransportWorker.class);

    private final ThreadPoolExecutor sendExecutor = new ThreadPoolExecutor(1, 1, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1000));

	protected Vector<AbstractMessageFactory> messageFactories = new Vector<AbstractMessageFactory>();
	//private MessageStorage messageStorage;
	private ProtocolWorkerObject protocolObject;
	private byte sourceIDLength = 4;



	public TransportWorker(ProtocolWorkerObject protocolObject){
		this.protocolObject = protocolObject;
        this.sendExecutor.prestartAllCoreThreads();
	}

	public Message OnReceived() throws IOException{
		Vector<Message> messages = ReceiveMessages();
		if (messages.isEmpty()){
			return null;
		} else if (messages.size() == 1){
			return messages.firstElement();
		} else {
			throw(new IOException("Received message was refactored to more than one message"));
		}
	}

	public Vector<Message> ReceiveMessages(){
		Vector<Message> messages = new Vector<Message>();

		try {
			NonInterpretedMessage receivedMessage = protocolObject.ReceiveMessage();
            if (receivedMessage != null) {
                if (LOG.isDebugEnabled()) LOG.debug("Received message=" + receivedMessage.toString() + " size=" + receivedMessage.getMessageBody().length);
                for (AbstractMessageFactory currentFactory : messageFactories) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Analyzing message in " + currentFactory.getClass().getSimpleName());
                    }
                    Message message = currentFactory.interpret(receivedMessage.getMessageBody(), sourceIDLength);
                    if (message != null) {

                        // sets parameters of received message
                        message.setSenderAddress(receivedMessage.getSenderAddress());
                        if (!protocolObject.isReliable()) message.setSenderPort(receivedMessage.getSenderPort());
                        message.setEncrypted(receivedMessage.isEncrypted());
                        message.setOverReliable(receivedMessage.isOverReliable());

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Received message was transformed to " + message.getClass() + " . Message = " + message.toString());
                        }
                        messages.add(message);

                    } else {
                        LOG.warn("Received bad massage: " + receivedMessage);
                    }
                }
            }/* else {
                if (logger.isTraceEnabled()) logger.trace("Protocol reader returned a null message");
            }*/
		} catch (IOException e) {
			// TODO what I need to do when I can't receive anything?
			LOG.error("Reader event", e);
		} catch (MalformedP2PPMessageException e) {
			LOG.error("Reader received a malformed P2PP message", e);
		} catch (UnsupportedGeneralObjectException e) {
			LOG.error("Unsupported message", e);
		} catch (Throwable e) {
            LOG.error("Error while receiving message", e);
        }
		return messages;
	}

	public boolean OnSend(Message message){
		if (LOG.isDebugEnabled()) {
            LOG.debug("Transport object sending message to peer " + message.getReceiverAddress() + ":" + message.getReceiverPort());
        }
		/*boolean success = protocolObject.SendMessage(message);
        if (success) {
            if (LOG.isDebugEnabled()) {
                StringBuilder strb = new StringBuilder("Transport object sends message ");
                strb.append(message.getClass().toString()).append(" correctly to ").append(message.getReceiverAddress()).append(":");
                strb.append(message.getReceiverPort());
                LOG.debug(strb.toString());
            }
        } else {
            StringBuilder strb = new StringBuilder("Could not send message ");
            strb.append(message.getClass().toString()).append(" to ").append(message.getReceiverAddress()).append(":");
            strb.append(message.getReceiverPort());
            LOG.warn(strb.toString());
        }*/
        this.sendExecutor.submit(new SendTask(this.protocolObject, message));
		return true;
	}

	public void addMessageFactory(AbstractMessageFactory newFactory) {
		messageFactories.add(newFactory);
	}

    public void setSourceIDLength(byte sourceIDLength) {
        this.sourceIDLength = sourceIDLength;
    }

	public boolean isMessageStateMachine() {
		return this.protocolObject.isMessageStateMachine();
	}

	public boolean isReliable() {
		return this.protocolObject.isReliable();
	}

    public boolean isEncrypted() {
        return this.protocolObject.isEncrypted();
    }

	public boolean isWorkerReady() {
		return this.protocolObject.isWorkerReady();
	}
}
