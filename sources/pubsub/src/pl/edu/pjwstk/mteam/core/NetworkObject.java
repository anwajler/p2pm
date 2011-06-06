package pl.edu.pjwstk.mteam.core;

/**
 * Class representing object, that can be stored in P2P network.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class NetworkObject{
	/**
	 * Object type used for storing standard String value
	 */
	public static final int TYPE_BYTEARRAY = 4;
	/**
	 * Object type used for storing tunneling publish-subscribe messages
	 * inside P2P insert requests
	 */
	public static final int TYPE_PUBSUB = 7;
	public static final int TYPE_PROTOTRUST = 8;
	
	private int    type;
	private String key;
	private byte[] value;
	
	/**
	 * Creates new <code>NetworkObject</code>.
	 * @param t Object type. Accepted values are:<p>
	 *          <li>{@link #TYPE_BYTEARRAY}, 
	 *          <li>{@link #TYPE_PUBSUB},
	 *          <li>{@link #TYPE_PROTOTRUST}.
	 * @param k Object identifier in network.
	 * @param val Value stored inside this <code>NetworkObject</code>.
	 */
	public NetworkObject(int t, String k, byte[] val){
		type = t;
		key = k;
		value = val;
	}
	
	/**
	 * @return Object type. Possible values are:<p>
	 *         <li>{@link #TYPE_BYTEARRAY}, 
	 *         <li>{@link #TYPE_PUBSUB}.
	 */
	public int getType(){
		return type;
	}
	
	/**
	 * @return Object identifier in network.
	 */
	public String getKey(){
		return key;
	}
	
	/**
	 * @return Value stored inside this object.
	 */
	public byte[] getValue(){
		return value;
	}
}
