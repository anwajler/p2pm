package pl.edu.pjwstk.p2pp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.net.proto.SupportedProtocols;
import pl.edu.pjwstk.p2pp.entities.Client;
import pl.edu.pjwstk.p2pp.entities.DiagnosticServer;
import pl.edu.pjwstk.p2pp.entities.EAndAServer;
import pl.edu.pjwstk.p2pp.entities.EntitiesSharedDataManager;
import pl.edu.pjwstk.p2pp.entities.P2PPEntity;
import pl.edu.pjwstk.p2pp.entities.Peer;
import pl.edu.pjwstk.p2pp.ice.ICEConstants;
import pl.edu.pjwstk.p2pp.messages.Message;
import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.responses.BootstrapResponse;
import pl.edu.pjwstk.p2pp.objects.AddressInfo;
import pl.edu.pjwstk.p2pp.objects.P2POptions;
import pl.edu.pjwstk.p2pp.testing.MessagesToDatabaseProvider;
import pl.edu.pjwstk.p2pp.transactions.Transaction;
import pl.edu.pjwstk.p2pp.transport.MessageStorage;
import pl.edu.pjwstk.p2pp.transport.TransportManager;
import pl.edu.pjwstk.p2pp.util.AbstractMessageFactory;

/**
 * <p>
 * Class being a center of everything in this P2PP implementation. It has a thread (as internal object) that gives time
 * slots for transport (readers/writers from/to sockets), P2PP entities and P2PP services. It also passes protocol
 * messages from sockets to entities/services and the other way round.
 * <p>
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class P2PPManager {

	private static final Logger LOG = org.apache.log4j.Logger.getLogger(P2PPManager.class);

	/** Protocol version in use. */
	public static final boolean[] CURRENT_PROTOCOL_VERSION = P2PPMessage.P2PP_PROTOCOL_VERSION_1;

	private int tcpPort;
	private int udpPort;
    private int sslPort;
	private int tlsPort;
	private int dtlsPort;

    private String encryptionKeys;
    private String encryptionPass;

	/** True if this manager is running. */
	private boolean running = true;

	/** Writers of P2PP messages. */
	//private Vector<CommunicationObject> communicationObjects = new Vector<CommunicationObject>();
	private TransportManager transportManager = new TransportManager();

	/**
	 * Factory of messages. Must be compatible with used implementation of P2PP.
	 */
	//private Vector<AbstractMessageFactory> messageFactories = new Vector<AbstractMessageFactory>();

	/**
	 * Manager of data shared between entities.
	 */
	private EntitiesSharedDataManager sharedManager;

	/** A vector of entities managed by this manager. */
	private Vector<P2PPEntity> entities = new Vector<P2PPEntity>();

	/** Messages waiting to be passed to writers. */
	//private Vector<Message> messagesToBeSend = new Vector<Message>();
    private Vector<Object[]> messagesToBeSend = new Vector<Object[]>();

	/** Datagram socket used by unreliable transport readers/writers. */
	//private DatagramSocket datagramSocket;

	/**
	 * Index of reader object used in previous reading.
	 */
	//private int previousCommunicationObject = -1;

	/**
	 * Storage for sent and received messages. Used for testing purposes, so that a test framework may see all the
	 * messages, send them to database etc.
	 */
	private MessageStorage messageStorage;

	private MessagesToDatabaseProvider messagesToDatabaseProvider;

	/**
	 * Listener of outgoing messages coming from entities or services.
	 */
	private OutgoingMessagesListener outgoingListener = new OutgoingMessagesListener() {
		public void onSend(Message message, Transaction transaction) {
			//messagesToBeSend.add(message);
            messagesToBeSend.add(new Object[]{message,transaction});
		}
	};

    /**
     * This manager's thread. It is responsible for giving time slot for entities and their shared data. It also gives
     * time for readers and writers to do their things.
     *
     */
    private Runnable managerThread = new Runnable() {

        public void run() {
            if (LOG.isDebugEnabled()) LOG.debug("P2PPManager starts in mode=" + getModesNamesAsOneString());
            try {
                while (running) {

                    long timeOfStart = System.currentTimeMillis();
                    // gives time slot for entities one by one
                    for (P2PPEntity currentEntity : entities) {
                        currentEntity.onTimeSlot();
                    }

                    // passes waiting messages to writers
                    passMessagesToWriters();
                    // logger.trace("P2PPManager passed messages to writers.");

                    // reads message from readers
                    Message message = readMessage();
                    if (message != null) {
                        // checks queue of messages from readers
                        checkIfContainsSourceIDInfo(message);
                        try {
                            fireOnReceive(message);
                        } catch (NullPointerException npe) {
                            LOG.error("NPE during fireOnReceive for message " + message, npe);
                        }
                        // logger.trace("P2PPManager passed message to entities.");
                    }

                    // time slot for entities' shared data
                    //sharedManager.onTimeSlot(outgoingListener);
                    // logger.trace("P2PPManager gave time for shared data.");

                    // TODO time slot for connections listener

                    // TODO probably time slot for ICEManager or something

                    // TODO probably delete or change... added for CPU not to be
                    // consumed so much
                    /*
                          * try { Thread.sleep(10); } catch (InterruptedException e) { // TODO Auto-generated catch block
                          * e.printStackTrace(); }
                          */

                    long timeOfEnd = System.currentTimeMillis();

                    // if too much time was spent
                    if (timeOfEnd - timeOfStart > 1000) {
                        if (LOG.isTraceEnabled()) LOG.trace("One loop took " + (timeOfEnd - timeOfStart) + " milliseconds. That's much!");
                    }

                }

            } catch (Throwable e) {
                // exceptions may be thrown when stop() method was invoked on this manager... if that's the case,
                // we aren't interested in exceptions
                if (running) {
                    LOG.error(e.getClass().getName() + " during run", e);
                } else {
                    if (LOG.isTraceEnabled()) LOG.trace(e.getClass().getName() + " but running=false");
                }
            }
            if (LOG.isInfoEnabled()) LOG.info("P2PPManager's thread ends.");

        }

		/**
		 * Reads message from one of communication objects.
		 * 
		 * @return
		 */
		private Message readMessage() throws IOException {
			return transportManager.getMessageFromQueue();
		}

		/**
		 * Passes to writers all the messages waiting to be send.
		 */
		private void passMessagesToWriters() {
			while (messagesToBeSend.size() > 0) {
				//Message message = messagesToBeSend.remove(0);
                Object[] message = messagesToBeSend.remove(0);
				if (LOG.isDebugEnabled()) LOG.debug("Message passed to writers " + message.toString());
				fireOnSend(message);
			}
		}

		/**
		 * Checks whether given received message contains some information about sourceID length. BootstrapResponse is
		 * interesting. (TODO probably leave should be also interesting). If it contains that kind of info,
		 * ComunicationObjects are informed.
		 * 
		 * @param message
		 */
		private void checkIfContainsSourceIDInfo(Message message) {
			try {
				// remembering sourceID length FIXME probably should be
				// changed because when communication will be poisoned with
				// fake BootstrapResponse, readers will have bad sourceID info
				if (message instanceof BootstrapResponse) {
					BootstrapResponse response = (BootstrapResponse) message;
					if (!response.isAcknowledgment() && response.isResponseACK()) {
						int sourceIDLength = (byte) response.getOriginatorPeerInfo().getPeerID().getPeerIDBytes().length;
						transportManager.setSourceIDLength((byte) sourceIDLength);
					}

				}
			} catch (NullPointerException e) {
                LOG.error("Error while checking for source id info", e);
				// ignore message
			}
		}

	};

    private final ExecutorService receivedExecutor = Executors.newCachedThreadPool();

	/**
	 * Creates P2PPManager that will be working on given ports. If, for instance, TCP won't be used, 0 has to be passed
	 * as the first argument. If port has to be used, number between 1 and 65555 has to be given as an appropriate
	 * argument. At least one port has to be passed.
	 * 
	 * @param tcpPort
	 *            TCP port to use.
	 * @param udpPort
	 *            UDP port to use.
	 * @param tlsPort
	 *            TLS port to use.
	 * @param dtlsPort
	 *            DTLS port to use.
	 * @param messageFactory
	 *            Factory of messages. Must be compatible with used implementation. May be null, but has to be added
	 *            before start of manager.
	 * @param unhashedID
	 *            UnhashedID that will be used. Can't be null.
	 */
	public P2PPManager(int tcpPort, int udpPort, int sslPort, int tlsPort, int dtlsPort, String encryptionKeys, String encryptionPass,
                       AbstractMessageFactory messageFactory, byte[] unhashedID) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Initializing P2PPManager with tcpPort=" + tcpPort + "; udpPort=" + udpPort + "; sslPort=" + sslPort + "; tlsPort=" +
                    tlsPort + "; encryptionKeys=" + encryptionKeys + "; encryptionPass=" + encryptionPass);
        }

		this.tcpPort = tcpPort;
		this.udpPort = udpPort;
        this.sslPort = sslPort;
		this.tlsPort = tlsPort;
		this.dtlsPort = dtlsPort;
        this.encryptionKeys = encryptionKeys;
        this.encryptionPass = encryptionPass;
		this.addMessageFactory(messageFactory);

		this.sharedManager = new EntitiesSharedDataManager(udpPort, tcpPort, sslPort, tlsPort, dtlsPort);
		this.sharedManager.setUnhashedID(unhashedID);
	}

	/**
	 * Gets an object responsible for sending gathered messages to database. Used for testing implementation.
	 * 
	 * @return
	 */
	public MessagesToDatabaseProvider getMessagesToDatabaseProvider() {
		return messagesToDatabaseProvider;
	}

	/**
	 * Sets an object responsible for sending gathered messages to database. Used for testing implementation. Has to be
	 * used after setting message storage in {@link #setMessageStorage(MessageStorage)}.
	 * 
	 * @param messagesToDatabaseProvider
	 */
	public void setMessagesToDatabaseProvider(MessagesToDatabaseProvider messagesToDatabaseProvider) {
		this.messagesToDatabaseProvider = messagesToDatabaseProvider;
		messagesToDatabaseProvider.setMessageStorage(messageStorage);
	}

	/**
	 * Gets message storage used for storing all the received and sent messages .
	 * 
	 * @return
	 */
	public MessageStorage getMessageStorage() {
		return messageStorage;
	}

	/**
	 * Sets message storage used for storing all the received and sent messages. If not set, or set to null, no storing
	 * happens.
	 * 
	 * @param messageStorage
	 */
	public void setMessageStorage(MessageStorage messageStorage) {
		this.messageStorage = messageStorage;
		if (messagesToDatabaseProvider != null) {
			messagesToDatabaseProvider.setMessageStorage(messageStorage);
		}
		
//		for (CommunicationObject currentCommunicationObject : communicationObjects) {
//			currentCommunicationObject.setMessageStorage(messageStorage);
//		}
		transportManager.setMessageStorage(messageStorage);

	}

	/**
	 * Adds {@link CommunicationObject} to this manager.
	 * 
	 * @param newCommunicationObject
	 *            Communication object to be added.
	 */
//	private void addCommunicationObject(CommunicationObject newCommunicationObject) {
//		communicationObjects.add(newCommunicationObject);
//		for (AbstractMessageFactory currentFactory : messageFactories) {
//			newCommunicationObject.addMessageFactory(currentFactory);
//		}
//	}

	/**
	 * Returns a vector of P2PPEntities being part of this manager.
	 * 
	 * @return
	 */
	public Vector<P2PPEntity> getEntities() {
		return entities;
	}

	public void setEntities(Vector<P2PPEntity> entities) {
		this.entities = entities;

	}

	public int getTcpPort() {
		return tcpPort;
	}

	public int getUdpPort() {
		return udpPort;
	}

    public int getSslPort() {
        return this.sslPort;
    }

	public int getTlsPort() {
		return tlsPort;
	}

	public int getDtlsPort() {
		return dtlsPort;
	}

    public String getEncryptionKeys() {
        return this.encryptionKeys;
    }

    public String getEncryptionPass() {
        return this.encryptionPass;
    }

	/**
	 * Returns names of modes in which this manager is working (for instance "Client" and "BootstrapServer" may be
	 * returned).
	 * 
	 * @return
	 */
	private String[] getModesNames() {
		String[] names = new String[entities.size()];
		for (int i = 0; i < names.length; i++) {
			P2PPEntity entity = entities.get(i);
			if (entity instanceof EAndAServer) {
				names[i] = "EnrollmentAndAuthenticationServer";
			} else if (entity instanceof pl.edu.pjwstk.p2pp.entities.BootstrapServer) {
				names[i] = "BootstrapServer";
			} else if (entity instanceof DiagnosticServer) {
				names[i] = "DiagnosticServer";
			} else if (entity instanceof Peer) {
				names[i] = "Peer";
			} else if (entity instanceof Client) {
				names[i] = "Client";
			}
		}
		return names;
	}

	/**
	 * Returns names (as one string) of modes in which this manager is working (for instance "Client BootstrapServer").
	 * 
	 * @return
	 */
	private String getModesNamesAsOneString() {
		String[] names = getModesNames();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < names.length; i++) {
			builder.append(names[i] + " ");
		}
		return builder.toString();
	}

	/**
	 * Adds an entity to this manager, so that this new entity will be managed by this manager. Entity is filled with
	 * appropriate data (shared data manager, outgoing listener and if an entity is a node it gets services manager).
	 * 
	 * @param entity
	 *            Entity to be added to this manager.
	 */
	public void addEntity(P2PPEntity entity) {

		entities.add(entity);
		entity.setSharedDataManager(sharedManager);
		entity.setOutgoingListener(outgoingListener);

	}

	/**
	 * Adds message factory to list of those that will be used for reading from sockets.
	 * 
	 * @param messageFactory
	 */
	public void addMessageFactory(AbstractMessageFactory messageFactory) {
		//this.messageFactories.add(messageFactory);
		transportManager.addMessageFactory(messageFactory);
	}

	/**
	 * Returns manager of data shared between entities managed by this P2PP manager.
	 * 
	 * @return
	 */
	public EntitiesSharedDataManager getSharedManager() {
		return sharedManager;
	}

	/**
	 * Starts this manager thread. Method returns when P2PP was started successfully. Can't be started if no entities,
	 * readers, writers, messages factories were added to manager.
	 * 
	 * @throws IOException
	 *             Thrown when at least one socket wasn't created.
	 */
	public void start() throws IOException, RuntimeException {

		if(LOG.isDebugEnabled()) LOG.debug("P2PPManager starting");

		if (entities.size() == 0) {
			throw new RuntimeException("Entity wasn't added to P2PPManger.");
		}

		running = initialize();

		// Creates objects responsible for reading, writing and accepting
		// connections.
//		int size = communicationObjects.size();
//		for (int i = 0; i < size; i++) {
//			CommunicationObject current = communicationObjects.get(i);
//			current.setDatagramSocket(datagramSocket);
//		}

		// passes shared manager to all the entities TODO probably may be deleted because setters do this
		//int size = entities.size();
		// byte[] address = datagramSocket.getLocalAddress().getAddress();
		//for (int i = 0; i < size; i++) {
			//P2PPEntity entity = entities.get(i);
			// entity.setSharedDataManager(sharedManager);
		//}

        this.transportManager.start();

		// Starts this manager's thread.
		Thread thread = new Thread(managerThread, "ManagerThread");
		thread.start();

		if (LOG.isDebugEnabled()) LOG.debug("P2PPManager started");

	}

	/**
	 * Stops this manager. This means that all transport is stopped.
	 */
	public void stop() {
		running = false;

		this.transportManager.stopManager();

		// if messages should be send to database TODO throw higher? This will influence users of library
		if (messagesToDatabaseProvider != null) {
			try {
				messagesToDatabaseProvider.sendMessagesToDatabase();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (LOG.isDebugEnabled()) LOG.debug("P2PPManager was stopped.");
	}

	/**
	 * Informs writers about message to be send. TODO synchronize?
	 * 
	 * @param message
	 */
	//private void fireOnSend(Message message) {
    private void fireOnSend(Object[] message) {
//		for (CommunicationObject current : communicationObjects) {
//			if (current.onSend(message))
//				break;
//		}
		transportManager.eventMessageToBeSend(message);

        // TODO Perhaps there should be some information which entity is sending particular message?
        /*if (this.entities.size() > 0) {
            this.entities.get(0).writeDebugInformation(message, false);
        } */
	}

	/**
	 * Informs entities about message received. TODO synchronize?
	 * 
	 * @param message
	 *            Message about which entities are to be informed.
	 */
    private void fireOnReceive(final Message message) {
        this.receivedExecutor.submit(new Runnable() {
            public void run() {
                if (LOG.isDebugEnabled()) LOG.debug("FireOnReceive message=" + message.toString());
                int size = entities.size();
                for (int i = 0; i < size; i++) {
                    P2PPEntity current = entities.get(i);
                    current.writeDebugInformation(message, true);
                    boolean consumed = current.onReceive(message);
                    /*// if current entity consumed given message, other entities aren't informed
                    if (consumed) {
                        break;
                    } // if no entity consumed the message
                    else*/ if (i == size - 1) {
                        if (LOG.isTraceEnabled()) LOG.trace("Message wasn't consumed by any entity.");
                    }
                }
        }});
    }

	/**
	 * Creates and starts subthreads for UDP and/or TCP. Creates UDP socket if UDP is to be used. If there's an error,
	 * every subthread is closed.
	 * 
	 * @return True if initialization was done without errors. False otherwise.
	 * @throws IOException
	 *             Thrown when at least one of sockets wasn't created or addresses couldn't be gathered.
	 */
	private boolean initialize() throws IOException {

		boolean result = true;

        // gets addresses and passes them to shared data manager
		Hashtable<String, Integer> map = determineAddresses();

		if (this.udpPort > 0) {
			transportManager.startListen(SupportedProtocols.UDP, null, udpPort);
            sharedManager.setAddresses(map, AddressInfo.UDP_TRANSPORT_TYPE);
		}

        if (this.tcpPort > 0) {
            GlobalConstants.isOverReliable = true;
            transportManager.startListen(SupportedProtocols.TCP, null, this.tcpPort);
            sharedManager.setAddresses(map, AddressInfo.TCP_TRANSPORT_TYPE);
        }

        if (this.sslPort > 0) {
            GlobalConstants.isOverReliable = true;
            transportManager.startListen(SupportedProtocols.TCP_SSL, null, this.sslPort, this.encryptionKeys, this.encryptionPass);
            sharedManager.setAddresses(map, AddressInfo.TCP_TRANSPORT_TYPE);
        }

        if (this.tlsPort > 0) {
            GlobalConstants.isOverReliable = true;
            transportManager.startListen(SupportedProtocols.TCP_TLS, null, this.tlsPort, this.encryptionKeys, this.encryptionPass);
            sharedManager.setAddresses(map, AddressInfo.TCP_TRANSPORT_TYPE);
        }

		// sets entities listener
		sharedManager.setOutgoingListener(outgoingListener);

		if (LOG.isDebugEnabled()) LOG.debug("shared manager initiated");

		return result;
	}

	/**
	 * Determines addresses and returns map that contains them in pairs "network address as string" and
	 * "type of address as Integer" (as defined by ICE). FIXME currently only IPv4 addresses are determined.
	 * 
	 * @return
	 * @throws SocketException
	 *             Throws when gathering of addresses wasn't possible.
	 */
	private Hashtable<String, Integer> determineAddresses() throws SocketException {
		Hashtable<String, Integer> map = new Hashtable<String, Integer>();

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.getMTU() < 1500) continue; // A bit brazenly (tmp)
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    // FIXME only IPv4 addresses are determined
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()
                            && inetAddress.getAddress().length == 4) {
                        // adds host candidate
                        String local = inetAddress.getHostAddress();
                        map.put(local, new Integer(ICEConstants.HOST_REFLEXIVE_ADDRESS));
                        // TODO maybe some server reflexive candidates using some
                        // public server?
                    }
                }
            }
        } catch (Throwable e) {
            LOG.error("Error while determining network interfaces", e);
        }

		return map;
	}

	/**
	 * 
	 * @param options
	 */
	public void setOptions(P2POptions options) {
		sharedManager.setOptions(options);
	}

}
