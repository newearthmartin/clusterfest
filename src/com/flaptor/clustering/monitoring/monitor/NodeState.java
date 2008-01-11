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

import java.util.Map;

import com.flaptor.clustering.monitoring.SystemProperties;

public class NodeState {

	public static enum Sanity {
		UNREACHABLE,
		UNKNOWN,
		GOOD,
		BAD
	}
	
	public static NodeState createUnreachableState(MonitorNode node) {
		NodeState state = new NodeState(node, null, null);
		state.sanity = Sanity.UNREACHABLE; 
		return state;
	}
	
    private Sanity sanity;
	private Map<String, Object> properties;
	private SystemProperties systemProperties;
	private long timestamp;
    private MonitorNode node;
	
    public NodeState(MonitorNode node, Map<String, Object> properties, SystemProperties systemProperties) {
        super();
        // should be updated later by a NodeChecker
        this.sanity = Sanity.UNKNOWN; 
        
        this.node = node;
        this.properties = properties;
        this.systemProperties = systemProperties;
        this.timestamp = System.currentTimeMillis();
    }
    
    public Sanity getSanity() {
        return sanity;
    }
    public Map<String, Object> getProperties() {
        return properties;
    }
    public SystemProperties getSystemProperties() {
        return systemProperties;
    }
    public long getTimestamp() {
        return timestamp;
    }

    public void updateSanity(NodeChecker checker) {
        if (checker != null) { 
        	this.sanity = checker.checkNode(node, this);
        } else {
        	this.sanity = Sanity.UNKNOWN;
        }
    }
}