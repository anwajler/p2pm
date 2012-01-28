
package pl.edu.pjwstk.mteam.jcsync.core.pubsub;

import pl.edu.pjwstk.mteam.jcsync.operation.JCsyncAbstractOperation;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.message.request.PublishRequest;
import pl.edu.pjwstk.mteam.pubsub.message.response.PubSubResponse;

/**
 * The <tt>MessageDeliveryObserver</tt> facilitates transmission of messages 
 * to the upper level (JCsync core). All messages from the pub-sub layer is 
 * filtered by {@link PubSubCustomisableAlgorithm PubSubCustomisableAlgorithm} 
 * and passed on only those that are related with shared objects.
 * 
 * @author Piotr Bucior
 */
public interface MessageDeliveryObserver {
    
    /**
     * Informs upper level that the request is received from the overlay.
     * @param req received request
     */
    void onDeliverRequest(PublishRequest req);
    /**
     * Informs upper level that the response is received from the overlay.
     * @param res received response
     */
    void onDeliverResponse(PubSubResponse res);
    /**
     * Informs upper level that the indication is received from the overlay.
     * @param ind received indication
     */
    void onDeliverIndication(JCsyncAbstractOperation ind);
    /**
     * Informs upper level that the response is received from the overlay for 
     * one of those operation (specified in {@link pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants PubSubConstants}):<br>
     * - {@link PubSubConstants#MSG_CREATETOPIC MSG_CREATETOPIC} <br>
     * - {@link PubSubConstants#MSG_SUBSCRIBE MSG_SUBSCRIBE} <br>
     * - {@link PubSubConstants#MSG_UNSUBSCRIBE MSG_UNSUBSCRIBE} <br>
     * - {@link PubSubConstants#EVENT_REMOVETOPIC EVENT_REMOVETOPIC} <br>
     * @param name object identifier
     * @param operationType operation to which the answer is related to
     * @param respCode response code
     * @param requestID request identifier (typically transaction identifier)
     */
    void onDeliverPubSubResponse(String name, short operationType, short respCode, long requestID);
}
