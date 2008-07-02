package com.flaptor.clusterfest.action;

import com.flaptor.clusterfest.ModuleNodeDescriptor;
import com.flaptor.clusterfest.NodeDescriptor;
import com.flaptor.clusterfest.exceptions.NodeException;

/**
 * Base class for nodes of the action module
 * @author Martin Massera
 */
public class ActionNodeDescriptor extends ModuleNodeDescriptor{

    private ActionReceiver receiver;
    
    public ActionNodeDescriptor(NodeDescriptor nodeDescriptor) {
        super(nodeDescriptor);
        receiver = ActionModule.getModuleListener(nodeDescriptor.getXmlrpcClient());
    }
    
    /**
     * sends an action to the node
     * @param action the name of the action
     * @param params the parameters (must be serializable)
     */
    public void action(String action, Object[] params) throws NodeException{
        try {
            receiver.action(action, params);
        } catch (Throwable t) {
            getNodeDescriptor().checkAndThrow(t);
        }
    }
}
