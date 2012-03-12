package pl.edu.pjwstk.net.proto;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.p2pp.messages.Message;
import pl.edu.pjwstk.p2pp.transactions.Transaction;

public class SendTask extends Thread {

    private static final Logger LOG = Logger.getLogger(SendTask.class);

    private ProtocolWriter writer;
    private final Message message;
    private final Transaction transaction;

    public SendTask(ProtocolWriter writer, Message message, Transaction transaction) {
        this.writer = writer;
        this.message = message;
        this.transaction = transaction;
    }

    public String getMessageReceiverAddress() {
        return this.message.getReceiverAddress() + ":" + this.message.getReceiverPort();
    }

    public void run() {
        try {

            if (LOG.isTraceEnabled()) {
                LOG.trace("SendTask started for " + this.message + " on " + this.writer.getClass().toString());
            }
            boolean success = false;
            try {
                success = this.writer.SendMessage(this.message);
            } catch (NullPointerException e) {
                LOG.error("An error occurred for message: " + this.message.toString(), e);

            }
            if (success) {
                if (LOG.isDebugEnabled()) {
                    StringBuilder strb = new StringBuilder("Transport object sends message ");
                    strb.append(message.getClass().toString()).append(" correctly to ").append(message.getReceiverAddress()).append(":");
                    strb.append(message.getReceiverPort());
                    LOG.debug(strb.toString());
                }
            } else {
                transaction.setState(Transaction.TRANSPORT_FAILURE_STATE);
                StringBuilder strb = new StringBuilder("Could not send message ");
                strb.append(message.getClass().toString()).append(" to ").append(message.getReceiverAddress()).append(":");
                strb.append(message.getReceiverPort());
                LOG.warn(strb.toString());
            }

        } catch (Throwable e) {
            LOG.error("Error while running SendTask for " + this.message + " on " + this.writer.getClass().getSimpleName(), e);
        }
    }


}
