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

package com.flaptor.clusterfest.exceptions;

import com.flaptor.clusterfest.NodeDescriptor;

/**
 * Exception to be thrown by rpc calls, meaning that the node is unreachable
 * Sets unreachable to the node
 *  
 * @author Martin Massera
 */
public class NodeCodeException extends NodeException {
    
    public NodeCodeException(NodeDescriptor node) {
        super(node, "unexpected exception in node code. node: " + node);
    }

    public NodeCodeException(NodeDescriptor node, Throwable cause) {
        super(node, "unexpected exception in node code. node: " + node + " cause: " + cause.getMessage(), cause);
    }
}
