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

package com.flaptor.clusterfest;

import java.net.MalformedURLException;
import java.net.URL;

import com.flaptor.clusterfest.exceptions.NodeCodeException;
import com.flaptor.clusterfest.exceptions.NodeException;
import com.flaptor.clusterfest.exceptions.NodeUnreachableException;
import com.flaptor.util.remote.RemoteHostCodeException;
import com.flaptor.util.remote.RpcConnectionException;
import com.flaptor.util.remote.XmlrpcClient;


/**
 * Represents a node of the cluster. Indicates the host:port of the node,
 * the type and if it's reachable. All modules of the Clusterfest
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
	 * @param installDir the path where the node is installed (on the target machine) (can be null)
     * @param type the node type (can be null)
	 */
    public NodeDescriptor(String host, int port, String installDir, String type) {
        this.host = host;
        this.port = port;
        this.installDir = installDir;
        this.type = type;
        try {
			xmlrpcClient = new XmlrpcClient(new URL("http://"+host+":"+port+"/"));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
        clusterableStub = (Clusterable)XmlrpcClient.proxy(NodeListener.CONTEXT, Clusterable.class, xmlrpcClient); 
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
    void updateInfo() throws NodeException {
        try {
            type = clusterableStub.getNodeType();
            reachable = true;
        } catch (Throwable t) {
            checkAndThrow(t);
        }
    }
    public String toString() {
        return host+":"+port+":"+installDir+":"+type;
    }
    
    /**
     * method for checking thrown exceptions, sees if it is an unreachable exception and throws it as a NodeUnreachableException.
     * Otherwise throws a NodeCodeException 
     * @param t
     * @throws NodeException
     */
    public void checkAndThrow(Throwable t) throws NodeException {
        if (t instanceof RpcConnectionException) throw new NodeUnreachableException(this, t);
        else if (t instanceof RemoteHostCodeException) throw new NodeCodeException(this, t);
        else if (t instanceof RuntimeException) throw (RuntimeException)t;
        else throw new RuntimeException("unexpected exception", t);
    }
}