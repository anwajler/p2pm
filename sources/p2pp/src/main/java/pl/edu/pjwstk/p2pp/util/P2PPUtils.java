package pl.edu.pjwstk.p2pp.util;

import pl.edu.pjwstk.p2pp.entities.BootstrapServer;
import pl.edu.pjwstk.p2pp.entities.Peer;
import pl.edu.pjwstk.p2pp.kademlia.KademliaBoostrapServer;
import pl.edu.pjwstk.p2pp.kademlia.KademliaPeer;
import pl.edu.pjwstk.p2pp.superpeer.SuperPeerBootstrapServer;
import pl.edu.pjwstk.p2pp.superpeer.SuperPeerPeer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class with constants and common methods for P2PP.
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 */
public final class P2PPUtils {

    public static final byte[] STUN_SERVICE_ID = "STUN_SERVICE".getBytes();
    public static final byte[] TURN_SERVICE_ID = "TURN_SERVICE".getBytes();
    public static final byte[] ICE_SERVICE_ID = "ICE_SERVICE".getBytes();

    public static final byte NONE_HASH_ALGORITHM = 0;
    public static final byte SHA1_HASH_ALGORITHM = 1;
    public static final byte SHA1_256_HASH_ALGORITHM = 2;
    public static final byte SHA1_512_HASH_ALGORITHM = 3;
    public static final byte MD4_HASH_ALGORITHM = 4;
    public static final byte MD5_HASH_ALGORITHM = 5;

    public static final byte CHORD_P2P_ALGORITHM = 0;
    public static final byte CAN_P2P_ALGORITHM = 1;
    public static final byte KADEMLIA_P2P_ALGORITHM = 2;
    public static final byte PASTRY_P2P_ALGORITHM = 3;
    public static final byte BAMBOO_P2P_ALGORITHM = 4;
    public static final byte TAPESTRY_P2P_ALGORITHM = 5;
    public static final byte ACCORDION_P2P_ALGORITHM = 6;
    public static final byte SKIPNET_P2P_ALGORITHM = 7;
    public static final byte MERCURY_P2P_ALGORITHM = 8;
    public static final byte GIA_P2P_ALGORITHM = 9;
    public static final byte SOCIALCIRCLE_P2P_ALGORITHM = 10;
    public static final byte SUPERPEER_P2P_ALGORITHM = 11;

    public static final byte USER_INFO_CONTENT_TYPE = 0;
    public static final byte USER_INFO_CONTENT_SUBTYPE = 0;
    public static final byte STUN_CONTENT_TYPE = 1;
    public static final byte TURN_CONTENT_TYPE = 2;
    public static final byte STUN_TURN_ICE_CONTENT_TYPE = 3;
    public static final byte STRING_VALUE_CONTENT_TYPE = 4;
    public static final byte SIP_CONTENT_TYPE = 5;
    public static final byte SUBSCRIPTION_INFO_CONTENT_TYPE = 6;
    public static final byte PENDING_SUBSCRIPTION_INFO_CONTENT_SUBTYPE = 0;
    public static final byte ACCEPTED_SUBSCRIPTION_INFO_CONTENT_SUBTYPE = 1;
    public static final byte MESSAGE_CONTENT_TYPE = 7;

    /**
     * Converts hash algorithm name to byte representing it in P2PP.
     *
     * @param name Name of hash algorithm. "-" can be omitted if present in name.
     * @return
     */
    public static byte convertHashAlgorithmName(String name) throws NoSuchAlgorithmException {
        if (name.equals("SHA-1") || name.equals("SHA1")) {
            return SHA1_HASH_ALGORITHM;
        } else if (name.equals("SHA256") || name.equals("SHA-256")) {
            return SHA1_256_HASH_ALGORITHM;
        } else if (name.equals("SHA-512") || name.equals("SHA512")) {
            return SHA1_512_HASH_ALGORITHM;
        } else if (name.equals("MD4")) {
            return MD4_HASH_ALGORITHM;
        } else if (name.equals("MD5")) {
            return MD5_HASH_ALGORITHM;
        }
        throw new NoSuchAlgorithmException(name + " hash algorithm is not supported by P2PP.");
    }

    /**
     * Converts P2P algorithm name to byte representing it in P2PP. Comparison ignores cases.
     *
     * @param name
     * @return
     * @throws NoSuchAlgorithmException Throws if given algorithm can't be handled.
     */
    public static byte convertP2PAlgorithmName(String name) throws NoSuchAlgorithmException {
        if (name.equalsIgnoreCase("Chord")) {
            return CHORD_P2P_ALGORITHM;
        } else if (name.equalsIgnoreCase("CAN")) {
            return CAN_P2P_ALGORITHM;
        } else if (name.equalsIgnoreCase("Kademlia")) {
            return KADEMLIA_P2P_ALGORITHM;
        } else if (name.equalsIgnoreCase("Pastry")) {
            return PASTRY_P2P_ALGORITHM;
        } else if (name.equalsIgnoreCase("Bamboo")) {
            return BAMBOO_P2P_ALGORITHM;
        } else if (name.equalsIgnoreCase("Tapestry")) {
            return TAPESTRY_P2P_ALGORITHM;
        } else if (name.equalsIgnoreCase("Accordion")) {
            return ACCORDION_P2P_ALGORITHM;
        } else if (name.equalsIgnoreCase("SkipNet")) {
            return SKIPNET_P2P_ALGORITHM;
        } else if (name.equalsIgnoreCase("Mercury")) {
            return MERCURY_P2P_ALGORITHM;
        } else if (name.equalsIgnoreCase("Gia")) {
            return GIA_P2P_ALGORITHM;
        } else if (name.equalsIgnoreCase("SocialCircle")) {
            return SOCIALCIRCLE_P2P_ALGORITHM;
        } else if (name.equalsIgnoreCase("SuperPeer")) {
            return SUPERPEER_P2P_ALGORITHM;
        }
        throw new NoSuchAlgorithmException(name + " P2P algorithm is not supported by P2PP");
    }

    /**
     * Converts given hash algorithm ID to hashAlgorithm name.
     *
     * @param hashAlgorithmID
     * @return
     * @throws NoSuchAlgorithmException When hash algorithm with given ID is not supported.
     */
    public static String convertHashAlgorithmID(byte hashAlgorithmID) throws NoSuchAlgorithmException {
        switch (hashAlgorithmID) {
            case NONE_HASH_ALGORITHM:
                return "NONE";
            case SHA1_HASH_ALGORITHM:
                return "SHA1";
            case SHA1_256_HASH_ALGORITHM:
                return "SHA-256";
            case SHA1_512_HASH_ALGORITHM:
                return "SHA-512";
            case MD4_HASH_ALGORITHM:
                return "MD4";
            case MD5_HASH_ALGORITHM:
                return "MD5";
            default:
                throw new NoSuchAlgorithmException("Hash algorithm with " + hashAlgorithmID + " ID can't be handled.");
        }
    }

    /**
     * Creates hash of given bytes using algorithm identified by constant defined in this class.
     *
     * @param toBeHashed
     * @param hashAlgorithm
     * @return
     * @throws NoSuchAlgorithmException When algorithm is not supported.
     */
    public static byte[] hash(byte[] toBeHashed, byte hashAlgorithm) throws NoSuchAlgorithmException {
        String hashAlgorithmName = null;
        switch (hashAlgorithm) {
            case NONE_HASH_ALGORITHM:
                // TODO do something?
                break;
            case SHA1_HASH_ALGORITHM:
                hashAlgorithmName = "SHA1";
                break;
            case SHA1_256_HASH_ALGORITHM:
                hashAlgorithmName = "SHA-256";
                break;
            case SHA1_512_HASH_ALGORITHM:
                hashAlgorithmName = "SHA-512";
                break;
            case MD4_HASH_ALGORITHM:
                hashAlgorithmName = "MD4";
                break;
            case MD5_HASH_ALGORITHM:
                hashAlgorithmName = "MD5";
                break;
        }
        MessageDigest digest = MessageDigest.getInstance(hashAlgorithmName);
        return digest.digest(toBeHashed);
    }

    /**
     * Returns protocol name for given preferences. TCP for true, false. UDP for false, false.
     * <p/>
     * <ul>
     * <li>false, false -> UDP</li>
     * <li>true, false -> TCP</li>
     * <li>true, true -> TLS</li>
     * <li>false, true -> DTLS</li>
     * </ul>
     *
     * @param overReliable
     * @param encrypted
     * @return
     */
    public static String protocolNameForPreferences(boolean overReliable, boolean encrypted) {
        if (overReliable) {
            if (encrypted) {
                return "TLS";
            } else {
                return "TCP";
			}
		} else {
			if (encrypted) {
				return "DTLS";
			} else {
				return "UDP";
			}
		}
	}

    public BootstrapServer getBootstrapForProtocol(byte protocolByte) throws NoSuchAlgorithmException {
        switch (protocolByte) {
            case KADEMLIA_P2P_ALGORITHM:
                return new KademliaBoostrapServer();
            case SUPERPEER_P2P_ALGORITHM:
                return new SuperPeerBootstrapServer();
            default:
                throw new NoSuchAlgorithmException("P2P algorithm id=" + protocolByte + " is not supported.");
        }
    }

    public Peer getPeerForProtocol(byte protocolByte) throws NoSuchAlgorithmException {
        switch (protocolByte) {
            case KADEMLIA_P2P_ALGORITHM:
                return new KademliaPeer();
            case SUPERPEER_P2P_ALGORITHM:
                return new SuperPeerPeer();
            default:
                throw new NoSuchAlgorithmException("P2P algorithm id=" + protocolByte + " is not supported.");
        }
    }

}
