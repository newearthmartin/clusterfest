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

package com.flaptor.clusterfest.monitoring.node;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.flaptor.clusterfest.monitoring.SystemProperties;
import com.flaptor.util.CommandUtil;
import com.flaptor.util.Execute;
import com.flaptor.util.FileUtil;
import com.flaptor.util.IOUtil;
import com.flaptor.util.ThreadUtil;

/**
 * Abstract implementation for the monitoreable interface
 * that has everything except the exported variables 
 *
 * @author Martin Massera
 */
public class MonitoreableImplementation implements Monitoreable {
    private static Logger logger = Logger.getLogger(Execute.whoAmI());

	protected Map<String, Object> properties = new HashMap<String, Object>();
	protected SystemProperties systemProperties;

	public boolean ping() {
		return true;
	}
	
	public Map<String, Object> getProperties() {
		updateProperties();
		return properties;
	}

	public Object getProperty(String property) {
		return properties.get(property);
	}

	public void setProperty(String property, Object value) {
		properties.put(property, value);
	}

	public SystemProperties getSystemProperties() {
		updateSystemProperties();
		return systemProperties;
	}

	/**
	 * gets the contents of a log
	 * @param logName can be "out", "err" or the full path of a log
	 * @return the contents of the log 
	 */
	public String getLog(String logName, int maxChars) {
		File logFile = null;
		String errorMessage = "log " + logName + " not found"; 

		try {
			logFile = FileUtil.getExistingFile(logName, true, false, false);
			if (maxChars > 0){
			    return new String(IOUtil.tail(logFile,maxChars)); //read max 500k
			} else {
			    return IOUtil.readAll(new FileReader(logFile));
			}
		} catch (IOException e) {
			logger.warn(errorMessage, e);
			return errorMessage + " - " + e;
		}
	}

	public void updateSystemProperties() {
		systemProperties = new SystemProperties();
		String dump;
		try {
			dump = CommandUtil.execute("top -b -n 1", null).second();
		} catch (IOException e) {
			dump = "problem getting top dump: " + e;
		}
		systemProperties.setTopDump(dump);
		
		try {
			dump = CommandUtil.execute("/sbin/ifconfig", null).second();
		} catch (IOException e) {
			dump = "problem getting ifconfig dump: " + e;
		}
		systemProperties.setIfconfigDump(dump);
	}
	
	/**
	 * Update properties before returning them
	 */
	public void updateProperties() {
		setProperty("threadNames", ThreadUtil.getThreadNames());
		long mem = Runtime.getRuntime().freeMemory();
		long total = Runtime.getRuntime().totalMemory();
		setProperty("freeMemory", mem);
		setProperty("usedMemory", total - mem);
		setProperty("totalMemory", total);
	}

	/**
	 * Update a property before returning them
	 */
	public void updateProperty(String property) {
	    //TODO update only property
		updateProperties();
	}
}
