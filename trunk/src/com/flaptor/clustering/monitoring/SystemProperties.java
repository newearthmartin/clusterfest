package com.flaptor.clustering.monitoring;

import java.io.Serializable;

/**
 * Represents the system properties 
 */
public class SystemProperties implements Serializable{

	private String topDump;
	private String ifconfigDump;

	public String getTopDump() {
		return topDump;
	}

	public void setTopDump(String topDump) {
		this.topDump = topDump;
	}

	public String getIfconfigDump() {
		return ifconfigDump;
	}

	public void setIfconfigDump(String ifconfigDump) {
		this.ifconfigDump = ifconfigDump;
	}
}
