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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
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
import com.flaptor.util.IOUtil;
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
    public List<Pair<NodeDescriptor,Throwable>> deployFiles(List<NodeDescriptor> nodes, final String path, final String filename, final byte[] content) {
        List<Pair<NodeDescriptor, Throwable>> errors = new ArrayList<Pair<NodeDescriptor, Throwable>>();
        Execution<Void> e = new Execution();

        for (NodeDescriptor node : nodes) {
            final DeployNodeDescriptor dnode= getModuleNode(node);
            if (dnode != null) {
                e.addTask(new CallableWithId<Void, NodeDescriptor>(node) {
                    public Void call() throws Exception {
                        dnode.deployFile(path, filename, content);
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
     * @param nodeListener
     * @param m
     */
    public static void addModuleListener(NodeListener nodeListener, DeployListener deployListener) {
        nodeListener.addModuleListener(MODULE_CONTEXT, XmlrpcSerialization.handler(deployListener));
    }
    /**
     * @param client
     * @return a proxy for action xmlrpc calls
     */
    public static DeployListener getModuleListener(XmlrpcClient client) {
        return (DeployListener)XmlrpcClient.proxy(MODULE_CONTEXT, DeployListener.class, client);    
    }

    public ActionReturn action(String action, HttpServletRequest request) {
        return null;
    }
    public String doPage(String page, HttpServletRequest request, HttpServletResponse response) {
        List<NodeDescriptor> nodes = new ArrayList<NodeDescriptor>();
        String [] nodesParam =request.getParameterValues("node");
        if (nodesParam != null) {
            for (String idx:nodesParam){
                nodes.add(ClusterManager.getInstance().getNodes().get(Integer.parseInt(idx)));
            }
        }
        request.setAttribute("nodes", nodes);

        if (ServletFileUpload.isMultipartContent(request)) {
            // Create a factory for disk-based file items
            FileItemFactory factory = new DiskFileItemFactory();

            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);
            
            String name = null;
            byte[] content = null; 
            String path = null;
            // Parse the request
            try {
                List<FileItem> items = upload.parseRequest(request);
                String message = "";
                for (FileItem item : items ) {
                    String fieldName = item.getFieldName();
                    if (fieldName.equals("node")) {
                        NodeDescriptor node = ClusterManager.getInstance().getNodes().get(Integer.parseInt(item.getString()));
                        if (!node.isReachable()) message += node + " is unreachable<br/>";
                        if (getModuleNode(node) != null) nodes.add(node);
                        else message += node + " is not registered as deployable<br/>";
                    }
                    if (fieldName.equals("path")) path = item.getString();

                    if (fieldName.equals("file")) {
                        name = item.getName();
                        content = IOUtil.readAllBinary(item.getInputStream());
                    }
                }
                List<Pair<NodeDescriptor,Throwable>> errors = deployFiles(nodes, path, name, content);
                if (errors != null && errors.size() > 0) {
                    request.setAttribute("deployCorrect", false);
                    request.setAttribute("deployErrors", errors);
                } else request.setAttribute("deployCorrect", true);
                request.setAttribute("message", message);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return "deploy.vm";
    }
    public List<String> getActions() {
        return new ArrayList<String>();
    }
    public String getNodeHTML(NodeDescriptor node, int nodeNum) {
        return null;
    }
    public List<String> getPages() {
        return Arrays.asList(new String[]{"deploy"});
    }
    public List<Pair<String, String>> getSelectedNodesActions() {
        List<Pair<String, String>> ret = new ArrayList<Pair<String,String>>();
        ret.add(new Pair<String, String>("deploy", "deploy files"));
        return ret;
    }
    public ActionReturn selectedNodesAction(String action, List<NodeDescriptor> nodes, HttpServletRequest request) {
        if (action.equals("deploy")) {
            return ActionReturn.redirectToPage("deploy");
        } else {
            return null;
        }
    }
    public String getModuleHTML() {
        return null;
    }
    public void setup(WebServer server) {}
}