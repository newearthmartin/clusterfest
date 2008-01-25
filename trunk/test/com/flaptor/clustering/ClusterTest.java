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
import java.util.List;

import com.flaptor.util.Config;
import com.flaptor.util.FileUtil;
import com.flaptor.util.TestCase;
import com.flaptor.util.remote.XmlrpcClient;

/**
 * test for clustering framework
 *
 * @author Martin Massera
 */
public class ClusterTest extends TestCase {
	private static XmlrpcClient client;
	private static List<ClusterableServer> clusterableServers = new ArrayList<ClusterableServer>();
	
	private static Cluster cluster;
	private static final int NUM_NODES = 10;
	static {
		String dir = FileUtil.createTempDir("clustertest", "").getAbsolutePath();
		String nodes = "";  
		for (int i = 0; i < NUM_NODES; i++) {
			clusterableServers.add(new ClusterableServer(50000+i, "searcher"));
			if (i != 0) nodes +=",";
			nodes += "localhost:"+(50000+i)+":"+dir;
		}
		Config.getConfig("clustering.properties").set("clustering.modules", "");
		Config.getConfig("clustering.properties").set("clustering.nodes", nodes);
		cluster = Cluster.getInstance();
	}
	
	protected void setUp() throws Exception {
		for (ClusterableServer s : clusterableServers) {
			s.start();
		}
	}
	protected void tearDown() throws Exception {
		for (ClusterableServer s : clusterableServers) {
			s.stop();
		}
	}

	public void testNodeRegistration() {
		filterOutputRegex("Avoiding.*");
		assertEquals(NUM_NODES, cluster.getNodes().size());
		for (Node node : cluster.getNodes()) {
			cluster.updateAllInfo(node);
			assertEquals("searcher", node.getType());
			assertTrue(node.isReachable());
		}
		Node n2 = cluster.registerNode("localhost", 12345, "lalalal");
		assertFalse(n2.isReachable());
		assertEquals(NUM_NODES + 1, cluster.getNodes().size());
		cluster.unregisterNode(n2);
		assertEquals(NUM_NODES, cluster.getNodes().size());
		
		unfilterOutput();
	}
}
