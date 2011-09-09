package pl.edu.pjwstk.mteam.pubsub.core;

import pl.edu.pjwstk.mteam.pubsub.interestconditions.InterestConditions;

public class Subscription {
	private InterestConditions interestConditions;
	
	//private long expirationTime; 
	
	public Subscription(){
		interestConditions = new InterestConditions(new Topic(""));
	}
	
	public InterestConditions getInterestConditions(){
		return interestConditions;
	}
	
	public void setInterestConditions(InterestConditions ic){
		interestConditions = ic;
	}
}
