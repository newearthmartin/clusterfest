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
package com.flaptor.clusterfest.action;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.flaptor.clusterfest.AbstractModule;
import com.flaptor.clusterfest.ClusterableListener;
import com.flaptor.clusterfest.ModuleUtil;
import com.flaptor.clusterfest.NodeDescriptor;
import com.flaptor.clusterfest.NodeUnreachableException;
import com.flaptor.clusterfest.WebModule;
import com.flaptor.clusterfest.monitoring.node.Monitoreable;
import com.flaptor.util.CallableWithId;
import com.flaptor.util.Execute;
import com.flaptor.util.Execution;
import com.flaptor.util.MultiExecutor;
import com.flaptor.util.Pair;
import com.flaptor.util.Execution.Results;
import com.flaptor.util.remote.NoSuchRpcMethodException;
import com.flaptor.util.remote.WebServer;
import com.flaptor.util.remote.XmlrpcClient;
import com.flaptor.util.remote.XmlrpcSerialization;

/**
 * Clusterfest Module for sending actions to nodes
 * 
 * @author Martin Massera
 */
public class ActionModule extends AbstractModule<ActionNodeDescriptor> {
    private static final Logger logger = Logger.getLogger(com.flaptor.util.Execute.whoAmI());
    MultiExecutor<Void> multiExecutor = new MultiExecutor<Void>(5, "sendAction");
    public final static String MODULE_CONTEXT = "action";
    
	protected void notifyModuleNode(ActionNodeDescriptor node) {
    }
	
    protected ActionNodeDescriptor createModuleNode(NodeDescriptor node) {
        return new ActionNodeDescriptor(node);
    }

    protected boolean shouldRegister(NodeDescriptor node) throws NodeUnreachableException {
        return ModuleUtil.nodeBelongs(node, MODULE_CONTEXT, false);
    }
    
    /**
     * sends an action to nodes. If a node fails, an exception is returned in the exception list 
     * @param nodes
     * @param action
     * @param params
     * @return
     */
    public List<Pair<NodeDescriptor,Throwable>> sendAction(List<NodeDescriptor> nodes, final String action, final Object[] params) {
        List<Pair<NodeDescriptor,Throwable>> errors = new ArrayList<Pair<NodeDescriptor,Throwable>>();
        Execution<Void> e = new Execution<Void>();

        int actualNodes = 0;
        for (NodeDescriptor node: nodes) {
            final ActionNodeDescriptor mnode = getModuleNode(node);
            if (mnode != null) {
                e.getTaskQueue().add(new CallableWithId<Void, NodeDescriptor>(node) {
                    public Void call() throws Exception {
                        mnode.action(action, params);
                        return null;
                    }
                });
                actualNodes++;
            }
        }
        multiExecutor.addExecution(e);
        while(true) {
            synchronized(e) {
                if (e.getResultsList().size() == actualNodes) break;
            }
            Execute.sleep(100);
        }
        for (Results<Void> result : e.getResultsList()) {
            if (!result.isFinishedOk()) {
                errors.add(new Pair<NodeDescriptor, Throwable>(
                        ((CallableWithId<Void, NodeDescriptor>)result.getTask()).getId(), 
                        result.getException()));
            }
        }
        return errors;
    }

    /**
     * adds an actionReceiver implementation to a clusterable listener
     * @param clusterableListener
     * @param m
     */
    public static void setActionReceiver(ClusterableListener clusterableListener, ActionReceiver actionReceiver) {
        clusterableListener.addModuleListener(MODULE_CONTEXT, XmlrpcSerialization.handler(actionReceiver));
    }
    /**
     * @param client
     * @return a proxy for action xmlrpc calls
     */
    public static ActionReceiver getActionReceiverProxy(XmlrpcClient client) {
        return (ActionReceiver)XmlrpcClient.proxy(MODULE_CONTEXT, ActionReceiver.class, client);    
    }
}