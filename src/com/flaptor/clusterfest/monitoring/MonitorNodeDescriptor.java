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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.flaptor.clusterfest.ModuleNodeDescriptor;
import com.flaptor.clusterfest.NodeDescriptor;
import com.flaptor.clusterfest.NodeUnreachableException;
import com.flaptor.clusterfest.monitoring.node.Monitoreable;
import com.flaptor.util.DateUtil;
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
		
    	NodeState state = null; 
		try {
			state = retrieveCurrentState();
	        state.updateSanity(checker);
	        updateLogs();
		} catch (NodeUnreachableException e) {
		    state = NodeState.createUnreachableState(this);
		    throw e;
		} finally {
	        states.add(state);
	        cleanupStateList();
		}
    }
    
    private void cleanupStateList() {
        long now = System.currentTimeMillis();
        NodeState lastState = null;
        for (NodeState state: states) {
            if (lastState == null || 
                now - state.getTimestamp() < DateUtil.MILLIS_IN_HOUR ||
                states.indexOf(state) == states.size() -1) {
                lastState = state;
                continue;
            }
            
            
            if (state.getSanity().getSanity() == lastState.getSanity().getSanity()) {
                if (now - state.getTimestamp() >  7 * DateUtil.MILLIS_IN_DAY) {
                    if (state.getTimestamp() - lastState.getTimestamp() < DateUtil.MILLIS_IN_DAY) {
                        states.remove(state);
                        continue;
                    }
                } else if (now - state.getTimestamp() >  DateUtil.MILLIS_IN_DAY) {
                    if (state.getTimestamp() - lastState.getTimestamp() < DateUtil.MILLIS_IN_HOUR) {
                        states.remove(state);
                        continue;
                    }
                } else if (now - state.getTimestamp() >  DateUtil.MILLIS_IN_HOUR * 12) {
                    if (state.getTimestamp() - lastState.getTimestamp() < 30 * DateUtil.MILLIS_IN_MINUTE) {
                        states.remove(state);
                        continue;
                    }
                } else {
                    if (state.getTimestamp() - lastState.getTimestamp() < 10 * DateUtil.MILLIS_IN_MINUTE) {
                        states.remove(state);
                        continue;
                    }
                }
            }
            lastState = state;
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