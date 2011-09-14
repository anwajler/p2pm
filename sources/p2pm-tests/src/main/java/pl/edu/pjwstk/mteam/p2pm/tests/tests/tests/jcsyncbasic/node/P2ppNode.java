package pl.edu.pjwstk.mteam.p2pm.tests.tests.tests.jcsyncbasic.node;

import pl.edu.pjwstk.mteam.p2p.P2PNode;

/**
 *
 * @author pb
 */
public class P2ppNode extends P2PNode{
    
    public P2ppNode(NodeCallback callback,P2PNode.RoutingAlgorithm algName ){
        super(callback, algName);
        super.enableDebug();
    }
    public String getUserId(){
        return super.getUserName();
    }
}
