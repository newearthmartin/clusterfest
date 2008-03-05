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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Class for representing a module 
 * it manages the node registration 
 *
 * @author Martin Massera
 */
abstract public class AbstractModule<T extends ModuleNodeDescriptor> implements Module {
	
	protected List<T> nodes = new ArrayList<T>();
	protected Map<NodeDescriptor, T> nodesMap = new HashMap<NodeDescriptor, T>();
	
	/**
	 * Create a module node from a clustering node 
	 * @param node
	 * @return the created module node
	 */
	abstract protected T createModuleNode(NodeDescriptor node);
	
	/**
	 * @param node
	 * @return true iff the node should be registered in this module
	 * @throws NodeUnreachableException if the node is unreachable 
	 */
	abstract protected boolean shouldRegister(NodeDescriptor node) throws NodeUnreachableException; 

	/**
	 * updates the info of the node 
	 * @param node should be registered
	 */
	abstract protected void notifyModuleNode(T node);
	
	public final void notifyNode(NodeDescriptor node) throws NodeUnreachableException {
	    if (shouldRegister(node)){
	        if (!isRegistered(node)) registerNode(node);
	    } else {
	        if (isRegistered(node)) unregisterNode(node);
	    }
	    if (isRegistered(node)) notifyModuleNode(getModuleNode(node));
	}
	
	public final void nodeUnregistered(NodeDescriptor node) {
	    unregisterNode(node);
	}
	
	/**
	 * registers a node, creating a module node for this node
	 * @param node a clustering node
	 * @return the created module node
	 * @throws NodeUnreachableException if the node is unreachable
	 */
	protected T registerNode(NodeDescriptor node) {
		T moduleNode = createModuleNode(node);
    	synchronized (nodes) {
    		nodes.add(moduleNode);
    		nodesMap.put(node, moduleNode);
    	}
		return moduleNode;
	}

	/**
	 * @return true iff the node is registered
	 */
	public boolean isRegistered(NodeDescriptor node) {
    	synchronized (nodes) {
    		return nodesMap.containsKey(node);
    	}
	}
	
	/**
	 * unregisters the node
	 * @param node
	 */
	protected void unregisterNode(NodeDescriptor node) {
    	synchronized (nodes) {
    		ModuleNodeDescriptor moduleNode = getModuleNode(node);
    		nodes.remove(moduleNode);
    		nodesMap.remove(node);
    	}
	}

	/**
	 * @return the list of registered nodes
	 */
	public List<T> getModuleNodeDescriptors() {
   		return Collections.unmodifiableList(nodes);
	}
	
	/**
	 * @return the list of registered nodes
	 */
	public Set<NodeDescriptor> getNodeDescriptors() {
	    return Collections.unmodifiableSet(nodesMap.keySet());
	}
	
	/**
	 * @param node a clustering node
	 * @return the module node corresponding to the clustering node 
	 */
	public T getModuleNode(NodeDescriptor node) {
    	synchronized (nodes) {
    		return nodesMap.get(node);
    	}
	}
}
