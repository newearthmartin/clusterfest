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

import org.apache.log4j.Logger;

import com.flaptor.clusterfest.ModuleNodeDescriptor;
import com.flaptor.clusterfest.NodeDescriptor;
import com.flaptor.clusterfest.controlling.node.Controllable;
import com.flaptor.util.CommandUtil;
import com.flaptor.util.Triad;

/**
 * represents a controller module node
 * to be used in Clusterfest server side
 *
 * @author Martin Massera
 */
public class ControllerNodeDescriptor extends ModuleNodeDescriptor {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(com.flaptor.util.Execute.whoAmI());

	private ControllerNodeState state;
	private Controllable controllable;
	
    public ControllerNodeDescriptor(NodeDescriptor node) {
    	super(node);
    	controllable = ControllerModule.getControllableProxy(node.getXmlrpcClient());
    }

	public void updateState() {
		try{
			state = controllable.getState();
		} catch (Exception e) {
			//TODO if the node doesnt respond, set it as STOPPED
    		state = ControllerNodeState.STOPPED;
    		getNodeDescriptor().setReachable(false);
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
			Triad<Integer, String, String> rv = CommandUtil.remoteSSHCommand(getNodeDescriptor().getHost(), -1, "cd "+getNodeDescriptor().getInstallDir() + "\n./start.sh", 5000);
			if (rv.first() == 0) return;
			else throw new IOException("could not start remote host. error code: " + rv.first() + " - " + rv.second() + " - " + rv.third()); 
		}
	}
	
	/**
	 * execute stop.sh through ssh
	 */
	public void kill() throws IOException {
		updateState();
		Triad<Integer, String, String> rv = CommandUtil.remoteSSHCommand(getNodeDescriptor().getHost(), -1, "cd "+getNodeDescriptor().getInstallDir() + "\n./stop.sh", 5000);
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
			    getNodeDescriptor().setReachable(false);
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
			    getNodeDescriptor().setReachable(false);
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
			    getNodeDescriptor().setReachable(false);
	        }
		}
		throw new UnsupportedOperationException("not yet implemented");
	}
}
