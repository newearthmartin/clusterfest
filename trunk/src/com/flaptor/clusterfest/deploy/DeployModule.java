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
package com.flaptor.clusterfest.deploy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.flaptor.clusterfest.AbstractModule;
import com.flaptor.clusterfest.ClusterManager;
import com.flaptor.clusterfest.NodeListener;
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
public class DeployModule extends AbstractModule<DeployNodeDescriptor> implements WebModule {
    private static final Logger logger = Logger.getLogger(com.flaptor.util.Execute.whoAmI());
    public final static String MODULE_CONTEXT = "deploy";
    
	protected void notifyModuleNode(DeployNodeDescriptor node) {
    }
	
    protected DeployNodeDescriptor createModuleNode(NodeDescriptor node) {
        return new DeployNodeDescriptor(node);
    }

    protected boolean shouldRegister(NodeDescriptor node) throws NodeUnreachableException {
        return ModuleUtil.nodeBelongs(node, MODULE_CONTEXT, false);
    }

    @SuppressWarnings("unchecked")
    public List<Pair<NodeDescriptor,Throwable>> deployFiles(List<NodeDescriptor> nodes, final String filename, final byte[] content) {
        List<Pair<NodeDescriptor, Throwable>> errors = new ArrayList<Pair<NodeDescriptor, Throwable>>();
        Execution<Void> e = new Execution();

        for (NodeDescriptor node : nodes) {
            final DeployNodeDescriptor dnode= getModuleNode(node);
            if (dnode != null) {
                e.addTask(new CallableWithId<Void, NodeDescriptor>(node) {
                    public Void call() throws Exception {
                        dnode.deployFile(null, filename, content);
                        return null;
                    }
                });
            }
        }
        ClusterManager.getInstance().getMultiExecutor().addExecution(e);
        e.waitFor();
        for (Pair<Callable<Void>, Throwable> problem : e.getProblems()) {
            errors.add(new Pair<NodeDescriptor, Throwable>(((CallableWithId<Void, NodeDescriptor>) problem.first()).getId(), problem.last()));

        }
        return errors;
    }
    
    /**
     * adds an actionReceiver implementation to a clusterable listener
     * @param clusterableListener
     * @param m
     */
    public static void addModuleListener(NodeListener clusterableListener, DeployListener deployListener) {
        clusterableListener.addModuleListener(MODULE_CONTEXT, XmlrpcSerialization.handler(deployListener));
    }
    /**
     * @param client
     * @return a proxy for action xmlrpc calls
     */
    public static DeployListener getModuleListener(XmlrpcClient client) {
        return (DeployListener)XmlrpcClient.proxy(MODULE_CONTEXT, DeployListener.class, client);    
    }

    @Override
    public String action(String action, HttpServletRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String doPage(String page, HttpServletRequest request, HttpServletResponse response) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getActions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getModuleHTML() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getNodeHTML(NodeDescriptor node, int nodeNum) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getPages() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Pair<String, String>> getSelectedNodesActions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String selectedNodesAction(String action, List<NodeDescriptor> nodes, HttpServletRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setup(WebServer server) {
        // TODO Auto-generated method stub
        
    }
}