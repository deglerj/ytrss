import java.net.URL;
import java.security.ProtectionDomain;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;


/**
 * Main class for the runnable WAR generated by the build script. Called when executing the WAR instead of deploying.<br>
 * Starts a Jetty instance contained in the WAR archive and deploys the WAR on it.
 * 
 * @author jdegler
 * 
 */
public class Main {

	/**
	 * Main method
	 */
	public static void main(final String[] args) throws Exception {
		int port = 80;

		// Check for port argument
		if (args.length == 2 && "-p".equals(args[0])) {
			port = Integer.valueOf(args[1]);
		}

		final Server server = new Server();
		final ServerConnector connector = new ServerConnector(server);
		connector.setIdleTimeout(1000 * 60 * 60);
		connector.setSoLingerTime(-1);
		connector.setPort(port);
		server.setConnectors(new Connector[] { connector });

		final WebAppContext context = new WebAppContext();
		context.setServer(server);
		context.setContextPath(System.getProperty("app.context", "/"));

		final ProtectionDomain protectionDomain = Main.class.getProtectionDomain();
		final URL location = protectionDomain.getCodeSource().getLocation();
		context.setWar(location.toExternalForm());

		server.setHandler(context);

		server.start();

		System.in.read();
		server.join();

	}

	private Main() {
		// Main class, no instances allowed
	}

}
