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

    private DeployListener deployListener;
    
    public DeployNodeDescriptor(NodeDescriptor nodeDescriptor) {
        super(nodeDescriptor);
        deployListener = DeployModule.getModuleListener(nodeDescriptor.getXmlrpcClient());
    }
    
    public void deployFile(String path, String filename, byte[] content) throws Exception {
        deployListener.deployFile(path, filename, content);
    }
}
