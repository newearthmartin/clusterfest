package com.flaptor.clustering;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;

import com.flaptor.clustering.controlling.controller.Controller;
import com.flaptor.clustering.controlling.controller.ControllerNodeState;
import com.flaptor.clustering.controlling.nodes.Controllable;
import com.flaptor.clustering.monitoring.SystemProperties;
import com.flaptor.clustering.monitoring.monitor.Monitor;
import com.flaptor.clustering.monitoring.monitor.MonitorNode;
import com.flaptor.clustering.monitoring.monitor.NodeState;
import com.flaptor.clustering.monitoring.nodes.Monitoreable;
import com.flaptor.util.TestCase;
import com.flaptor.util.remote.XmlrpcClient;
import com.flaptor.util.remote.XmlrpcServer;

public class ClusteringRpcTest extends TestCase {
	private Node node;
	private XmlrpcClient client;
	private ClusterableServer clusterableServer;
	private Monitoreable monitoreableProxy;
	private Controllable controllableProxy;

	protected void setUp() throws Exception {
		clusterableServer = new ClusterableServer(50001, "searcher");
		Monitor.addMonitorServer(clusterableServer, new MonitoreableImp());
		Controller.addControllerServer(clusterableServer, new ControllableImp());
		node = new Node("localhost", 50001, "/tmp/search4j");
		client = new XmlrpcClient(new URL("http://localhost:50001"));
		monitoreableProxy = Monitor.getMonitoreableProxy(node.getXmlrpcClient());
		controllableProxy = Controller.getControllableProxy(node.getXmlrpcClient());
	}

	protected void tearDown() throws Exception {
		clusterableServer.stop();
	}

	public void testClusterable() throws Exception {
		node.updateInfo();
		assertEquals("searcher", node.getType());
	}

	public void testPing() throws Exception {
		assertTrue(monitoreableProxy.ping());
		assertTrue(controllableProxy.ping());
	}

	public void testMonitoreable() throws Exception {
		MonitorNode mnode = new MonitorNode(node);
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
