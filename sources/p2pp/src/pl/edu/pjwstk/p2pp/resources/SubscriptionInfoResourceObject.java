package pl.edu.pjwstk.p2pp.resources;

import pl.edu.pjwstk.p2pp.objects.ResourceObject;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

/**
 * SubscriptionInfo resource object as defined in PJIIT extension to P2PP APIs.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class SubscriptionInfoResourceObject extends ResourceObject {

	/**
	 * Creates SubscriptionInfo resource object.
	 * 
	 * @param contentSubType
	 *            SubscriptionInfo subtype. May be {@link P2PPUtils#PENDING_SUBSCRIPTION_INFO_CONTENT_SUBTYPE} or
	 *            {@link P2PPUtils#ACCEPTED_SUBSCRIPTION_INFO_CONTENT_SUBTYPE}.
	 */
	public SubscriptionInfoResourceObject(byte contentSubType) {
		super(P2PPUtils.SUBSCRIPTION_INFO_CONTENT_TYPE, contentSubType);
	}

	@Override
	public String getValueAsString() {
		// TODO Auto-generated method stub
		return null;
	}

}
