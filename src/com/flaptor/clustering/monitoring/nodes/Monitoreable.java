package com.flaptor.clustering.monitoring.nodes;

import java.util.Map;

import com.flaptor.clustering.monitoring.SystemProperties;

/**
 * interface for monitoring nodes
 */
public interface Monitoreable {
	
	/**
	 * pinging method for determining if the node is monitoreable
	 * @return true
	 */
	public boolean ping() throws Exception;
	
	/**
	 * @return a map of property -> value
	 * 
	 */
	public Map<String, Object> getProperties()  throws Exception;
	
	/**
	 * @param property the property to get a value for
	 * @return the value (can be any type of object)
	 */
	public Object getProperty(String property) throws Exception;
	
	/**
	 * @return the system properties
	 */
	public SystemProperties getSystemProperties() throws Exception;
	
	/**
	 * gets a log from the node
	 * @param logName the name of the log to be retrieved
	 * @return the log
	 */
	public String getLog(String logName) throws Exception;
}
