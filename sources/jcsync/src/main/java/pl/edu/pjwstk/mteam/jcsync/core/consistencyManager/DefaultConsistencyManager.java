package pl.edu.pjwstk.mteam.jcsync.core.consistencyManager;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.Comparator;
import java.util.TreeMap;
import java.io.Serializable;
import java.util.ArrayList;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import org.apache.log4j.Logger;
import java.util.HashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import pl.edu.pjwstk.mteam.jcsync.core.AbstractConsistencyManager;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncAbstractSharedObject;
import pl.edu.pjwstk.mteam.jcsync.core.JCsyncAlgorithInterface;
import pl.edu.pjwstk.mteam.jcsync.exception.ObjectExistsException;
import pl.edu.pjwstk.mteam.jcsync.exception.ObjectNotExistsException;
import pl.edu.pjwstk.mteam.jcsync.exception.OperationForbiddenException;
import pl.edu.pjwstk.mteam.jcsync.operation.JCsyncAbstractOperation;
import pl.edu.pjwstk.mteam.jcsync.operation.MethodCarrier;
import pl.edu.pjwstk.mteam.jcsync.operation.RegisteredOperations;
import pl.edu.pjwstk.mteam.pubsub.message.request.PublishRequest;
import static pl.edu.pjwstk.mteam.jcsync.core.JCSyncConstans.*;
import static pl.edu.pjwstk.mteam.jcsync.operation.RegisteredOperations.*;

/**
 * Basic implementation of <tt>AbstractConsistencyManager</tt>. Its represents 
 * FIFO (first-in-first-out) policy for managing operation.
 * <br>
 * Every request are passed to the object root node, where it receives a serial 
 * number used to determine which of them is the first one. 
 * <pre>
 * <code>
 * class RequestsConsumer implements Runnable {
 *      //delivered requests
protected PriorityBlockingQueue<DeliveredRequest> deliveredIndications = new PriorityBlockingQueue<DeliveredRequest>(100);
protected JCsyncAlgorithInterface core;
 * 
protected RequestsConsumer(JCsyncAlgorithInterface core) {
this.core = core;
}

public void run() {
DeliveredRequest dr = null;
JCsyncAbstractOperation op;
while (true) {
try {
//takes next operation from the buffer
dr = this.deliveredIndications.take();
} catch (InterruptedException ex) {
}
log.trace("Operation:" + dr.operation + ", taken from the request stack");
op = dr.operation;
if (dr.operation.getOperationType() == OP_REQ_LOCK_APPLY) {
[...] 
}
 *              else if (dr.operation.getOperationType() == OP_REQ_WRITE_METHOD) {
Object retVal = null;
try {
//try to invoke operation on the shared object
retVal = invoke(this.core.getObject(op.getObjectID()), op.getMethodCarrier());
log.trace("Operation invoked: " + dr.operation);
[...]
} catch (Exception e) {
log.error("An error occurred while invoking method.", e);
}
<strong>
// create new object to publish method and result in the overlay
MethodCarrier mc = new MethodCarrier(op.getMethodCarrier().getGenericMethodName());
mc.setRetVal((Serializable) retVal);
mc.setArgTypes(op.getMethodCarrier().getArgTypes());
mc.setArgValues(op.getMethodCarrier().getArgValues());
//set the serial number
mc.setOperationIndex(this.core.getObject(op.getObjectID()).getCurrentOperationID());
JCsyncAbstractOperation op_ = JCsyncAbstractOperation.getByType(OP_IND_WRITE_METHOD, op.getObjectID(), mc);
op_.setReqestID(op.getReqestID());</strong>
// publish operation in the overlay for all involved nodes
this.core.sendMessage(dr.request, op_, true);
} else {
[...]
}
}
}
}
 * </code>
 * </pre>
 * Then in the other nodes by calling {@link DefaultConsistencyManager#indicationReceived(pl.edu.pjwstk.mteam.jcsync.operation.JCsyncAbstractOperation)  indicationReceived(...)} the received indication is passed to the buffer which 
 * where is also sorted by the assigned serial number:
 * <pre>
 * <code>
class NotifyConsumer implements Runnable {
protected PriorityBlockingQueue<JCsyncAbstractOperation> deliveredIndications =
new PriorityBlockingQueue<JCsyncAbstractOperation>(100,
new Comparator<JCsyncAbstractOperation>() {
<strong>public int compare(JCsyncAbstractOperation o1, JCsyncAbstractOperation o2)</strong> {
// operation  index is increased only when the operation is a one of 'WRITE' type
if (o1.getMethodCarrier().getOperationIndex() == o2.getMethodCarrier().getOperationIndex()) {
if (o1.getOperationType() + 128 == OP_IND_LOCK_APPLY
|| o1.getOperationType() + 128 == OP_IND_LOCK_RELEASE) {
return 1;
} else if (o2.getOperationType() + 128 == OP_IND_LOCK_APPLY
|| o2.getOperationType() + 128 == OP_IND_LOCK_RELEASE) {
return -1;
}
return 0;
} else if (o1.getMethodCarrier().getOperationIndex() < o2.getMethodCarrier().getOperationIndex()) {
return -1;
} else {
return 1;
}
}
});
[...]
}
 * </code>
 * </pre>
 * <tt>NotifyConsumer</tt> in the thread loop compares that the operation 
 * serial number from the head of the <tt>deliveredIndications</tt> buffer is 
 * the next one for related shared object, if <tt>true</tt> then the operation is 
 * invoked, else <tt>NotifyConsumer</tt> waits for the next incoming indication.
 * <p>
 * <b>Note</b> that the operation which is already sent in a short time before 
 * sending another operation by other node <strong> will not always be invoked 
 * as a first one</strong>.
 * <bre>For example, let's imagine that three nodes put three different 
 * <tt>String</tt> value in the loop to the <tt>JCSyArrayList</tt> collection in 
 * the similar time and the result of it could be as below: <br>
 * <code>{"node1_value1","node3_value1", "node2_value1","node3_value2","node1_value2","node2_value2","node2_value3","node3_value3","node1_value3"}</code>
 * 
 * @author Piotr Bucior
 */
public class DefaultConsistencyManager extends AbstractConsistencyManager {

    /**
     * map with operation which was requested by current node and which calling 
     * threads will be suspended until the operation will be done in the overlay.
     */
    TreeMap<JCsyncAbstractOperation, Locker> blockingRequests = new TreeMap<JCsyncAbstractOperation, Locker>();
    /**
     * map with non-blocking operation (feature not implemented yet)
     */
    TreeMap<JCsyncAbstractOperation, Short> callableRequests = new TreeMap<JCsyncAbstractOperation, Short>();
    /**
     * the logger
     */
    protected final Logger log = Logger.getLogger("pl.edu.pjwstk.mteam.jcsync.core.consistencyManager.DefaultConsistencyManager");
    /**
     * worker threads for objects
     */
    HashMap<String, RequestsRelay> requestManagers = new HashMap<String, RequestsRelay>(100);
    /**
     * thread group determines relay threads
     */
    private ThreadGroup relayThreadsGroup = new ThreadGroup("RelayThreads");
    /**
     * thread group determines consumer threads
     */
    private ThreadGroup consumersThreadsGroup = new ThreadGroup("ConsumerThreads");
    /**
     * thread group determines notify consumer threads
     */
    private ThreadGroup notifyConsumersThreadsGroup = new ThreadGroup("NotifyConsumerThreads");
    /**
     * An instance of JCsync core related with this instance of 
     * <tt>DefaultConsistencyManager</tt>
     */
    protected JCsyncAlgorithInterface core;
    private final HashMap<String, ArrayList<JCsyncAbstractOperation>> operationBuffer;

    /**
     * Creates new instance of <tt>DefaultConsistencyManager</tt>
     */
    public DefaultConsistencyManager() {
        operationBuffer = new HashMap<String, ArrayList<JCsyncAbstractOperation>>();
    }

    /**
     * checks whether the operation is already inserted to the map.
     * @param op operation which will be inserted.
     * @param blocking if <tt>true</tt> then operation will be searched in the 
     * <tt>blockingRequests</tt>.
     */
    private void checkIfExists(JCsyncAbstractOperation op, boolean blocking) {
        if (blocking) {
            if (this.blockingRequests.containsKey(op)) {
                throw new IllegalStateException("There is already registered locker for thin name and operation type");
            }
        } else {
            if (this.callableRequests.containsKey(op)) {
                throw new IllegalStateException("There is already registered locker for thin name and operation type");
            }
        }
    }

    /**
     * Called when the response is received from the overlay.
     * <br>Response may be related to the one of two types of operations 
     * (one- and two-phase), then if the operation is the one of one-phase type 
     * then the operation calling thread is resumed, if the response is for 
     * two-phase operation then the response code is set, but the calling thread 
     * will wait for the indication (second phase of the request) for called request.
     * @see RegisteredOperations
     */
    @Override
    public void responseReceived(JCsyncAbstractOperation op, short respCode) {
        log.trace("(response received (" + respCode + ")): " + op.toString());
        if (this.blockingRequests.containsKey(op)) {
            synchronized (this.blockingRequests.get(op).methodAlreadyInvoked) {
                //if the operation is a request
                if ((op.getOperationType() & OP_REQ_GENERIC) == OP_REQ_GENERIC) {
                    //if the operation is a single phase operation
                    if ((op.getOperationType() & OP_REQ_ONE_PHASE_GENERIC) == OP_REQ_ONE_PHASE_GENERIC) {
                        //set response code
                        this.blockingRequests.get(op).respCode = respCode;
                        //release semaphore
                        this.blockingRequests.get(op).semaphore.release();
                    } else if ((op.getOperationType() & OP_REQ_BIPHASE_GENERIC) == OP_REQ_BIPHASE_GENERIC) {
                        /* operation depends on respCode, allowe response codes:
                         * JCSyncConstans.J_RESP_GENERAL_SUCCESS;
                         * JCSyncConstans.J_ERR_COLLECTION_EXISTS;
                         * JCSyncConstans.J_ERR_OBJECT_NOT_EXISTS;
                         * JCSyncConstans.J_ERR_COLLECTION_AUTH_ERROR;
                         * If the response code is SUCCESS then go to the second phase (wait for the indication)
                         * else throw exception depending on the respCOde
                         * 
                         * differentation just for clarity
                         */
                        switch (respCode) {
                            case (J_RESP_GENERAL_SUCCESS): {
                                log.trace("received success response for biphase operation: " + op.toString());
                                //synchronized (this.blockingRequests.get(op).methodAlreadyInvoked) {
                                this.blockingRequests.get(op).respCode = respCode;
                                //sometimes response is sent after the indication is received, if that - just release the semaphore
                                if (this.blockingRequests.get(op).methodAlreadyInvoked == true) {
                                    this.blockingRequests.get(op).semaphore.release();
                                }
                                //}
                                break;
                            }
                            default: {
                                log.trace("received fail code for biphase operation: " + op.toString());
                                this.blockingRequests.get(op).respCode = respCode;
                                this.blockingRequests.get(op).semaphore.release();
                                break;
                            }
                        }

                    }
                } else {
                    log.fatal("(responseReceived)- Unhandled operation type (with respCode:" + respCode + "):" + op.toString());
                }
            }
        } else if (this.callableRequests.containsKey(op)) {
            //todo implement callable 
        } else {
            log.fatal("(responseReceived)- Received response, but operation isn't in the monitor map: "
                    + op + ", respCode:" + respCode + "\n\t, monitor map: " + this.blockingRequests);
        }
    }

    /**
     * Calls when the request is received from the overlay. All of them is 
     * passed through to the {@link RequestsRelay RequestRelay} assigned to 
     * related shared object with the exception of two cases:<br>
     * <b>1.</b> If the operation is a OP_REQ_LOCK_RELEASE (as a result of calling 
     * {@link JCSyncAbstractSharedObject#afterPublishWriteOperation() JCSyncAbstractSharedObject.afterPublishWriteOperation()}
     *  or  {@link JCSyncAbstractSharedObject#afterPublishReadOperation() JCSyncAbstractSharedObject.afterPublishReadOperation()})
     * then related <tt>RequestRelay</tt> will be unlocked and the rest of received
     * requests (while object was locked) will be now consumed.<br>
     * <b>2.</b>If the operation contained in the request is a one of 'WRITE' operation
     * during the shared object is locked is passed through to the locked buffer 
     * in the <tt>RequestRelay</tt> except if the publisher of this request is 
     * the same who owns current locker on the shared object - then given request 
     * is passed through to the {@link RequestsConsumer RequestConsumer} where 
     * it can be consumed.
     * 
     */
    @Override
    public synchronized void requestReceived(PublishRequest req, JCsyncAbstractOperation op) {
        log.trace(this.core.getNodeInfo().getName() + ": (requestReceived): " + op + ", publisher: " + req.getPublisher());
        if (op.getOperationType() == OP_REQ_LOCK_RELEASE) {
            this.core.sendResponse(req, PubSubConstants.RESP_SUCCESS);
            JCsyncAbstractOperation op_ = op.changeTypeToIndication();
            op_.setMethodCarrier(new MethodCarrier());
            op_.getMethodCarrier().setOperationIndex(this.core.getObject(op.getObjectID()).getCurrentOperationID());
            this.core.sendMessage(req, op_, false);
            // release the locker for the incoming requests buffer
            this.requestManagers.get(req.getTopicID()).setIsLocked(false);
        } else if (this.requestManagers.get(req.getTopicID()).isLocked == true) {
            //if the locker for incoming requests is acquired
            if (this.requestManagers.get(req.getTopicID()).lockerOwner.request.getSourceInfo().getID().compareTo(req.getSourceInfo().getID()) == 0) {
                // if the publisher is the same who sent the LOCK_APPLY then 
                // just pass this request to invoke, otherwise put the request 
                // to the blocked  buffer
                DeliveredRequest dr = new DeliveredRequest(req, op);
                this.core.sendResponse(req, PubSubConstants.RESP_SUCCESS);
                this.requestManagers.get(req.getTopicID()).rc.deliveredRequests.add(dr);
            } else {
                // just put to the locked buffer in the requestRelay
                try {
                    log.trace(this.core.getNodeInfo().getName() + ":(requestReceived): " + op + " passing operation to the first queue, reqManager blocked? :" + this.requestManagers.get(req.getTopicID()).isLocked);
                    DeliveredRequest dr = new DeliveredRequest(req, op);
                    this.requestManagers.get(req.getTopicID()).deliveredRequests.add(dr);
                    this.core.sendResponse(req, PubSubConstants.RESP_SUCCESS);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error(this.core.getNodeInfo().getName() + ":An error occurred:", e);
                }
            }
        } else {
            // if the shared object is not locked just pass request to the requestRelay buffer
            try {
                log.trace(this.core.getNodeInfo().getName() + ":(requestReceived): " + op + " passing operation to the first queue, reqManager blocked? :" + this.requestManagers.get(req.getTopicID()).isLocked);
                DeliveredRequest dr = new DeliveredRequest(req, op);
                this.core.sendResponse(req, PubSubConstants.RESP_SUCCESS);
                this.requestManagers.get(req.getTopicID()).deliveredRequests.add(dr);
            } catch (Exception e) {
                e.printStackTrace();
                log.error(this.core.getNodeInfo().getName() + ":An error occurred:", e);
            }
        }
    }

    @Override
    public void indicationReceived(JCsyncAbstractOperation op) {
        log.trace(this.core.getNodeInfo().getName() + ": (indicationReceived) - called with: " + op.toString());
        //log.trace("Operation:" + op + ", taken from the notify stack");
        if (this.blockingRequests.containsKey(op)) {
            log.trace(this.core.getNodeInfo().getName() + ": (indicationReceived) - contained by blockingRequests: " + op.toString());
            //if the operation is a Indication
            if ((op.getOperationType() + 128 & OP_INDICATION_GENERIC) == OP_INDICATION_GENERIC) {
                //if the operation is a single phase operation
                if ((op.getOperationType() + 128) == OP_IND_TRANSFER_OBJECT) {
                    //set response code
                    blockingRequests.get(op).retVal = op.getDetails();
                    synchronized (blockingRequests.get(op).methodAlreadyInvoked) {
                        blockingRequests.get(op).methodAlreadyInvoked = true;
                        //release semaphore only when the respCode already is set up;
                        if (blockingRequests.get(op).respCode != -1) {
                            blockingRequests.get(op).semaphore.release();
                        }
                    }
                } else {
                    this.requestManagers.get(op.getObjectID()).nc.deliveredIndications.add(op);
//                    synchronized (this.requestManagers.get(op.getObjectID()).nc.locker) {
//                        this.requestManagers.get(op.getObjectID()).nc.locker.notifyAll();
//                    }
                }
            } else {
                log.fatal(this.core.getNodeInfo().getName() + ":(indicationReceived)- Unhandled operation type :" + op.toString());
            }
        } else if (callableRequests.containsKey(op)) {
            //todo implement callable 
            log.trace(this.core.getNodeInfo().getName() + ": (indicationReceived) - TODO, operation passed to callableRequests: " + op.toString());
            this.requestManagers.get(op.getObjectID()).nc.deliveredIndications.add(op);

        } else {
            boolean b = false;
            synchronized (operationBuffer) {
                if (operationBuffer.containsKey(op.getObjectID())) {
                    log.trace(this.core.getNodeInfo().getName() + ": no registered notifyConsumer yet, passing operation to the buffer: " + op.toString());
                    operationBuffer.get(op.getObjectID()).add(op);
                    b = true;
                }
            }
            if (!b) {
                log.trace(this.core.getNodeInfo().getName() + ": (indicationReceived) - passed to notifyConsumer : " + op.toString());
                this.requestManagers.get(op.getObjectID()).nc.deliveredIndications.add(op);
//                    synchronized (this.requestManagers.get(op.getObjectID()).nc.locker) {
//                        this.requestManagers.get(op.getObjectID()).nc.locker.notifyAll();
//                    }
            }
        }
    }

    @Override
    public void beforeRequestSend(JCsyncAbstractOperation op, boolean blocking) throws Exception {
        checkIfExists(op, blocking);
        if (blocking) {
            Locker l = new Locker(new Semaphore(1), (short) -1);
            l.semaphore.acquire();
            this.blockingRequests.put(op, l);
        } else {
            this.callableRequests.put(op, (short) -1);
        }
    }

    @Override
    public Object afterRequestSend(JCsyncAbstractOperation op, boolean blocking) throws Exception {

        if (blocking) {
            try {
                Locker l = this.blockingRequests.get(op);
                l.semaphore.acquire();
                //if the operation is one of the request
                if ((op.getOperationType() & OP_REQ_GENERIC) == OP_REQ_GENERIC) {
                    //one phase operation 
                    if (op.getOperationType()
                            == OP_REQ_CREATE_SHARED_OBJECT
                            || op.getOperationType()
                            == OP_REQ_SUBSCRIBE
                            || op.getOperationType()
                            == OP_REQ_UNSUBSCRIBE
                            || op.getOperationType()
                            == OP_REQ_REMOVE) {
                        return l.respCode;
                    } //thwo-phases operations
                    else if ((op.getOperationType() & OP_REQ_BIPHASE_GENERIC) == OP_REQ_BIPHASE_GENERIC) {
                        short retCode = l.respCode;
                        //if the returned code is the one of fail codes just throw exception
                        if (retCode == J_ERR_COLLECTION_AUTH_ERROR) {
                            throw OperationForbiddenException.instance();
                        } else if (retCode == J_ERR_COLLECTION_EXISTS) {
                            throw ObjectExistsException.instance();
                        } else if (retCode == J_ERR_OBJECT_NOT_EXISTS) {
                            throw ObjectNotExistsException.instance();
                        } else if (retCode == J_RESP_GENERAL_SUCCESS) {
                            //if the code is success
                            Object retVal = l.retVal;
                            return retVal;
                        } else {
                            log.warn(this.core.getNodeInfo().getName() + ":(afterRequestSend) - Response code is not set (propably response is not delivered yet): "
                                    + retCode + ", for operation:" + op.toString());
                        }
                    } else {
                        log.fatal(this.core.getNodeInfo().getName() + ":(afterRequestSend) - Unhandled request type: " + op.getOperationType() + ", object name: " + op.getObjectID());
                        return null;
                    }
                }
            } finally {
                this.blockingRequests.remove(op);
            }
        } else {
            //ignore
            return null;
        }
        return null;
    }

    @Override
    public void registerObject(String id) {
        if (this.requestManagers.containsKey(id)) {
            throw new IllegalArgumentException("Collection already registered: " + id);
        } else {
            log.trace(this.core.getNodeInfo().getName() + ":Registering new shared object: " + id);
            RequestsRelay rr = new RequestsRelay(this.core);
            this.requestManagers.put(id, rr);
            Thread tr = new Thread(relayThreadsGroup, rr, "relay: " + id);
            Thread tc = new Thread(consumersThreadsGroup, rr.rc, "consumer: " + id);
            Thread nc = new Thread(notifyConsumersThreadsGroup, rr.nc, "consumer: " + id);
            tc.start();
            tr.start();
            nc.start();
            JCsyncAbstractOperation op;
            ArrayList<JCsyncAbstractOperation> operations = null;
            synchronized (operationBuffer) {
                if (operationBuffer.containsKey(id)) {
                    operations = operationBuffer.remove(id);
                }
            }
            if (operations != null) {
                for (int i = 0; i < operations.size(); i++) {
                    op = operations.get(i);
                    log.trace(this.core.getNodeInfo().getName() + ": operation taken from the buffer: " + op);
                    this.requestManagers.get(id).nc.deliveredIndications.add(op);
//                        synchronized (this.requestManagers.get(id).nc.locker) {
//                            this.requestManagers.get(id).nc.locker.notifyAll();
//                        }
                }
            }
        }
    }

    @Override
    public void setCoreAlgorith(JCsyncAlgorithInterface core) {
        this.core = core;
    }

    public void inititBuffer(String name) {
        if (operationBuffer.containsKey(name)) {
            return;
        } else {
            operationBuffer.put(name, new ArrayList<JCsyncAbstractOperation>(100));
        }
    }

    /**
     * Provides locking mechanism for calling thread which will be suspended.
     */
    protected class Locker {

        /**
         * uses to lock process (e.g. <tt>waitForWriteResults(...)</tt> ) 
         */
        public Semaphore semaphore;
        /**
         * response code of invoked method
         */
        public short respCode;
        /**
         * returned value
         */
        public Object retVal = null;
        /**
         * 
         */
        public volatile Boolean methodAlreadyInvoked = false;

        /**
         * create new instance
         * @param sem
         * @param resp
         */
        public Locker(Semaphore sem, short resp) {
            this.semaphore = sem;
            this.respCode = resp;
        }

        /**
         * set up the response code
         * @param resp 
         */
        public void setRespCode(short resp) {
            this.respCode = resp;
        }

        /**
         * set up the returned value
         * @param ret 
         */
        public void setRetVal(Object ret) {
            this.retVal = ret;
        }

        @Override
        protected void finalize() throws Throwable {
            this.semaphore = null;
            this.retVal = null;
            super.finalize();
        }
    }

    /**
     * Provides relay functionality between <tt>DefaultConsistencyManager</tt> 
     * and a <tt>RequestManager</tt>. It is passed all incoming request to the 
     * <tt>RequestManager</tt> until the request is not a 'OP_REQ_LOCK_APPLY'
     * type - then the main loop of it is suspended until the 'OP_REQ_LOCK_RELEASE'
     * operation is received. During suspension all received request from 
     * locker non-owner nodes is passed to the buffer. 
     */
    protected class RequestsRelay implements Runnable {

        /**
         * provides information whether the buffer is locked
         */
        protected volatile boolean isLocked = false;
        private final Object lock = new Object();
        /**
         * Buffer for incoming requests
         */
        protected final PriorityBlockingQueue<DeliveredRequest> deliveredRequests = new PriorityBlockingQueue<DeliveredRequest>(100);
        /**
         * Requests consumer thread related with this <tt>RequestRelay</tt>
         */
        protected final RequestsConsumer rc;
        /**
         * Notify consumer thread related with this <tt>RequestRelay</tt>
         */
        protected final NotifyConsumer nc;
        /**
         * Core algorithm instance 
         */
        protected final JCsyncAlgorithInterface core;
        /**
         * Keeps the information about the last published 'OP_REQ_LOCK_APPLY' 
         * operation
         */
        protected volatile DeliveredRequest lockerOwner = null;

        /**
         * Creates new instance with given core algorithm instance.
         */
        protected RequestsRelay(JCsyncAlgorithInterface core) {
            this.core = core;
            this.rc = new RequestsConsumer(core);
            this.nc = new NotifyConsumer(core);
        }

        /**
         * Creates new instance with given arguments/
         * @param core core algorithm instance 
         * @param r_consumer request consumer which will be associated with 
         * this relay
         * @param n_consumer notify consumer which will be associated with this 
         * relay.
         */
        protected RequestsRelay(JCsyncAlgorithInterface core, RequestsConsumer r_consumer, NotifyConsumer n_consumer) {
            this.core = core;
            this.rc = r_consumer;
            this.nc = n_consumer;
        }

        /**
         * Main loop of the relay. All request is passed to the request consumer
         * until the request is not holds a 'OP_REQ_LOCK_APPLY', then the main 
         * loop is suspended until the 'OP_REQ_LOCK_RELEASE' is received.
         */
        @Override
        public void run() {
            DeliveredRequest dr = null;
            while (true) {
                while (this.isLocked) {
                    try {
                        synchronized (this.lock) {
                            this.lock.wait();
                        }
                    } catch (InterruptedException ex) {
                        log.error(this.core.getNodeInfo().getName() + ":An error occurred:", ex);
                    }
                }
                this.lockerOwner = null;
                try {
                    dr = this.deliveredRequests.take();
                } catch (InterruptedException ex) {
                }
                if (dr.operation.getOperationType() == OP_REQ_LOCK_APPLY) {
                    // lets perform all submitted Requests, but not upload the next until the OP_REQ_LOCK_RELEASE will be received
                    this.rc.deliveredRequests.add(dr);
                    this.lockerOwner = dr;
                    setIsLocked(true);
                } else {
                    this.rc.deliveredRequests.add(dr);

                }
            }
        }

        /**
         * Checks whether the main loop is locked.
         * @return
         */
        public boolean isLocked() {
            return this.isLocked;
        }

        /**
         * Lock/unlock main loop.
         * @param b if <tt>true</tt> then the buffer will be locked, otherwise 
         * buffer will not be locked.
         */
        public void setIsLocked(boolean b) {
            synchronized (this.lock) {
                this.isLocked = b;
                this.lock.notifyAll();
            }

        }
    }

    /**
     * Manages all received requests. It is response for sending indications as 
     * a result of received requests.
     * <strong>It is used only by the root node.</strong>
     */
    protected class RequestsConsumer implements Runnable {

        /**
         * Buffer to keeps incoming requests.
         */
        protected LinkedBlockingQueue<DeliveredRequest> deliveredRequests = new LinkedBlockingQueue();
        private JCsyncAlgorithInterface core;

        /**
         * Creates new instance of <tt>RequestConsumer</tt> with given core 
         * algorithm as a executor. 
         */
        protected RequestsConsumer(JCsyncAlgorithInterface core) {
            this.core = core;
        }

        @Override
        public void run() {
            DeliveredRequest dr = null;
            JCsyncAbstractOperation op;
            while (true) {
                try {
                    dr = this.deliveredRequests.take();
                } catch (InterruptedException ex) {
                    log.error(this.core.getNodeInfo().getName() + ":An error occurred:", ex);
                }
                log.trace(this.core.getNodeInfo().getName() + ":Operation:" + dr.operation + ", publisher: " + dr.request.getPublisher() + " , taken from the request stack");
                op = dr.operation;
                if (dr.operation.getOperationType() == OP_REQ_LOCK_APPLY) {
                    JCsyncAbstractOperation op_ = op.changeTypeToIndication();
                    op_.setMethodCarrier(new MethodCarrier());
                    op_.getMethodCarrier().setOperationIndex(this.core.getObject(op.getObjectID()).getCurrentOperationID());
                    this.core.sendMessage(dr.request, op_, false);
                } else if (dr.operation.getOperationType() == OP_REQ_WRITE_METHOD) {
                    Object retVal = null;
                    try {
                        retVal = invoke(this.core.getObject(op.getObjectID()), op.getMethodCarrier());
                        log.trace(this.core.getNodeInfo().getName() + ":vOperation invoked: " + dr.operation);
                        if (blockingRequests.containsKey(op)) {
                            log.trace(this.core.getNodeInfo().getName() + ":Operation is in the map request, setting retValue: " + dr.operation);
                            blockingRequests.get(op).retVal = retVal;
                            synchronized (blockingRequests.get(op).methodAlreadyInvoked) {
                                blockingRequests.get(op).methodAlreadyInvoked = true;
                                if (blockingRequests.get(op).respCode != -1) {
                                    log.trace(this.core.getNodeInfo().getName() + ":Operation is in the map request, releasing semaphore: " + dr.operation);
                                    blockingRequests.get(op).semaphore.release();
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error(this.core.getNodeInfo().getName() + ":An error occurred while invoking method.", e);
                    }
                    MethodCarrier mc = new MethodCarrier(op.getMethodCarrier().getGenericMethodName());
                    mc.setRetVal((Serializable) retVal);
                    mc.setArgTypes(op.getMethodCarrier().getArgTypes());
                    mc.setArgValues(op.getMethodCarrier().getArgValues());
                    mc.setOperationIndex(this.core.getObject(op.getObjectID()).getCurrentOperationID());
                    JCsyncAbstractOperation op_ = JCsyncAbstractOperation.getByType(OP_IND_WRITE_METHOD, op.getObjectID(), mc, op.getPublisher());
                    op_.setReqestID(op.getReqestID());
                    log.trace("Passing operation to sent it to children: " + op_);
                    this.core.sendMessage(dr.request, op_, true);
                } else {
                    log.fatal(this.core.getNodeInfo().getName() + ":Unhandled operation: " + dr.operation);
                }

            }
        }
    }

    /**
     * It is response for invoking received indications.
     */
    protected class NotifyConsumer implements Runnable {

        private final Logger log = Logger.getLogger("pl.edu.pjwstk.mteam.jcsync.core.consistencyManager.DefaultConsistencyManager.NotifyConsumer");
        /**
         * Buffer for incoming indications.
         */
        protected PriorityBlockingQueue<JCsyncAbstractOperation> deliveredIndications =
                new PriorityBlockingQueue<JCsyncAbstractOperation>(10000,
                new Comparator<JCsyncAbstractOperation>() {

                    @Override
                    public int compare(JCsyncAbstractOperation o1, JCsyncAbstractOperation o2) {
                        if (o1.getMethodCarrier().getOperationIndex() == o2.getMethodCarrier().getOperationIndex()) {
                            if (o1.getOperationType() + 128 == OP_IND_LOCK_APPLY
                                    || o1.getOperationType() + 128 == OP_IND_LOCK_RELEASE) {
                                return 1;
                            } else if (o2.getOperationType() + 128 == OP_IND_LOCK_APPLY
                                    || o2.getOperationType() + 128 == OP_IND_LOCK_RELEASE) {
                                return -1;
                            }
                            return 0;
                        } else if (o1.getMethodCarrier().getOperationIndex() < o2.getMethodCarrier().getOperationIndex()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });
        private JCsyncAlgorithInterface core;
        private final Object locker = new Object();

        /**
         * Creates new instance of <tt>NotifyConsumer</tt> with given core 
         * algorithm instance as a executor. 
         */
        protected NotifyConsumer(JCsyncAlgorithInterface core) {
            this.core = core;
        }

        @Override
        public void run() {
            JCsyncAbstractOperation op = null;

            while (true) {



                while (true) {
                    try {



                        op = this.deliveredIndications.peek();
                        if (op != null
                                && op.getMethodCarrier() != null
                                && op.getMethodCarrier().getOperationIndex() - 1
                                <= this.core.getObject(op.getObjectID()).getCurrentOperationID()) {
                            try {
                                op = this.deliveredIndications.take();
                                if (op.getMethodCarrier().getOperationIndex() - 1 < this.core.getObject(op.getObjectID()).getCurrentOperationID()) {
                                    if (op.getOperationType() + 128 == OP_IND_LOCK_APPLY || op.getOperationType() + 128 == OP_IND_LOCK_RELEASE) {
                                        break;
                                    }
                                    log.warn(this.core.getNodeInfo().getName() + ": ignoring history operation :" + op);
                                    continue;
                                }
                                break;
                            } catch (InterruptedException ex) {
                                log.fatal(this.core.getNodeInfo().getName() + ":An error occurred", ex);
                            }
                        } else {
                            if (op != null) {
                                if (op.getOperationType() + 128 == OP_IND_LOCK_APPLY || op.getOperationType() + 128 == OP_IND_LOCK_RELEASE) {
                                    try {
                                        op = this.deliveredIndications.take();
                                        break;
                                    } catch (InterruptedException ex) {
                                        log.fatal(this.core.getNodeInfo().getName() + ":An error occurred", ex);

                                    }
                                }
                                log.debug(this.core.getNodeInfo().getName() + "@" + op.getObjectID() + ": Waiting for the next ordered operation, current operation ID: "
                                        + this.core.getObject(op.getObjectID()).getCurrentOperationID()
                                        + ", next operation in the buffer: " + op.getMethodCarrier().getOperationIndex()
                                        + ", queue size: " + this.deliveredIndications.size());
                            } else {
                                //log.trace("Waiting for the next ordered operation - (queue is empty!)");
                            }
//                            synchronized (this.locker) {
//                                try {
//                                    this.locker.wait(500);
//                                } catch (InterruptedException ex) {
//                                    log.error(this.core.getNodeInfo().getName() + ":An error occurred:", ex);
//                                }
//                            }
                            Thread.sleep(500);
                        }
                    } catch (Exception e) {
                        log.fatal("An error occurred: ", e);
                    }
                }

                log.trace(this.core.getNodeInfo().getName() + ":Operation:" + op + ", taken from the notify stack");
                if (blockingRequests.containsKey(op)) {
                    //if the operation is a Indication (must be)
                    if ((op.getOperationType() + 128 & OP_INDICATION_GENERIC) == OP_INDICATION_GENERIC) {
                        //if the operation is a single phase operation
                        synchronized (blockingRequests.get(op).methodAlreadyInvoked) {
                            if ((op.getOperationType() + 128) == OP_IND_WRITE_METHOD) {
                                Object result = null;
                                try {
                                    result = invoke(this.core.getObject(op.getObjectID()), op.getMethodCarrier());
                                    log.trace(this.core.getNodeInfo().getName() + ":Method invoked (" + op.getMethodCarrier().genericMethodName + ") on: " + op.toString());
                                    if (result != null) {
                                        if (result.equals(op.getMethodCarrier().getRetVal())) {
                                            log.trace(this.core.getNodeInfo().getName() + ":Result value may not be the same as given in indication!");
                                        }
                                    }
                                } catch (Exception e) {
                                    result = e;
                                }

                                blockingRequests.get(op).retVal = result;
                                //synchronized (blockingRequests.get(op).methodAlreadyInvoked) {
                                blockingRequests.get(op).methodAlreadyInvoked = true;
                                // }
                            } else {
                                blockingRequests.get(op).retVal = op.getDetails();
                                //release semaphore only when the respCode already is set up;                    
                                if (op.getOperationType() + 128 == OP_IND_LOCK_APPLY || op.getOperationType() + 128 == OP_IND_LOCK_RELEASE) {
                                    blockingRequests.get(op).retVal = "";
                                    //synchronized (blockingRequests.get(op).methodAlreadyInvoked) {
                                    blockingRequests.get(op).methodAlreadyInvoked = true;
                                    //}
                                }
                            }
                            //release semaphore only when the respCode already is set up;

                            if (blockingRequests.get(op).respCode != -1) {
                                blockingRequests.get(op).semaphore.release();
                            }
                        }
                    } else {
                        log.fatal(this.core.getNodeInfo().getName() + ":(indicationReceived)- Unhandled operation type :" + op.toString());
                    }
                } else if (callableRequests.containsKey(op)) {
                    if ((op.getOperationType() + 128) == OP_IND_WRITE_METHOD) {
                        Object result = null;
                        try {
                            result = invoke(this.core.getObject(op.getObjectID()), op.getMethodCarrier());
                            log.trace(this.core.getNodeInfo().getName() + ":Method invoked (" + op.getMethodCarrier().genericMethodName + ") on: " + op.toString());
                            if (result != null) {
                                if (result.equals(op.getMethodCarrier().getRetVal())) {
                                    log.trace(this.core.getNodeInfo().getName() + ":Result value may not be the same as given in indication!");
                                }
                            }
                        } catch (Exception e) {
                            result = e;
                        }
                        callableRequests.remove(op);
                    }
                } else {
                    if ((op.getOperationType() + 128) == OP_IND_WRITE_METHOD) {
                        Object result = null;
                        try {
                            result = invoke(this.core.getObject(op.getObjectID()), op.getMethodCarrier());
                            log.debug(this.core.getNodeInfo().getName() + ":Method invoked (" + op.getMethodCarrier().genericMethodName + ") on: " + op.toString());
                            if (result.equals(op.getMethodCarrier().getRetVal())) {
                                log.trace(this.core.getNodeInfo().getName() + ":Result value may not be the same as given in indication!");
                            }
                        } catch (Exception e) {
                            // set throwned exception as a result of invoked operation
                            result = e;
                        }
                    }
                }
            }
        }
    }

    /**
     * Stores information about received operation.
     */
    protected class DeliveredRequest implements Comparable<DeliveredRequest> {

        private long deliveryTime = 0;
        private PublishRequest request;
        private JCsyncAbstractOperation operation;

        /**
         * Creates new instance with given arguments.
         */
        protected DeliveredRequest(PublishRequest req, JCsyncAbstractOperation op) {
            this.request = req;
            this.operation = op;
            this.deliveryTime = System.currentTimeMillis();
        }

        @Override
        public int compareTo(DeliveredRequest o) {
            if (this.deliveryTime < o.deliveryTime) {
                return -1;
            } else if (this.deliveryTime == o.deliveryTime) {
                return 0;
            }
            return 1;
        }
    }
}
