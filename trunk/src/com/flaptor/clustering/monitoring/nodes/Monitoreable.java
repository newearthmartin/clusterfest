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

package com.flaptor.clustering.monitoring.nodes;

import java.util.Map;

import com.flaptor.clustering.monitoring.SystemProperties;

/**
 * interface for monitoring nodes
 *
 * @author martinmassera
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
