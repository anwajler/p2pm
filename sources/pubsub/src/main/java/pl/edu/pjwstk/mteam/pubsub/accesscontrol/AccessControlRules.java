package pl.edu.pjwstk.mteam.pubsub.accesscontrol;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Hashtable;

import pl.edu.pjwstk.mteam.pubsub.core.Event;
import pl.edu.pjwstk.mteam.pubsub.core.Operation;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.Rule;
import pl.edu.pjwstk.mteam.pubsub.core.RuleSet;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.operation.CreateTopicOperation;
import pl.edu.pjwstk.mteam.pubsub.operation.KeepAliveOperation;
import pl.edu.pjwstk.mteam.pubsub.operation.NotifyOperation;
import pl.edu.pjwstk.mteam.pubsub.operation.PublishOperation;
import pl.edu.pjwstk.mteam.pubsub.operation.SubscribeOperation;
import pl.edu.pjwstk.mteam.pubsub.operation.UnsubscribeOperation;

/**
 * Class representing set of access control rules.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class AccessControlRules extends RuleSet{

	/**
	 * Sets default limitations. Only topic owner can remove topic or modify access
	 * control rules.
	 * 
	 * @param t Topic, these rules are defined for.
	 */
	public AccessControlRules(Topic t){
		Operation createTopic = new CreateTopicOperation(t.getID());
		Operation subscribe = new SubscribeOperation(t.getID(), null, new Event(PubSubConstants.EVENT_ALL));
		Operation unsubscribe = new UnsubscribeOperation(t.getID());
		Operation publish = new PublishOperation(t.getID());
		Operation notify = new NotifyOperation(t.getID());
		Operation keepalive = new KeepAliveOperation(t.getID(), null, new Event(PubSubConstants.EVENT_ALL));
		AccessControlRule createTopicRule = new AccessControlRule(createTopic);
		AccessControlRule subscribeRule = new AccessControlRule(subscribe);
		AccessControlRule unsubscribeRule = new AccessControlRule(unsubscribe);
		AccessControlRule publishRule = new AccessControlRule(publish);
		publishRule.addUser(PubSubConstants.EVENT_REMOVETOPIC, t.getOwner());
		publishRule.addUser(PubSubConstants.EVENT_MODIFYAC, t.getOwner());
		AccessControlRule notifyRule = new AccessControlRule(notify);
		AccessControlRule keepaliveRule = new AccessControlRule(keepalive);
		addRule(createTopicRule);
		addRule(subscribeRule);
		addRule(unsubscribeRule);
		addRule(publishRule);
		addRule(notifyRule);
		addRule(keepaliveRule);
		t.setAccessControlRules(this);
	}
	
	public AccessControlRules(byte[] bytes){
		ByteArrayInputStream istr = new ByteArrayInputStream(bytes);
		DataInputStream dtstr = new DataInputStream(istr);
		
		rules = new Hashtable<Short, Rule>();
		
		try {
			int ruleNum = dtstr.readInt();
			for(int i=0; i<ruleNum; i++){
				int encRuleLen = dtstr.readInt();
				byte[] encRule = new byte[encRuleLen];
				dtstr.read(encRule);
				addRule(new AccessControlRule(encRule));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Check, whether operation is allowed according to this set of rules.
	 * @param o 
	 * @return Value indicating, whether the requested operation is allowed
	 *         or not.
	 */
	public boolean isAllowed(Operation o){
		return matches(o);
	}
}
