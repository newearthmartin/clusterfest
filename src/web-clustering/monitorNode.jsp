<%@page import="com.flaptor.util.Pair"%>
<%@page import="com.flaptor.clustering.monitoring.SystemProperties"%>
<%@page import="com.flaptor.clustering.Node"%>
<%@page import="com.flaptor.clustering.Cluster"%>
<%@page import="com.flaptor.clustering.monitoring.monitor.Monitor"%>
<%@page import="com.flaptor.clustering.monitoring.monitor.MonitorNode"%>
<%@page import="com.flaptor.clustering.monitoring.monitor.NodeState"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Map"%>

<%
    Cluster cluster = Cluster.getInstance();
    int idx = Integer.parseInt(request.getParameter("node"));
    Node clusterNode = cluster.getNodes().get(idx);

    String action = request.getParameter("action");
    if ("update".equals(action)) {
        cluster.updateAllInfo(clusterNode);
    }

    Monitor monitor = (Monitor)cluster.getModule("monitor");
    MonitorNode node = (MonitorNode)monitor.getNode(clusterNode);

    request.setAttribute("pageTitle", "monitor - " + clusterNode.getType() + " @ " + clusterNode.getHost()+":"+clusterNode.getPort());
%>

<%@include file="include.top.jsp" %>


<%if (!clusterNode.isReachable()) { %>
    <h2>Node unreachable!</h2>
<%
}%>

<%if (node == null) {%>
    <h2>This node is not monitoreable.</h2>
<%  
}
else {
    NodeState lastState = node.getLastState();
    if (lastState != null) {
%>
        <div id="monitorSideBar">
            <a href="?action=update&node=<%= idx %>">update node</a></br>
            <table>
                <tr><th>logs</th></tr>
<%              for (Map.Entry<String, Pair<String, Long>> e : node.getLogs().entrySet()) {
%>          
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
<%              for (NodeState state : node.getStates()) { 
%>
                    <tr><td><%= state.getSanity() %> at <%= new Date(state.getTimestamp()).toString() %></td></tr>
<%              } 
%>
            </table>
        </div>

        <h2>State: <%= lastState.getSanity() %> <span class="fuzzy">at <%= new Date(lastState.getTimestamp()).toString() %></span></h2>

        <div id="monitorNodeProperties">
<%          Map<String, Object> properties = lastState.getProperties();
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
<%          SystemProperties systemProperties = lastState.getSystemProperties();
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
