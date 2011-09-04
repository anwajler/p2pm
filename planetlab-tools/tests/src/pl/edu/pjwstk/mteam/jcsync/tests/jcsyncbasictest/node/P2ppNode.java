/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.pjwstk.mteam.jcsync.tests.jcsyncbasictest.node;

import pl.edu.pjwstk.mteam.p2p.P2PNode;

/**
 *
 * @author pb
 */
public class P2ppNode extends P2PNode{
    
    public P2ppNode(NodeCallback callback,P2PNode.RoutingAlgorithm algName ){
        super(callback, algName);
    }
    public String getUserId(){
        return super.getUserName();
    }
}
