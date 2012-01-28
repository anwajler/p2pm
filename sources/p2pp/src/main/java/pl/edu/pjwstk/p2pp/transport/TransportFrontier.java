package pl.edu.pjwstk.p2pp.transport;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.p2pp.messages.Message;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class TransportFrontier {

    public static final Logger LOG = Logger.getLogger(TransportFrontier.class);

    private ConcurrentHashMap<String, LinkedList<Message>> frontier = new ConcurrentHashMap<String, LinkedList<Message>>();

    final private CopyOnWriteArrayList<String> addresses = new CopyOnWriteArrayList<String>();

    private AtomicInteger size = new AtomicInteger(0);


    public void add(Message message) {

        if (message == null) {
            LOG.warn("Trying to add null message");
            return;
        }

        String receiverAddress = message.getReceiverAddress();

        synchronized (this.addresses) {

            if (!this.frontier.containsKey(receiverAddress)) {
                this.frontier.put(receiverAddress, new LinkedList<Message>());
            }

            if (!this.addresses.contains(receiverAddress)) {
                this.addresses.add(receiverAddress);
            }
            //sometimes calledMethod removes below address from frontier, below 
            // code moves to synchronized block
            this.size.incrementAndGet();
            this.frontier.get(receiverAddress).add(message);

        }

        

    }

    private String getRandomAddress() {

        String randomAddress;

        synchronized (this.addresses) {
            int addressesSize = this.addresses.size();
            if (addressesSize == 0) return null;
            randomAddress = this.addresses.get(((int) (Math.random() * Integer.MAX_VALUE)) % addressesSize);
        }

        return randomAddress;
    }

    private Message pollMessage(String address) {

        if (address == null) return null;
        Queue<Message> queue = this.frontier.get(address);
        if (queue == null) return null;

        Message message;
        synchronized (queue) {
            message = queue.poll();
            if (queue.size() < 1) {
                synchronized (this.addresses) {
                    this.frontier.remove(address);
                    this.addresses.remove(address);
                }
            }
        }

        this.size.decrementAndGet();
        return message;
    }

    public Message poll() {
        if (this.size.intValue() < 1) return null;

        String address = this.getRandomAddress();

        return this.pollMessage(address);
    }

}
