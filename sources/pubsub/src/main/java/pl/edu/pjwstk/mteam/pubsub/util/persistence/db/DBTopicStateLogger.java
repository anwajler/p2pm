package pl.edu.pjwstk.mteam.pubsub.util.persistence.db;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.message.PubSubMessage;
import pl.edu.pjwstk.mteam.pubsub.message.indication.NotifyIndication;

/**
 *
 * @author Piotr Bucior
 */

public class DBTopicStateLogger implements pl.edu.pjwstk.mteam.pubsub.util.persistence.TopicStateLogger {

    /**
     *    
     * @param operationID
     * @param ni
     */
    public void onDeliverPublishIndication(int operationID, NotifyIndication ni) {
        log.trace("Storing NotifyIndication: "+ni.toString());
        try{
        DBConnection.getConnection().insertPublishNotify(operationID, ni.getTopicID(),ni.getEventType(), ni.isHistorical(),new String(ni.getMessage()),ni.getPublisher(),ni.encode());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public NotifyIndication getPublishOperation(String topicID, int opID) {
        return null;
    }

    public ArrayList<NotifyIndication> getPublishOperations(String topicID,int fromID) {
        ArrayList<NotifyIndication> result = new ArrayList<NotifyIndication>();
        NotifyIndication ni = null;
        try {
            ResultSet rs = DBConnection.getConnection().getPublishNotifyIndication(fromID);
            while(rs.next()){
                ni = (NotifyIndication) PubSubMessage.parseMessage(rs.getBytes(7));
                result.add(ni);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return result;
    }
    private ByteArrayOutputStream bostr;
    private ObjectOutputStream oostr;
    
    public synchronized void addNewTopic(Topic t) {
        try {
            //TODO Piotr Bucior - topic.getOwner() returns null
            DBConnection.getConnection().addTopic(t.getID(), /*t.getOwner().getNodeInfo().getName()*/"",t.getCurrentOperationID());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
//        finally{
//            try {
//                bostr.close();
//            } catch (IOException ex) {
//
//            }
//            bostr = null;
//            oostr = null;
//        }
    }

    public Topic getTopic(String topicID) {
        try{
        return DBConnection.getConnection().getTopic(topicID);
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void clearTopicState(String tID) {
        try{
        DBConnection.getConnection().clearTopicState(tID);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

