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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;

import com.flaptor.clustering.Cluster;
import com.flaptor.clustering.ClusterableServer;
import com.flaptor.clustering.Node;
import com.flaptor.clustering.NodeUnreachableException;
import com.flaptor.clustering.controlling.nodes.Controllable;
import com.flaptor.clustering.modules.ModuleNode;
import com.flaptor.clustering.modules.NodeContainerModule;
import com.flaptor.clustering.modules.WebModule;
import com.flaptor.clustering.monitoring.monitor.MonitorNode;
import com.flaptor.clustering.monitoring.monitor.NodeState;
import com.flaptor.clustering.monitoring.monitor.NodeState.Sanity;
import com.flaptor.util.remote.WebServer;
import com.flaptor.util.remote.XmlrpcClient;
import com.flaptor.util.remote.XmlrpcSerialization;

/**
 * this is the module that handles the controlling. It includes the methods to
 * start, stop, pasue, resume and kill nodes 
 *
 * @author martinmassera
 */
public class Controller extends NodeContainerModule implements WebModule {
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

	//************ WEB MODULE **************
	public String getModuleHTML() {
		return "<a href=\"?action=startall\"><img src=\"media/start.png\"/>ALL</a>  <a href=\"?action=killall\"><img src=\"media/stop.png\"/>ALL</a>";
	}
	public String getNodeHTML(Node node, int nodeNum) {
		if (!isRegistered(node)) return null;
		ControllerNode cnode = (ControllerNode)getNode(node);
        ControllerNodeState state = node.isReachable() ? getState(cnode) : ControllerNodeState.STOPPED;
        String ret = "";
        if (state == ControllerNodeState.RUNNING) ret += "<a href=\"?action=kill&node="+nodeNum+"\"><img src=\"media/stop.png\"/></a>";
        if (state == ControllerNodeState.PAUSED) ret += "<a href=\"?action=resume&node="+nodeNum+"\"><img src=\"media/start.png\"/></a><a href=\"?action=kill&node="+nodeNum+"\"><img src=\"media/stop.png\"/></a>";
        if (state == ControllerNodeState.STOPPED) ret += "<a href=\"?action=start&node="+nodeNum+"\"><img src=\"media/start.png\"/></a>";
        return ret;
	}
	public void setup(WebServer server) {
	}

	public String action(String action, HttpServletRequest request) {
		Cluster cluster = Cluster.getInstance();
		String message = null;
		int idx = -1;
		String nodeParam = request.getParameter("node");
		if (nodeParam != null ) idx = Integer.parseInt(nodeParam);
        if ("start".equals(action)) {
            Node node = cluster.getNodes().get(idx);
            message = ControllingFrontend.startNode(cluster, node);
        }    
        if ("startall".equals(action)) {
            message = ControllingFrontend.startall(cluster);
        }    
        if ("killall".equals(action)) {
            message = ControllingFrontend.killall(cluster);
        }    
        if ("kill".equals(action)) {
            Node node = cluster.getNodes().get(idx);
            message = ControllingFrontend.killNode(cluster, node);
        }    
        if ("pause".equals(action)) {
            Node node = cluster.getNodes().get(idx);
            ControllerNode cnode = (ControllerNode)getNode(node);
            pauseNode(cnode);
        }    
        if ("resume".equals(action)) {
            Node node = cluster.getNodes().get(idx);
            ControllerNode cnode = (ControllerNode)getNode(node);
            resumeNode(cnode);
        }    
        if ("stop".equals(action)) {
            Node node = cluster.getNodes().get(idx);
            ControllerNode cnode = (ControllerNode)getNode(node);
            stopNode(cnode);
        }
        return message;
	}

	public List<String> getActions() {
		List<String> l = new ArrayList<String>();
		l.add("start");
		l.add("pause");
		l.add("stop");
		l.add("kill");
		l.add("startall");
		l.add("killall");
		return l;
	}
}
