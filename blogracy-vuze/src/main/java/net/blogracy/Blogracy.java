/**
 * 
 */
package net.blogracy;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;

import net.blogracy.logging.Logger;
import net.blogracy.services.DownloadService;
import net.blogracy.services.LookupService;
import net.blogracy.services.SeedService;
import net.blogracy.services.StoreService;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.gudy.azureus2.plugins.Plugin;
import org.gudy.azureus2.plugins.PluginException;
import org.gudy.azureus2.plugins.PluginInterface;

/**
 * @author mic
 *
 */
public class Blogracy implements Plugin {

    private ConnectionFactory connectionFactory;
    private Connection connection;
    private StoreService storeService;
    private LookupService lookupService;
    private SeedService seedService;
    private DownloadService downloadService;

	/* (non-Javadoc)
	 * @see org.gudy.azureus2.plugins.Plugin#initialize(org.gudy.azureus2.plugins.PluginInterface)
	 */
	@Override
	public void initialize(PluginInterface plugin) throws PluginException {
		// TODO Auto-generated method stub
		createQueues(plugin);
	}

    void createQueues(final PluginInterface plugin) {
    	BasicConfigurator.configure();
    	org.apache.log4j.Logger.getLogger("org.apache").setLevel(Level.INFO);
    	
    	String brokerUrl = ActiveMQConnection.DEFAULT_BROKER_URL;
        try {
	        connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
	        connection = connectionFactory.createConnection();
	        connection.start();
	        
	        storeService = new StoreService(connection, plugin);
	        lookupService = new LookupService(connection, plugin);
	        seedService = new SeedService(connection, plugin);
	        downloadService = new DownloadService(connection, plugin);
			Logger.info("Blogracy Vuze plugin has started correctly");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
