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

package com.flaptor.clustering;

import com.flaptor.util.remote.XmlrpcSerialization;
import com.flaptor.util.remote.XmlrpcServer;

/**
 * Implementation of all interfaces exposed by clustering framework.
 * Implementation of these interfaces can be set or not, this node aggregates the set interfaces
 * and tells the client which are set.
 *
 * @author martinmassera
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
	
	/**
	 * adds a module server to the clusterable server. Basically this means
	 * that this node will export some service provided by moduleServer. 
	 * 
	 * @param context the context where the moduleServer will be exported 
	 * @param moduleServer a handler to be exported as rpc
	 */
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
