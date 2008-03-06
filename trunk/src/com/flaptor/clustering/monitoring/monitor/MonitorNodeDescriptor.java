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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.flaptor.clustering.ModuleNodeDescriptor;
import com.flaptor.clustering.NodeDescriptor;
import com.flaptor.clustering.NodeUnreachableException;
import com.flaptor.clustering.monitoring.SystemProperties;
import com.flaptor.clustering.monitoring.nodes.Monitoreable;
import com.flaptor.util.Pair;

/**
 * represents a monitoring module node
 *
 * @author Martin Massera
 */
public class MonitorNodeDescriptor extends ModuleNodeDescriptor {

	private LinkedList<NodeState> states;
	private Map<String, Pair<String, Long>> logs;
    private NodeChecker checker;
    private Monitoreable monitoreable;
	
    public MonitorNodeDescriptor(NodeDescriptor node) {
    	super(node);
    	
        this.states = new LinkedList<NodeState>();
        this.logs = new HashMap<String, Pair<String,Long>>();
        this.monitoreable = MonitorModule.getMonitoreableProxy(node.getXmlrpcClient());
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
    public NodeState getNodeState(int stateNumber) {
        if (states.size() > stateNumber) return states.get(stateNumber);
        else return null;
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
            getNodeDescriptor().setReachable(true);
            logs.put(logName, new Pair<String, Long>(log, System.currentTimeMillis()));
            
        } catch (Exception e) {
        	getNodeDescriptor().setUnreachable(e);
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
            getNodeDescriptor().setReachable(true);
            return properties;
        } catch (Throwable t) {
        	getNodeDescriptor().setUnreachable(t);
        	return null; //never called
        }
    }
    
    private SystemProperties retrieveSystemProperties() throws NodeUnreachableException {
        try {
            SystemProperties systemProperties = monitoreable.getSystemProperties();
            getNodeDescriptor().setReachable(true);
            return systemProperties;
        } catch (Throwable t) {
            getNodeDescriptor().setUnreachable(t);
        	return null; //never called
        }
    }
}