package pl.edu.pjwstk.p2pp.util;

/**
 * Class wrapping timers values for P2PP's Node.
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 *
 */
public class NodeTimers {

	/**
	 * Seconds after which if there wasn't any message from
	 */
	public static final int KEEP_ALIVE_TIMER_SECONDS = 10;

	public static final int ROUTING_TABLE_MAINTENANCE_TIMER_SECONDS = 30;
	public static final int NEIGHBOR_TABLE_MAINTENANCE_TIMER_SECONDS = 30;
	public static final int REPLICATION_MANAGEMENT_TIMER_SECONDS = 30;
	/**
	 * TODO probably not needed because every resource has own refresh time.
	 */
	public static final int RESOURCE_REFRESH_TIMER_SECONDS = 30;

	public static final int START_OF_RANGE_TIME_SECONDS = 10;
	public static final int END_OF_RANGE_TIME_SECONDS = 30;

    public static final int PEER_LOOKUP_BOOTSTRAP_TIMER_SECONDS = 30;

}
