package com.flaptor.clustering.monitoring.monitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.flaptor.clustering.Node;
import com.flaptor.clustering.NodeUnreachableException;
import com.flaptor.clustering.modules.ModuleNode;
import com.flaptor.clustering.monitoring.SystemProperties;
import com.flaptor.clustering.monitoring.nodes.Monitoreable;
import com.flaptor.util.Pair;

/**
 * represents a monitoring module node
 */
public class MonitorNode implements ModuleNode {

	private Node node;
	private LinkedList<NodeState> states;
	private Map<String, Pair<String, Long>> logs;
    private NodeChecker checker;
    private Monitoreable monitoreable;
	
    public MonitorNode(Node node) {
    	this.node = node;
    	
        this.states = new LinkedList<NodeState>();
        this.logs = new HashMap<String, Pair<String,Long>>();
        this.monitoreable = Monitor.getMonitoreableProxy(node.getXmlrpcClient());
        // TODO set default checker
        
        //TODO hardcoded logs
        this.logs.put("out", null);
        this.logs.put("err", null);
    }

    public List<NodeState> getStates() {
        return Collections.unmodifiableList(states);
    }
    public NodeState getLastState() {
        return states.size() > 0 ? states.getLast() : null;
    }
    public Node getClusterNode() {
    	return node;
    }

    public Map<String, Pair<String, Long>> getLogs() {
    	return logs;
    }

	public NodeChecker getChecker() {
		return checker;
	}

	public void setChecker(NodeChecker checker) {
		this.checker = checker; 
	}

    /**
     * gets the last version of the log
     * @param logName
     * @return a pair of the content and the timestamp of the log
     */
    public Pair<String, Long> retrieveLog(String logName) {
    	return logs.get(logName);
    }
    
    /**
     * update all logs
     * @throws NodeUnreachableException
     */
    public void updateLogs() throws NodeUnreachableException {
		for (String logName: logs.keySet()) {
			updateLog(logName);
		}
    }
    
    /**
     * updates the version of the log
     * @param logName
     * @throws NodeUnreachableException
     */
    public void updateLog(String logName) throws NodeUnreachableException {
    	try {
            String log = monitoreable.getLog(logName);
            node.setReachable(true);
            logs.put(logName, new Pair<String, Long>(log, System.currentTimeMillis()));
            
        } catch (Exception e) {
        	node.setUnreachable(e);
        }
    }
    
    public void updateState() throws NodeUnreachableException {
		
    	NodeState state = NodeState.createUnreachableState(this); 
		try {
			state = retrieveCurrentState();
	        state.updateSanity(checker);

	        updateLogs();
		} finally {
	        states.add(state);
		}
    }
    
    private NodeState retrieveCurrentState() throws NodeUnreachableException {
        return new NodeState(this, retrieveProperties(), retrieveSystemProperties());
    }
    
    private Map<String, Object> retrieveProperties() throws NodeUnreachableException {
    	try {
            Map<String, Object> properties = monitoreable.getProperties();
            node.setReachable(true);
            return properties;
        } catch (Exception e) {
        	node.setUnreachable(e);
        	return null; //never called
        }
    }
    
    private SystemProperties retrieveSystemProperties() throws NodeUnreachableException {
        try {
            SystemProperties systemProperties = monitoreable.getSystemProperties();
            node.setReachable(true);
            return systemProperties;
        } catch (Exception e) {
        	node.setUnreachable(e);
        	return null; //never called
        }
    }
}