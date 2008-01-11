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

package com.flaptor.clustering.controlling.nodes;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import com.flaptor.clustering.controlling.controller.ControllerNodeState;
import com.flaptor.util.Execute;

/**
 * RmiServer that exports the RmiMonitoredNode interface. 
 */
public class ControllableImplementation implements Controllable {
    private static Logger logger = Logger.getLogger(Execute.whoAmI());

    protected ControllerNodeState state = ControllerNodeState.RUNNING;
    
	//@Override
	public ControllerNodeState getState() {
		return state;
	}

	public void stop() {
		logger.error("exiting from Clustering controlling request");
		state = ControllerNodeState.STOPPED;
		System.exit(0);
	}

	public void pause() throws RemoteException {
//		state = NodeState.PAUSED;
		throw new RemoteException("pause not yet implemented");
	}

	public void resume() throws RemoteException {
//		state = NodeState.RUNNING;
		throw new RemoteException("resume not yet implemented");
	}

	public boolean ping() throws Exception {
		return true;
	}
}
