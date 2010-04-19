package org.protege.owl.server.util;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.protege.owl.server.api.ConflictManager;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerConnection;
import org.protege.owl.server.api.ServerFactory;
import org.protege.owl.server.configuration.ServerConfiguration;

/*
 * This is suitable for declarative services but the junit task uses him also.
 */

public class Configurator {
	private Logger logger = Logger.getLogger(Configurator.class);
    private ServerConfiguration configuration;
    private Set<ServerFactory> serverFactories = new HashSet<ServerFactory>();
    
    private ServerFactory currentServerFactory;
    private Server server;
    private ServerFactory currentConnectionFactory;
    private ServerConnection connection;
    private ConflictManager conflictManager;
    private ServerFactory currentConflictFactory;
    
    public Configurator(ServerConfiguration configuration) {
    	this.configuration = configuration;
    }
    
    public Server getCurrentServer() {
        return server;
    }
    
    public void addServerFactory(ServerFactory factory) {
    	if (factory.hasSuitableServer(configuration) || 
    	        factory.hasSuitableConflictManager(configuration) ||
    	        factory.hasSuitableConnection(configuration)) {
    		serverFactories.add(factory);
    		if (!isReady() || (conflictManager == null && factory.hasSuitableConflictManager(configuration))) {
    			rebuild();
    		}
    	}
    }

    public void removeServerFactory(ServerFactory factory)  {
    	serverFactories.remove(factory);
    	boolean needsRebuild = false;
    	if (factory == currentServerFactory) {
    		server.dispose();
    		server = null;
    		currentServerFactory = null;
    		
    		if (connection != null) connection.dispose();
    		connection = null;
    		currentConnectionFactory = null;
    		
    		conflictManager = null;
    		currentConflictFactory = null;
    		
    		needsRebuild = true;
    	}
    	else if (factory == currentConnectionFactory) {
    		if (connection != null) connection.dispose();
    		connection = null;
    		currentConnectionFactory = null;
    		
    		conflictManager = null;
    		currentConflictFactory = null;
    		
    		needsRebuild = true;
    	}
    	else if (factory == currentConflictFactory) {
    		conflictManager = null;
    		currentConflictFactory = null;
    	}
    	else {
    		needsRebuild = false;
    	}
    	if (needsRebuild) rebuild();
    }

	public void start() {
		rebuild();
	}
	
	public void stop() {
		if (connection != null) {
			connection.dispose();
			connection = null;
		}
		server = null;
	}
	
    
    public boolean isReady() {
        return server != null && connection != null;
    }
    
    private void rebuild() {
    	try {
    		if (server == null) {
    		    for (ServerFactory factory : serverFactories) {
    		        if (factory.hasSuitableServer(configuration)) {
    		            server = factory.createServer(configuration);
    		            currentServerFactory = factory;
    		            break;
    		        }
    			}
    		}
    		if (server != null && connection == null) {
                for (ServerFactory factory : serverFactories) {
                	if (factory.hasSuitableConnection(configuration)) {
                		connection = factory.createServerConnection(configuration);
                		connection.initialize(server);
                		currentConnectionFactory = factory;
                		break;
                	}
                }
    		}
    		if (server != null && connection != null && conflictManager == null) {
    			for (ServerFactory factory : serverFactories) {
    				if (factory.hasSuitableConflictManager(configuration)) {
    					conflictManager = factory.createConflictManager(configuration);
    					conflictManager.initialise(server);
    					server.setConflictManager(conflictManager);
    					currentConflictFactory = factory;
    					break;
    					
    				}
    			}
    		}
    	}
    	catch (Throwable t) {
    		ServerConnection tmp = connection;
    		server = null;
    		currentServerFactory = null;
    		connection = null;
    		currentConnectionFactory = null;
    		conflictManager = null;
    		currentConflictFactory = null;
    		logger.warn("Exception caught trying to configure server ", t);
    		if (tmp != null) {
    			tmp.dispose();
    		}
    	}
    }
}
