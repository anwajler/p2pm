package pl.edu.pjwstk.p2pp;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.*;

import org.apache.log4j.Logger;

import pl.edu.pjwstk.p2pp.entities.BootstrapServer;
import pl.edu.pjwstk.p2pp.entities.Node;
import pl.edu.pjwstk.p2pp.entities.P2PPEntity;
import pl.edu.pjwstk.p2pp.messages.requests.Request;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.ResourceObject;
import pl.edu.pjwstk.p2pp.resources.StringValueResourceObject;
import pl.edu.pjwstk.p2pp.util.ByteUtils;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

/**
 * Class for reading P2PP client commands from command line.
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 */
public class ConsoleInterface extends Thread implements P2PPNodeCallback {

    private static Logger logger = Logger.getLogger(ConsoleInterface.class);

    /**
     * Constant for join command in command line.
     */
    private static final String JOIN_COMMAND = "join";
    /**
     * Constant for lookup object command in command line.
     */
    private static final String LOOKUP_OBJECT_COMMAND = "lookupObject";
    /**
     * Constant for lookup peer command in command line.
     */
    private static final String LOOKUP_PEER_COMMAND = "lookupPeer";
    /**
     * Constant for leave command in command line.
     */
    private static final String LEAVE_COMMAND = "leave";
    /**
     * Constant for query command in command line.
     */
    private static final String QUERY_COMMAND = "query";
    /**
     * Constant for publish command in command line.
     */
    private static final String PUBLISH_COMMAND = "publish";
    /**
     * Constant for remove command in command line.
     */
    private static final String REMOVE_COMMAND = "remove";
    /**
     * Constant for show command in command line.
     */
    private static final String SHOW_COMMAND = "show";
    /**
     * Constant for send command in command line.
     */
    private static final String SEND_MESSAGE_COMMAND = "send";

    private static final String ROUTING = "routing";
    private static final String NEIGHBOR = "neighbor";
    private static final String BOOTSTRAP_PEERS = "bootstrap";

    private static final String NODE_COMMANDS_HELP = "\t" + JOIN_COMMAND + " overlayID ipAddress port\n" + "\t"
            + LOOKUP_OBJECT_COMMAND + " key\n" + "\t" + LEAVE_COMMAND + " UNDER_DISCUSSION\n" + "\t" + QUERY_COMMAND
            + " UNDER_DISCUSSION\n" + "\t" + PUBLISH_COMMAND + " key value\n" + "\t" + REMOVE_COMMAND
            + " key\n" + "\t" + SHOW_COMMAND + " what[" + ROUTING + "," + NEIGHBOR + "]";

    private static final String BOOTSTRAP_SERVER_COMMANDS_HELP = "\t" + SHOW_COMMAND + " " + BOOTSTRAP_PEERS;

    private boolean running;

    /**
     * Entities that receive messages from command line.
     */
    private final Vector<P2PPEntity> entities = new Vector<P2PPEntity>();

    /**
     * Constructor for issuing commands on given node.
     */
    public ConsoleInterface() {
        super("ConsoleInterface");
    }

    /**
     * Adds entity to a list of entities that are listening for messages from command line. TODO Probably only one Node
     * may be added.
     *
     * @param entity entity
     */
    public void addEntity(P2PPEntity entity) {
        entities.add(entity);
    }

    @Override
    public void run() {

        System.out.println("Console commands for node:\n" + NODE_COMMANDS_HELP + "\n"
                + "Console commands for bootstrap server:\n" + BOOTSTRAP_SERVER_COMMANDS_HELP);

        running = true;
        while (running) {

            String line = null;

            try {

                // scans standard input and gets one line from it
                Scanner in = new Scanner(System.in);
                line = in.nextLine();

                if (line != null) {

                    if (logger.isDebugEnabled()) logger.debug("Line read from input: " + line);

                    // splits line using space character as a separator... and gets first word (it is a type of command)
                    String[] tokens = line.split("\\s");
                    String commandType = tokens[0];

                    if (logger.isDebugEnabled()) logger.debug("commandType=" + commandType);

                    if (commandType.equals(JOIN_COMMAND)) {
                        System.out.println("Join command received in command line.");
                        onJoin(tokens);
                    } else if (commandType.equals(LOOKUP_OBJECT_COMMAND)) {
                        System.out.println("Lookup object command received in command line.");
                        onLookupObject(tokens);
                    } else if (commandType.equals(LOOKUP_PEER_COMMAND)) {
                        System.out.println("Lookup peer received in command line.");
                        onLookupPeer(tokens);
                    } else if (commandType.equals(LEAVE_COMMAND)) {
                        System.out.println("Leave command received in command line.");
                        onLeave(tokens);
                    } else if (commandType.equals(QUERY_COMMAND)) {
                        System.out.println("Query command received in command line.");
                        onQuery(tokens);
                    } else if (commandType.equals(PUBLISH_COMMAND)) {
                        System.out.println("Publish command received in command line.");
                        onPublish(tokens);
                    } else if (commandType.equals(REMOVE_COMMAND)) {
                        System.out.println("Remove command received in command line.");
                        onRemove(tokens);
                    } else if (commandType.equals(SHOW_COMMAND)) {
                        System.out.println("Show command received in command line.");
                        onShow(tokens);
                    } else if (SEND_MESSAGE_COMMAND.equals(commandType)) {
                        System.out.println("Send command received in command line.");
                        onSend(tokens);
                    } else {
                        System.out.println("Unknown command received in command line. Command=" + commandType);
                    }
                }

                // TODO probably delete... added so that CPU won't be consumed so much
                Thread.sleep(500);

            } catch (InterruptedException e) {
                logger.error(e.toString());
                System.out.println("Internal error. Program ends.");
            } catch (IllegalArgumentException e) {
                logger.error("Line=" + line);
                System.out.println("Bad arguments.\tUse proper arguments.");
            } catch (NullPointerException e) {
                logger.error("Line=" + line, e);
                System.out.println("Bad arguments.\tUse proper arguments.");
            } catch (NoSuchElementException e) {
                // ignored because it is thrown when there's nothing in Scanner
            }
        }
    }

    /**
     * Assumes that tokens contains "lookupPeer" at index 0.
     *
     * @param tokens tokens
     */
    private void onLookupPeer(String[] tokens) {
        logger.warn("Not implemented yet!.");
        System.out.println("Not implemented yet!.");
    }

    /**
     * Assumes that tokens contains "show" at index 0. At index 1 should be "routing", "neighbor" or "bootstrap".
     *
     * @param tokens tokens
     */
    private void onShow(String[] tokens) {
        String whatToken = tokens[1];
        if (whatToken.equalsIgnoreCase(ROUTING)) {

            boolean nodeFound = false;
            for (P2PPEntity current : this.entities) {
                if (current instanceof Node) {
                    nodeFound = true;
                    String[] entriesDescriptions = ((Node) current).getRoutingTableToString();
                    StringBuilder builder = new StringBuilder();
                    if (entriesDescriptions != null) {
                        for (String entryDesc : entriesDescriptions) {
                            builder.append("\t").append(entryDesc).append("\n");
                        }
                    } else {
                        builder.append("\tRouting table is empty.");
                    }
                    System.out.println("Routing table:\n" + builder.toString());

                    break;
                }

            }
            if (!nodeFound) {
                System.out.println("There's no local node running.");
                if (logger.isDebugEnabled()) logger.debug("There's no local node running.");
            }
        } else if (whatToken.equalsIgnoreCase(NEIGHBOR)) {

            logger.warn("Not implemented yet!.");
            System.out.println("Not implemented yet!.");

        } else if (whatToken.equalsIgnoreCase(BOOTSTRAP_PEERS)) {
            boolean bootstrapFound = false;
            for (P2PPEntity current : this.entities) {
                if (current instanceof BootstrapServer) {
                    bootstrapFound = true;
                    BootstrapServer server = (BootstrapServer) current;
                    String[] description = server.getDescriptionOfBootstrappedPeers();
                    StringBuilder builder = new StringBuilder();
                    for (String desc : description) {
                        builder.append("\t").append(desc).append("\n");
                    }

                    System.out.println("Bootstrapped peers:\n" + builder.toString());

                    break;
                }

            }
            if (!bootstrapFound) {
                System.out.println("There's no local node running.");
                logger.trace("There's no local node running.");
            }
        }

    }

    private void onRemove(String[] tokens) {

        try {
            String resourceID = tokens[1];

            synchronized (this.entities) {
                for (P2PPEntity current : this.entities) {
                    if (current instanceof Node) {
                        ((Node) current).remove(P2PPUtils.STRING_VALUE_CONTENT_TYPE, (byte) 0, resourceID.getBytes("UTF-8"), null);
                        break;
                    }
                }
            }

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException();
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("remove command must be followed with resourceID");
        } catch (Throwable e) {
            logger.error("Error while removing resource", e);
        }

    }

    private void onPublish(String[] tokens) {
        try {
            String resourceID = tokens[1];
            String resourceObjectValue = tokens[2];

            // TODO maybe synchornize?
            for (P2PPEntity current : entities) {

                if (current instanceof Node) {
                    ((Node) current).publish(resourceID.getBytes(), new StringValueResourceObject(resourceID, resourceObjectValue));
                    break;
                }
            }

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException();
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("publish command must be followed with resourceID and value.");
        }
    }

    private void onSend(String[] tokens) {
        try {
            String target = tokens[1];
            String protocol = tokens[2];
            String text = "";
            int tokensLength = tokens.length;
            for (int i=3; i<tokensLength; i++) {
                text += tokens[i];
                if (i < tokensLength) text += " ";
            }

            // TODO maybe synchornize?
            for (P2PPEntity current : entities) {

                if (current instanceof Node) {
                    ((Node) current).sendMessage(target.getBytes("UTF-8"), protocol, text.getBytes());
                    break;
                }
            }

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException();
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("send command must be followed with peerID, protocol and message text");
        } catch (Throwable e) {
            logger.error("Error onSend tokens=" + Arrays.asList(tokens), e);
        }
    }


    private void onQuery(String[] tokens) {
        logger.warn("Not implemented yet!.");
        System.out.println("Not implemented yet!.");
    }

    private void onLeave(String[] tokens) {
        for (P2PPEntity current : this.entities) {
            if (current instanceof Node) {
                try {
                    ((Node) current).leave();
                } catch (Throwable e) {
                    logger.error(e);
                }
                break;
            }
        }
    }

    private void onLookupObject(String[] tokens) {

        try {
            String key = tokens[1];

            synchronized (this.entities) {
                for (P2PPEntity current : this.entities) {
                    if (current instanceof Node) {
                        ((Node) current).lookup(P2PPUtils.STRING_VALUE_CONTENT_TYPE, (byte) 0, key.getBytes("UTF-8"), null);
                        break;
                    }
                }
            }

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("lookupObject command must be followed with a proper key.");
        }

    }

    /**
     * Analyzes given string array assuming that it contains "join" and its parameters ("join" is at index 0). Those
     * parameters are "overlayID", "overlayAddress" and "overlayPort". After getting their values from array, this
     * method invokes join command on connected node object.
     *
     * @param tokens tokens
     * @throws IllegalArgumentException Throws this when given tokenizer contains bad data.
     */
    private void onJoin(String[] tokens) throws IllegalArgumentException {

        try {
            byte[] overlayID = tokens[1].getBytes();
            String overlayAddress = tokens[2];
            int overlayPort = Integer.parseInt(tokens[3]);

            boolean nodeFound = false;
            // TODO maybe synchornize?
            for (P2PPEntity current : this.entities) {
                if (current instanceof Node) {
                    nodeFound = true;
                    ((Node) current).join(overlayID, overlayAddress, overlayPort);
                    break;
                }
            }
            if (!nodeFound) {
                System.out.println("There's no local node running.");
                logger.warn("There's no local node running.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException();
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("join command must be followed with overlayID, overlayAddress and overlayPort.");
        }
    }

    public void joinCallback() {
        if (logger.isInfoEnabled()) logger.info("JOINED.");
        System.out.println("JOINED.");
    }

    public void leaveCallback() {
        if (logger.isInfoEnabled()) logger.info("LEFT");
        System.out.println("LEFT.");
    }

    public void lookupCallback(Vector<ResourceObject> resourceObjects) {

        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (ResourceObject currentResource : resourceObjects) {
            builder.append(i).append(")type=").append(currentResource.getContentType()).append(", subtype=").append(currentResource.getContentSubtype()).
                    append(", id=").append(ByteUtils.byteArrayToHexString(currentResource.getResourceID().getResourceID())).append("; value=").
                    append(ByteUtils.byteArrayToHexString(currentResource.getValue().getValue()));
            i++;
        }
        if (logger.isInfoEnabled()) logger.info("Node has found a ResourceObject(s):" + builder.toString());
        System.out.println("Node has found a ResourceObject(s):" + builder.toString());
    }

    public void publishCallback(byte contentType, byte contentSubtype, byte[] key, byte[] resourceObjectValue) {
        if (logger.isInfoEnabled()) {
            logger.info("Resource with contentType=" + contentType + " subtype=" + contentSubtype + " key="
                    + new String(key) + " value=" + new String(resourceObjectValue));
        }
        System.out.println("Resource with contentType=" + contentType + " subtype=" + contentSubtype + " key="
                + new String(key) + " value=" + ByteUtils.byteArrayToHexString(resourceObjectValue));
    }

    public void queryCallback(byte[] overlayID, byte algorithm, byte hashAlgorithm, short hashAlgorithmLength) {
        if (logger.isInfoEnabled()) {
            logger.info("Node has received a query response: overlayID=" + ByteUtils.byteArrayToHexString(overlayID)
                    + ", p2palgorithm=" + algorithm + ", hashAlgorithm=" + hashAlgorithm + "hashAlgorithmLength="
                    + hashAlgorithmLength);
        }
        System.out.println("Node has received a query response: overlayID=" + ByteUtils.byteArrayToHexString(overlayID)
                + ", p2palgorithm=" + algorithm + ", hashAlgorithm=" + hashAlgorithm + "hashAlgorithmLength="
                + hashAlgorithmLength);
    }

    public void removeCallback() {
        if (logger.isInfoEnabled()) logger.info("Node has removed an object.");
        System.out.println("Node has removed an object.");
    }

    public void removeCallback(ResourceObject removedResource) {
        logger.info("Removed resource " + removedResource.getResourceID().toString());
    }

    public boolean onNeighborJoin(PeerInfo newNode, int nodeType) {
        // System.out.println("Node has new neighbor: type=" + nodeType + ", id="
        // + ByteUtils.byteArrayToHexString(newNode.getPeerID().getPeerIDBytes()));
        if (logger.isDebugEnabled()) {
            logger.debug("Node has new neighbor: type=" + nodeType + ", id="
                + ByteUtils.byteArrayToHexString(newNode.getPeerID().getPeerIDBytes()));
        }
        return true;
    }

    public void onNeighborLeave(PeerInfo newNode, int nodeType) {
        // System.out.println("Node's neighbor has left: type=" + nodeType + ", id="
        // + ByteUtils.byteArrayToHexString(newNode.getPeerID().getPeerIDBytes()));
        if (logger.isDebugEnabled()) {
            logger.debug("Node's neighbor has left: type=" + nodeType + ", id="
                + ByteUtils.byteArrayToHexString(newNode.getPeerID().getPeerIDBytes()));
        }
    }

    public boolean onDeliverRequest(Request request, List<ResourceObject> objectList) {

        // counts objects
        int i = 0;
        for (ResourceObject currentResource : objectList) {
            if (currentResource != null) {
                i++;
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Received request of " + request.getClass().getName() + " type and " + i
                + " resource objects were passed to callback.");
        }
        // System.out.println("Received request of " + request.getClass().getName() + " type and " + i
        // + " resource objects were passed to callback.");
        // TODO now always returns true (for testing P2PP)
        return true;
    }

    public boolean onForwardingRequest(Request request, List<ResourceObject> objectList) {
        // counts objects
        int i = 0;
        for (ResourceObject currentResource : objectList) {
            if (currentResource != null) {
                i++;
            }
        }

        // System.out.println("Received request of " + request.getClass().getName() + " type and " + i
        // + " resource objects were passed to callback.");
        logger.debug("Received request of " + request.getClass().getName() + " type and " + i
                + " resource objects were passed to callback.");
        // TODO now always returns true (for testing P2PP)
        return true;
    }

    public void errorCallback(ErrorInterface errorObject, int errorCode) {
        switch (errorCode) {
            case P2PPNodeCallback.BOOTSTRAP_ERROR_CODE: {
                if (logger.isInfoEnabled()) logger.info("Error during bootstrapping.");
                System.out.println("Error during bootstrapping.");
            }
            break;
            case P2PPNodeCallback.INSERT_ERROR_CODE: {
                if (logger.isInfoEnabled()) logger.info("Error during insert/publish.");
                System.out.println("Error during insert/publish.");
            }
            break;
            case P2PPNodeCallback.NAT_ERROR_CODE: {
                if (logger.isInfoEnabled()) logger.info("Error concerning NAT.");
                System.out.println("Error concerning NAT.");
            }
            break;
            case P2PPNodeCallback.RESOURCE_LOOKUP_ERROR_CODE: {
                if (logger.isInfoEnabled()) logger.info("Error during resource lookup.");
                System.out.println("Error during resource lookup.");
            }
            break;
            case P2PPNodeCallback.USER_LOOKUP_ERROR_CODE: {
                if (logger.isInfoEnabled()) logger.info("Error user lookup.");
                System.out.println("Error user lookup.");
            }
            break;
        }
    }
}
