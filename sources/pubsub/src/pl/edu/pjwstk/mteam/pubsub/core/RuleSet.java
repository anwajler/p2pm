package pl.edu.pjwstk.mteam.pubsub.core;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Class representing set of rules.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public abstract class RuleSet{
	protected static final byte MODIFICATION_ADDRULE = 0;
	protected static final byte MODIFICATION_REMOVERULE = 1;
	/**
	 * Rules stored in this set (indexed by operation type, they are
	 * associated with).
	 */
	protected Hashtable<Byte, Rule> rules;
	
	/**
	 * Creates new set of rules.
	 */
	public RuleSet(){
		rules = new Hashtable<Byte, Rule>();
	}
	
	/**
	 * Checks whether specified operation isn't against defined rules.
	 * @param o Operation to be checked.
	 * @return Value indicating, whether specified operation is OK according to
	 *         defined rules.
	 */
	protected boolean matches(Operation o){
		boolean result = false;
		Rule r = rules.get(o.getType());
		if(r != null){
			result = r.matches(o);
		}
		return result;
	}
	
	/**
	 * Adds new rule or overrides existing one (if there 
	 * already is any rule for specified operation).
	 * @param newRule New rule to be added to this set.
	 * @return Replaced rule for specified operation (if it already 
	 *         existed) or <code>null</code> otherwise. 
	 */
	public Rule addRule(Rule newRule){
		return rules.put(newRule.getOperation().getType(), newRule);
	}
	
	/**
	 * Removes rule from this set.
	 * @param operationType Value indicating, which rule remove.
	 * @return Removed rule or <code>null</code> if there was no rule for 
	 *         specified operation. 
	 */
	public Rule removeRule(byte operationType){
		return rules.remove(new Byte(operationType));
	}
	
	/**
	 * @param operationType Type of operation requested rule is associated with.
	 * @return Rule for operation of specified type or <code>null</code> if it
	 * 	 	   is not defined in this set. 
	 */
	public Rule getRule(byte operationType){
		return rules.get(operationType);
	}
	
	public byte[] encode(){
		ByteArrayOutputStream ostr = new ByteArrayOutputStream();
		DataOutputStream dtstr = new DataOutputStream(ostr);
		
		try {
			//writing rules number
			dtstr.writeInt(rules.size());
			Collection<Rule> rcont = rules.values(); 
			Iterator<Rule> it = rcont.iterator();
			while(it.hasNext()){
				Rule r = (Rule)it.next();
				byte[] encRule = r.encode();
				//writing rule object length and rule itself
				dtstr.writeInt(encRule.length);
				dtstr.write(encRule);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return ostr.toByteArray();
	}
	
	public String toString(){
		Collection<Rule> rcont = rules.values(); 
		Iterator<Rule> it = rcont.iterator();
		String result = "\nRules:\n";
		int i = 0; 
		while(it.hasNext()){
			i++;
			result += i+") "+it.next();
		}
		return result;
	}
}
