package com.flaptor.clusterfest;

import com.flaptor.clusterfest.exceptions.NodeException;

/**
 * interface for modules
 * 
 * @author Martin Massera
 */
public interface Module {

    /**
     * cluster periodically calls this method on all registered nodes.
     *  
     * @param nodeDescriptor
     * @throws NodeException
     */
    public void notifyNode(NodeDescriptor nodeDescriptor) throws NodeException;

    /**
     * ClusterManager will call this method to notify that a node has been unregistered
     * from the cluster 
     * 
     * @param node
     */
    public void nodeUnregistered(NodeDescriptor node);
    
}
