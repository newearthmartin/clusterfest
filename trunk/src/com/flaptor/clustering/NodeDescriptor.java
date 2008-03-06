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

package com.flaptor.clustering;

import java.net.MalformedURLException;
import java.net.URL;

import com.flaptor.util.remote.XmlrpcClient;


/**
 * Represents a node of the cluster. Indicates the host:port of the node,
 * the type and if it's reachable. All modules of the clustering framework
 * should check and update the reachable status.
 *
 * @author Martin Massera
 */
public class NodeDescriptor {

	private String host;
	private int port;
	private String installDir;
	private String type;
	private boolean reachable = true;
	private Clusterable clusterableStub;
	
	private XmlrpcClient xmlrpcClient;
	
	/**
	 * creates a node
	 * @param host
	 * @param port
	 * @param installDir the path where the node is installed (on the target machine)
	 */
    public NodeDescriptor(String host, int port, String installDir) {
        this.host = host;
        this.port = port;
        this.installDir = installDir;
        try {
			xmlrpcClient = new XmlrpcClient(new URL("http://"+host+":"+port+"/"));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
        clusterableStub = (Clusterable)XmlrpcClient.proxy(ClusterableListener.CONTEXT, Clusterable.class, xmlrpcClient); 
    }
    public String getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }
    public String getType() {
    	return type;
    }
    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }
    public String getInstallDir() {
		return installDir;
	}
	public boolean isReachable() {
        return reachable;
    }
    public XmlrpcClient getXmlrpcClient() {
		return xmlrpcClient;
	}

    /**
     * updates the info of the node
     */
    void updateInfo() throws NodeUnreachableException {
        try {
            type = clusterableStub.getNodeType();
            reachable = true;
        } catch (Exception e) {
        	setUnreachable(e);
        }
    }

    /**
     * marks the node as unreachable and throws a NodeUnreachableException
     * @throws NodeUnreachableException always!!!
     */
    public void setUnreachable(Throwable t) throws NodeUnreachableException {
    	reachable = false;
    	throw new NodeUnreachableException(t);
    }
}