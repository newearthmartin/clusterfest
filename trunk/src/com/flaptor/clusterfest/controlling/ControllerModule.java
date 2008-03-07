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

package com.flaptor.clusterfest.controlling;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.flaptor.clusterfest.AbstractModule;
import com.flaptor.clusterfest.ClusterManager;
import com.flaptor.clusterfest.ClusterableListener;
import com.flaptor.clusterfest.NodeDescriptor;
import com.flaptor.clusterfest.NodeUnreachableException;
import com.flaptor.clusterfest.WebModule;
import com.flaptor.clusterfest.controlling.node.Controllable;
import com.flaptor.util.remote.NoSuchRpcMethodException;
import com.flaptor.util.remote.WebServer;
import com.flaptor.util.remote.XmlrpcClient;
import com.flaptor.util.remote.XmlrpcSerialization;

/**
 * this is the module that handles the controlling. It includes the methods to
 * start, stop, pasue, resume and kill nodes 
 *
 * @author Martin Massera
 */
public class ControllerModule extends AbstractModule<ControllerNodeDescriptor> implements WebModule {

    /**
     * adds a controller implementation to a clusterableListener
     * @param listener
     * @param c
     */
    public static void addControllerListener(ClusterableListener listener, Controllable c) {
        listener.addModuleListener("controller", XmlrpcSerialization.handler(c));
    }
    /**
     * @param client
     * @return a proxy for Controllable xmlrpc calls
     */
    public static Controllable getControllableProxy(XmlrpcClient client) {
        return (Controllable)XmlrpcClient.proxy("controller", Controllable.class, client);
    }

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(com.flaptor.util.Execute.whoAmI());

	public ControllerModule() {
	}

	@Override
	protected ControllerNodeDescriptor createModuleNode(NodeDescriptor node){
		return new ControllerNodeDescriptor(node);
	}

	@Override
    protected void notifyModuleNode(ControllerNodeDescriptor node) {
        //does nothing
    }

	protected boolean shouldRegister(NodeDescriptor node) throws NodeUnreachableException {
	    try {
	        getControllableProxy(node.getXmlrpcClient()).ping();
	        return true;
	    } catch (NoSuchRpcMethodException e) {
	        return false;
	    } catch (Exception e) {
	        return true; //return true by default
	    }
	}

	/**
	 * 
	 * @param node
	 * @return the state of the node (for the controller framework)
	 */
	public ControllerNodeState getState(NodeDescriptor node) {
		return getModuleNode(node).getState();
	}
	
	/**
	 * starts a node by calling start.sh through ssh
	 * @param node
	 * @throws IOException
	 */
	public void startNode(NodeDescriptor node) throws IOException{
	    getModuleNode(node).start();
	}

	/**
	 * starts a node by calling stop.sh through ssh
	 * @param node
	 * @throws IOException
	 */
	public void killNode(NodeDescriptor node) throws IOException {
	    getModuleNode(node).kill();
	}
	
	/**
	 * pauses a node through rpc
	 * @param node
	 */
	public void pauseNode(NodeDescriptor node) {
	    getModuleNode(node).pause();
	}

	/**
	 * resumes a node through rpc
	 * @param node
	 */
	public void resumeNode(NodeDescriptor node) {
	    getModuleNode(node).resume();
	}
	
	/**
	 * stops a node through rpc
	 * @param node
	 */
	public void stopNode(NodeDescriptor node) {
	    getModuleNode(node).stop();
	}


	//************ WEB MODULE **************
	public String getModuleHTML() {
		return "<a href=\"?action=startall\"><img src=\"media/start.png\"/>ALL</a>  <a href=\"?action=killall\"><img src=\"media/stop.png\"/>ALL</a>";
	}
	public String getNodeHTML(NodeDescriptor node, int nodeNum) {
		if (!isRegistered(node)) return null;
        ControllerNodeState state = node.isReachable() ? getState(node) : ControllerNodeState.STOPPED;
        String ret = "";
        if (state == ControllerNodeState.RUNNING) ret += "<a href=\"?action=kill&node="+nodeNum+"\"><img src=\"media/stop.png\"/></a>";
        if (state == ControllerNodeState.PAUSED) ret += "<a href=\"?action=resume&node="+nodeNum+"\"><img src=\"media/start.png\"/></a><a href=\"?action=kill&node="+nodeNum+"\"><img src=\"media/stop.png\"/></a>";
        if (state == ControllerNodeState.STOPPED) ret += "<a href=\"?action=start&node="+nodeNum+"\"><img src=\"media/start.png\"/></a>";
        return ret;
	}
	public void setup(WebServer server) {
	}

	public String action(String action, HttpServletRequest request) {
		ClusterManager cluster = ClusterManager.getInstance();
		String message = null;
		int idx = -1;
		String nodeParam = request.getParameter("node");
		if (nodeParam != null ) idx = Integer.parseInt(nodeParam);
        if ("start".equals(action)) {
            NodeDescriptor node = cluster.getNodes().get(idx);
            message = ControllingFrontend.startNode(cluster, node);
        }    
        if ("startall".equals(action)) {
            message = ControllingFrontend.startall(cluster);
        }    
        if ("killall".equals(action)) {
            message = ControllingFrontend.killall(cluster);
        }    
        if ("kill".equals(action)) {
            NodeDescriptor node = cluster.getNodes().get(idx);
            message = ControllingFrontend.killNode(cluster, node);
        }    
        if ("pause".equals(action)) {
            NodeDescriptor node = cluster.getNodes().get(idx);
            pauseNode(node);
        }    
        if ("resume".equals(action)) {
            NodeDescriptor node = cluster.getNodes().get(idx);
            resumeNode(node);
        }    
        if ("stop".equals(action)) {
            NodeDescriptor node = cluster.getNodes().get(idx);
            stopNode(node);
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