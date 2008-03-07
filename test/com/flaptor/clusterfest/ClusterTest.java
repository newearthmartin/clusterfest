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

import java.util.ArrayList;
import java.util.List;

import com.flaptor.clusterfest.ClusterManager;
import com.flaptor.clusterfest.ClusterableListener;
import com.flaptor.clusterfest.NodeDescriptor;
import com.flaptor.util.Config;
import com.flaptor.util.FileUtil;
import com.flaptor.util.TestCase;
import com.flaptor.util.TestInfo;

/**
 * test for Clusterfest
 *
 * @author Martin Massera
 */
public class ClusterTest extends TestCase {
	private static final int NUM_NODES = 3;
    
    private ClusterManager cluster;
    private List<ClusterableListener> clusterableListeners;
	
	public void setUp() throws Exception {
	    clusterableListeners = new ArrayList<ClusterableListener>();
        String dir = FileUtil.createTempDir("clustertest", "").getAbsolutePath();
        String nodes = "";
        for (int i = 0; i < NUM_NODES; i++) {
            ClusterableListener s = new ClusterableListener(50000+i);
            s.setNodeType("searcher");
            clusterableListeners.add(s);
            if (i != 0) nodes +=",";
            nodes += "localhost:"+(50000+i)+":"+dir;
        }
        Config.getConfig("clustering.properties").set("clustering.modules", "");
        Config.getConfig("clustering.properties").set("clustering.nodes", nodes);
        cluster = ClusterManager.getInstance();
		for (ClusterableListener s : clusterableListeners) {
			s.start();
		}
	}
	public void tearDown() throws Exception {
	    // TODO: wait until they're actually stopped or a timeout is reached
		for (ClusterableListener s : clusterableListeners) {
	        s.requestStop();
		}
	}

	@TestInfo(testType = TestInfo.TestType.SYSTEM)//, requiresPort = {50000,50001,50002})
	public void testNodeRegistration() {
		filterOutputRegex("Avoiding.*");
		assertEquals(NUM_NODES, cluster.getNodes().size());
		for (NodeDescriptor node : cluster.getNodes()) {
			cluster.updateAllInfo(node);
			assertEquals("searcher", node.getType());
			assertTrue(node.isReachable());
		}
		NodeDescriptor n2 = cluster.registerNode("localhost", 12345, "lalalal");
		assertFalse(n2.isReachable());
		assertEquals(NUM_NODES + 1, cluster.getNodes().size());
		cluster.unregisterNode(n2);
		assertEquals(NUM_NODES, cluster.getNodes().size());
		
		unfilterOutput();
	}
}
