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
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.flaptor.clusterfest.ClusterManager;
import com.flaptor.clusterfest.ModuleNodeDescriptor;
import com.flaptor.clusterfest.NodeDescriptor;
import com.flaptor.clusterfest.exceptions.NodeException;
import com.flaptor.clusterfest.exceptions.NodeUnreachableException;
import com.flaptor.clusterfest.monitoring.node.Monitoreable;
import com.flaptor.util.Config;
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
	
	private List<NodeState> states;
	private Map<String, Pair<String, Long>> logs;
    private NodeChecker checker;
    private Monitoreable monitoreable;
    private int logSize;

    private static Object stateIdLock = new Object();
    private static long maxStateId = 0;

    FileSerializer stateFileSerializer;
    
    @SuppressWarnings("unchecked")
    public MonitorNodeDescriptor(NodeDescriptor node, File statesDir, List<String> logNames) {
    	super(node);
    	
        stateFileSerializer = new FileSerializer(new File(statesDir, node.getHost()+"."+node.getPort()+".states"));
        
        if (Config.getConfig("clustering.properties").getBoolean("clustering.monitor.states.saving.enable")) {
            states = (List<NodeState>)stateFileSerializer.deserialize(true);
        }
        if (states == null) states = new ArrayList<NodeState>();
        for (NodeState state : states) {
            synchronized (stateIdLock) {
                if (state.getStateId() > maxStateId) maxStateId = state.getStateId();  
            }
        }

        
        logs = new HashMap<String, Pair<String,Long>>();
        monitoreable = MonitorModule.getModuleListener(node.getXmlrpcClient());

        logSize = Config.getConfig("clustering.properties").getInt("clustering.monitor.logs.size");
        addLogs(logNames);
    }

    public List<NodeState> getStates() {
        synchronized (states) {
            return Collections.unmodifiableList(states);
        }
    }

    public NodeState getLastState() {
        synchronized (states) {
            return states.size() > 0 ? states.get(states.size()-1) : null;
        }
    }
    public NodeState getNodeState(long stateId) {
        for (NodeState state : states) {
            if (state.getStateId() == stateId) return state;
        }
        return null;
    }
    
    public void addLogs(List<String> logNames) {
        for (String logname: logNames) {
            if (!logs.containsKey(logname)) logs.put(logname, null);
        }
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
            String log = monitoreable.getLog(logName, logSize);
            getNodeDescriptor().setReachable(true);
            logs.put(logName, new Pair<String, Long>(log, System.currentTimeMillis()));
        } catch (Throwable t) {
            getNodeDescriptor().checkAndThrow(t, logger);
        }
    }
    
    @SuppressWarnings("unchecked")
    public void updateState() throws NodeException {
		
    	NodeState state = null; 
		try {
		    Map<String, Object> properties = retrieveProperties();
		    SystemProperties systemProperties = retrieveSystemProperties(); 
			
            state = new NodeState(properties, systemProperties);
            state.updateSanity(checker, this);

            updateLogs();
		} catch (NodeUnreachableException e) {
            state = NodeState.createUnreachableState();
            throw e;
        } catch (NodeException e) {
            logger.error("remote code exception", e.getCause());
            state = NodeState.createNodeErrorState(e.getCause());
            throw e;
		} catch (Throwable t) {
		    logger.error("unexpected throwable",t);
		    state = NodeState.createClusterManagerErrorState(t);
        } finally {
			synchronized (states) {
			    synchronized (stateIdLock) {
			        maxStateId++;
			        state.setStateId(maxStateId);
			    }
			    states.add(state);
		         
		        cleanupStateList();

		        if (Config.getConfig("clustering.properties").getBoolean("clustering.monitor.states.saving.enable")) {
			        ClusterManager.getMultiExecutor().addExecution(
			            new Execution(new Callable(){
	                        public Object call() throws Exception {
                                synchronized (states) {
                                    stateFileSerializer.serialize(states, true);
                                    return null;
                                }
                            }
			            })
			        );
		        }
			}
		}
    }
    
    private void cleanupStateList() {
        synchronized (states) {
            
            
            long now = System.currentTimeMillis();
            List<NodeState> statesToRemove = new ArrayList<NodeState>();
            NodeState lastState = null;
            
            for (NodeState state: states) {
                //keep all states of the last hour
                //keep the first & last state
                //keep states with different sanity
                if (    lastState == null || 
                        now - state.getTimestamp() < DateUtil.MILLIS_IN_HOUR || 
                        states.indexOf(state) == states.size() -1 || 
                        state.getSanity().getSanity() != lastState.getSanity().getSanity()) {
                    lastState = state;
                    continue;
                }
                
                if (now - state.getTimestamp() >  DateUtil.MILLIS_IN_DAY) {
                    if (state.getTimestamp() - lastState.getTimestamp() < DateUtil.MILLIS_IN_DAY) {
                        statesToRemove.add(state);
                        continue;
                    }
                } else {
                    if (state.getTimestamp() - lastState.getTimestamp() < DateUtil.MILLIS_IN_HOUR) {
                        statesToRemove.add(state);
                        continue;
                    }
                }
                lastState = state;
            }
            states.removeAll(statesToRemove);

            //finally trim the list to keep it the max count
            int MAX_SIZE = Config.getConfig("clustering.properties").getInt("clustering.monitor.states.maxcount");
            int size = states.size();
            if (size > MAX_SIZE) {
                List<NodeState> tempList = new ArrayList<NodeState>(states.subList(size - MAX_SIZE, size));
                states.clear();
                states.addAll(tempList);
            }
        }
    }

    private Map<String, Object> retrieveProperties() throws NodeException {
    	try {
            Map<String, Object> properties = monitoreable.getProperties();
            getNodeDescriptor().setReachable(true);
            return properties;
        } catch (Throwable t) {
            getNodeDescriptor().checkAndThrow(t, logger);
            return null; //never called
        }
    }
    
    private SystemProperties retrieveSystemProperties() throws NodeException {
        try {
            SystemProperties systemProperties = monitoreable.getSystemProperties();
            getNodeDescriptor().setReachable(true);
            return systemProperties;
        } catch (Throwable t) {
            getNodeDescriptor().checkAndThrow(t, logger);
            return null; //never called
        }
    }
}