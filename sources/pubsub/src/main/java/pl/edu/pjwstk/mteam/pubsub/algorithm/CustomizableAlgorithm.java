package pl.edu.pjwstk.mteam.pubsub.algorithm;

import pl.edu.pjwstk.mteam.core.AbstractCustomizableAlgorithm;
import pl.edu.pjwstk.mteam.pubsub.core.CoreAlgorithm;

public abstract class CustomizableAlgorithm extends AbstractCustomizableAlgorithm
                                            implements AlgorithmMessageInterface,
                                            		   AlgorithmInterface{
	
	public CoreAlgorithm getCoreAlgorithm(){
		return (CoreAlgorithm)psManager;
	}
        
}
