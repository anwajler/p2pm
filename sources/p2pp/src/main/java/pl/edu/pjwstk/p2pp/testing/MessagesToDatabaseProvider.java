package pl.edu.pjwstk.p2pp.testing;

import java.io.IOException;
import java.sql.SQLException;

import pl.edu.pjwstk.p2pp.transport.MessageStorage;

/**
 * Superclass for all the classes that want to send messages contained in MessageStorage class. Where, how and what to
 * send depends on subclass.
 * 
 * @author szeldon
 * @see MessageStorage
 */
public abstract class MessagesToDatabaseProvider {

	/**
	 * Message storage that will provide messages that will be send.
	 */
	protected MessageStorage messageStorage;

	protected String databaseAddress;
	protected int databasePort;
	protected String login;
	protected String password;

	/**
	 * 
	 * @param databaseAddress
	 * @param databasePort
	 * @param login
	 * @param password
	 */
	public MessagesToDatabaseProvider(String databaseAddress, int databasePort, String login, String password) {
		this.databaseAddress = databaseAddress;
		this.databasePort = databasePort;
		this.login = login;
		this.password = password;
	}

	/**
	 * Sets message storage that will provide messages that will be send.
	 * 
	 * @param messageStorage
	 */
	public void setMessageStorage(MessageStorage messageStorage) {
		this.messageStorage = messageStorage;
	}

	/**
	 * Method invoked when provider should send all messages from message storage to database. After that, all the
	 * messages are deleted from storage.
	 * 
	 * @throws IOException
	 *             Thrown when there was a problem while connecting to Database.
	 * @throws SQLException
	 */
	public abstract void sendMessagesToDatabase() throws IOException, SQLException;
}
