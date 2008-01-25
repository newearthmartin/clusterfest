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

package com.flaptor.clustering.controlling.controller;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.flaptor.clustering.Cluster;
import com.flaptor.clustering.Node;
import com.flaptor.clustering.modules.ModuleNode;
import com.flaptor.util.ThreadUtil;

/**
 * util for control ui frontend
 *
 * @author Martin Massera
 */
public class ControllingFrontend {
    private static final Logger logger = Logger.getLogger(com.flaptor.util.Execute.whoAmI());

    static ExecutorService threadPool = Executors.newFixedThreadPool(50);
    
	/**
	 * start a node through ssh
	 * @param cluster
	 * @param node
	 * @return the message (ok or error explanation)
	 */
	public static String startNode(Cluster cluster, Node node) {
        Controller control = (Controller)cluster.getModule("controller");
		ControllerNode cnode = (ControllerNode)control.getNode(node);
        
		if (cnode == null) {
			logger.error("trying to start a node that is not registered in the controlling framework");
			return "trying to start a node that is not registered in the controlling framework";
		}
		
        try {
        	control.startNode(cnode);
        	long ms = System.currentTimeMillis();
        	String message = "Starting node..."; 
        	while(true) {
        		if (System.currentTimeMillis() - ms > 5000) break;
        		if (control.getState(cnode) == ControllerNodeState.RUNNING) {
        			message = "node started";
        			break;
        		}
        		ThreadUtil.sleep(500);
        	}
        	cluster.updateAllInfo(node);
        	return message;
        } catch (IOException e) {
        	return "Problem starting node: " + e.getMessage();
        }
	}

	/**
	 * stop a node through ssh
	 * @param cluster
	 * @param node
	 * @return the message (ok or error explanation)
	 */
	public static String killNode(Cluster cluster, Node node) {
        Controller control = (Controller)cluster.getModule("controller");
		ControllerNode cnode = (ControllerNode)control.getNode(node);

		if (cnode == null) {
			logger.error("trying to kill a node that is not registered in the controlling framework");
			return "trying to kill a node that is not registered in the controlling framework";
		}
        
        try {
        	control.killNode(cnode);
        	long ms = System.currentTimeMillis();
        	String message = "killing node...";
        	while(true) {
        		if (System.currentTimeMillis() - ms > 5000) break;
        		ControllerNodeState state = control.getState(cnode);
        		if (state == ControllerNodeState.STOPPED) {
        			message = "node killed";
        			node.setReachable(false);
        			break;
        		}
        		ThreadUtil.sleep(500);
        	}
//hangs everything, commented
//        	cluster.updateAllInfo(node);
        	return message;
        } catch (IOException e) {
        	return "Problem killing node: " + e.getMessage();
        }
	}

	public static String killall(Cluster cluster) {
        Controller control = (Controller)cluster.getModule("controller");
		for (final ModuleNode n: control.getNodes()) {
			threadPool.submit(new Runnable() {
				public void run() {
					try {
						((ControllerNode)n).kill();
					} catch (IOException e) {logger.error(e);}
				}
			});
		}
		return "sent kill to all nodes";
	}

	public static String startall(Cluster cluster) {
        Controller control = (Controller)cluster.getModule("controller");

		for (final ModuleNode n: control.getNodes()) {
			threadPool.submit(new Runnable() {
				public void run() {
					try {
						((ControllerNode)n).start();
					} catch (IOException e) {logger.error(e);}
				}
			});
		}
		return "sent start to all nodes";
	}
}


