package com.flaptor.clustering;

import com.flaptor.util.remote.XmlrpcSerialization;
import com.flaptor.util.remote.XmlrpcServer;

/**
 * Implementation of all interfaces exposed by clustering framework.
 * Implementation of these interfaces can be set or not, this node aggregates the set interfaces
 * and tells the client which are set.
 */
public class ClusterableServer {

	final String nodeType;
	final XmlrpcServer xmlrpcServer;
	
	public static final String CONTEXT = "clustereable"; 
	
	/**
	 * creates and starts a clusterable server
	 * @param port
	 * @param nodeType
	 */
	public ClusterableServer(int port, String nodeType) {
		this.nodeType = nodeType;
		xmlrpcServer = new XmlrpcServer(port);
		xmlrpcServer.addHandler(CONTEXT, new ClusterableImpl(nodeType));
		xmlrpcServer.start();
	}

	//needs to be public for xmlrpc
	public static class ClusterableImpl implements Clusterable {
		String nodeType;
		ClusterableImpl(String nodeType) {
			this.nodeType = nodeType;
		}
		public String getNodeType() {
			return nodeType;
		}
	}
	
	public void addModuleServer(String context, Object moduleServer) {
		xmlrpcServer.addHandler(context, moduleServer);
	}
	
	/**
	 * starts the server (the constructor starts the server by default)
	 */
	public void start() {
		xmlrpcServer.start();
	}
	
	/**
	 * stops the server
	 */
	public void stop() {
		xmlrpcServer.requestStop();
	}
}
