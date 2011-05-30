package pl.edu.pjwstk.p2pp.util;

import java.io.InputStream;

import pl.edu.pjwstk.p2pp.messages.MalformedP2PPMessageException;
import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;

/**
 * Class with utility methods for creating P2PP messages using current version of protocol.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public abstract class AbstractMessageFactory {

	/**
	 * Method that reads bytes from given InputStream and creates proper Message for this. There's an assumption that
	 * the first byte read from the given stream is a first byte of message. TODO what happens when corrupted data are
	 * read? Where the stream stays?
	 * 
	 * @param stream
	 *            Stream containing data.
	 * @param sourceIDLength
	 *            Length (in bytes) of sourceID (and responseID if the message is a response).
	 * @return Message read from stream. Null if nothing was read from stream.
	 */
	public abstract P2PPMessage readAndInterpret(InputStream stream, byte sourceIDLength)
			throws MalformedP2PPMessageException, UnsupportedGeneralObjectException;

	/**
	 * Analyzes given data. Data may consist of only common header of P2PP Message or also contain further data. Only
	 * the common header is used. So the returned Message contains only the data that are included in common header.
	 * 
	 * @param data
	 * @param sourceIDLength
	 *            Length of sourceID (in bytes). Used only when given data aren't of bootstrap, authenticate or enroll
	 *            message.
	 * @return Message object but containing only the data that are included in common header.
	 * @throws MalformedP2PPMessageException
	 *             Throws this exception if data are corrupted.
	 */
	public abstract P2PPMessage interpret(byte[] data, byte sourceIDLength) throws MalformedP2PPMessageException,
			UnsupportedGeneralObjectException;

}
