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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.flaptor.clustering.modules.NodeContainerModule;
import com.flaptor.clustering.modules.WebModule;
import com.flaptor.util.ClassUtil;
import com.flaptor.util.Config;

/**
 * Represents a cluster of the clustering framework. Here you can register nodes
 * and all the different parts of the framework (monitoring, control, etc) 
 * will get access to them
 *
 * @author Martin Massera
 */
public class Cluster {
	
    private static final Logger logger = Logger.getLogger(com.flaptor.util.Execute.whoAmI());
	
    private static class InitializiationOnDemandHolder {
        private static Cluster instance = new Cluster();    
    }
    
    private Map<String, NodeContainerModule> modules = new HashMap<String, NodeContainerModule>();
    private List<WebModule> webModules = new ArrayList<WebModule>();
    private Map<String, WebModule> moduleActionMap = new HashMap<String, WebModule>();
    
    private Cluster() {
    	//after initializing everything, register all nodes
    	Config config = Config.getConfig("clustering.properties");
    	String[] hosts = config.getStringArray("clustering.nodes");
    	String[] moduleDefs = config.getStringArray("clustering.modules");
        
    	for (String moduleDef : moduleDefs) {
    		String [] defs = moduleDef.split(":");
    		NodeContainerModule module = (NodeContainerModule)ClassUtil.instance(defs[1]); 
    		modules.put(defs[0], module);
    		if (module instanceof WebModule) addWebModule((WebModule)module);
    	}
    	
    	for (int i = 0; i < hosts.length; i++) {
            String[] host = hosts[i].split(":");
            String hostName = host[0];
            int port = Integer.parseInt(host[1].trim());
            String installDir = (host.length > 2) ? host[2] : null;
            registerNode(hostName, port, installDir);
        }
        
		new Timer().scheduleAtFixedRate(new TimerTask(){
			public void run() {
				updateNodes();
			}
		}, 0, config.getInt("clustering.checkNodesInterval"));
    }
    
    
	private void addWebModule(WebModule wm) {
		for (String action : wm.getActions()) {
			moduleActionMap.put(action, wm);
		}
		webModules.add(wm);
	}
	
    /**
     * @return the singleton instance of Cluster
     */
    public static Cluster getInstance() {
        return InitializiationOnDemandHolder.instance;
    }

	private List<Node> nodes = new ArrayList<Node>();

	/**
	 * Registers a node in the clustering framework and all its modules
	 * @param host the host where the node is
	 * @param port the port where that node is listening for clusterfest 
	 * @param installDir the directory where it is installed in that host (not the baseDir, should be something like baseDir/searcher)
	 * @return the registered node
	 */
	public Node registerNode(String host, int port, String installDir) {
		synchronized (nodes) {
			Node node = new Node(host, port, installDir);
	        nodes.add(node);
	       	updateClusterNodeInfo(node);
			return node;
		}
	}

	/**
	 * removes a node from the cluster and all the modules of the clustering framework
	 */
	public void unregisterNode(Node node) {
		synchronized (nodes) {
			nodes.remove(node);
			for (NodeContainerModule module: modules.values()) {
				module.unregisterNode(node);
			}
		}
	}

	/**
	 * updates all info of all nodes
	 */
	public void updateAllInfoAllNodes() {
		synchronized (nodes) {
			ExecutorService threadPool = Executors.newFixedThreadPool(nodes.size());
			for(final Node node: nodes) {
				threadPool.submit(new Runnable() {
					public void run() {
						updateAllInfo(node);
					}
				});
			}
			try {
			    threadPool.shutdown();
			    if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) logger.error("nodes took too long to update");
			} catch (InterruptedException e) {logger.error("unexpected interruption while waiting", e);}
		}
	}

	/**
	 * updates the node info in the cluster and in all the modules where it s registered
	 * @throws NodeUnreachableException 
	 * @return true if all states where updated correctly
	 */
	public boolean updateAllInfo(Node node) {
		updateClusterNodeInfo(node);
		
		//TODO analyse taking this method out
		
		for (NodeContainerModule module: modules.values()) {
			if (module.isRegistered(node)) {
				module.updateNode(module.getNode(node));
			}
		}

		return node.isReachable();
	}
	
	/**
	 * updates the info of the node related to the cluster 
	 * and registers in the modules it belongs if it hasnt already been registered 
	 * @param node
	 * @return true if the state was updated correctly
	 */
	public boolean updateClusterNodeInfo(Node node) {
		boolean noErrors = true;
		try {
			node.updateInfo();
		} catch (NodeUnreachableException e) {
			logger.warn("tried to update info for unreachable node at " + node.getHost() + ":" + node.getPort(), e);
			noErrors = false;
		}
		for (NodeContainerModule module: modules.values()) {
			try {
				if (module.nodeBelongs(node)){
					if (!module.isRegistered(node)) module.registerNode(node);
				} else {
					if (module.isRegistered(node)) module.unregisterNode(node);
				}
			} catch (NodeUnreachableException e) {
				logger.warn("tried to update info for unreachable node at " + node.getHost() + ":" + node.getPort(), e);
				noErrors = false;
			}
		}
		return noErrors;
	}

	/**
	 * @return the list of registered nodes
	 */
	public List<Node> getNodes() {
		return nodes;
	}
	
	public NodeContainerModule getModule(String moduleName) {
		return modules.get(moduleName);
	}
	
	public List<WebModule> getWebModules() {
		return webModules;
	}
	
	public WebModule getModuleForAction(String action) {
		return moduleActionMap.get(action);
	}
	
	/**
	 * check to see if nodes that weren't alive came to live
	 */
	private void updateNodes() {
		synchronized (nodes) {
			for (Node node: nodes) {
				updateClusterNodeInfo(node);
			}
		}
	}
}
