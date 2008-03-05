package com.flaptor.clustering;


public interface Module {

    public void notifyNode(NodeDescriptor nodeDescriptor) throws NodeUnreachableException;

    public void nodeUnregistered(NodeDescriptor node);
    
}
