package pl.edu.pjwstk.mteam.pubsub.util.persistence.dummy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.message.indication.NotifyIndication;
import pl.edu.pjwstk.mteam.pubsub.util.persistence.TopicStateLogger;

/**
 *
 * @author Piotr Bucior
 * @version 1.0
 */
public class DummyTopicStateLogger implements TopicStateLogger{
    private final ConcurrentHashMap<String, ConcurrentHashMap<Integer, NotifyIndication>> historyIndication;
    private final ConcurrentHashMap<String, Topic> topics;

    public DummyTopicStateLogger(){
        this.historyIndication = new ConcurrentHashMap<String, ConcurrentHashMap<Integer, NotifyIndication>>(10);
        this.topics = new ConcurrentHashMap<String, Topic>(10);
    }
    @Override
    public void onDeliverPublishIndication(int operationID, NotifyIndication ni) {
        String topicID = ni.getTopicID();
        if(!this.historyIndication.containsKey(topicID)){
            log.fatal("Topic: "+topicID+" does not exists!");
            throw new IllegalArgumentException("Topic: "+topicID+" does not exists!");
        }
        ConcurrentHashMap<Integer,NotifyIndication> topicOperation = this.historyIndication.get(topicID);
        if((topicOperation.putIfAbsent(operationID, ni))!=null){
            log.fatal("OperationID: "+operationID+"for topic: "+topicID+" already exists!");
            throw new IllegalArgumentException("OperationID: "+operationID+"for topic: "+topicID+" already exists!");
        }
        else{
            log.trace("Added publish operation: "+ni);
        }
    }

    public NotifyIndication getPublishOperation(String topicID,int operationID) {
        if(!this.historyIndication.containsKey(topicID)){
            log.fatal("Topic: "+topicID+" does not exists!");
            throw new IllegalArgumentException("Topic: "+topicID+" does not exists!");
        }
        ConcurrentHashMap<Integer,NotifyIndication> topicOperation = this.historyIndication.get(topicID);

        if(!topicOperation.containsKey(operationID)){
            log.fatal("OperationID: "+operationID+"for topic: "+topicID+" does not exist!");
            throw new IllegalArgumentException("OperationID: "+operationID+"for topic: "+topicID+" does not exist!");
        }
        else{
            return topicOperation.get(operationID);
        }
    }
    @Override
    public void addNewTopic(Topic t) {
        if((this.topics.putIfAbsent(t.getID(), t))!=null){
            log.fatal("Topic: "+t.getID()+" already exists!");
            throw new IllegalArgumentException("Topic: "+t.getID()+" already exists!");
        }
        else{
            this.historyIndication.put(t.getID(), new ConcurrentHashMap<Integer, NotifyIndication>(50));
        }
    }

    @Override
    public Topic getTopic(String topicID) {
        return this.topics.get(topicID);
    }

    @Override
    public ArrayList<NotifyIndication> getPublishOperations(String topicID,int fromID) {
        if(!this.historyIndication.containsKey(topicID)){
            log.fatal("Topic: "+topicID+" does not exists!");
            throw new IllegalArgumentException("Topic: "+topicID+" does not exists!");
        }
        ConcurrentHashMap<Integer,NotifyIndication> topicOperation = this.historyIndication.get(topicID);
        List<Integer> list = Collections.list(topicOperation.keys());
        Collections.sort(list);
        ArrayList<NotifyIndication> indications = new ArrayList<NotifyIndication>(10);
        if(list.indexOf(fromID)==-1){
            log.fatal("OperationID:"+fromID+"for Topic: "+topicID+" does not exists!");
            return indications;
        }
        for(int i = list.indexOf(fromID);i <list.size();i++){
            indications.add(topicOperation.get((list.get(i))));
        }       

        return indications;
    }

    public void clearTopicState(String tID) {
    }

}
