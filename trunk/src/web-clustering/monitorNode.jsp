<%-- 
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
--%>
<%-- author Martin Massera --%>

<%@page import="com.flaptor.util.Pair"%>
<%@page import="com.flaptor.clustering.monitoring.SystemProperties"%>
<%@page import="com.flaptor.clustering.*"%>
<%@page import="com.flaptor.clustering.monitoring.*"%>
<%@page import="com.flaptor.clustering.monitoring.monitor.*"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Map"%>

<%
    ClusterManager cluster = ClusterManager.getInstance();
    int idx = Integer.parseInt(request.getParameter("node"));
    NodeDescriptor node = cluster.getNodes().get(idx);

    MonitorModule monitor = (MonitorModule)cluster.getModule("monitor");
    MonitorNodeDescriptor monitorNode = monitor.getModuleNode(node);

    String action = request.getParameter("action");
    if ("update".equals(action)) {
        monitor.updateNodeInfo(monitorNode);
    }

    request.setAttribute("pageTitle", "monitor - " + node.getType() + " @ " + node.getHost()+":"+node.getPort());
%>

<%@include file="include.top.jsp" %>


<%if (!node.isReachable()) { %>
    <h2>Node unreachable!</h2>
<%
}%>

<%if (!monitor.isRegistered(node)) {%>
    <h2>This node is not monitoreable.</h2>
<%  
}
else {
    NodeState nodeState;
    String stateNum =request.getParameter("stateNum"); 
    if (stateNum != null) {
        nodeState = monitorNode.getNodeState(Integer.parseInt(stateNum));
    } else {
        nodeState = monitorNode.getLastState();        
    }
    if (nodeState != null) {
%>
        <div id="monitorSideBar">
            <a href="?action=update&node=<%= idx %>">update node</a></br>
            <table>
                <tr><th>logs</th></tr>
<%              for (Map.Entry<String, Pair<String, Long>> e : monitorNode.getLogs().entrySet()) {%>
                    <tr><td><a href="monitorLog.jsp?log=<%=e.getKey()%>&node=<%= idx %>"><%=e.getKey()%></a> <span class="fuzzy">at <%= new Date(e.getValue().last()).toString() %></span></td></tr>
<%              } %>            
                <tr><td>
                    <form action="monitorLog.jsp">
                        <input type="hidden" name="node" value="<%= idx %>"/>
                        <input type="text" name="log" value=""/>
                        <input type="submit" value="log file"/>
                    </form>
                </td></tr>
            </table>

            <table>
                <tr><th>Saved states</th></tr>                
<%              int num = 0;
				for (NodeState state : monitorNode.getStates()) {
%>                  <tr><td><%= state.getSanity() %> at <a href="monitorNode.jsp?node=<%=idx%>&stateNum=<%=num%>"><%= new Date(state.getTimestamp()).toString() %></a></td></tr>
<%              	num++;
				} 
%>
            </table>
        </div>

        <h2>State: <%= nodeState.getSanity() %> <span class="fuzzy">at <%= new Date(nodeState.getTimestamp()).toString() %></span></h2>

        <div id="monitorNodeProperties">
<%          Map<String, Object> properties = nodeState.getProperties();
            if (properties != null) {%>
                <table>
                    <th colspan="2">Properties</th>
<%              for (Map.Entry<String, Object> entry : properties.entrySet()) {
%> 
                    <tr>
                        <td><%= entry.getKey() %></td><td><%= entry.getValue() %></td>
                    </tr>
<%              }%>
                </table>
<%          }%>
        </div>
        
        <div id="monitorNodeSystenProperties">
<%          SystemProperties systemProperties = nodeState.getSystemProperties();
            if (systemProperties != null) {%>
                <h2>System properties</h2>
                <h3>Top</h3>
                <pre><%= systemProperties.getTopDump() %></pre>
                <h3>ifconfig</h3>
                <pre><%= systemProperties.getIfconfigDump() %></pre>
<%          } %>        
        </div>
<%    }
}
%>
<%@include file="include.bottom.jsp" %>
