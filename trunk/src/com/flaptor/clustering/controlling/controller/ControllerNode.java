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

	public void start() throws IOException {
		updateState();
		if (state == ControllerNodeState.STOPPED) {
			Triad<Integer, String, String> rv = CommandUtil.remoteSSHCommand(node.getHost(), -1, "cd "+node.getInstallDir() + "\n./start.sh", 5000);
			if (rv.first() == 0) return;
			else throw new IOException("could not start remote host. error code: " + rv.first() + " - " + rv.second() + " - " + rv.third()); 
		}
	}
	public void kill() throws IOException {
		updateState();
		Triad<Integer, String, String> rv = CommandUtil.remoteSSHCommand(node.getHost(), -1, "cd "+node.getInstallDir() + "\n./stop.sh", 5000);
		if (rv.first() == 0) return;
		else throw new IOException("could not kill remote host. error code: " + rv.first() + " - " + rv.second() + " - " + rv.third()); 
	}

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
