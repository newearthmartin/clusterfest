package com.flaptor.clustering;

import java.net.MalformedURLException;
import java.net.URL;

import com.flaptor.util.remote.XmlrpcClient;
import com.flaptor.util.remote.XmlrpcSerialization;


/**
 * Represents a node of the cluster. Indicates the host:port of the node,
 * the type and if it's reachable. All modules of the clustering framework
 * should check and update the reachable status.
 */
public class Node {

	private String host;
	private int port;
	private String installDir;
	private String type;
	private boolean reachable = true;
	private Clusterable clusterableStub;
	
	private XmlrpcClient xmlrpcClient;
	
    public Node(String host, int port, String installDir) {
        this.host = host;
        this.port = port;
        this.installDir = installDir;
        try {
			xmlrpcClient = new XmlrpcClient(new URL("http://"+host+":"+port+"/"));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
        clusterableStub = (Clusterable)XmlrpcClient.proxy(ClusterableServer.CONTEXT, Clusterable.class, xmlrpcClient); 
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
        if (type == null) {
            try {
                type = clusterableStub.getNodeType();
                reachable = true;
            } catch (Exception e) {
            	setUnreachable(e);
            }
        }
    }

    /**
     * marks the node as unreachable and throws a NodeUnreachableException
     * @throws NodeUnreachableException always!!!
     */
    public void setUnreachable(Exception e) throws NodeUnreachableException {
    	reachable = false;
    	throw new NodeUnreachableException(e);
    }
}