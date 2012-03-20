package pl.edu.pjwstk.p2pp.launchers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.PropertyConfigurator;

import pl.edu.pjwstk.p2pp.ConsoleInterface;
import pl.edu.pjwstk.p2pp.P2PPManager;
import pl.edu.pjwstk.p2pp.debug.processor.DebugWriter;
import pl.edu.pjwstk.p2pp.debug.processor.subsystems.writers.RawTextWriter;
import pl.edu.pjwstk.p2pp.entities.Client;
import pl.edu.pjwstk.p2pp.entities.Peer;
import pl.edu.pjwstk.p2pp.ice.STUNService;
import pl.edu.pjwstk.p2pp.kademlia.KademliaBoostrapServer;
import pl.edu.pjwstk.p2pp.kademlia.KademliaConstants;
import pl.edu.pjwstk.p2pp.kademlia.KademliaPeer;
import pl.edu.pjwstk.p2pp.objects.AddressInfo;
import pl.edu.pjwstk.p2pp.objects.P2POptions;
import pl.edu.pjwstk.p2pp.superpeer.*;
import pl.edu.pjwstk.p2pp.util.P2PPMessageFactory;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;
import pl.edu.pjwstk.p2pp.socialcircle.SocialCircleBootstrapServer;
import pl.edu.pjwstk.p2pp.socialcircle.SocialCircleConstants;
import pl.edu.pjwstk.p2pp.socialcircle.SocialCirclePeer;

/**
 * Launcher for P2PP entities (server, client, peer) using command line.
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 */
public class CommandLineLauncher {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(CommandLineLauncher.class);

    private static final String TCP_PORT_OPTION = "tcp";
    private static final String UDP_PORT_OPTION = "udp";
    private static final String SSL_PORT_OPTION = "ssl";  
    private static final String TLS_PORT_OPTION = "tls";
    private static final String DTLS_PORT_OPTION = "dtls";
    private static final String MODE_OPTION = "m";
    private static final String PROTOCOL_OPTION = "p";
    private static final String UNHASHED_ID_OPTION = "id";
    private static final String HASH_ALGORITHM_OPTION = "h";
    private static final String OVERLAY_ID_OPTION = "o";
    private static final String HASH_LENGTH_OPTION = "hl";
    private static final String HASH_BASE_OPTION = "hb";
    private static final String DEBUG_OPTION = "debug";
    private static final String SOCIAL_FILE = "sf";

    private static final String ENCRYPTION_KEYS = "keys";
    private static final String ENCRYPTION_PASS = "pass";

    // TODO temporary until ICE implementation complete
    private static final String SERVER_REFLEXIVE_ADDRESS = "sra";
    private static final String SERVER_REFLEXIVE_PORT = "srp";

    // modes
    private static final int PEER_MODE = 1;
    private static final int CLIENT_MODE = 2;
    private static final int BOOTSTRAP_SERVER_MODE = 4;
    private static final int DIAGNOSTICS_SERVER_MODE = 8;
    private static final int E_AND_A_MODE = 16;

    /**
     * Command-line arguments.
     */
    private String[] args;

    /**
     * Command line object that contains arguments.
     */
    private CommandLine commandLine;

    /**
     * Object that contains command line options.
     */
    private Options options;

    /**
     * Constructor of launcher of P2PP entities using command line.
     *
     * @param args Command-line arguments.
     */
    public CommandLineLauncher(String[] args) {
        this.args = args;
    }

    /**
     * Parses command-line arguments and checks if they are OK. Returns true if they are OK. False otherwise.
     *
     * @return
     */
    public boolean parseAndCheckArguments() {

        if (logger.isInfoEnabled()) logger.info("parsing starts");

        try {
            commandLine = createCommandLine(args);

            // gets values for arguments
            try {

                String modeString = commandLine.getOptionValue(MODE_OPTION);
                if (logger.isTraceEnabled()) logger.trace("mode=" + modeString);
                int mode = Integer.parseInt(modeString);
                String protocolName = commandLine.getOptionValue(PROTOCOL_OPTION);

                if (logger.isTraceEnabled()) logger.trace(mode + ", " + protocolName);

                // checks if BootstrapServer mode arguments are OK
                if ((mode & BOOTSTRAP_SERVER_MODE) > 0) {
                    String hashAlgorithm = commandLine.getOptionValue(HASH_ALGORITHM_OPTION);
                    byte hashLength = (byte) Integer.parseInt(commandLine.getOptionValue(HASH_LENGTH_OPTION));
                    byte hashBase = (byte) Integer.parseInt(commandLine.getOptionValue(HASH_BASE_OPTION));
                    String overlayID = commandLine.getOptionValue(OVERLAY_ID_OPTION);
                    String serverReflexiveAddress = commandLine.getOptionValue(SERVER_REFLEXIVE_ADDRESS);
                    int serverReflexivePort = Integer.parseInt(commandLine.getOptionValue(SERVER_REFLEXIVE_PORT));
                    if (logger.isTraceEnabled()) {
                        logger.trace(hashAlgorithm + ", " + hashLength + ", " + hashBase + ", " + overlayID);
                    }
                    if (protocolName == null || hashAlgorithm == null || overlayID == null
                            || serverReflexiveAddress == null || serverReflexivePort <= 0) {
                        return false;
                    }
                }
                // checks if client/peer mode arguments are OK
                if ((mode & CLIENT_MODE) > 0 || (mode & PEER_MODE) > 0) {
                    String unhashedID = commandLine.getOptionValue(UNHASHED_ID_OPTION);
                    String serverReflexiveAddress = commandLine.getOptionValue(SERVER_REFLEXIVE_ADDRESS);
                    int serverReflexivePort = Integer.parseInt(commandLine.getOptionValue(SERVER_REFLEXIVE_PORT));
                    if (logger.isTraceEnabled()) {
                        logger.trace(unhashedID + ", " + serverReflexiveAddress + ", " + serverReflexivePort);
                    }
                    // TODO remove two last if ICE implementation complete
                    if (unhashedID == null || serverReflexiveAddress == null || serverReflexivePort <= 0) {
                        return false;
                    }
                }
                // TODO more checking for more modes

                if (commandLine.hasOption(DEBUG_OPTION)) {
                    String id = ((mode & BOOTSTRAP_SERVER_MODE) > 0) ? "bootstrap" : commandLine.getOptionValue(UNHASHED_ID_OPTION);
                    DebugWriter.setOption(DebugWriter.WRITER_SUBSYSTEM_SETTINGS,
                            new Object[]{"logs/debug_" + id + ".log", RawTextWriter.OVERWRITE, 128});
                    DebugWriter.makeInstance();
                }

            } catch (NumberFormatException e) {
                logger.error("problem with number", e);
                return false;
            }

            // gets ports and counts them (if none is used, arguments are bad)
            int portsCounter = 0;
            try {
                int tcpPort = Integer.parseInt(commandLine.getOptionValue(TCP_PORT_OPTION));
                portsCounter++;
                if (logger.isTraceEnabled()) logger.trace("tcp=" + tcpPort);
            } catch (NumberFormatException e) {
                // just ignore
            }
            try {
                int udpPort = Integer.parseInt(commandLine.getOptionValue(UDP_PORT_OPTION));
                portsCounter++;
                if (logger.isTraceEnabled()) logger.trace("udp=" + udpPort);
            } catch (NumberFormatException e) {
                // just ignore
            }
            try {
                int sslPort = Integer.parseInt(commandLine.getOptionValue(SSL_PORT_OPTION));
                portsCounter++;
                if (logger.isTraceEnabled()) logger.trace("ssl=" + sslPort);
            } catch (NumberFormatException e) {
                // just ignore
            }
            try {
                int tlsPort = Integer.parseInt(commandLine.getOptionValue(TLS_PORT_OPTION));
                portsCounter++;
                if (logger.isTraceEnabled()) logger.trace("tls=" + tlsPort);
            } catch (NumberFormatException e) {
                // just ignore
            }
            try {
                int dtlsPort = Integer.parseInt(commandLine.getOptionValue(DTLS_PORT_OPTION));
                portsCounter++;
                if (logger.isTraceEnabled()) logger.trace("dtls=" + dtlsPort);
            } catch (NumberFormatException e) {
                // just ignore
            }

            // Checks if arguments are OK
            return portsCounter != 0;
        } catch (ParseException e) {
            e.printStackTrace();
            logger.error("Parse exception", e);
            return false;
        }
    }

    /**
     * Configures Logger using log4j library.
     */
    private static void configureLogger() {
        Calendar calendar = new GregorianCalendar();

        // sets properties that will enable time stamps in log file names
        System.setProperty("log4j.year", "" + calendar.get(Calendar.YEAR));
        System.setProperty("log4j.month", "" + (calendar.get(Calendar.MONTH) + 1));
        System.setProperty("log4j.day", "" + calendar.get(Calendar.DAY_OF_MONTH));
        System.setProperty("log4j.hour", "" + calendar.get(Calendar.HOUR_OF_DAY));
        System.setProperty("log4j.minute", "" + calendar.get(Calendar.MINUTE));
        System.setProperty("log4j.second", "" + calendar.get(Calendar.SECOND));

        // sets a file that contains info about what things to log and where to log
        PropertyConfigurator.configure("log4j.properties");
    }

    /**
     * Creates command line object that is used for parsing command line arguments.
     *
     * @param args Command-line arguments.
     * @return
     * @throws ParseException Throws when bad arguments are given.
     */
    private CommandLine createCommandLine(String[] args) throws ParseException {
        options = new Options();
        options.addOption(TCP_PORT_OPTION, true,
                "TCP port to be used. If present, TCP protocol will be used.");
        options.addOption(UDP_PORT_OPTION, true,
                "UDP port to be used. If present, UDP protocol will be used. (only one supported at the moment)");
        options.addOption(SSL_PORT_OPTION, true, "SSL port to be used. If present, SSL protocol will be used.");
        options.addOption(TLS_PORT_OPTION, true,
                "TLS port to be used. If present, TLS protocol will be used.");
        options.addOption(DTLS_PORT_OPTION, true, "DTLS port to be used. If present, "
                + "DTLS protocol will be used. (NOT SUPPORTED)");
        options.addOption(MODE_OPTION, true, "Mode. Peer(1), client(2), bootstrap server(4), diagnostics server(8), "
                + "enrollment and authentication server(16) and combinations of them (5 "
                + "is bootstrap server and peer).");
        options.addOption(PROTOCOL_OPTION, true, "Name of P2P protocol.");
        options.addOption(UNHASHED_ID_OPTION, true, "UnhashedID used by peer or client.");
        options.addOption(HASH_ALGORITHM_OPTION, true, "Hash algorithm. Used by bootstrap server.");
        options.addOption(HASH_LENGTH_OPTION, true, "Length of hash. Used by bootstrap server.");
        options.addOption(OVERLAY_ID_OPTION, true, "ID of overlay. Used by bootstrap server.");
        options.addOption(HASH_BASE_OPTION, true, "Hash base. Used by bootstrap server.");
        options.addOption(DEBUG_OPTION, false, "Enables debug mode");
        options.addOption(SOCIAL_FILE, true, "File with social network (only with SocialCircle protocol)");
        options.addOption(ENCRYPTION_KEYS, true, "Path to a file with ssl keys");
        options.addOption(ENCRYPTION_PASS, true, "Passphrase to unlock the keys");

        // TODO temporary... until ICE implementation complete
        options.addOption(SERVER_REFLEXIVE_ADDRESS, true, "TEMPORARILY USED (ICE implementation isn't ready). "
                + "Server reflexive address (in xxx.xxx.xxx.xxx form)");
        options.addOption(SERVER_REFLEXIVE_PORT, true,
                "TEMPORARILY USED (ICE implementation isn't ready). Server reflexive port");

        CommandLineParser parser = new PosixParser();
        return parser.parse(options, args);
    }

    /**
     * Creates entities described in settings reader and adds them to manager. Creates and starts console interface.
     * Also adds manager of data shared between local entities. TODO probably some kind of exception should be thrown
     * when some settings are bad.
     *
     * @throws NoSuchAlgorithmException Throws when chosen hash algorithm is not supported.
     */
    private P2PPManager createManagerAndAddEntities() throws NoSuchAlgorithmException, UnsupportedEncodingException {

        ConsoleInterface console = new ConsoleInterface();

        P2PPManager manager = null;

        String protocolName = commandLine.getOptionValue(PROTOCOL_OPTION);

        // gets unhashedID
        byte[] unhashedIDAsBytes = null;
        String unhashedID = commandLine.getOptionValue(UNHASHED_ID_OPTION);
        if (unhashedID != null) {
            unhashedIDAsBytes = unhashedID.getBytes("UTF-8");
        }

        // gets mode
        int mode = Integer.parseInt(commandLine.getOptionValue(MODE_OPTION));

        // gets ports
        int tcpPort = 0;
        int udpPort = 0;
        int sslPort = 0;
        int tlsPort = 0;
        int dtlsPort = 0;
        String tcpPortAsString = commandLine.getOptionValue(TCP_PORT_OPTION);
        String udpPortAsString = commandLine.getOptionValue(UDP_PORT_OPTION);
        String sslPortAsString = commandLine.getOptionValue(SSL_PORT_OPTION);
        String tlsPortAsString = commandLine.getOptionValue(TLS_PORT_OPTION);
        String dtlsPortAsString = commandLine.getOptionValue(DTLS_PORT_OPTION);
        if (tcpPortAsString != null)
            tcpPort = Integer.parseInt(tcpPortAsString);
        if (udpPortAsString != null)
            udpPort = Integer.parseInt(udpPortAsString);
        if (sslPortAsString != null)
            sslPort = Integer.parseInt(sslPortAsString);
        if (tlsPortAsString != null)
            tlsPort = Integer.parseInt(tlsPortAsString);
        if (dtlsPortAsString != null)
            dtlsPort = Integer.parseInt(dtlsPortAsString);

        String encryptionKeys = commandLine.getOptionValue(ENCRYPTION_KEYS);
        String encryptionPass = commandLine.getOptionValue(ENCRYPTION_PASS);

        boolean[] transportType = AddressInfo.TCP_TRANSPORT_TYPE;
        if (udpPort > 0) transportType = AddressInfo.UDP_TRANSPORT_TYPE;

        if (protocolName.equals(KademliaConstants.KADEMLIA_PROTOCOL_NAME)) {

            // creates P2PP manager
            manager = new P2PPManager(tcpPort, udpPort, sslPort, tlsPort, dtlsPort, encryptionKeys, encryptionPass, new P2PPMessageFactory(), unhashedIDAsBytes);

            // if bootstrap server was chosen
            if ((mode & BOOTSTRAP_SERVER_MODE) > 0) {

                // gets and sets options for bootstrap server
                String hashAlgorithm = commandLine.getOptionValue(HASH_ALGORITHM_OPTION);
                byte hashLength = Byte.parseByte(commandLine.getOptionValue(HASH_LENGTH_OPTION));
                byte hashBase = Byte.parseByte(commandLine.getOptionValue(HASH_BASE_OPTION));
                String overlayID = commandLine.getOptionValue(OVERLAY_ID_OPTION);
                manager.setOptions(new P2POptions(P2PPUtils.convertHashAlgorithmName(hashAlgorithm), hashLength,
                        P2PPUtils.convertP2PAlgorithmName(protocolName), hashBase, overlayID.getBytes("UTF-8")));

                // creates bootstrap server that uses Kademlia and adds it to manager
                KademliaBoostrapServer server = new KademliaBoostrapServer();
                manager.addEntity(server);
                console.addEntity(server);

            } else if ((mode & PEER_MODE) > 0) {
                Peer peer = new KademliaPeer();

                // adds services to peer
                // ICEService iceService = new ICEService(true);
                STUNService stunService = new STUNService(true, transportType);
                // TURNService turnService = new TURNService(true);
                // peer.addService(iceService);
                peer.addService(stunService);
                // peer.addService(turnService);

                //
                manager.addEntity(peer);

                // peer.addResource(new STUNPortResource(9898));
                console.addEntity(peer);

                // sets console interface as listener of callbacks
                peer.setCallback(console);

            } else if ((mode & CLIENT_MODE) > 0) {
                Client c = new Client();
                manager.addEntity(c);
                // peer.addResource(new STUNPortResource(9898));
                console.addEntity(c);
                // sets console interface as listener of callbacks
                c.setCallback(console);
            } else {
                throw new RuntimeException("Currently modes different than Peer and BootstrapServer aren't implemented.");
            }
        } else if (protocolName.equals(SocialCircleConstants.SOCIALCIRCLE_PROTOCOL_NAME)) {

            // creates P2PP manager
            manager = new P2PPManager(tcpPort, udpPort, sslPort, tlsPort, dtlsPort, encryptionKeys, encryptionPass, new P2PPMessageFactory(), unhashedIDAsBytes);

            // if bootstrap server was chosen
            if ((mode & BOOTSTRAP_SERVER_MODE) > 0) {

                // gets and sets options for bootstrap server
                String hashAlgorithm = commandLine.getOptionValue(HASH_ALGORITHM_OPTION);
                byte hashLength = Byte.parseByte(commandLine.getOptionValue(HASH_LENGTH_OPTION));
                byte hashBase = Byte.parseByte(commandLine.getOptionValue(HASH_BASE_OPTION));
                String overlayID = commandLine.getOptionValue(OVERLAY_ID_OPTION);
                manager.setOptions(new P2POptions(P2PPUtils.convertHashAlgorithmName(hashAlgorithm), hashLength,
                        P2PPUtils.convertP2PAlgorithmName(protocolName), hashBase, overlayID.getBytes("UTF-8")));

                // creates bootstrap server that uses SocialCircle and adds it to manager
                SocialCircleBootstrapServer server = new SocialCircleBootstrapServer();
                manager.addEntity(server);
                console.addEntity(server);

            } else if ((mode & PEER_MODE) > 0) {
                String hashAlgorithm = commandLine.getOptionValue(HASH_ALGORITHM_OPTION);
                byte hashLength = Byte.parseByte(commandLine.getOptionValue(HASH_LENGTH_OPTION));
                byte hashBase = Byte.parseByte(commandLine.getOptionValue(HASH_BASE_OPTION));
                String overlayID = commandLine.getOptionValue(OVERLAY_ID_OPTION);

                manager.setOptions(new P2POptions(P2PPUtils.convertHashAlgorithmName(hashAlgorithm), hashLength,
                        P2PPUtils.convertP2PAlgorithmName(protocolName), hashBase, overlayID.getBytes("UTF-8")));

                Peer peer = new SocialCirclePeer();

                // adds services to peer
                // ICEService iceService = new ICEService(true);
                STUNService stunService = new STUNService(true, transportType);
                // TURNService turnService = new TURNService(true);
                // peer.addService(iceService);
                peer.addService(stunService);
                // peer.addService(turnService);

                //
                manager.addEntity(peer);

                String socialFile = commandLine.getOptionValue(SOCIAL_FILE);
                peer.parseSocialFile(unhashedID, socialFile);

                // peer.addResource(new STUNPortResource(9898));
                console.addEntity(peer);

                // sets console interface as listener of callbacks
                peer.setCallback(console);

            } else if ((mode & CLIENT_MODE) > 0) {
                Client c = new Client();
                manager.addEntity(c);
                // peer.addResource(new STUNPortResource(9898));
                console.addEntity(c);
                // sets console interface as listener of callbacks
                c.setCallback(console);
            } else {
                throw new RuntimeException("Currently modes different than Peer and BootstrapServer aren't implemented.");
            }
        } else if (protocolName.equals(SuperPeerConstants.SUPERPEER_PROTOCOL_NAME)) {

            if (logger.isDebugEnabled()) {
                logger.debug("Starting with " + SuperPeerConstants.SUPERPEER_PROTOCOL_NAME + " protocol");
            }

            manager = new P2PPManager(tcpPort, udpPort, sslPort, tlsPort, dtlsPort, encryptionKeys, encryptionPass, new P2PPMessageFactory(), unhashedIDAsBytes);

            if ((mode & BOOTSTRAP_SERVER_MODE) > 0) {

                String hashAlgorithm = commandLine.getOptionValue(HASH_ALGORITHM_OPTION);
                byte hashLength = Byte.parseByte(commandLine.getOptionValue(HASH_LENGTH_OPTION));
                byte hashBase = Byte.parseByte(commandLine.getOptionValue(HASH_BASE_OPTION));
                String overlayID = commandLine.getOptionValue(OVERLAY_ID_OPTION);
                manager.setOptions(new P2POptions(P2PPUtils.convertHashAlgorithmName(hashAlgorithm), hashLength,
                        P2PPUtils.convertP2PAlgorithmName(protocolName), hashBase, overlayID.getBytes("UTF-8")));

                SuperPeerBootstrapServer server = new SuperPeerBootstrapServer();
                manager.addEntity(server);
                console.addEntity(server);

            } else if ((mode & PEER_MODE) > 0) {

                String hashAlgorithm = commandLine.getOptionValue(HASH_ALGORITHM_OPTION);
                byte hashLength = Byte.parseByte(commandLine.getOptionValue(HASH_LENGTH_OPTION));
                byte hashBase = Byte.parseByte(commandLine.getOptionValue(HASH_BASE_OPTION));
                String overlayID = commandLine.getOptionValue(OVERLAY_ID_OPTION);

                manager.setOptions(new P2POptions(P2PPUtils.convertHashAlgorithmName(hashAlgorithm), hashLength,
                        P2PPUtils.convertP2PAlgorithmName(protocolName), hashBase, overlayID.getBytes("UTF-8")));

                Peer peer = new SuperPeerPeer();
                // ICEService iceService = new ICEService(true);
                STUNService stunService = new STUNService(true, transportType);
                // TURNService turnService = new TURNService(true);
                // peer.addService(iceService);
                peer.addService(stunService);
                manager.addEntity(peer);
                // peer.addResource(new STUNPortResource(9898));
                console.addEntity(peer);
                peer.setCallback(console);

            } else if ((mode & CLIENT_MODE) > 0) {
                Client c = new Client();
                manager.addEntity(c);
                console.addEntity(c);
                c.setCallback(console);
            } else {
                throw new RuntimeException("Currently modes different than Peer and BootstrapServer aren't implemented.");
            }
        } else {
            throw new RuntimeException("Currently algorithms different than Kademlia aren't implemented.");
        }

        console.start();

        return manager;

    }

    /**
     * Launches P2PP entities as described in settings file.
     *
     * @throws IOException Throws when reading from settings didn't happen.
     */
    public void launch() throws IOException, NoSuchAlgorithmException {

        // TODO to be deleted when ICE implementation is ready
        STUNService.serverReflexiveAddress = commandLine.getOptionValue(SERVER_REFLEXIVE_ADDRESS);
        STUNService.serverReflexivePort = Integer.parseInt(commandLine.getOptionValue(SERVER_REFLEXIVE_PORT));

        P2PPManager manager = createManagerAndAddEntities();

        // starts manager (i.e. starts P2PP)
        manager.start();

        if (logger.isInfoEnabled()) {
            logger.info("P2PPManager started at " + new GregorianCalendar().getTime().toString());
        }
    }

    /**
     * Returns an array of modes' names.
     *
     * @return
     */
    public String[] getModeNames() {

        // gets mode
        int mode = Integer.parseInt(commandLine.getOptionValue(MODE_OPTION));

        Vector<String> modeNames = new Vector<String>();

        if ((mode & BOOTSTRAP_SERVER_MODE) > 0) {
            modeNames.add("BootstrapServer");
        }
        if ((mode & PEER_MODE) > 0) {
            modeNames.add("Peer");
        }
        if ((mode & CLIENT_MODE) > 0) {
            modeNames.add("Client");
        }
        if ((mode & E_AND_A_MODE) > 0) {
            modeNames.add("EnrollmentAndAuthenticateServer");
        }
        if ((mode & DIAGNOSTICS_SERVER_MODE) > 0) {
            modeNames.add("DiagnosticsServer");
        }
        return modeNames.toArray(new String[0]);
    }

    public static void main(String[] args) {

    	String[] argsStatic = {"pl.edu.pjwstk.p2pp.launchers.CommandLineLauncher", "-m", "4", "-tcp", "7080", "-p", "SuperPeer", "-h", "SHA-1", "-o", "overlayid", "-hl", "20", "-hb", "2", "-sra", "127.0.0.1", "-srp", "708"};
        configureLogger();

        CommandLineLauncher launcher = new CommandLineLauncher(argsStatic);

        try {
            boolean argsOK = launcher.parseAndCheckArguments();
            if (!argsOK) {
                if (logger.isInfoEnabled()) logger.info("Bad arguments.");
                System.out.println("Bad arguments. Read help.");
                launcher.printHelp();
                return;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        // prints info about launching
        String[] names = launcher.getModeNames();
        StringBuilder result = new StringBuilder();
        for (String name : names) {
            result.append(name);
            result.append(" ");
        }
        if (logger.isInfoEnabled()) logger.info("P2PP starts in mode = " + result.toString());

        try {
            launcher.launch();
        } catch (IOException e) {
            System.out.println("IO exception during starting.\n" + e);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Given hash algorithm is not supported.");
        } catch (Exception e) {
            logger.error("Error during launch.", e);
        }

    }

    /**
     * Prints help information to standard output.
     */
    private void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Arguments depend on mode.", "Example for bootstrap server: \n"
                + "-m 4 -udp port_number -p overlay_protocol_name -h hash_algorithm_name -o overlay_id "
                + "-hl hash_byte_length -hb hash_base -sra server_reflexive_address -srp server_reflexive_port\n" + "Example for peer: \n"
                + "-p overlay_protocol_name -m 1 -udp port_number -id peer_id -sra server_reflexive_ip_address "
				+ "-srp server_reflexive_port", options, null);

	}
}
