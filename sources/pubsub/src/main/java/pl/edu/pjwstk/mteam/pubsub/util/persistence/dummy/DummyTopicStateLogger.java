package pl.edu.pjwstk.mteam.pubsub.util.persistence.dummy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.message.indication.NotifyIndication;
import pl.edu.pjwstk.mteam.pubsub.util.persistence.TopicStateLogger;

/**
 *
 * @author Piotr Bucior
 * @version 1.0
 */
public class DummyTopicStateLogger implements TopicStateLogger {

    private final ConcurrentHashMap<String, WeakHashMap<Integer, NotifyIndication>> historyIndication;
    private final ConcurrentHashMap<String, Topic> topics;

    public DummyTopicStateLogger() {
        this.historyIndication = new ConcurrentHashMap<String, WeakHashMap<Integer, NotifyIndication>>();
        this.topics = new ConcurrentHashMap<String, Topic>();
    }

    @Override
    public boolean onDeliverPublishIndication(String nodeName, int operationID, NotifyIndication ni) {
        String topicID = ni.getTopicID();
        boolean retVal = false;
        if (!this.historyIndication.containsKey(topicID)) {
            log.fatal("Topic: " + topicID + " does not exists!");
            // there is no needed to throw exception, just log this fact
            //throw new IllegalArgumentException("Topic: "+topicID+" does not exists!");
        }
        WeakHashMap<Integer, NotifyIndication> topicOperation = this.historyIndication.get(topicID);
        if (topicOperation.containsKey(operationID)) {
            retVal = false;
            log.warn(nodeName + ": OperationID for topic: " + topicID + " already exists!: " + operationID);
            //there is no needed to throw exception, just log about this fact
            //throw new IllegalArgumentException("OperationID: "+operationID+" for topic: "+topicID+" already exists!, indication:"+ni);
        } else {
            topicOperation.put(operationID, ni);
            log.trace(nodeName + ":Added publish operation: " + ni);
            retVal = true;
        }
        checkSize(topicOperation);
        return retVal;
    }

    public NotifyIndication getPublishOperation(String topicID, int operationID) {
        if (!this.historyIndication.containsKey(topicID)) {
            log.fatal("Topic: " + topicID + " does not exists!");
            throw new IllegalArgumentException("Topic: " + topicID + " does not exists!");
        }
        WeakHashMap<Integer, NotifyIndication> topicOperation = this.historyIndication.get(topicID);

        if (!topicOperation.containsKey(operationID)) {
            log.fatal("OperationID: " + operationID + "for topic: " + topicID + " does not exist!");
            throw new IllegalArgumentException("OperationID: " + operationID + "for topic: " + topicID + " does not exist!");
        } else {
            return topicOperation.get(operationID);
        }
    }

    @Override
    public void addNewTopic(Topic t) {
        if ((this.topics.putIfAbsent(t.getID(), t)) != null) {
            log.fatal("Topic: " + t.getID() + " already exists!");
            throw new IllegalArgumentException("Topic: " + t.getID() + " already exists!");
        } else {
            this.historyIndication.put(t.getID(), new WeakHashMap<Integer, NotifyIndication>(50));
        }
    }

    @Override
    public Topic getTopic(String topicID) {
        return this.topics.get(topicID);
    }

    @Override
    public ArrayList<NotifyIndication> getPublishOperations(String topicID, int fromID) {
        if (!this.historyIndication.containsKey(topicID)) {
            log.fatal("Topic: " + topicID + " does not exists!");
            throw new IllegalArgumentException("Topic: " + topicID + " does not exists!");
        }
        WeakHashMap<Integer, NotifyIndication> topicOperation = this.historyIndication.get(topicID);
        List<Integer> list = new ArrayList<Integer>();
        list.addAll(topicOperation.keySet());
        Collections.sort(list);
        ArrayList<NotifyIndication> indications = new ArrayList<NotifyIndication>(10);
        if (indications.size() > 0) {
            if (list.indexOf(fromID) == -1) {
                log.fatal("OperationID:" + fromID + "for Topic: " + topicID + " does not exists!");
                return indications;
            }
            for (int i = list.indexOf(fromID); i < list.size(); i++) {
                indications.add(topicOperation.get((list.get(i))));
            }
        }

        return indications;
    }

    public void clearTopicState(String tID) {
    }

    private void checkSize(WeakHashMap<Integer, NotifyIndication> operations) {
        if (operations.size() > 150) {
            List<Integer> list = new ArrayList<Integer>();
            list.addAll(operations.keySet());
            Collections.sort(list);
            for (int i = 0; i < list.size() - 100; i++) {
                operations.remove(list.get(i));
            }
            System.gc();
        }
    }
}
