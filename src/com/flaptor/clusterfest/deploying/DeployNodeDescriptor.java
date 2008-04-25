package com.flaptor.clusterfest.deploying;

import com.flaptor.clusterfest.ModuleNodeDescriptor;
import com.flaptor.clusterfest.NodeDescriptor;
import com.flaptor.clusterfest.NodeUnreachableException;
import com.flaptor.clusterfest.monitoring.MonitorModule;

/**
 * Base class for nodes of the action module
 * @author Martin Massera
 */
public class DeployNodeDescriptor extends ModuleNodeDescriptor{

    private DeployListener receiver;
    
    public DeployNodeDescriptor(NodeDescriptor nodeDescriptor) {
        super(nodeDescriptor);
        receiver = DeployModule.getModuleListener(nodeDescriptor.getXmlrpcClient());
    }
}
