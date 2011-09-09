package pl.edu.pjwstk.mteam.core;

import pl.edu.pjwstk.mteam.pubsub.core.CoreAlgorithm;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;

public abstract class AbstractCustomizableAlgorithm implements PubSubInterface{
	protected AbstractCoreAlgorithm psManager;
	
	public void setCoreAlgorithm(CoreAlgorithm manager){
		psManager = manager;
	}
}
