package pl.edu.pjwstk.mteam.pubsub.util.persistence.dummy;

import java.util.Enumeration;
import java.util.Hashtable;
import pl.edu.pjwstk.mteam.pubsub.core.Transaction;
import pl.edu.pjwstk.mteam.pubsub.util.persistence.TransactionStateLogger;

/**
 *
 * @author Piotr Bucior
 */
public class DummyTransactionStateLogger implements TransactionStateLogger{

    private final Hashtable<Integer,Transaction> pendingTransactions;
    private final Hashtable<Integer,Transaction> completedTransactions;
    private final Hashtable<Integer,Transaction> cancelledTransactions;

    public DummyTransactionStateLogger(){
        this.pendingTransactions = new Hashtable<Integer, Transaction>(100);
        this.completedTransactions = new Hashtable<Integer, Transaction>(100);
        this.cancelledTransactions = new Hashtable<Integer, Transaction>(100);
    }
    
    public boolean addTransaction(Transaction t) {
        log.debug("adding transaction with ID: "+t.toString());
        if(this.pendingTransactions.containsKey(t.getID())){
            log.fatal("Transacation with id: "+t.getID()+" already exist in pending queue");
            throw new IllegalArgumentException("Transaction exists");
        }else{
            this.pendingTransactions.put(t.getID(), t);
            return true;
        }
    }

    public Transaction getTransaction(int tID) {
        Transaction t = this.pendingTransactions.get(tID);
        if(t == null){
            t = this.completedTransactions.get(tID);
            if(t==null){
                log.fatal("Transaction with ID: "+tID+" doesn't exist!");
                throw new IllegalArgumentException("Transaction doesn't exist");
            }
        }
        return t;
    }

    public boolean markTransactionEnd(int tID, byte code) {
        Transaction t = this.pendingTransactions.get(tID);        
        if(t == null){
                log.fatal("Transaction with ID: "+tID+" doesn't exist!");
                throw new IllegalArgumentException("Transaction doesn't exist");
        }else{
            t.terminateTransaction(code);
            log.trace("Transaction: "+tID+" marked with code:"+code);
            this.pendingTransactions.remove(tID);
            if((code&Transaction.COMPLETED)==Transaction.COMPLETED){
                this.completedTransactions.put(tID, t);
            }
            else if((code&Transaction.CANCELLED)==Transaction.CANCELLED){
                this.cancelledTransactions.put(tID, t);
            }
            log.trace("Pending transactions count: "+this.pendingTransactions.size());
            log.trace("Completed transactions count: "+this.completedTransactions.size());
            return true;
        }
    }

    public Enumeration<Transaction> getPendingTransactions() {
        return this.pendingTransactions.elements();
    }

}
