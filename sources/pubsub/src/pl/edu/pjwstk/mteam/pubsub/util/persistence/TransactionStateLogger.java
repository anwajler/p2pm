
package pl.edu.pjwstk.mteam.pubsub.util.persistence;

import java.util.Enumeration;
import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.pubsub.core.Transaction;

/**
 *
 * @author Piotr Bucior
 */
public interface TransactionStateLogger {
    public static Logger log = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.util.TransactionStateLogger");
    public boolean addTransaction(Transaction t);
    public Transaction getTransaction(int tID);
    public boolean markTransactionEnd(int tID, byte code);
    public Enumeration<Transaction> getPendingTransactions();
    
}
