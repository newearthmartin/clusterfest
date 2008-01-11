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

package com.flaptor.clustering.controlling.controller;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.flaptor.clustering.ClusterableServer;
import com.flaptor.clustering.Node;
import com.flaptor.clustering.NodeUnreachableException;
import com.flaptor.clustering.controlling.nodes.Controllable;
import com.flaptor.clustering.modules.ModuleNode;
import com.flaptor.clustering.modules.NodeContainerModule;
import com.flaptor.util.remote.XmlrpcClient;
import com.flaptor.util.remote.XmlrpcSerialization;

/**
 * this is the module that handles the controlling. It includes the methods to
 * start, stop, pasue, resume and kill nodes 
 *
 * @author martinmassera
 */
public class Controller extends NodeContainerModule {
    private static final Logger logger = Logger.getLogger(com.flaptor.util.Execute.whoAmI());

	public Controller() {
	}

	@Override
	protected ModuleNode createModuleNode(Node node){
		ControllerNode cnode = new ControllerNode(node);
		return cnode;
	}

	/**
	 * 
	 * @param node
	 * @return the state of the node (for the controller framework)
	 */
	public ControllerNodeState getState(ControllerNode node) {
		return node.getState();
	}
	
	/**
	 * starts a node by calling start.sh through ssh
	 * @param node
	 * @throws IOException
	 */
	public void startNode(ControllerNode node) throws IOException{
		node.start();
	}

	/**
	 * starts a node by calling stop.sh through ssh
	 * @param node
	 * @throws IOException
	 */
	public void killNode(ControllerNode node) throws IOException {
		node.kill();
	}
	
	/**
	 * pauses a node through rpc
	 * @param node
	 */
	public void pauseNode(ControllerNode node) {
		node.pause();
	}

	/**
	 * resumes a node through rpc
	 * @param node
	 */
	public void resumeNode(ControllerNode node) {
		node.resume();
	}
	
	/**
	 * stops a node through rpc
	 * @param node
	 */
	public void stopNode(ControllerNode node) {
		node.stop();
	}

	/**
	 * updates the state of a node 
	 * @param node
	 * @return
	 * TODO make return depend on success 
	 */
	public boolean updateNodeState(ControllerNode node) {
//		try {
			node.updateState();
			return true;
//		} catch (NodeUnreachableException e) {
//			logger.warn(e);
//			return false;
//		}
	}

	private void updateNodes() {
    	//update states of all the monitored nodes
    	for (ModuleNode node : nodes) {
			updateNodeState((ControllerNode) node);
    	}
    }

	@Override
	public boolean nodeBelongs(Node node) throws NodeUnreachableException {
		try {
			boolean ret = getControllableProxy(node.getXmlrpcClient()).ping();
			return ret;
		} catch (NoSuchMethodException e) {
			return false;
		} catch (Exception e) {
			return true; //return true by default
		}
	}

	@Override
	public boolean updateNode(ModuleNode node) {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * adds a monitoreable implementation to a clusterable server
	 * @param clusterableServer
	 * @param m
	 */
	public static void addControllerServer(ClusterableServer clusterableServer, Controllable c) {
		clusterableServer.addModuleServer(Controller.class.getName(), XmlrpcSerialization.handler(c));
	}
	/**
	 * @param client
	 * @return a proxy for monitoreable xmlrpc calls
	 */
	public static Controllable getControllableProxy(XmlrpcClient client) {
		return (Controllable)XmlrpcClient.proxy(Controller.class.getName(), Controllable.class, client);
	}
}
