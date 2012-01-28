package pl.edu.pjwstk.mteam.jcsync.core;

import pl.edu.pjwstk.mteam.jcsync.operation.RegisteredOperations;
import pl.edu.pjwstk.mteam.pubsub.accesscontrol.AccessControlRules;
import pl.edu.pjwstk.mteam.pubsub.core.Event;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import static pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants.*;
import static pl.edu.pjwstk.mteam.jcsync.operation.RegisteredOperations.*;
/**
 * The extension of {@link pl.edu.pjwstk.mteam.pubsub.accesscontrol.AccessControlRules AccessControlRules}, 
 * just holds new events related with jcsync mechanism which are described in 
 * {@link RegisteredOperations RegisteredOperations}.
 * @see pl.edu.pjwstk.mteam.pubsub.accesscontrol.AccessControlRules
 * @see RegisteredOperations
 * @author Piotr Bucior
 */
public class AccessControlLists extends AccessControlRules {
    
    
    public AccessControlLists(Topic t){
        super(t);
        //just register new event types related with jcsync 
        getRule(OPERATION_PUBLISH).getOperation().addEvent(new Event(OP_REQ_TRANSFER_OBJECT));
        getRule(OPERATION_PUBLISH).addUser(OP_REQ_TRANSFER_OBJECT, null);
        getRule(OPERATION_NOTIFY).getOperation().addEvent(new Event(OP_IND_TRANSFER_OBJECT));
        getRule(OPERATION_NOTIFY).addUser(OP_IND_TRANSFER_OBJECT, null);
        
        getRule(OPERATION_PUBLISH).getOperation().addEvent(new Event(OP_REQ_LOCK_APPLY));
        getRule(OPERATION_PUBLISH).addUser(OP_REQ_LOCK_APPLY, null);
        
        getRule(OPERATION_NOTIFY).getOperation().addEvent(new Event(OP_IND_LOCK_APPLY));
        getRule(OPERATION_NOTIFY).addUser(OP_IND_LOCK_APPLY, null);
        
        getRule(OPERATION_PUBLISH).getOperation().addEvent(new Event(OP_REQ_LOCK_RELEASE));
        getRule(OPERATION_PUBLISH).addUser(OP_REQ_LOCK_RELEASE, null);
        
        getRule(OPERATION_NOTIFY).getOperation().addEvent(new Event(OP_IND_LOCK_RELEASE));
        getRule(OPERATION_NOTIFY).addUser(OP_IND_LOCK_RELEASE, null);
        
        getRule(OPERATION_NOTIFY).getOperation().addEvent(new Event(OP_IND_WRITE_METHOD));
        getRule(OPERATION_NOTIFY).addUser(OP_IND_WRITE_METHOD, null);
        
        getRule(OPERATION_PUBLISH).getOperation().addEvent(new Event(OP_REQ_WRITE_METHOD));
        getRule(OPERATION_PUBLISH).addUser(OP_REQ_WRITE_METHOD, null);
        
        getRule(OPERATION_PUBLISH).getOperation().addEvent(new Event(OP_REQ_READ_METHOD));
        getRule(OPERATION_PUBLISH).addUser(OP_REQ_READ_METHOD, null);
        
        getRule(OPERATION_NOTIFY).getOperation().addEvent(new Event(OP_IND_READ_METHOD));
        getRule(OPERATION_NOTIFY).addUser(OP_IND_READ_METHOD, null);
    }
}
