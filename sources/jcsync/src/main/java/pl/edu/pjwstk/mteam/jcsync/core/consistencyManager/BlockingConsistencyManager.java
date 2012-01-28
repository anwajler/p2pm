//package pl.edu.pjwstk.mteam.jcsync.core.consistencyManager;
//
//import java.io.Serializable;
//import java.util.Vector;
//import org.apache.log4j.Logger;
//import pl.edu.pjwstk.mteam.jcsync.core.JCsyncAlgorithInterface;
//import pl.edu.pjwstk.mteam.jcsync.operation.JCsyncAbstractOperation;
//import pl.edu.pjwstk.mteam.jcsync.operation.MethodCarrier;
//import static pl.edu.pjwstk.mteam.jcsync.operation.RegisteredOperations.*;
//import pl.edu.pjwstk.mteam.pubsub.message.request.PublishRequest;
//
///**
// *
// * @author Piotr Bucior
// */
//public class BlockingConsistencyManager extends DefaultConsistencyManager {
//
//    protected final Logger log = Logger.getLogger("pl.edu.pjwstk.mteam.jcsync.core.consistencyManager.BlockingConsistencyManager");
//    public static final short OP_REQ_WRITE_SLEEPY = 1414;
//    public static final short OP_IND_WRITE_SLEEPY = OP_REQ_WRITE_SLEEPY + 128;
//
//    static {
//        registerOperation(OP_REQ_WRITE_SLEEPY, "OP_REQ_WRITE_SLEEPY");
//        registerOperation(OP_IND_WRITE_SLEEPY, "OP_IND_WRITE_SLEEPY");
//
//    }
//
//    public BlockingConsistencyManager() {
//    }
//
//    @Override
//    public void registerObject(String id) {
//        if (super.requestManagers.containsKey(id)) {
//            throw new IllegalArgumentException("Collection already registered: " + id);
//        } else {
//            log.trace("Registering new shared object: " + id);
//            RequestsRelay rr = new RequestsRelay(this.core);
//            super.requestManagers.put(id, rr);
////            Thread tr = new Thread(super.relayThreadsGroup, rr, "relay: " + id);
////            Thread tc = new Thread(super.consumersThreadsGroup, rr.rc, "consumer: " + id);
////            Thread nc = new Thread(super.notifyConsumersThreadsGroup, rr.nc, "consumer: " + id);
////            tc.start();
////            tr.start();
////            nc.start();
//        }
//    }
//
//    @Override
//    public synchronized void requestReceived(PublishRequest req, JCsyncAbstractOperation op) {
//        if (op.getOperationType() == OP_REQ_WRITE_SLEEPY) {
//            
//        } else {
//            super.requestReceived(req, op);
//        }
//
//    }
//
//    class RequestsRelay extends DefaultConsistencyManager.RequestsRelay {
//
//        protected RequestsRelay(JCsyncAlgorithInterface core) {
//            super(core, new RequestsConsumer(core), new NotifyConsumer(core));
//        }
//
//        @Override
//        public void run() {
//            DeliveredRequest dr = null;
//            while (true) {
//                //   try{
//                //this.locker.readLock().lock();
//                while (this.isLocked) {
////                    try {
//////                        synchronized (this.lock) {
//////                            this.lock.wait();
//////                        }
////                    } catch (InterruptedException ex) {
////                        log.error("An error occurred while waiting.", ex);
////                    }
//                }
//                this.lockerOwner = null;
//                try {
//                    dr = this.deliveredRequests.take();
//                } catch (InterruptedException ex) {
//                }
//                if (dr.operation.getOperationType() == OP_REQ_LOCK_APPLY) {
//                    // Let perform all submitted Requests, but not upload the next until the OP_REQ_LOCK_RELEASE will be received
//                    this.rc.deliveredRequests.add(dr);
//                    this.lockerOwner = dr;
////                this.locker.readLock().unlock();
////                this.locker.writeLock().lock();
//                    setIsLocked(true);
//                } else {
//                    this.rc.deliveredRequests.add(dr);
//
//                }
//                //  }finally{
//                //      this.locker.readLock().unlock();
//                //  }
//            }
//        }
//
//        public boolean isLocked() {
//            return this.isLocked;
//        }
//
//        public void setIsLocked(boolean b) {
////            synchronized (this.lock) {
////                this.isLocked = b;
////                this.lock.notifyAll();
////            }
//
//        }
//    }
//
//    class RequestsConsumer extends DefaultConsistencyManager.RequestsConsumer implements Runnable {
//
//        protected RequestsConsumer(JCsyncAlgorithInterface core) {
//            super(core);
//        }
//
//        @Override
//        public void run() {
//            Vector<JCsyncAbstractOperation> keys;
//            while (true) {
//                DeliveredRequest dr = null;
//                try {
//                    dr = this.deliveredRequests.take();
//                } catch (InterruptedException ex) {
//                }
//                log.trace("Operation:" + dr.operation + ", taken from the request stack");
//                JCsyncAbstractOperation op = dr.operation;
//                if (dr.operation.getOperationType() == OP_REQ_LOCK_APPLY) {
//                    this.core.sendMessage(dr.request, op.changeTypeToIndication(), false);
//                } else if (dr.operation.getOperationType() == OP_REQ_WRITE_METHOD) {
//                    Object retVal = null;
//                    try {
//                        
//                        if (blockingRequests.containsKey(op)) {
//                            keys = null;
//                            keys = new Vector<JCsyncAbstractOperation>(blockingRequests.size());
//                            keys.addAll(blockingRequests.keySet());
//                            op = keys.get(keys.indexOf(op));
//                            retVal = invoke(this.core.getObject(op.getObjectID()), op.getMethodCarrier());
//                            blockingRequests.get(op).retVal = retVal;
//                            blockingRequests.get(op).methodAlreadyInvoked = true;
//                            if (blockingRequests.get(op).respCode != -1) {
//                                blockingRequests.get(op).semaphore.release();
//                            }
//                        } else {
//                            retVal = invoke(this.core.getObject(op.getObjectID()), op.getMethodCarrier());
//                        }
//                    } catch (Exception e) {
//                        log.error("An error occurred while invoking method.", e);
//                    }
//                    MethodCarrier mc = new MethodCarrier(op.getMethodCarrier().getGenericMethodName());
//                    mc.setRetVal((Serializable) retVal);
//                    mc.setArgTypes(op.getMethodCarrier().getArgTypes());
//                    mc.setArgValues(op.getMethodCarrier().getArgValues());
//                    mc.setOperationIndex(this.core.getObject(op.getObjectID()).getCurrentOperationID());
//                    JCsyncAbstractOperation op_ = JCsyncAbstractOperation.getByType(OP_IND_WRITE_METHOD, op.getObjectID(), mc);
//                    op_.setReqestID(op.getReqestID());
//                    this.core.sendMessage(dr.request, op_, true);
//                } else {
//                    log.fatal("Unhandled operation: " + dr.operation);
////                this.rc.deliveredIndications.add(dr);
////                this.locker.readLock().unlock();
//                }
//
//            }
//        }
//    }
//
//    class NotifyConsumer extends DefaultConsistencyManager.NotifyConsumer {
//
//        protected NotifyConsumer(JCsyncAlgorithInterface core) {
//            super(core);
//        }
//
//        @Override
//        public void run() {
//            Vector<JCsyncAbstractOperation> keys;
//            while (true) {
//                JCsyncAbstractOperation op = null;
//                try {
//                    op = this.deliveredIndications.take();
//                } catch (InterruptedException ex) {
//                }
//                log.trace("Operation:" + op + ", taken from the notify stack");
//                if (blockingRequests.containsKey(op)) {
//                    //if the operation is a Indication
//                    if ((op.getOperationType() + 128 & OP_INDICATION_GENERIC) == OP_INDICATION_GENERIC) {
//                        //if the operation is a single phase operation
//                        if ((op.getOperationType() + 128) == OP_IND_WRITE_METHOD) {
//                            keys = null;
//                            keys = new Vector<JCsyncAbstractOperation>(blockingRequests.size());
//                            keys.addAll(blockingRequests.keySet());
//                            op = keys.get(keys.indexOf(op));
//                            Object result = null;
//                            try {
//                                result = invoke(this.core.getObject(op.getObjectID()), op.getMethodCarrier());
//                                log.trace("Method invoked (" + op.getMethodCarrier().genericMethodName + ") on: " + op.toString());
//                                if (result != null) {
//                                    if (result.equals(op.getMethodCarrier().getRetVal())) {
//                                        log.trace("Result value may not be the same as given in indication!");
//                                    }
//                                }
//                            } catch (Exception e) {
//                                result = e;
//                            }
//
//                            blockingRequests.get(op).retVal = result;
//                            blockingRequests.get(op).methodAlreadyInvoked = true;
//                        } else {
//                            blockingRequests.get(op).retVal = op.getDetails();
//                            //release semaphore only when the respCode already is set up;                    
//                            if (op.getOperationType() + 128 == OP_IND_LOCK_APPLY || op.getOperationType() + 128 == OP_IND_LOCK_RELEASE) {
//                                blockingRequests.get(op).retVal = "";
//                                blockingRequests.get(op).methodAlreadyInvoked = true;
//                            }
//                        }
//                        //release semaphore only when the respCode already is set up;
//                        if (blockingRequests.get(op).respCode != -1) {
//                            blockingRequests.get(op).semaphore.release();
//                        }
//                    } else {
//                        log.fatal("(indicationReceived)- Unhandled operation type :" + op.toString());
//                    }
//                } else if (callableRequests.containsKey(op)) {
//                    //todo implement callable 
//                } else {
//                    if ((op.getOperationType() + 128) == OP_IND_WRITE_METHOD) {
//                        Object result = null;
//                        try {
//                            result = invoke(this.core.getObject(op.getObjectID()), op.getMethodCarrier());
//                            log.debug("Method invoked (" + op.getMethodCarrier().genericMethodName + ") on: " + op.toString());
//                            if (result.equals(op.getMethodCarrier().getRetVal())) {
//                                log.trace("Result value may not be the same as given in indication!");
//                            }
//                        } catch (Exception e) {
//                            result = e;
//                        }
//                    }
//                }
//
//
//            }
//        }
//    }
//}
