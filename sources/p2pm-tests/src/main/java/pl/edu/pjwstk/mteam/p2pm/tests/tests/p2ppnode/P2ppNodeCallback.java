package pl.edu.pjwstk.mteam.p2pm.tests.tests.p2ppnode;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.p2pm.tests.core.events.EventManager;
import pl.edu.pjwstk.p2pp.ErrorInterface;
import pl.edu.pjwstk.p2pp.P2PPNodeCallback;
import pl.edu.pjwstk.p2pp.messages.requests.PublishObjectRequest;
import pl.edu.pjwstk.p2pp.messages.requests.Request;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.ResourceObject;
import pl.edu.pjwstk.p2pp.resources.MessageResourceObject;
import pl.edu.pjwstk.p2pp.superpeer.messages.requests.IndexRequest;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

import java.util.List;
import java.util.Vector;

public class P2ppNodeCallback implements P2PPNodeCallback {

    public static final Logger LOG = Logger.getLogger(P2ppNodeCallback.class);

    public void joinCallback() {
        LOG.info("joinCallback invoked");
        EventManager.getInstance().addEventToQueue(P2ppNode.EVENT_ONJOIN, null);
    }

    public void leaveCallback() {
        LOG.info("leaveCallback invoked");
    }

    public void lookupCallback(Vector<ResourceObject> resourceObjects) {
        LOG.info("lookupCallback invoked resourceObjects=" + resourceObjects);
        EventManager.getInstance().addEventToQueue(P2ppNode.EVENT_ONLOOKUP, new Object[]{resourceObjects});
    }

    public boolean onDeliverRequest(Request req, List<ResourceObject> objectList) {
        LOG.info("onDeliverRequest invoked req=" + req + " objectList=" + objectList);
        EventManager.getInstance().addEventToQueue(P2ppNode.EVENT_ONLOOKUP, new Object[]{objectList});
        return objectList != null;
    }

    public boolean onForwardingRequest(Request req, List<ResourceObject> objectList) {
        LOG.info("onForwardingRequest invoked req=" + req + " objectList=" + objectList);
        return objectList != null;
    }

    @Override
    public boolean onNeighborJoin(PeerInfo arg0, int arg1) {
        LOG.info("onNeighborJoin invoked arg0=" + arg0 + " arg1=" + arg1);
        return true;
    }

    @Override
    public void onNeighborLeave(PeerInfo node, int nodeType) {
        LOG.info("onNeighborLeave invoked node=" + node + " nodeType=" + nodeType);
    }

    @Override
    public void queryCallback(byte[] arg0, byte arg1, byte arg2, short arg3) {
        // TODO Auto-generated method stub
    }

    @Override
    public void removeCallback() {
        LOG.info("removeCallback invoked");
    }

    @Override
    public void errorCallback(ErrorInterface errorObject, int errorCode) {
        LOG.info("errorCallback invoked errorCode=" + errorCode);
        /*switch (errorCode) {
            case P2PPNodeCallback.BOOTSTRAP_ERROR_CODE:
                logger.trace("Bootstap error...");
                break;
            case P2PPNodeCallback.NAT_ERROR_CODE:
                logger.trace("NAT error...");
                //TODO: Currently P2PP NAT traversal is not available
                break;
            case P2PPNodeCallback.RESOURCE_LOOKUP_ERROR_CODE:
                logger.trace("Resource not found...");
                for (NodeCallback listener : nodeCallbacks) {
                    listener.onOverlayError(self, null, errorCode);
                }
                //nodeCallback.onOverlayError(self, null, errorCode);
                break;
            case P2PPNodeCallback.USER_LOOKUP_ERROR_CODE:
                logger.trace("User not found...");
                for (NodeCallback listener : nodeCallbacks) {
                    listener.onOverlayError(self, null, errorCode);
                }
                //nodeCallback.onOverlayError(self, null, errorCode);
                break;
            case P2PPNodeCallback.INSERT_ERROR_CODE:
                logger.trace("Could not insert object...");
                for (NodeCallback listener : nodeCallbacks) {
                    listener.onOverlayError(self, null, errorCode);
                }
                //nodeCallback.onOverlayError(self, null, errorCode);
                break;
        }*/
        EventManager.getInstance().addEventToQueue(P2ppNode.EVENT_ONERROR, new Object[]{errorObject, errorCode});
    }

    @Override
    public void publishCallback(byte contentType, byte contentSubtype, byte[] key, byte[] resourceObjectValue) {
        LOG.info("publishCallback invoked contentType=" + contentType + " contentSubtype=" + contentSubtype + " key=" + new String(key) +
                " valueLength=" + resourceObjectValue.length);
        EventManager.getInstance().addEventToQueue(P2ppNode.EVENT_ONPUBLISH, new Object[]{contentType,contentSubtype,key});
    }

    @Override
    public void removeCallback(ResourceObject arg0) {
        LOG.info("removeCallback invoked arg0=" + arg0);

    }

}
