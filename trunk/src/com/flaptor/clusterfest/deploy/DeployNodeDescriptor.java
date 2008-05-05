package com.flaptor.clusterfest.deploy;

import org.apache.log4j.Logger;

import com.flaptor.clusterfest.ModuleNodeDescriptor;
import com.flaptor.clusterfest.NodeDescriptor;
import com.flaptor.clusterfest.NodeUnreachableException;
import com.flaptor.clusterfest.monitoring.MonitorModule;

/**
 * Base class for nodes of the action module
 * @author Martin Massera
 */
public class DeployNodeDescriptor extends ModuleNodeDescriptor{
    private static final Logger logger = Logger.getLogger(com.flaptor.util.Execute.whoAmI());

    private DeployListener deployListener;
    
    public DeployNodeDescriptor(NodeDescriptor nodeDescriptor) {
        super(nodeDescriptor);
        deployListener = DeployModule.getModuleListener(nodeDescriptor.getXmlrpcClient());
    }
    
    public void deployFile(String path, String filename, byte[] content) throws Exception {
        logger.info("deploying " + path + "/" + filename + " to " + getNodeDescriptor().getHost()+":"+getNodeDescriptor().getPort()+":"+getNodeDescriptor().getInstallDir());
        deployListener.deployFile(path, filename, content);
    }
}
