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

package com.flaptor.clusterfest.controlling;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.flaptor.clusterfest.ClusterManager;
import com.flaptor.clusterfest.ModuleNodeDescriptor;
import com.flaptor.clusterfest.ModuleUtil;
import com.flaptor.clusterfest.NodeDescriptor;
import com.flaptor.clusterfest.monitoring.NodeState;
import com.flaptor.util.CallableWithId;
import com.flaptor.util.Execution;
import com.flaptor.util.Pair;
import com.flaptor.util.ThreadUtil;

/**
 * util for control ui frontend
 *
 * @author Martin Massera
 */
public class ControllingFrontend {
    private static final Logger logger = Logger.getLogger(com.flaptor.util.Execute.whoAmI());
 
    private static void startOrKill(ClusterManager cluster, NodeDescriptor node, boolean start) throws IllegalStateException, IOException {
        ControllerModule control = (ControllerModule)cluster.getModule("controller");

        if (!control.isRegistered(node)) {
            logger.error("node not registered in controller framework");
            throw new IllegalStateException("node not registered in controller framework");
        }

        ControllerNodeState targetState = start ? ControllerNodeState.RUNNING : ControllerNodeState.STOPPED;
        if (start) control.startNode(node);
        else control.killNode(node);
        
        long ms = System.currentTimeMillis();
        while(true) {
            if (System.currentTimeMillis() - ms > 10000) break;
            if (control.getState(node) == targetState) {
                break;
            }
            ThreadUtil.sleep(500);
        }

        if (start) cluster.updateAllInfo(node); //if stop, hangs everything
    }
    
	/**
	 * start a node through ssh
	 * @param cluster
	 * @param node
	 * @return the message (ok or error explanation)
	 * @throws Exception 
	 */
	public static String startNode(ClusterManager cluster, NodeDescriptor node) {
        try {
            startOrKill(cluster, node, true);
        } catch (IllegalStateException e1) {
            return e1.getMessage();
        } catch (IOException e1) {
            return "Problem starting node: " + e1.getMessage();
        }
        return "starting node...";
	}

	/**
	 * stop a node through ssh
	 * @param cluster
	 * @param node
	 * @return the message (ok or error explanation)
	 */
	public static String killNode(ClusterManager cluster, NodeDescriptor node) {
        try {
            startOrKill(cluster, node, false);
        } catch (IllegalStateException e1) {
            return e1.getMessage();
        } catch (IOException e1) {
            return "Problem killing node: " + e1.getMessage();
        }
        return "killing node...";
	}

    @SuppressWarnings("unchecked")
    public static String killAll(final ClusterManager cluster, List<NodeDescriptor> nodes) {
        ControllerModule control = (ControllerModule)cluster.getModule("controller");
        Execution<Void> e = new Execution<Void>();
        for (final NodeDescriptor node: nodes) {
            final ControllerNodeDescriptor cnode = control.getModuleNode(node);
            if (cnode!= null) {
                e.addTask(new CallableWithId<Void, NodeDescriptor>(node) {
                    public Void call() throws Exception {
                        startOrKill(cluster, node, false);
                        return null;
                    }
                });
            }
        }
        ClusterManager.getMultiExecutor().addExecution(e);
        e.waitFor();
        List<Pair<Callable<Void>,Throwable>> problems = e.getProblems();
        if (problems.size() > 0) {
            return "problems while killing nodes:<br/>" + ModuleUtil.problemListToHTML(problems);
        } else {
            return "nodes killed";    
        }
    }
    
	public static String killAll(ClusterManager cluster) {
        ControllerModule control = (ControllerModule)cluster.getModule("controller");
        return killAll(cluster, cluster.getNodes());
	}

	@SuppressWarnings("unchecked")
    public static String startAll(final ClusterManager cluster, List<NodeDescriptor> nodes) {
        ControllerModule control = (ControllerModule)cluster.getModule("controller");
        Execution<Void> e = new Execution<Void>();
        for (final NodeDescriptor node: nodes) {
            final ControllerNodeDescriptor cnode = control.getModuleNode(node); 
            if (cnode!= null) {
                e.addTask(new CallableWithId<Void, NodeDescriptor>(node) {
                    public Void call() throws Exception {
                        startOrKill(cluster, node, true);
                        return null;
                    }
                });
            }
        }
        ClusterManager.getMultiExecutor().addExecution(e);
        e.waitFor();
        List<Pair<Callable<Void>,Throwable>> problems = e.getProblems();
        if (problems.size() > 0) {
            return "problems while starting nodes:<br/>" + ModuleUtil.problemListToHTML(problems);
        } else {
            return "nodes started";    
        }
    }

    public static String startAll(ClusterManager cluster) {
        ControllerModule control = (ControllerModule)cluster.getModule("controller");
        return startAll(cluster, cluster.getNodes());
	}
}


