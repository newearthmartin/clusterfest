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

<%@page import="java.util.Calendar"%>
<%@page import="com.flaptor.util.Pair"%>
<%@page import="com.flaptor.clusterfest.*"%>
<%@page import="com.flaptor.clusterfest.monitoring.*"%>
<%@page import="com.flaptor.clusterfest.monitoring.node.*"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Map"%>
<%@page import="java.io.Serializable"%>

<%
    ClusterManager cluster = ClusterManager.getInstance();
    int idx = Integer.parseInt(request.getParameter("node"));
    NodeDescriptor node = cluster.getNodes().get(idx);
    MonitorModule monitor = (MonitorModule)cluster.getModule("monitor");
    MonitorNodeDescriptor monitorNode = monitor.getModuleNode(node);
    
    String logName = request.getParameter("log");

    String action = request.getParameter("action");
    if ("update".equals(action)) {
        monitor.updateLogs(node);
    }
    
    request.setAttribute("node", idx);
    request.setAttribute("pageTitle", "monitor - " + node.getType() + " @ " + node.getHost()+":"+node.getPort() + " - log: " + logName);
    
    Pair<String, Long> log = monitor.retrieveLog(node, logName);
%>

<%@include file="include.top.jsp" %>

<div id="monitorSideBar">
        <a href="?action=update&node=<%= idx %>&log=<%=logName%>">update logs</a></br>
        <table>
            <tr><th>logs</th></tr>
<%          for (Map.Entry<String, Pair<String, Long>> e : monitorNode.getLogs().entrySet()) {
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

<%    if (log == null) { %>
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
