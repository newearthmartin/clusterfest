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

package com.flaptor.clusterfest.monitoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.flaptor.clusterfest.AbstractModule;
import com.flaptor.clusterfest.ClusterableListener;
import com.flaptor.clusterfest.NodeDescriptor;
import com.flaptor.clusterfest.NodeUnreachableException;
import com.flaptor.clusterfest.WebModule;
import com.flaptor.clusterfest.monitoring.NodeChecker.Sanity;
import com.flaptor.clusterfest.monitoring.node.Monitoreable;
import com.flaptor.util.ClassUtil;
import com.flaptor.util.Config;
import com.flaptor.util.Pair;
import com.flaptor.util.remote.NoSuchRpcMethodException;
import com.flaptor.util.remote.WebServer;
import com.flaptor.util.remote.XmlrpcClient;
import com.flaptor.util.remote.XmlrpcSerialization;

/**
 * Monitor module for Clusterfest. It allows to retrieve variables
 * from the nodes and write a checker to analyse these variables and determine
 * the state of each node
 *  
 * @author Martin Massera
 */
public class MonitorModule extends AbstractModule<MonitorNodeDescriptor> implements WebModule {
    public final static String MODULE_CONTEXT = "monitor";
	
	private static final Logger logger = Logger.getLogger(com.flaptor.util.Execute.whoAmI());

	private final Map<String, PropertyFormatter> formatters = new HashMap<String, PropertyFormatter>();
	
	public MonitorModule() {
		Config config = Config.getConfig("clustering.properties");
		new Timer().scheduleAtFixedRate(new TimerTask(){
			public void run() {
				updateNodes();
			}
		}, 0, config.getInt("clustering.monitor.refreshInterval"));
	}

	@Override
	protected MonitorNodeDescriptor createModuleNode(NodeDescriptor node) {
		MonitorNodeDescriptor monitorNode = new MonitorNodeDescriptor(node);
		try {
			monitorNode.setChecker(getCheckerForType(node.getType()));
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
		updateNodeInfo(monitorNode);
		return monitorNode;
	}

    protected void notifyModuleNode(MonitorNodeDescriptor node) {
        // we update the node info every time notify gets invoked
        updateNodeInfo(node);
    }

    private void updateNodes() {
    	//update states of all the monitored nodes
    	synchronized (nodes) {
	    	for (MonitorNodeDescriptor node : nodes) {
				updateNodeInfo(node);
	    	}
    	}
    }
    
	@Override
	public boolean shouldRegister(NodeDescriptor node) throws NodeUnreachableException {
		try {
			boolean ret = getMonitoreableProxy(node.getXmlrpcClient()).ping();
			return ret;
		} catch (NoSuchRpcMethodException e) {
			return false;
		} catch (Exception e) {
			throw new NodeUnreachableException(e);
		}
	}

	public boolean updateNodeInfo(MonitorNodeDescriptor node) {
		try {
			node.updateState();
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
	public Pair<String, Long> retrieveLog(NodeDescriptor node, String logName) {
	    MonitorNodeDescriptor moduleNode = getModuleNode(node);
		if (!moduleNode.getLogs().containsKey(logName)) {
			try {
                moduleNode.updateLog(logName);
			} catch (NodeUnreachableException e) {
				logger.warn(e);
			}
		}
		return moduleNode.retrieveLog(logName);
	}
	
	public void updateLogs(NodeDescriptor node) {
	    try {
	        getModuleNode(node).updateLogs();
	    } catch (NodeUnreachableException e) {
	        logger.warn(e);
	    }
	}

	public void setChecker(MonitorNodeDescriptor node, NodeChecker checker) {
		node.setChecker(checker);
	}

	/**
	 * @param type
	 * @return the checker defined in clustering.properties for a particular node type
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	private NodeChecker getCheckerForType(String type) {
		Config config = Config.getConfig("clustering.properties");
		try {
			String clazz = config.getString("clustering.monitor.checker."+type);
			if (clazz == null) clazz = config.getString("clustering.monitor.checker");
            return (NodeChecker) ClassUtil.instance(clazz);
		}catch (Throwable t) {
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
    private PropertyFormatter getFormatterForType(String type) {
        PropertyFormatter formatter = formatters.get(type);
        if (formatter == null) {
            Config config = Config.getConfig("clustering.properties");
            try {
                String clazz = config.getString("clustering.monitor.formatter."+type);
                if (clazz == null) {
                    clazz = config.getString("clustering.monitor.formatter");
                }
                formatter = (PropertyFormatter) ClassUtil.instance(clazz);
                formatters.put(type, formatter);
            }catch (Throwable t) {
                logger.error(t);
                return new DefaultPropertyFormatter();
            }
        }
        return formatter;
    }
	
	/**
	 * adds a monitoreable implementation to a clusterable server
	 * @param clusterableServer
	 * @param m
	 */
	public static void addMonitorListener(ClusterableListener clusterableServer, Monitoreable m) {
		clusterableServer.addModuleListener(MODULE_CONTEXT, XmlrpcSerialization.handler(m));
	}
	/**
	 * @param client
	 * @return a proxy for monitoreable xmlrpc calls
	 */
	public static Monitoreable getMonitoreableProxy(XmlrpcClient client) {
		return (Monitoreable)XmlrpcClient.proxy(MODULE_CONTEXT, Monitoreable.class, client);
	}
	
	//************ WEB MODULE **************
	public String getModuleHTML() {
		return null;
	}
	public String getNodeHTML(NodeDescriptor node, int nodeNum) {
        MonitorNodeDescriptor monitorNode = (MonitorNodeDescriptor)getModuleNode(node);

        Sanity sanity = node.isReachable() ? Sanity.UNKNOWN : Sanity.UNREACHABLE;
    
        if (monitorNode != null) {
            NodeState state = monitorNode.getLastState();
            if (state != null) sanity = state.getSanity().getSanity();
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
	
	/**
	 * formats properties so that they can be displayed in HTML 
	 * @param name
	 * @param value
	 * @return
	 */
	public String format(String nodeType, String name, Object value) {
	    return getFormatterForType(nodeType).format(name, value); 
	}

    public List<Pair<String, String>> getSelectedNodesActions() {
        return new ArrayList<Pair<String,String>>();
    }

    public String selectedNodesAction(String action, List<NodeDescriptor> nodes, HttpServletRequest request) {
        return null;
    }
}
