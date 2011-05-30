package pl.edu.pjwstk.p2pp.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Class used for reading settings from settings file.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class SettingsReader {

	/**
	 * Returns hash algorithm ID as used in P2PPConstants.
	 * 
	 * @return
	 */
	public byte getHashAlgorithm() {
		return hashAlgorithm;
	}

	/** Constant for working on bootstrap mode. */
	public static final int BOOTSTRAP_MODE = 1;
	/** Constant for working on enrollment and authentication mode. */
	public static final int E_AND_A_MODE = 2;
	/** Constant for working on peer mode. */
	public static final int PEER_MODE = 4;
	/** Constant for working on client mode. */
	public static final int CLIENT_MODE = 8;

	/** Name of file with settings. */
	private String filename;

	/** TCP port that this P2PP entity uses. */
	private int tcpPort = -1;

	/** UDP port that this P2PP entity uses. */
	private int udpPort = -1;

	/** Protocol name. */
	private String protocolName;

	/** Hash algorithm name. */
	private String hashAlgorithmName;

	/** Hash size (in bytes). */
	private int hashSize;

	/** True if recursive routing is used. False if iterative. */
	private boolean isRecursive;

	/** True if parallel. False otherwise. */
	private boolean isParallel;

	/** Hash base. */
	private int hashBase;

	/**
	 * Mode that this P2PP entity will work on. May contain many modes at once. Using AND on this mode and constants for
	 * modes gives information about current modes.
	 */
	private int mode;

	/** DTLS port to be used. */
	private int dtlsPort;
	/** TLS port to be used. */
	private int tlsPort;

	@SuppressWarnings("unused")
	private byte[] unhashedID;
	private byte hashAlgorithm;
	private byte protocol;

	public byte[] getUnhashedID() {
		return "szeldon".getBytes();
	}

	/**
	 * Creates reader of settings for given filename.
	 * 
	 * @param filename
	 *            Name of the file with settings of P2PP program.
	 */
	public SettingsReader(String filename) {
		this.filename = filename;
	}

	/**
	 * Reads settings from a file given in constructor. TODO now everything is static (always peer mode).
	 * 
	 * @throws FileNotFoundException
	 *             Thrown when there's no settings file with given name.
	 * @throws IOException
	 *             Thrown when there was an exception while reading from settings file.
	 */
	public void readSettings() throws FileNotFoundException, IOException {

		FileReader fileReader;

		fileReader = new FileReader(filename);
		BufferedReader reader = new BufferedReader(fileReader);

		String line = null;
		while ((line = reader.readLine()) != null) {
			@SuppressWarnings("unused")
			int spaceIndex = line.indexOf(" ");

		}

		tcpPort = 8998;
		udpPort = 8998;
		protocolName = "Kademlia";
		protocol = P2PPUtils.KADEMLIA_P2P_ALGORITHM;
		hashAlgorithmName = "SHA1";
		hashAlgorithm = P2PPUtils.SHA1_HASH_ALGORITHM;
		hashSize = 20;
		isRecursive = false;
		isParallel = false;
		hashBase = 2;
		mode = PEER_MODE;
	}

	public String getFilename() {
		return filename;
	}

	/**
	 * Returns TCP port to be used. -1 is returned when there's no TCP port defined in a settings file.
	 * 
	 * @return
	 */
	public int getTcpPort() {
		return tcpPort;
	}

	/**
	 * Returns UDP port to be used. -1 is returned when there's no UDP port defined in a settings file.
	 * 
	 * @return
	 */
	public int getUdpPort() {
		return udpPort;
	}

	public int getTlsPort() {
		return tlsPort;
	}

	public int getDtlsPort() {
		return dtlsPort;
	}

	public byte getProtocol() {
		return protocol;
	}

	public String getProtocolName() {
		return protocolName;
	}

	public String getHashAlgorithmName() {
		return hashAlgorithmName;
	}

	public byte getHashSize() {
		return (byte) hashSize;
	}

	public boolean isRecursive() {
		return isRecursive;
	}

	public boolean isParallel() {
		return isParallel;
	}

	public int getHashBase() {
		return hashBase;
	}

	/**
	 * Returns mode that has to be used. This integer may contain information about many modes at once. Use AND function
	 * with mode constants to discover current mode.
	 * 
	 * @return
	 */
	public int getMode() {
		return mode;
	}

}
