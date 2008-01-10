package com.flaptor.clustering;

import java.rmi.Remote;

/**
 * interface for all nodes of the clustering framework 
 * For marking nodes as clusterable and tells what type the node is   
 */
public interface Clusterable extends Remote{

	/**
	 * @return the type of node
	 */
	String getNodeType() throws Exception;
}
