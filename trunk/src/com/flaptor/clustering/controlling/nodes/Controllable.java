package com.flaptor.clustering.controlling.nodes;

import com.flaptor.clustering.controlling.controller.ControllerNodeState;

/**
 * interface for controlling nodes
 */
public interface Controllable {

	/**
	 * pinging method for determining if the node is controllable
	 * @return true
	 */
	public boolean ping() throws Exception;
	
	/**
	 * @return the state of the node
	 */
	public ControllerNodeState getState() throws Exception;
	
	/**
	 * pause the service
	 */
	public void pause() throws Exception;

	/**
	 * resume the service
	 */
	public void resume() throws Exception;
	
	/**
	 * stop and exit the process gently
	 */
	public void stop() throws Exception;
}
