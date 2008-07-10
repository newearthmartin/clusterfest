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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.flaptor.clusterfest.ClusterManager;
import com.flaptor.clusterfest.ModuleNodeDescriptor;
import com.flaptor.clusterfest.NodeDescriptor;
import com.flaptor.clusterfest.exceptions.NodeException;
import com.flaptor.clusterfest.exceptions.NodeUnreachableException;
import com.flaptor.clusterfest.monitoring.node.Monitoreable;
import com.flaptor.util.DateUtil;
import com.flaptor.util.Execution;
import com.flaptor.util.FileSerializer;
import com.flaptor.util.Pair;

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
	
    FileSerializer stateFileSerializer;

    @SuppressWarnings("unchecked")
    public MonitorNodeDescriptor(NodeDescriptor node, File statesDir, List<String> logNames) {
    	super(node);
    	
        stateFileSerializer = new FileSerializer(new File(statesDir, node.getHost()+"."+node.getPort()+".states"));

        states = (LinkedList<NodeState>)stateFileSerializer.deserialize();
        if (states == null) states = new LinkedList<NodeState>();
        
        this.logs = new HashMap<String, Pair<String,Long>>();
        this.monitoreable = MonitorModule.getModuleListener(node.getXmlrpcClient());

        for (String log : logNames) {
            this.logs.put(log, null);
        }
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
     * @throws NodeException
     */
    public void updateLogs() throws NodeException {
		for (String logName: logs.keySet()) {
			updateLog(logName);
		}
    }
    
    /**
     * updates the version of the log
     * 
     * @param logName
     * @throws NodeException
     */
    public void updateLog(String logName) throws NodeException {
    	try {
            String log = monitoreable.getLog(logName, 512*1024); //retrieve only 500k
            getNodeDescriptor().setReachable(true);
            logs.put(logName, new Pair<String, Long>(log, System.currentTimeMillis()));
        } catch (Throwable t) {
            getNodeDescriptor().checkAndThrow(t);
        }
    }
    
    @SuppressWarnings("unchecked")
    public void updateState() throws NodeException {
		
    	NodeState state = null; 
		try {
			state = retrieveCurrentState();
	        state.updateSanity(checker, this);
	        updateLogs();
		} catch (NodeUnreachableException e) {
            state = NodeState.createUnreachableState();
            throw e;
        } catch (NodeException e) {
            logger.error("remote code exception", e.getCause());
            state = NodeState.createErrorState(e.getCause());
            throw e;
		} finally {
			synchronized (states) {
		        states.add(state);
		         
		        cleanupStateList();
		        ClusterManager.getMultiExecutor().addExecution(
		            new Execution(new Callable(){
                        public Object call() throws Exception {
                                synchronized (states) {
                                    stateFileSerializer.serialize(states);
                                    return null;
                                }
                            }
		            })
		        );
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

    private NodeState retrieveCurrentState() throws NodeException {
        return new NodeState(retrieveProperties(), retrieveSystemProperties());
    }
    
    private Map<String, Object> retrieveProperties() throws NodeException {
    	try {
            Map<String, Object> properties = monitoreable.getProperties();
            getNodeDescriptor().setReachable(true);
            return properties;
        } catch (Throwable t) {
            getNodeDescriptor().checkAndThrow(t);
            return null; //never called
        }
    }
    
    private SystemProperties retrieveSystemProperties() throws NodeException {
        try {
            SystemProperties systemProperties = monitoreable.getSystemProperties();
            getNodeDescriptor().setReachable(true);
            return systemProperties;
        } catch (Throwable t) {
            getNodeDescriptor().checkAndThrow(t);
            return null; //never called
        }
    }
}