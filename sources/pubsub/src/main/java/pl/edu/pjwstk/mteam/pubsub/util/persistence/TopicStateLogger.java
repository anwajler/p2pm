package pl.edu.pjwstk.mteam.pubsub.util.persistence;

import java.util.ArrayList;
import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.message.indication.NotifyIndication;

/**
 *
 * @author Piotr Bucior
 * @version 1.0
 */
public interface TopicStateLogger {
    public static Logger log = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.util.TopicStateLogger");
    public boolean onDeliverPublishIndication(String nodeName,int operationID, NotifyIndication ni);
    //public NotifyIndication getPublishOperation(String topicID,int opID);
    public ArrayList<NotifyIndication> getPublishOperations(String topicID,int fromID);
    public void addNewTopic(Topic t);
    public Topic getTopic(String topicID);
    public void clearTopicState(String tID);

}
