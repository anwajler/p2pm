package pl.edu.pjwstk.p2pp.transactions;

import pl.edu.pjwstk.p2pp.entities.P2PPEntity;
import pl.edu.pjwstk.p2pp.messages.requests.Request;
import pl.edu.pjwstk.p2pp.messages.responses.Response;

/**
 * Listener of transactions. Has to be implemented by classes that want to be notified about what happened to
 * transaction.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public interface TransactionListener {
	/**
	 * Method invoked when a transaction has ended.
	 * 
	 * @param transactionID
	 *            ID of a transaction about which this notification is.
	 * @param transactionState
	 *            State in which transaction has ended.
	 * @param transactionType
	 *            Type of transaction. Constants in {@link Transaction} class.
	 * @param request
	 *            Request for which the transaction was created (if it is request-type transaction).
	 * @param response
	 *            Response that was received and ended matching transaction. May be null, because transaction might have
	 *            ended without response received
	 * @param transactionTable
	 *            Transaction table that may be used for creating new transaction.
	 * @param node
	 *            Local entity that handles transaction.
	 */
	public abstract void transactionEnded(byte[] transactionID, byte transactionState, byte transactionType,
			Request request, Response response, TransactionTable transactionTable, P2PPEntity node);
}
