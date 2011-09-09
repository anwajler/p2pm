package pl.edu.pjwstk.mteam.pubsub.util.persistence;

import pl.edu.pjwstk.mteam.pubsub.util.persistence.db.DBTopicStateLogger;
import pl.edu.pjwstk.mteam.pubsub.util.persistence.dummy.DummyTopicStateLogger;
import pl.edu.pjwstk.mteam.pubsub.util.persistence.dummy.DummyTransactionStateLogger;

/**
 *
 * @author Piotr Bucior
 * @version 1.0
 */
public class StateLoggerManager {
    private static TransactionStateLogger ts_logger;
    private static TopicStateLogger t_logger;

    public static TopicStateLogger getTopicStateLogger(){
        if(t_logger==null){
            //t_logger = new DBTopicStateLogger();
            t_logger = new DummyTopicStateLogger();
        }
        return t_logger;
    }

    public static TransactionStateLogger getTransactionStateLogger(){
        if(ts_logger==null){
            ts_logger = new DummyTransactionStateLogger();
        }
        return ts_logger;
    }

}
