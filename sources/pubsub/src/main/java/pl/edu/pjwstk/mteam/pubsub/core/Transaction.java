/**
 * 
 */
package pl.edu.pjwstk.mteam.pubsub.core;

import java.util.Iterator;
import java.util.Vector;

/**
 * Class representing publish-subscribe transaction. Its instances are created when
 * node sends publish-subscribe request.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl, Piotr Bucior
 */
public class Transaction{
	/**
	 * When new transaction is created this value is increased. Perhaps transaction
	 * IDs should be generated in some more sophisticated way in future.
	 */
	private static int transactionCounter;
	/**
	 * Unique transaction identifier.
	 */
	private Integer id;
	/**
	 * Operations associated with this transaction. There may be more than
	 * one, because topic creation may be associated with the subscription
	 * process.
	 */
	private Vector<Operation> operations;
	
	/**
	 * Topic, this transaction is associated with.
	 */
	private Topic topic;

        protected long start,stop;

        protected boolean isDone = false;

        protected final Object LOCKER = new Object();

        public static final byte COMPLETED = 2;
        public static final byte CANCELLED = 4;
        public static final byte PENDING = 1;
        public static final byte TO_REMOVE = COMPLETED&CANCELLED;

        public byte state = PENDING;
	/**
	 * Creates new transaction.
	 * @param o Operation associated with this transaction.
	 */
	public Transaction(Operation o){
		id = new Integer(generateID());
		operations = new Vector<Operation>();
                if(o!=null)
		operations.add(o);
		topic = new Topic("");
	}
	
	public Transaction(Operation o, Topic t){
		id = new Integer(generateID());
		operations = new Vector<Operation>();
                if(o!=null)
		operations.add(o);
		topic = t;
	}
	
	/**
	 * 
	 * @return Unique transaction identifier.
	 */
	public int getID(){
		return id.intValue();
	}
	
	public Topic getTopic(){
		return topic;
	}
	
	/**
	 * 
	 * @return The first operation associated with this transaction.
	 */
	public Operation getOperation(){
		return operations.get(0);
	}
	
	/**
	 * 
	 * @return Set of operations associated with this transaction.
	 */
	public Vector<Operation> getOperations(){
		return operations;
	}
	
	public void addOperation(Operation o){
		operations.add(o);
	}
	
	/**
	 * Starts the timer associated with the transaction.
	 */
	public void begin(){
		this.start = System.currentTimeMillis();
	}
	
	/**
	 * Stops the timer associated with the transaction.
	 */
//	public void terminate(){
//		synchronized(this.LOCKER){
//                    this.stop = System.currentTimeMillis();
//                    this.isDone = true;
//                }
//	}
        public void terminateTransaction(byte code){
                synchronized(this.LOCKER){
                    this.state = code;
                    this.stop = System.currentTimeMillis();
                    this.isDone = this.state==COMPLETED ? true : false;
                }
        }
        /**
         * check that transaction is completed
         * @return true if transaction is completed, otherwise false
         */
        public boolean isDone(){
            boolean b = false;
            synchronized(this.LOCKER){
                b = this.isDone;
            }
            return b;
        }

        public byte getState(){
            byte st = 0;
            synchronized(this.LOCKER){
                st = this.state;
            }
            return st;
        }
	
	/**
	 * Generates transaction IDs. For now it only increases {@link #transactionCounter}
	 * value. Perhaps IDs should be generated in some more sophisticated way in future.
	 * @return Generated ID.
	 */
	private synchronized static Integer generateID(){
		//TODO: Maybe think of some more sophisticated way of generating IDs.
		if(transactionCounter == Integer.MAX_VALUE)
			transactionCounter = 0;
		else
			transactionCounter++;
		return transactionCounter;
	}
	
	public String toString(){
		String msg = "transaction "+getID();//+" ("+getOperation()+"): ";
		Iterator<Operation> iter = operations.iterator();
		while(iter.hasNext()){
			msg += "'"+PubSubConstants.STR_OPERATION[((Operation)iter.next()).getType()]+"' ";
		}
		return msg;
	}
	
	public boolean equals(Object compareWith){
		Transaction t = (Transaction)compareWith;
		if(getID() == t.getID())
			return true;
		return false;
	}
}
