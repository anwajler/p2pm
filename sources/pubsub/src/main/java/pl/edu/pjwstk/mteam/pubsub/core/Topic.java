package pl.edu.pjwstk.mteam.pubsub.core;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.accesscontrol.AccessControlRules;

/**
 * Class representing topic.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class Topic{

	static Logger logger = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.core.Topic");
	
	/**
	 * Node's parents for all the topics. This list is created because one node may
	 * be this node's parent for several topics and perhaps there could be only one 
	 * keep-alive message sent from the parent - not separate ones for each topic.
	 * Every <code>Topic</code> object stores only ID of the parent - for detailed
	 * information about the node (like IP address, port number, etc) it has to
	 * reference this list. 
	 */
	private static Hashtable<String, Subscriber> parents = new Hashtable<String, Subscriber>();
	/**
	 * Node's children for all the topics. This list is created because one node may
	 * be this node's child for several topics and perhaps it could send only one
	 * keep-alive message - not separate ones for each topic the child is associated 
	 * with.
	 */
	private static Hashtable<String, Subscriber> children = new Hashtable<String, Subscriber>();

//        /**
//         * Node's grandparents for all topics. This list is created because one node may
//	 * be this node's g-parents for several topics and perhaps it could send only one
//	 * keep-alive message - not separate ones for each topic.
//         */
//        private static Hashtable<String, Subscriber> gParents = new Hashtable<String, Subscriber>();

        private Hashtable<String, Subscriber> cache = new Hashtable<String, Subscriber>();
	
	/**
	 * Topic ID.
	 */
	protected String id;
	/**
	 * Distance between this node's id, and topic id
	 */
	protected int distance;
	/**
	 * User that has created this topic.
	 */
	protected Subscriber owner;
	
	protected AccessControlRules acRules;
	
	/**
	 * Node's parent for the topic on <code>parents</code> list. If this
	 * value is <code>null</code>, than specified node is topic root.
	 */
	protected String parentId;
	/**
	 * Node's children identifiers for the topic on <code>children</code>.
	 */
	protected Vector<String> childrenIds;

        protected String gparentId;

        protected Vector<String> neighbournsIDs;
        private NodeInfo proposedNewRoot = null;
        private AtomicInteger operationID = new AtomicInteger(0);

//    @Override
//    protected void finalize() throws Throwable {
//        this.acRules = null;
//        this.cache = null;
//        this.children = null;
//        this.childrenIds = null;
//        this.gparentId = null;
//        this.id = null;
//        this.neighbournsIDs = null;
//        this.owner = null;
//        this.parentId = null;
//        this.parents = null;
//        this.proposedNewRoot = null;
//    }
	
	public Topic(String topicID){
		this.id = topicID;
		this.owner = null;
		parentId = null;
		childrenIds = new Vector<String>();
		acRules = new AccessControlRules(this);
                neighbournsIDs = new Vector<String>();
		distance = -1;
	}
	
	public Topic(String topicID, NodeInfo parent){
		this(topicID);
		setParent(parent);
	}
        public Topic(String topicID, int latestOperationID){
            this(topicID);
            this.operationID = new AtomicInteger(latestOperationID);
        }
	
	public String getID(){
		return this.id;
	}
	
	/**
	 * 
	 * @return Distance between this node's ID and topic ID according to the
	 *         overlay-specific metrics.
	 */
	public int getDistance(){
		return distance;
	}
	
	/**
	 * @return User, that has created this topic or <code>null</code>, if it is not set.
	 */
	public Subscriber getOwner(){
		return this.owner;
	}
	
	public AccessControlRules getAccessControlRules(){
		return acRules;
	}
	
	public Subscriber getParent(){
		Subscriber prnt = null;
		if(parentId != null)
			prnt = parents.get(parentId);
		return prnt;
	}
	
	/**
	 * 
	 * @return Full information about specified child subscribed for this topic.
	 */
	public Subscriber getChild(String childId){
		if(childrenIds.contains(childId)){
			return children.get(childId);
		}
		return null; 
	}
	
	/**
	 * 
	 * @return Identifiers of all the children subscribed for this topic.
	 */
	public Vector<String> getChildren(){
            Vector<String> retVal = new Vector<String>();
            retVal.addAll(this.childrenIds);
		return retVal;            
	}
	
	/**
	 * Sets the distance between this node's ID and topic ID according to the
	 * overlay-specific metrics.
	 * @param newDistance New distance.
	 */
	public void setDistance(int newDistance){
		distance = newDistance;
	}
	
	public void setOwner(Subscriber newOwner){
		this.owner = newOwner;
	}
	
	public void setParent(NodeInfo parent){
		//removing topic reference to old parent
                //TODO uncomment it
//		if(parentId != null){
//			Subscriber oldPrnt = parents.get(parentId);
//			if(oldPrnt.removeTopic(this))
//				parents.remove(parentId);
//		}
                
		//setting new parent
		if(parent != null){
			parentId = parent.getID();
			Subscriber prnt = parents.get(parentId);
			if(prnt == null){
				prnt = new Subscriber(this, parent);
				parents.put(parentId, prnt);
			}
			else{
				prnt.addTopic(this);
			}
		}
		else{
			parentId = null;
		}
                //remove parentID from children list
//                if(this.childrenIds.contains(parent.getID())){
//                    this.childrenIds.remove(parent.getID());
//                }
	}
    public void setGrandParent(NodeInfo grandParentNodeInfo) {
        //removing topic reference to old parent
        //TODO uncomment it
//		if(gparentId != null){
//			Subscriber oldPrnt = parents.get(gparentId);
//			if(oldPrnt.removeTopic(this))
//				parents.remove(gparentId);
//                        oldPrnt = cache.get(gparentId);
//			if(oldPrnt.removeTopic(this))
//				cache.remove(gparentId);
//		}
		//setting new parent
		if(grandParentNodeInfo != null){
			gparentId = grandParentNodeInfo.getID();
			Subscriber prnt = parents.get(gparentId);
			if(prnt == null){
                            prnt = cache.get(gparentId);
                            if(prnt==null){
				prnt = new Subscriber(this, grandParentNodeInfo);				
                            }
			}
			else{
				prnt.addTopic(this);
			}
                        cache.put(gparentId, prnt);
		}
		else{
			gparentId = null;
		}
    }

    public void addNeighbourns(Vector<NodeInfo> neighbournIDs){
        Subscriber so = null;
        for(NodeInfo ni : neighbournIDs){
            if(this.neighbournsIDs.contains(ni.getID())) continue;
            so = cache.get(ni.getID());
            if(so==null){
                so = children.get(ni.getID());
                if(so==null){
                    so = new Subscriber(this, ni);
                }
                cache.put(ni.getID(), so);
            }
            this.neighbournsIDs.add(ni.getID());
        }
        
    }

    public Subscriber getNeighbourn(String n) {
        Subscriber s = null;
        if(n!=null) s = cache.get(n);
        return s;
    }

    public Subscriber getGrandParent() {
        Subscriber s = null;
        if(this.gparentId!=null){
            s = cache.get(this.gparentId);
        }
        return s;
    }
	
	public void setAccessControlRules(AccessControlRules ac){
		acRules = ac;
	}
	
	public boolean isTopicRoot(String id){
		boolean result = false;
		if(parentId == null){
			result = true;
		}else if(parentId.compareTo(id)==0){
                    result = true;
                }
		return result;
	}
	
	/**
	 * Adds new topic subscriber. All the subscribers are indexed by ID.
	 * @param newSubscriber
	 */
	public void addSubscriber(NodeInfo newSubscriber){
		if(!childrenIds.contains(newSubscriber.getID()))
			childrenIds.add(newSubscriber.getID());
		Subscriber chld = children.get(newSubscriber.getID());
		if(chld == null){
			chld = new Subscriber(this, newSubscriber);
			children.put(newSubscriber.getID(), chld);
			chld.addTopic(this);
		}
		else{
			chld.addTopic(this);
		}
	}
	
	/**
	 * 
	 * @param toRemoveNode
	 * @return If this value is <code>true</code>, there are no more subscribers for this topic - 
	 *         publish-subscribe manager may remove it, if this node is not the topic root.
	 */
	public boolean removeSubscriber(String id){
		Subscriber forRemoval = children.get(id);
		if(forRemoval != null){
			//removing from this topic children list 
			childrenIds.remove(id);
                        /*
                         * BUGFIX - there is no able to work with two instances 
                         * of PUB-SUB in one JVM when this blok is active 
                         */
//			if(forRemoval.removeTopic(this)){
//				/* there are no more topics, this node is child for, 
//				 * so it can be removed
//				 */
//				children.remove(id);
//			}
		}
		if(childrenIds.size() == 0)
			return true;
		return false;
	}
        /**
	 * s3544
	 * @param toRemoveNode child to remove from topic
	 * @return true if child was removed from cache.
	 */
        public boolean removeSubscriber(NodeInfo toRemoveNode){
           if(this.childrenIds.remove(toRemoveNode.getID())){
               return true;
           }
           else return false;
	}
	
	public boolean equals(Object compareWith){
		Topic t = (Topic)compareWith;
		if(t.getID().equals(getID()))
			return true;
		return false;
	}
	
	public String toString(){
		return "\nTopic:\n   id = "+id+
		       "\n   owner = "+owner+
		       "\n   parent = "+getParent();
	}
        public String DEBUG_toString(){
            StringBuilder children_ = new StringBuilder();
            StringBuilder neighbourns = new StringBuilder();
            for(String n : childrenIds){
                children_.append("\n\t"+getChild(n));
            }
            for(String n : neighbournsIDs){
                neighbourns.append("\n\t"+getNeighbourn(n));
            }

            return "\n***TOPIC:\n   id =\t"+getID()+
		       "\n   owner =\t"+getOwner()+
                       "\n   is root =\t"+isTopicRoot("")+
		       "\n   parent =\t"+getParent()+
                       "\n ======== CACHE ========= "+
                       "\n grandparent:\t"+this.getGrandParent()+
                       "\n parent:\t"+this.getParent()+
                       "\n proposed ROOT:\t"+this.proposedNewRoot+
                       "\n children:\t"+children_.toString()+
                       "\n neighbours:\t"+neighbourns+
                       "\n ======== \\CACHE ========= ";
        }
	
	public void DEBUG_showParent(){
		Subscriber prnt = null;
		if(parentId != null)
			prnt = parents.get(parentId);
		logger.debug("Parent for the topic \""+getID()+"\": "+prnt);
	}
	
	public void DEBUG_showChildren(){
		String msg = "\nChildren for the topic: "+this.getID()+"\n";
		Iterator<String> iterator = childrenIds.iterator();
		while(iterator.hasNext()){
			msg += children.get(iterator.next())+"\n";
		}
		logger.debug(msg);
	}

	public static void DEBUG_showParents(){
		String msg = "\nAll node's parents:\n";
		Enumeration<Subscriber> prnts = parents.elements();
		while(prnts.hasMoreElements()){
			Subscriber s = prnts.nextElement();
			msg += s.DEBUG_getTopics()+"\n";
		}
		logger.debug(msg);
	}
	
	public static void DEBUG_showAllChildren(){
		String msg = "\nAll node's children:\n";
		Enumeration<Subscriber> chdlrn = children.elements();
		while(chdlrn.hasMoreElements()){
			Subscriber s = chdlrn.nextElement();
			msg += s.DEBUG_getTopics()+"\n";
		}
		logger.debug(msg);
	}

    public void removeNeighbour(Vector<NodeInfo> neighbournIDs) {
        Subscriber so = null;
        for(NodeInfo ni : neighbournIDs){
            this.neighbournsIDs.remove(ni.getID());
            this.cache.remove(ni.getID());            
        }
    }

    public NodeInfo getProposedNewRoot() {
        return this.proposedNewRoot;
    }
    public void setProposedNewRoot(NodeInfo newRoot){
        this.proposedNewRoot = newRoot;
    }

    public void removeNeighbour(String iD) {
        if(this.neighbournsIDs.contains(iD)) this.neighbournsIDs.remove(iD);
    }

    public Vector<String> getNeighbours() {
        return this.neighbournsIDs;
    }

    public synchronized int getCurrentOperationID() {
        return this.operationID.get();
    }
    public synchronized int increaseCurrentOperation(String nodeName,int opID){
        int retVal = 0;
        if(opID == -10){
            retVal = this.operationID.incrementAndGet();         
            if(logger.isTraceEnabled())
                logger.trace(nodeName+":"+this.id+" operation ID+=1 to value:"+this.operationID);
            return retVal;
        }else{
            if(opID>=this.operationID.get()){
                this.operationID.set(opID);
                if(logger.isTraceEnabled())
                    logger.trace(nodeName+":"+this.id+" operation ID set to value:"+this.operationID);
            }else{
                logger.debug(nodeName+":"+this.id+" received indication with older operationID: (current="+this.operationID+"), received:"+opID);
            }
        }
        retVal = this.operationID.get();
        return retVal;
    }
}
