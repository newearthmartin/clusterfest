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

import java.util.HashMap;
import java.util.Map;

import com.flaptor.clusterfest.NodeListener;
import com.flaptor.clusterfest.NodeDescriptor;
import com.flaptor.clusterfest.controlling.ControllerModule;
import com.flaptor.clusterfest.controlling.ControllerNodeState;
import com.flaptor.clusterfest.controlling.node.Controllable;
import com.flaptor.clusterfest.monitoring.MonitorModule;
import com.flaptor.clusterfest.monitoring.MonitorNodeDescriptor;
import com.flaptor.clusterfest.monitoring.NodeState;
import com.flaptor.clusterfest.monitoring.SystemProperties;
import com.flaptor.clusterfest.monitoring.node.Monitoreable;
import com.flaptor.util.Config;
import com.flaptor.util.TestCase;
import com.flaptor.util.TestInfo;

/**
 * Test cases for testing rpcs in the clustering framework 
 *
 * @author Martin Massera
 */
public class ClusterfestRpcTest extends TestCase {
	private NodeDescriptor node;
	private NodeListener clusterableListener;
	private Monitoreable monitoreableProxy;
	private Controllable controllableProxy;
	
	private static final int PORT = 50001;

	protected void setUp() throws Exception {
		Config cfg = Config.getConfig("clustering.node.properties");
		cfg.set("clustering.node.type", "searcher");
		cfg.set("clustering.node.listeners",   
		        "controller:com.flaptor.clusterfest.ClusteringRpcTest$ControllableImp,"+
		        "monitor:com.flaptor.clusterfest.ClusteringRpcTest$MonitoreableImp");
	    clusterableListener = new NodeListener(PORT);
		
		node = new NodeDescriptor("localhost", PORT, "/tmp/lalala", "typee");
		monitoreableProxy = MonitorModule.getMonitoreableProxy(node.getXmlrpcClient());
		controllableProxy = ControllerModule.getControllableProxy(node.getXmlrpcClient());
	}

	protected void tearDown() throws Exception {
	    // TODO: wait until it's actually stopped or a timeout is reached
		clusterableListener.requestStop();
	}

    @TestInfo(testType = TestInfo.TestType.INTEGRATION)//, requiresPort = {PORT})
	public void testClusterable() throws Exception {
		node.updateInfo();
		assertEquals("searcher", node.getType());
	}

    @TestInfo(testType = TestInfo.TestType.INTEGRATION)//, requiresPort = {PORT})
	public void testPing() throws Exception {
		assertTrue(monitoreableProxy.ping());
		assertTrue(controllableProxy.ping());
	}

    @TestInfo(testType = TestInfo.TestType.INTEGRATION)//, requiresPort = {PORT})
	public void testMonitoreable() throws Exception {
		MonitorNodeDescriptor mnode = new MonitorNodeDescriptor(node);
		mnode.updateState();
		
		NodeState nodeState = mnode.getLastState();
		assertEquals("log", mnode.retrieveLog("out").first());
		assertEquals(1, nodeState.getProperties().get("hola"));
		assertEquals("ho", nodeState.getSystemProperties().getIfconfigDump());
	}

	public static class MonitoreableImp implements Monitoreable{
		public String getLog(String logName) throws Exception {
			return "log";
		}
		public Map<String, Object> getProperties() throws Exception {
			Map<String, Object> m = new HashMap<String,Object>();
			m.put("hola", 1);
			return m;
		}
		public Object getProperty(String property) throws Exception {
			return 1.5f;
		}
		public SystemProperties getSystemProperties() throws Exception {
			SystemProperties p = new SystemProperties();
			p.setIfconfigDump("ho");
			p.setTopDump("la");
			return p;
		}
		public boolean ping() throws Exception {
			return true;
		}
	}

	public static class ControllableImp implements Controllable{
		public ControllerNodeState getState() throws Exception {
			return ControllerNodeState.RUNNING;
		}
		public void pause() throws Exception {
		}
		public boolean ping() throws Exception {
			return true;
		}
		public void resume() throws Exception {
		}
		public void stop() throws Exception {
		}
	}
}
