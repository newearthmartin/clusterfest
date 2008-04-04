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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.flaptor.clusterfest.ModuleNodeDescriptor;
import com.flaptor.clusterfest.NodeDescriptor;
import com.flaptor.clusterfest.NodeUnreachableException;
import com.flaptor.clusterfest.monitoring.node.Monitoreable;
import com.flaptor.util.DateUtil;
import com.flaptor.util.Pair;
import com.flaptor.util.remote.RpcConnectionException;

/**
 * represents a monitoring module node
 *
 * @author Martin Massera
 */
public class MonitorNodeDescriptor extends ModuleNodeDescriptor {

	private static final Logger logger = Logger.getLogger(com.flaptor.util.Execute.whoAmI());
	
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
            throw new NodeUnreachableException(e, getNodeDescriptor());
        }
    }
    
    public void updateState() throws NodeUnreachableException {
		
    	NodeState state = null; 
		try {
			state = retrieveCurrentState();
	        state.updateSanity(checker);
	        updateLogs();
		} catch (NodeUnreachableException e) {
			if (e.getCause() == null || !(e.getCause() instanceof RpcConnectionException)) {
				logger.error("node unreachable but not because of RpcConnectionException, cause: "+ e.getMessage(), e);
				System.out.println("node unreachable but not because of RpcConnectionException, cause: "+ e.getMessage());
			}
		    state = NodeState.createUnreachableState(this);
		    throw e;
		} finally {
			synchronized (states) {
		        states.add(state);
		        cleanupStateList();
			}
		}
    }
    
    private void cleanupStateList() {
        long now = System.currentTimeMillis();
        List<NodeState> statesToRemove = new ArrayList<NodeState>();
        NodeState lastState = null;
        for (NodeState state: states) {
            if (lastState == null || now - state.getTimestamp() < DateUtil.MILLIS_IN_HOUR || states.indexOf(state) == states.size() -1) {
                lastState = state;
                continue;
            }
            
            if (state.getSanity().getSanity() == lastState.getSanity().getSanity()) {
                if (now - state.getTimestamp() >  7 * DateUtil.MILLIS_IN_DAY) {
                    if (state.getTimestamp() - lastState.getTimestamp() < DateUtil.MILLIS_IN_DAY) {
                        statesToRemove.add(state);
                        continue;
                    }
                } else if (now - state.getTimestamp() >  DateUtil.MILLIS_IN_DAY) {
                    if (state.getTimestamp() - lastState.getTimestamp() < DateUtil.MILLIS_IN_HOUR) {
                        statesToRemove.add(state);
                        continue;
                    }
                } else if (now - state.getTimestamp() >  DateUtil.MILLIS_IN_HOUR * 12) {
                    if (state.getTimestamp() - lastState.getTimestamp() < 30 * DateUtil.MILLIS_IN_MINUTE) {
                        statesToRemove.add(state);
                        continue;
                    }
                } else {
                    if (state.getTimestamp() - lastState.getTimestamp() < 10 * DateUtil.MILLIS_IN_MINUTE) {
                        statesToRemove.add(state);
                        continue;
                    }
                }
            }
            lastState = state;
        }
        
        states.removeAll(statesToRemove);
        
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
            throw new NodeUnreachableException(t, getNodeDescriptor());
        }
    }
    
    private SystemProperties retrieveSystemProperties() throws NodeUnreachableException {
        try {
            SystemProperties systemProperties = monitoreable.getSystemProperties();
            getNodeDescriptor().setReachable(true);
            return systemProperties;
        } catch (Throwable t) {
            throw new NodeUnreachableException(t, getNodeDescriptor());
        }
    }
}