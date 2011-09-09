/**
 * 
 */
package pl.edu.pjwstk.net.message.TURN.tools;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.PropertyConfigurator;

import pl.edu.pjwstk.net.message.TURN.tools.TURNTestClient;
import pl.edu.pjwstk.net.message.TURN.tools.TURNTestLauncher;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.message.TURN.tools
 */
public class TURNTestLauncher {
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(TURNTestLauncher.class);
	private static final String UDP_ADDRESS_OPTION = "udp_ip";
	private static final String UDP_PORT_OPTION = "udp_port";
	private static final String TURN_MODE = "mode";
	private static final String TURN_USERNAME = "tuser";
	private static final String TURN_PASSWORD = "tpassword";
	private static final String TURN_DOMAIN = "tdomain";
	private Options options;
	private static CommandLine cmd;
	
	private static void configureLogger() {
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

	private CommandLine createCommandLine(String[] args) throws ParseException {
		options = new Options();
		options.addOption(TURN_MODE, true,
				"TURN mode [SERVER/CLIENT]");
		options.addOption(TURN_USERNAME, true,
				"TURN username");
		options.addOption(TURN_PASSWORD, true,
				"TURN password");
		options.addOption(TURN_DOMAIN, true,
				"TURN domain");
		options.addOption(UDP_ADDRESS_OPTION, true, 
				"UDP address to be used. If present UDP protocol will be used.");
		options.addOption(UDP_PORT_OPTION, true,
				"UDP port to be used. If present, UDP protocol will be used.");
		

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);
		return cmd;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		configureLogger();
		logger.info("Starting TURN launcher");
		TURNTestLauncher turnTestLauncher = new TURNTestLauncher();
		try {
			logger.info("Parsing parameters");
			cmd = turnTestLauncher.createCommandLine(args);
			
			boolean argsOK = turnTestLauncher.parseAndCheckArguments();
			if (!argsOK) {
				logger.info("Bad arguments.");
				System.out.println("Bad arguments. Read help.");
				turnTestLauncher.printHelp();
				return;
			}
			String modeString = cmd.getOptionValue(TURN_MODE);
			logger.info("mode=(" + modeString + ")");

			String udp_address = cmd.getOptionValue(UDP_ADDRESS_OPTION);
			logger.info("ip=" + udp_address);

			String udp_port = cmd.getOptionValue(UDP_PORT_OPTION);
			logger.info("port=" + udp_port);
			
			String TURNUsername = cmd.getOptionValue(TURN_USERNAME);
			logger.info("TURN user = " + TURNUsername);
			
			String TURNPassword = cmd.getOptionValue(TURN_PASSWORD);
			logger.info("TURN password = " + TURNPassword);
			
			String TURNDomain = cmd.getOptionValue(TURN_DOMAIN);
			logger.info("TURN domain = " + TURNDomain);
						
			if (modeString.equals("SERVER")){
					//TURNTestServer TURNTestServer = new TURNTestServer(InetAddress.getByName(udp_address),Integer.parseInt(udp_port));
					//TURNTestServer.start();
			} else if (modeString.equals("CLIENT")) {
					TURNTestClient TURNTestClient = new TURNTestClient(InetAddress.getByName(udp_address),Integer.parseInt(udp_port), TURNUsername, TURNPassword, TURNDomain);
					TURNTestClient.start();
			}  

		} catch (Exception e) {
			logger.error("Parameters parsing error: ",e);
		}

	}

	private void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Arguments depend on mode.","",options,null);
	}

	private boolean parseAndCheckArguments() {

		return true;
	}


}
