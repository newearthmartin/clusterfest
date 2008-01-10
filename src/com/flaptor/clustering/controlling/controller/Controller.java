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
 */
public class Controller extends NodeContainerModule {
    private static final Logger logger = Logger.getLogger(com.flaptor.util.Execute.whoAmI());

	public Controller() {
//		Config config = Config.getConfig("clustering.properties");
	}

	@Override
	protected ModuleNode createModuleNode(Node node){
		ControllerNode cnode = new ControllerNode(node);
		return cnode;
	}

	public ControllerNodeState getState(ControllerNode node) {
		return node.getState();
	}
	
	public void startNode(ControllerNode node) throws IOException{
		node.start();
	}

	public void killNode(ControllerNode node) throws IOException {
		node.kill();
	}
	
	public void pauseNode(ControllerNode node) {
		node.pause();
	}

	public void resumeNode(ControllerNode node) {
		node.resume();
	}
	
	public void stopNode(ControllerNode node) {
		node.stop();
	}

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
