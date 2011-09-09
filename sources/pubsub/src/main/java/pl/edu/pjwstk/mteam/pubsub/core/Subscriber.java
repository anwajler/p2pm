package pl.edu.pjwstk.mteam.pubsub.core;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;

import pl.edu.pjwstk.mteam.pubsub.logging.Logger;

import pl.edu.pjwstk.mteam.core.NodeInfo;

/**
 * Class representing topic subscriber.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class Subscriber extends User implements Serializable{
	static Logger logger = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.core.Subscriber");;
	
	/**
	 * List of the topics, this subscriber is associated with.
	 */
	protected Hashtable<String, Topic> topics;
	/**
	 * List of the subscriptions containing IC and expiration time
	 * associated with the particular topic.
	 */
	protected Hashtable<String, Subscription> subscriptions;


	public Subscriber(String name, Topic t){
		super(name);
		topics = new Hashtable<String, Topic>();
		subscriptions = new Hashtable<String, Subscription>();
	}

    @Override
    protected void finalize() throws Throwable {
        this.topics = null;
        this.subscriptions = null;
        super.finalize();
    }

	
	public Subscriber(Topic t, NodeInfo ninf){
		super(ninf);
		topics = new Hashtable<String, Topic>();
		subscriptions = new Hashtable<String, Subscription>();
	}
	
	public Subscriber(Topic t, NodeInfo ninf, int expirationTime){
		this(t, ninf);
		//add expiration time to the Subscription object
	}
	
	public Subscriber(byte[] bytes){
		super(bytes);
		subscriptions = new Hashtable<String, Subscription>();
	}
	
	public Subscription getSubscription(String topicID){
		Subscription res = subscriptions.get(topicID);
		return res;
	}
	
	public void addTopic(Topic t){
		logger.debug(this.nodeInfo.getName()+" - Trying to add topic: '"+ t.getID()+"'");
		topics.put(t.getID(), t);
		subscriptions.put(t.getID(), new Subscription());
	}
	
	/**
	 * 
	 * @param t
	 * @return If this value is <code>true</code>, it means there are no more topics
	 *         this subscriber is associated with. If it was one of the node's parents
	 *         or children - it can be removed from the appropriate list. Otherwise
	 *         there are still some topics, this <code>Subscriber</code> object refers
	 *         to and it should not be deleted.
	 */
	public boolean removeTopic(Topic t){
		logger.debug(this.nodeInfo.getName()+" - Trying to remove topic: "+ t.getID());
		topics.remove(t.getID());
		subscriptions.remove(t.getID());
		if(topics.size() == 0)
			return true;
		return false;
	}
	
	public String DEBUG_getTopics(){
		String msg = "\nTopics for: "+this;
		Enumeration<Topic> tpcs = topics.elements();
		while(tpcs.hasMoreElements()){
			msg += tpcs.nextElement();
		}
		return msg;
	}
	
	public void DEBUG_showTopics(){
		logger.debug(DEBUG_getTopics());
	}
}
