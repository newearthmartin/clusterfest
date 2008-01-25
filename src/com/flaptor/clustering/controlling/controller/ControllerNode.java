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

import org.apache.log4j.Logger;

import com.flaptor.clustering.Node;
import com.flaptor.clustering.controlling.nodes.Controllable;
import com.flaptor.clustering.modules.ModuleNode;
import com.flaptor.util.CommandUtil;
import com.flaptor.util.Triad;

/**
 * represents a controller module node
 * to be used inclustering server side
 *
 * @author Martin Massera
 */
public class ControllerNode implements ModuleNode{
    private static final Logger logger = Logger.getLogger(com.flaptor.util.Execute.whoAmI());

	private Node node;
	private ControllerNodeState state;
	private Controllable controllable;
	
    public ControllerNode(Node node) {
    	this.node = node;
    	controllable = Controller.getControllableProxy(node.getXmlrpcClient());
    }

	public void updateState() {
		try{
			state = controllable.getState();
		} catch (Exception e) {
			//TODO if the node doesnt respond, set it as STOPPED
    		state = ControllerNodeState.STOPPED;
    		node.setReachable(false);
        }
		//TODO if there is another error, log it
	}

	public ControllerNodeState getState() {
		updateState();
		return state;
	}

	/**
	 * execute start.sh through ssh
	 */
	public void start() throws IOException {
		updateState();
		if (state == ControllerNodeState.STOPPED) {
			Triad<Integer, String, String> rv = CommandUtil.remoteSSHCommand(node.getHost(), -1, "cd "+node.getInstallDir() + "\n./start.sh", 5000);
			if (rv.first() == 0) return;
			else throw new IOException("could not start remote host. error code: " + rv.first() + " - " + rv.second() + " - " + rv.third()); 
		}
	}
	
	/**
	 * execute stop.sh through ssh
	 */
	public void kill() throws IOException {
		updateState();
		Triad<Integer, String, String> rv = CommandUtil.remoteSSHCommand(node.getHost(), -1, "cd "+node.getInstallDir() + "\n./stop.sh", 5000);
		if (rv.first() == 0) return;
		else throw new IOException("could not kill remote host. error code: " + rv.first() + " - " + rv.second() + " - " + rv.third()); 
	}

	/**
	 * call pause through rpc
	 */
	public void pause() {
		updateState();
		if (state == ControllerNodeState.RUNNING) {
			try{
				controllable.pause();
			} catch (Exception e) {
	    		node.setReachable(false);
	        }
		}
	}

	/**
	 * call resume through rpc
	 */
	public void resume() {
		updateState();
		if (state == ControllerNodeState.PAUSED) {
			try{
				controllable.resume();
			} catch (Exception e) {
	    		node.setReachable(false);
	        }
		}
	}

	/**
	 * call stop through rpc
	 */
	public void stop() {
		updateState();
		if (state == ControllerNodeState.RUNNING || state == ControllerNodeState.PAUSED) {
			try{
				controllable.stop();
			} catch (Exception e) {
	    		node.setReachable(false);
	        }
		}
		throw new UnsupportedOperationException("not yet implemented");
	}
}
