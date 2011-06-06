package pl.edu.pjwstk.mteam.jcsync.persistence;

import pl.edu.pjwstk.mteam.jcsync.persistence.dummy.DummyCollectionStateLogger;
import pl.edu.pjwstk.mteam.pubsub.util.persistence.TopicStateLogger;
import pl.edu.pjwstk.mteam.pubsub.util.persistence.TransactionStateLogger;

/**
 *
 * @author Piotr Bucior
 * @version 1.0
 */
public class StateLoggerManager extends pl.edu.pjwstk.mteam.pubsub.util.persistence.StateLoggerManager {
    private static CollectionStateLogger colls_logger;
    public static TopicStateLogger getTopicStateLogger(){
        return pl.edu.pjwstk.mteam.pubsub.util.persistence.StateLoggerManager.getTopicStateLogger();
    }
    public static TransactionStateLogger getTransactionStateLogger(){
        return pl.edu.pjwstk.mteam.pubsub.util.persistence.StateLoggerManager.getTransactionStateLogger();
    }
    public static CollectionStateLogger getCollectionStateLogger(){
        if(colls_logger ==null){
            colls_logger = new DummyCollectionStateLogger();
        }
        return colls_logger;
    }

}
