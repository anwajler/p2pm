package pl.edu.pjwstk.mteam.pubsub.interestconditions;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Hashtable;

import pl.edu.pjwstk.mteam.pubsub.accesscontrol.AccessControlRule;
import pl.edu.pjwstk.mteam.pubsub.core.Operation;
import pl.edu.pjwstk.mteam.pubsub.core.Rule;
import pl.edu.pjwstk.mteam.pubsub.core.RuleSet;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.operation.NotifyOperation;

/**
 * Class representing set of interest conditions.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class InterestConditions extends RuleSet{

	/**
	 * Creates standard set of interest conditions for pub-sub. Automatically adds 
	 * rules for NOTIFY operation.
	 * @param t Topic, these interest conditions are defined for.
	 */
	public InterestConditions(Topic t){
		Operation notify = new NotifyOperation(t.getID());
		InterestCondition notifyCond = new InterestCondition(notify);
		addRule(notifyCond);
	}
	
	public InterestConditions(byte[] bytes){
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
	 * Check, whether operation is interesting according to this set of rules.
	 * @param o Operation to be examined.
	 */
	public boolean isInteresting(Operation o){
		return matches(o);
	}
	
	/**
	 * Checks if specified interest conditions are subset of this interest conditions
	 * @param compareWith Interest conditions to be examined. 
	 */
	public boolean contains(InterestConditions compareWith){
		//TODO: implement this to enable creation of subtrees with similar IC's
		return true;
	}
}
