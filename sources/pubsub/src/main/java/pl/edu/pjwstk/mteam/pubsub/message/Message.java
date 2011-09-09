package pl.edu.pjwstk.mteam.pubsub.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.h2.command.ddl.DeallocateProcedure;

import pl.edu.pjwstk.mteam.core.GeneralNodeInfo;


/*
 *
 *
 * Message header format:
 *
 *     0                   1                   2                   3
 *     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |    IP version     |		   Source IP                  //
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     //                  |   		Destination IP                //
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     //                  |	       Source port                //
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     //                  |        Destination port              //
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     //                  |
 *     +-+-+-+-+-+-+-+-+-+-+
 *
 *	   IP version (8 bits): The IP version number, 4 or 6
 *
 *	   Source IP (32 or 64 bits): IP address of message sender
 *
 *	   Destination IP (32 or 64 bits): IP address of message receiver
 *
 *
 */
/**
 * Abstract message pattern. Its header contains only general information like:
 * IP version, source and destination IP addresses and ports.
 *
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public abstract class Message {

    /**
     * Value indicating, that source and destination IP addresses are IPv4
     */
    private static final byte FLAG_IPv4 = 4;
    /**
     * Value indicating, that source and destination IP addresses are IPv6
     */
    private static final byte FLAG_IPv6 = 6;
    /**
     * Value indicating, that source and destination IP addresses are IPv6
     */
    private static final byte FLAG_IPv46 = 10;
    /**
     * Header length, when source and destination IP addresses are IPv4
     */
    private static final int LENGTH_IPv4 = 19;
    /**
     * Header length, when source and destination IP addresses are IPv6
     */
    private static final int LENGTH_IPv6 = 43;
    /**
    * Header length, when source and destination IP addresses are mixed versions
    */
   private static final int LENGTH_IPv46 = 31;
    /**
     * Value indicating, that error occurred
     */
    private static final int ERROR = -1;
    /**
     * Value indicating source and destination IP version (IPv4, IPv6, mixed)
     */
    private byte ipVersion;

    private byte sourceIpVersion;

    private byte destIpVersion;
    /**
     * Source IP address and port number
     */
    private GeneralNodeInfo destination;
    /**
     * Destination IP address and port number
     */
    private GeneralNodeInfo source;

    /**
     * Creates new message.
     * @param src Message sender.
     * @param dest Message destination.
     */
    public Message(GeneralNodeInfo src, GeneralNodeInfo dest) {
        source = src;
        destination = dest;
        ipVersion = 2*FLAG_IPv4;
    }

    @Override
    protected void finalize() throws Throwable {
        this.destination = null;
        this.source = null;
        super.finalize();
    }

    private ByteArrayOutputStream ostream;
    private DataOutputStream dtstr;
    private byte[] msgToByte;

    /**
     * Used for preparing message for sending
     * @return Bytes to be send
     */
    public byte[] encode() {
    	ipVersion = 0;
    	try {
            if (InetAddress.getByName(getSourceInfo().getIP()).getAddress().length == 4) {
                ipVersion += FLAG_IPv4;
                sourceIpVersion = FLAG_IPv4;
            }
            else {
                ipVersion += FLAG_IPv6;
                sourceIpVersion = FLAG_IPv6;
            }
            if (InetAddress.getByName(getDestinationInfo().getIP()).getAddress().length == 4) {
                ipVersion += FLAG_IPv4;
                destIpVersion = FLAG_IPv4;
            }
            else {
                ipVersion += FLAG_IPv6;
                destIpVersion = FLAG_IPv6;
            }
        } catch (Exception e) {}

        ostream = new ByteArrayOutputStream();
        dtstr = new DataOutputStream(ostream);
        //writing IP version
        ostream.write(ipVersion);
        try {
        	//writing source IP version
        	ostream.write(sourceIpVersion);
        	//writing destination IP version
        	ostream.write(destIpVersion);
            //writing source IP address
            dtstr.write(InetAddress.getByName(source.getIP()).getAddress());
            //writing destination IP address
            dtstr.write(InetAddress.getByName(destination.getIP()).getAddress());
            //writing source port number
            dtstr.writeInt(source.getPort());
            //writing destination port number
            dtstr.writeInt(destination.getPort());
            msgToByte = ostream.toByteArray();
        } catch (Exception e) {
        } finally {
            try {
                ostream.close();
                ostream = null;
                dtstr = null;
            } catch (IOException ex) {
            }
        }
        return msgToByte;
    }
    private ByteArrayInputStream istream;
    private DataInputStream distr;

    /**
     * Used for parsing received byte stream
     * @param stream Bytes read directly from socket
     */
    public void parse(byte[] stream) {
        istream = new ByteArrayInputStream(stream);
        distr = new DataInputStream(istream);
        //reading IP version
        ipVersion = (byte) istream.read();
        try {
            //reading source & destination IP address
        	sourceIpVersion = distr.readByte();
        	destIpVersion = distr.readByte();
            byte[] sourceIP = null, destIP = null;
            if (sourceIpVersion == FLAG_IPv4) {
                sourceIP = new byte[4];
            }else{
            	sourceIP = new byte[16];
            }
            if (destIpVersion == FLAG_IPv4){
                destIP = new byte[4];
            }else{
                destIP = new byte[16];
            }
            distr.read(sourceIP);
            distr.read(destIP);
            InetAddress srcAddr = InetAddress.getByAddress(sourceIP);
            InetAddress destAddr = InetAddress.getByAddress(destIP);
            //reading source & destination port
            int sourcePort = distr.readInt();
            int destPort = distr.readInt();
            source.setIP(srcAddr.getHostAddress());
            source.setPort(sourcePort);
            destination.setIP(destAddr.getHostAddress());
            destination.setPort(destPort);
        } catch (Exception e) {
        } finally {
            try {
                istream.close();
                istream = null;
                distr = null;
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Returns GeneralNodeInfo object describing, where to send message
     * @return GeneralNodeInfo object (containing peer ID, IP address, port number, etc.)
     */
    public GeneralNodeInfo getDestinationInfo() {
        return destination;
    }

    /**
     * Returns GeneralNodeInfo object describing message sender
     * @return GeneralNodeInfo object (containing peer ID, IP address, port number, etc.)
     */
    public GeneralNodeInfo getSourceInfo() {
        return source;
    }

    /**
     * @return Returns IP version. Possible values are:
     * 		   <li> {@link #FLAG_IPv4},
     * 		   <li> {@link #FLAG_IPv6}.
     */
    public byte getIPVersion() {
        return ipVersion;
    }

    /**
     * Sets IP version for encoding source and destination addresses.
     * @param ipVer Acceptable values are:
     * 				<li> {@link #FLAG_IPv4},
     * 				<li> {@link #FLAG_IPv6}.
     */
    public void setIPVersion(byte ipVer) {
        ipVersion = ipVer;
    }

    /**
     * @return Header length - useful while parsing (for subclasses).
     */
    protected static int getHeaderLength(byte[] stream) {
    	//System.out.println(stream[0]);
        if (stream[0] == 8) {
            return LENGTH_IPv4;
        } else if (stream[0] == 12) {
            return LENGTH_IPv6;
        } else if (stream[0] == 10){
        	return LENGTH_IPv46;
        }else{
            return ERROR;
        }
    }
}