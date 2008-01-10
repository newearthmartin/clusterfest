package com.flaptor.clustering.monitoring.monitor;

import com.flaptor.clustering.monitoring.monitor.NodeState.Sanity;

public interface NodeChecker {

    /**
     * Checks a node for a given state. It should return a sanity
     * value to be set for that state.
     * 
     * @param node the node to check
     * @param state the state in which the node should be checked
     * @return the sanity value
     */
    public Sanity checkNode(MonitorNode node, NodeState state);
}
