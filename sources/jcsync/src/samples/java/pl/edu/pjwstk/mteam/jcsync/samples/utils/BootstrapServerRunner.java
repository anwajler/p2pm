package pl.edu.pjwstk.mteam.jcsync.samples.utils;

import pl.edu.pjwstk.p2pp.P2PPManager;
import pl.edu.pjwstk.p2pp.objects.P2POptions;
import pl.edu.pjwstk.p2pp.superpeer.SuperPeerBootstrapServer;
import pl.edu.pjwstk.p2pp.superpeer.SuperPeerConstants;
import pl.edu.pjwstk.p2pp.util.P2PPMessageFactory;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

/**
 * 
 * @author Piotr Bucior
 */
public class BootstrapServerRunner extends Thread{
    
    private P2PPManager manager;
    private final int port;
    
    public BootstrapServerRunner(int port){       
        this.port = port;
    }
    @Override
    public void run(){
        try{
                this.manager = new P2PPManager(0, this.port, 0, 0, 0, "", "", new P2PPMessageFactory(), "myOverlayID".getBytes("UTF-8"));
                String hashAlgorithm = "SHA-1";
                byte hashLength = 20;
                byte hashBase = 2;
                String overlayID = "myOverlayID";
                this.manager.setOptions(new P2POptions(P2PPUtils.convertHashAlgorithmName(hashAlgorithm), hashLength,
                        P2PPUtils.convertP2PAlgorithmName(SuperPeerConstants.SUPERPEER_PROTOCOL_NAME), hashBase, overlayID.getBytes("UTF-8")));

                SuperPeerBootstrapServer server = new SuperPeerBootstrapServer();
                this.manager.addEntity(server);
                this.manager.start();
                }catch(Exception e){
                    e.printStackTrace();
                }
    }
}
