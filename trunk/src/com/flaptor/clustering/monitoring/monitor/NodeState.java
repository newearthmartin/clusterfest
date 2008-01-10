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