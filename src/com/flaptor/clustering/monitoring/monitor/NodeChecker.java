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

package com.flaptor.clustering.monitoring.monitor;

import com.flaptor.clustering.monitoring.monitor.NodeState.Sanity;

public interface NodeChecker {

    /**
     * Checks a node for a given state. It should return a sanity
     * value to be set for that state.
     * 
     * @param node the node to check
     * @param state the state in which the node should be checked
     * @return the sanity value
     */
    public Sanity checkNode(MonitorNode node, NodeState state);
}
