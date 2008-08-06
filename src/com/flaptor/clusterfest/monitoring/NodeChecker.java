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

package com.flaptor.clusterfest.monitoring;

import java.io.Serializable;
import java.util.List;

/**
 * Interface for node checkers. Its basically a sanity check on the node's state
 * @author Martin Massera
 */
public interface NodeChecker {

    /**
     * Checks a node for a given state. It should return a sanity
     * value to be set for that state.
     * 
     * @param node the node to check
     * @param state the state in which the node should be checked
     * @return a Result object
     */
    public Result checkNode(MonitorNodeDescriptor node, NodeState state);

    public static enum Sanity {
        UNREACHABLE,
        UNKNOWN,
        GOOD,
        BAD,
        ERROR
    }
    
    /**
     * For expressing the result of the checker, contains a sanity value
     * and a list of (human readable) remarks that express what's wrong
     */
    public static class Result implements Serializable{
        
        private static final long serialVersionUID = 1L;
        
        private Sanity sanity;
        private List<String> remarks;

        public Result(Sanity sanity, List<String> remarks) {
            super();
            this.sanity = sanity;
            this.remarks = remarks;
        }
        public Sanity getSanity() {
            return sanity;
        }
        public List<String> getRemarks() {
            return remarks;
        }
    }
}
