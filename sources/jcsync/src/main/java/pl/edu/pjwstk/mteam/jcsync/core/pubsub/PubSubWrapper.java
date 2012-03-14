package pl.edu.pjwstk.mteam.jcsync.core.pubsub;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.p2p.P2PNode;
import pl.edu.pjwstk.mteam.pubsub.core.CoreAlgorithm;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.message.request.PubSubRequest;
import pl.edu.pjwstk.mteam.pubsub.message.request.PublishRequest;
import pl.edu.pjwstk.mteam.pubsub.message.request.SubscribeRequest;
import pl.edu.pjwstk.mteam.pubsub.message.response.StandardResponse;
import pl.edu.pjwstk.mteam.pubsub.transport.PubSubTransport;

/**
 * An extension of {@link CoreAlgorithm CoreAlgorithm}.
 * @author Piotr Bucior
 */
public class PubSubWrapper extends pl.edu.pjwstk.mteam.pubsub.core.CoreAlgorithm {

    private final MessageDeliveryObserver observer;
    public PubSubCustomisableAlgorithm pubSubCustomAlg;
    private Logger log = Logger.getLogger(PubSubWrapper.class);
    
    /**
     * Creates new instance with given arguments.
     * @param port communication port that will be used by pubsub layer.
     * @param n node associated with this layer.
     * @param observer an instance of jcsync core algorithm associated with this 
     * pubsub layer.
     */
    public PubSubWrapper(int port, P2PNode n, MessageDeliveryObserver observer) {
        super(port, n, new pl.edu.pjwstk.mteam.jcsync.core.pubsub.PubSubAlgorithmConfigurator());
        this.observer = observer;        
    }

    /**
     * Initialises pubsub layer.
     * @throws Exception 
     */
    public void initialize() throws Exception {
        if(getNode().isConnected()==false){
            throw new Exception("Node is not connected!");
        }
        super.init();
        //super.algorithm.setCoreAlgorithm(this);
        this.pubSubCustomAlg = (PubSubCustomisableAlgorithm) super.getCustomizableAlgorithm();
        this.pubSubCustomAlg.setMessageDeliveryObserver(this.observer);
        super.getNode().addCallback(this.pubSubCustomAlg.getNCallback());
        
    }
    /**
     * Returns <tt>PubSubCustomisableAlgoritm</tt> associated with this pubsub 
     * layer. 
     */
    public PubSubCustomisableAlgorithm getCustomAlgorith(){
        return this.pubSubCustomAlg;
    }
    
//    public void sendResponse(int respCode, PubSubRequest req, Topic t, long currentReqID) {
//        StandardResponse resp = null;
//        if (req instanceof SubscribeRequest && respCode == PubSubConstants.RESP_SUCCESS) {
//            resp = new StandardResponse(req.getTransactionID(),
//                    getNodeInfo(),
//                    req.getSourceInfo(),
//                    req.getTopicID(),
//                    t.getAccessControlRules());
//            sendMessage(resp, PubSubTransport.ROUTING_DIRECT, null);
//        } else {
//            resp = new StandardResponse(req.getTransactionID(), respCode,
//                    getNodeInfo(), req.getSourceInfo(),
//                    req.getTopicID());
//            sendMessage(resp, PubSubTransport.ROUTING_DIRECT, null);
//        }
//        if (req instanceof PublishRequest) {
//            log.trace("Response (" + respCode + ") for: " + req.getTopicID() + "@" + PubSubConstants.STR_OPERATION.get((short) req.getType())
//                    + " - event: " + PubSubConstants.STR_EVENT.get((short) ((PublishRequest) req).getEventType())
//                    +", tID: "+resp.getTransactionID()
//                    + " sent to '" + resp.getDestinationInfo() + "'");
//        } else {
//            log.trace("Response (" + respCode + ") for: " + req.getTopicID() + "@" + PubSubConstants.STR_OPERATION.get((short) req.getType())
//                    +", tID: "+resp.getTransactionID()
//                    + " sent to '" + resp.getDestinationInfo() + "'");
//        }
//    }
}
