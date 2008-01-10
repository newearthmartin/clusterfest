package com.flaptor.clustering.monitoring.nodes;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.flaptor.clustering.monitoring.SystemProperties;
import com.flaptor.util.CommandUtil;
import com.flaptor.util.Execute;
import com.flaptor.util.FileUtil;
import com.flaptor.util.IOUtil;

/**
 * RmiServer that exports the RmiMonitoredNode interface. 
 */
abstract public class AbstractMonitoreable implements Monitoreable {
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
	public String getLog(String logName) {
		File logFile = null;
		String errorMessage = "log " + logName + " not found"; 

		try {
			if ("out".equals(logName) || "err".equals(logName)) {
				for (File file : FileUtil.getExistingFile("logs", true, false, true).listFiles()) {
					if (file.getName().contains(logName)) logFile = file;  
				}
				if (logFile == null) {
					logger.warn(errorMessage);
					return errorMessage;
				}	
			} else {
				logFile = FileUtil.getExistingFile(logName, true, false, false);
			}
			return IOUtil.readAll(new FileReader(logFile));
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
			dump = CommandUtil.execute("ifconfig", null).second();
		} catch (IOException e) {
			dump = "problem getting ifconfig dump: " + e;
		}
		systemProperties.setIfconfigDump(dump);
	}
	
	/**
	 * Update properties before returning them
	 */
	abstract public void updateProperties();

	/**
	 * Update a properti before returning them
	 */
	abstract public void updateProperty(String property);
	
}
