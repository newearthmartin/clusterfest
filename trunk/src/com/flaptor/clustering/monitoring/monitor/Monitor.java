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

package com.flaptor.clustering.monitoring.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.flaptor.clustering.ClusterableServer;
import com.flaptor.clustering.Node;
import com.flaptor.clustering.NodeUnreachableException;
import com.flaptor.clustering.modules.ModuleNode;
import com.flaptor.clustering.modules.NodeContainerModule;
import com.flaptor.clustering.modules.WebModule;
import com.flaptor.clustering.monitoring.monitor.NodeState.Sanity;
import com.flaptor.clustering.monitoring.nodes.Monitoreable;
import com.flaptor.util.ClassUtil;
import com.flaptor.util.Config;
import com.flaptor.util.Pair;
import com.flaptor.util.remote.WebServer;
import com.flaptor.util.remote.XmlrpcClient;
import com.flaptor.util.remote.XmlrpcSerialization;

/**
 * Monitor module for the clustering framework. It allows to retrieve variables
 * from the nodes and write a checker to analyse these variables and determine
 * the state of each node
 *  
 * @author martinmassera
 */
public class Monitor extends NodeContainerModule implements WebModule {
    public final static String MODULE_CONTEXT = "monitor";
	
	private static final Logger logger = Logger.getLogger(com.flaptor.util.Execute.whoAmI());

	public Monitor() {
		Config config = Config.getConfig("clustering.properties");
		new Timer().scheduleAtFixedRate(new TimerTask(){
			public void run() {
				updateNodes();
			}
		}, 0, config.getInt("clustering.monitor.refreshInterval"));
	}

	@Override
	protected ModuleNode createModuleNode(Node node) {
		MonitorNode mnode = new MonitorNode(node);
		try {
			mnode.setChecker(getCheckerForType(node.getType()));
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
		updateNode(mnode);
		return mnode;
	}

    private void updateNodes() {
    	//update states of all the monitored nodes
    	synchronized (nodes) {
	    	for (ModuleNode node : nodes) {
				updateNode((MonitorNode)node);
	    	}
    	}
    }
    
	@Override
	public boolean nodeBelongs(Node node) throws NodeUnreachableException {
		try {
			boolean ret = getMonitoreableProxy(node.getXmlrpcClient()).ping();
			return ret;
		} catch (NoSuchMethodException e) {
			return false;
		} catch (Exception e) {
			throw new NodeUnreachableException(e);
		}
	}

	public boolean updateNode(ModuleNode node) {
		try {
			((MonitorNode)node).updateState();
			return true;
		} catch (NodeUnreachableException e) {
			logger.warn(e);
			return false;
		}
	}

	/**
	 * retrieves a log from the node, may not be at the moment but an older copy 
	 *  
	 * @param node
	 * @param logName out, err, or a filename
	 * @return a pair (log, timestamp)
	 */
	public Pair<String, Long> retrieveLog(MonitorNode node, String logName) {
		if (!node.getLogs().containsKey(logName)) {
			try {
				node.updateLog(logName);
			} catch (NodeUnreachableException e) {
				logger.warn(e);
			}
		}
		return node.retrieveLog(logName);
	}

	public void setChecker(MonitorNode node, NodeChecker checker) {
		node.setChecker(checker);
	}

	
	public NodeChecker getChecker(String checkerClassName) {
		
		try {
			return (NodeChecker) ClassUtil.instance(checkerClassName);
		} catch (Throwable t) {
			logger.error(t);
			return null;
		}
	}

	/**
	 * @param type
	 * @return the checker defined in clustering.properties for a particular node type
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public NodeChecker getCheckerForType(String type) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		Config config = Config.getConfig("clustering.properties");
		try {
			String clazz = config.getString("clustering.monitor.checker."+type);
			return getChecker(clazz);
		}catch (IllegalStateException e) {
			logger.error(e);
			return null;
		}
    }
	/**
	 * adds a monitoreable implementation to a clusterable server
	 * @param clusterableServer
	 * @param m
	 */
	public static void addMonitorServer(ClusterableServer clusterableServer, Monitoreable m) {
		clusterableServer.addModuleServer(Monitor.class.getName(), XmlrpcSerialization.handler(m));
	}
	/**
	 * @param client
	 * @return a proxy for monitoreable xmlrpc calls
	 */
	public static Monitoreable getMonitoreableProxy(XmlrpcClient client) {
		return (Monitoreable)XmlrpcClient.proxy(Monitor.class.getName(), Monitoreable.class, client);
	}

	
	//************ WEB MODULE **************
	public String getModuleHTML() {
		return null;
	}
	public String getNodeHTML(Node node, int nodeNum) {
        MonitorNode monitorNode = (MonitorNode)getNode(node);

        Sanity sanity = node.isReachable() ? Sanity.UNKNOWN : Sanity.UNREACHABLE;
    
        if (monitorNode != null) {
            NodeState state = monitorNode.getLastState();
            if (state != null) sanity = state.getSanity();
        }
        return "<a class=\"sanity"+sanity+"\" href=\"monitorNode.jsp?node=" + nodeNum + "\">"+ sanity +"</a>";
	}
	public void setup(WebServer server) {
	}
	public String action(String action, HttpServletRequest request) {
		return null;
	}

	public List<String> getActions() {
		return new ArrayList<String>();
	}
}
