/*
Copyright 2008 Flaptor (flaptor.com) 

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

    http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/

package com.flaptor.clusterfest;

import org.apache.log4j.Logger;

import com.flaptor.util.ClassUtil;
import com.flaptor.util.Config;
import com.flaptor.util.Execute;
import com.flaptor.util.Pair;
import com.flaptor.util.Stoppable;
import com.flaptor.util.remote.XmlrpcSerialization;
import com.flaptor.util.remote.XmlrpcServer;

/**
 * This is the listener that runs on the nodes, that listens to clusterfest RPC.
 * 
 * It is a xmlrpc server that exports the clusterable interface. Modules can register other
 * services here too.
 * 
 * @author Martin Massera
 */
public class NodeListener implements Stoppable {
    private static final Logger logger = Logger.getLogger(Execute.whoAmI());

	private String nodeType = "generic-node";
	private XmlrpcServer xmlrpcServer;
	
	public static final String CONTEXT = "clusterable"; 
	
	/**
	 * creates and starts a clusterable server. It reads clustering.node.properties for configuration
	 * or can be configured later programatically. If no nodeType is set in the config file, it 
	 * will be a <code>generic-node</code>.
     *
	 * @param port the port where it will listen for connections
	 */
	public NodeListener(int port) {
        xmlrpcServer = new XmlrpcServer(port);
		xmlrpcServer.addHandler(CONTEXT, new ClusterableImpl());
        try {
            config(Config.getConfig("clustering.node.properties"));
        } catch (IllegalStateException e) {
            logger.warn("problem opening clustering.node.properties, skipping configuration through files - " + e);
        }
        xmlrpcServer.start();
	}
	
	/**
	 * same as other constructor but specifying the config file 
	 * @param port
	 * @param cfg
	 */
    public NodeListener(int port, Config cfg) {
        xmlrpcServer = new XmlrpcServer(port);
        xmlrpcServer.addHandler(CONTEXT, new ClusterableImpl());
        config(cfg);
        xmlrpcServer.start();
    }
    private void config(Config cfg) {
        try {
            nodeType = cfg.getString("clustering.node.type");
        } catch (IllegalStateException e) {logger.warn("clustering.node.type not set in " + cfg.getFilename());}
        
        try {
            for (Pair<String,String> service : cfg.getPairList("clustering.node.listeners")) {
                Object serviceInstance = ClassUtil.instance(service.last());
                xmlrpcServer.addHandler(service.first(), XmlrpcSerialization.handler(serviceInstance));   
            }
        } catch (IllegalStateException e) {logger.warn("clustering.node.services not set in " + cfg.getFilename());}
        try {
            for (String ip: cfg.getStringArray("clustering.node.connections.accept")) {
                xmlrpcServer.addAcceptedClient(ip);
            }
        } catch (IllegalStateException e) {logger.warn("clustering.node.connections.accept not set in " + cfg.getFilename());}
        try {
            for (String ip: cfg.getStringArray("clustering.node.connections.deny")) {
                xmlrpcServer.addDeniedClient(ip);
            }
        } catch (IllegalStateException e) {logger.warn("clustering.node.connections.deny not set in " + cfg.getFilename());}
    }
	
	public void setNodeType(String nodeType) {
	    this.nodeType = nodeType;
	}
	
	/**
	 * adds a module server to the clusterable server. Basically this means
	 * that this node will export some service provided by moduleServer. 
	 * 
	 * @param context the context where the moduleServer will be exported 
	 * @param moduleServer a handler to be exported as rpc
	 */
	public void addModuleListener(String context, Object moduleServer) {
		xmlrpcServer.addHandler(context, moduleServer);
	}
	
	/**
	 * starts the server (the constructor starts the server by default)
	 */
	public void start() {
		xmlrpcServer.start();
	}
	
	//needs to be public for xmlrpc
    public class ClusterableImpl implements Clusterable {
        public String getNodeType() {
            return nodeType;
        }
    }

    public boolean isStopped() {
        return xmlrpcServer.isStopped();
    }

    public void requestStop() {
        xmlrpcServer.requestStop();
    }
    
}
