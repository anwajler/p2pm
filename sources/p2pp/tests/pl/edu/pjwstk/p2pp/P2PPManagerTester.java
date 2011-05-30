package pl.edu.pjwstk.p2pp;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;

import pl.edu.pjwstk.p2pp.util.P2PPMessageFactory;

public class P2PPManagerTester {

	@Before
	public void setUp() {
		Calendar calendar = new GregorianCalendar();

		// sets properties that will enable time stamps in log file names
		System.setProperty("log4j.year", "" + calendar.get(Calendar.YEAR));
		System.setProperty("log4j.month", "" + (calendar.get(Calendar.MONTH) + 1));
		System.setProperty("log4j.day", "" + calendar.get(Calendar.DAY_OF_MONTH));
		System.setProperty("log4j.hour", "" + calendar.get(Calendar.HOUR_OF_DAY));
		System.setProperty("log4j.minute", "" + calendar.get(Calendar.MINUTE));
		System.setProperty("log4j.second", "" + calendar.get(Calendar.SECOND));

		// sets a file that contains info about what things to log and where to log
		PropertyConfigurator.configure("log4j.properties");
	}

	@Test
	public void testStart() throws IOException, RuntimeException {
		P2PPManager manager = new P2PPManager(9999, 9999, 9999, 9999, 9999, "", "", new P2PPMessageFactory(), new byte[] { (byte) 3 });

		manager.start();
	}

	@Test
	public void testStop() {
		fail("Not yet implemented");
	}

}
