<%@page import="java.util.Calendar"%>
<%@page import="com.flaptor.util.Pair"%>
<%@page import="com.flaptor.clustering.monitoring.SystemProperties"%>
<%@page import="com.flaptor.clustering.Node"%>
<%@page import="com.flaptor.clustering.Cluster"%>
<%@page import="com.flaptor.clustering.monitoring.monitor.Monitor"%>
<%@page import="com.flaptor.clustering.monitoring.monitor.MonitorNode"%>
<%@page import="com.flaptor.clustering.monitoring.monitor.NodeState"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Map"%>
<%@page import="java.io.Serializable"%>

<%
    Cluster cluster = Cluster.getInstance();
    int idx = Integer.parseInt(request.getParameter("node"));
    Node clusterNode = cluster.getNodes().get(idx);
    Monitor monitor = (Monitor)cluster.getModule("monitor");
    MonitorNode node = (MonitorNode)monitor.getNode(clusterNode);

    String logName = request.getParameter("log");

    String action = request.getParameter("action");
    if ("update".equals(action)) {
        node.updateLogs();
    }
    
    request.setAttribute("node", idx);
    request.setAttribute("pageTitle", "monitor - " + clusterNode.getType() + " @ " + clusterNode.getHost()+":"+clusterNode.getPort() + " - log: " + logName);
    
    Pair<String, Long> log = monitor.retrieveLog(node, logName);
%>

<%@include file="include.top.jsp" %>

    <div id="monitorSideBar">
        <a href="?action=update&node=<%= idx %>&log=<%=logName%>">update logs</a></br>
        <table>
            <tr><th>logs</th></tr>
<%          for (Map.Entry<String, Pair<String, Long>> e : node.getLogs().entrySet()) {
%>          
                <tr><td><a href="monitorLog.jsp?log=<%=e.getKey()%>&node=<%= idx %>"><%=e.getKey()%></a> <span class="fuzzy">at <%= new Date(e.getValue().last()).toString() %></span></td></tr>
<%          } %>            
            <tr><td>
                <form action="monitorLog.jsp">
                    <input type="hidden" name="node" value="<%= idx %>"/>
                    <input type="text" name="log" value=""/>
                    <input type="submit" value="log file"/>
                </form>
            </td></tr>
        </table>
    </div>

<%
    if (log == null) { %>
        <h2>This log is unavailable</h2>
<%  }else{ %>
        <h2>Log: <%=logName %> <span class="fuzzy">at <%= new Date(log.last()).toString() %></span></h2>
<%      if (log.first().length() > 0) {%>
            <pre><%=log.first()%></pre>
<%      } else {%>
        The log is empty!
<%      }
    }
%>

<%@include file="include.bottom.jsp" %>
