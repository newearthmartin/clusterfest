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

package com.flaptor.clustering.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.flaptor.clustering.Node;
import com.flaptor.clustering.NodeUnreachableException;

/**
 * Class for representing a module that needs nodes to be registered 
 */
abstract public class NodeContainerModule {
	
	protected List<ModuleNode> nodes = new ArrayList<ModuleNode>();
	protected Map<Node, ModuleNode> nodesMap = new HashMap<Node, ModuleNode >();
	
	/**
	 * Create a module node from a clustering node 
	 * @param node
	 * @return the created module node
	 */
	abstract protected ModuleNode createModuleNode(Node node);
	
	/**
	 * @param node
	 * @return true iff the node should be registered in this module
	 * @throws NodeUnreachableException if the node is unreachable 
	 */
	abstract public boolean nodeBelongs(Node node) throws NodeUnreachableException; 

	/**
	 * updates the info of the node 
	 * @param node should be registered
	 * @return true iff update finished ok
	 */
	abstract public boolean updateNode(ModuleNode node);
	
	/**
	 * registers a node, creating a module node for this node
	 * @param node a clustering node
	 * @return the created module node
	 * @throws NodeUnreachableException if the node is unreachable
	 */
	public ModuleNode registerNode(Node node) {
		ModuleNode moduleNode = createModuleNode(node);
    	synchronized (nodes) {
    		nodes.add(moduleNode);
    		nodesMap.put(node, moduleNode);
    	}
		return moduleNode;
	}

	/**
	 * @return true iff the node is registered
	 */
	public boolean isRegistered(Node node) {
    	synchronized (nodes) {
    		return nodesMap.containsKey(node);
    	}
	}
	
	/**
	 * unregisters the node
	 * @param node
	 */
	public void unregisterNode(Node node) {
    	synchronized (nodes) {
    		ModuleNode moduleNode = getNode(node);
    		nodes.remove(moduleNode);
    		nodesMap.remove(node);
    	}
	}

	/**
	 * @return the list of registered nodes
	 */
	public List<ModuleNode> getNodes() {
   		return Collections.unmodifiableList(nodes);
	}
	
	/**
	 * @param node a clustering node
	 * @return the module node corresponding to the clustering node 
	 */
	public ModuleNode getNode(Node node) {
    	synchronized (nodes) {
    		return nodesMap.get(node);
    	}
	}
}
