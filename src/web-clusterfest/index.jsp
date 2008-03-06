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

<%@page import="com.flaptor.clusterfest.*"%>
<%@page import="com.flaptor.clusterfest.monitoring.*"%>
<%@page import="com.flaptor.clusterfest.controlling.*"%>
<%@page import="com.flaptor.clusterfest.controlling.node.*"%>
<%@page import="java.util.Date"%>
<%@page import="java.io.IOException"%>
<%@page import="com.flaptor.util.ThreadUtil"%>

<%request.setAttribute("pageTitle","home");%>
<%@include file="include.top.jsp" %>

<%
    ClusterManager cluster = ClusterManager.getInstance();
    MonitorModule monitor = (MonitorModule)cluster.getModule("monitor");
    ControllerModule control = (ControllerModule)cluster.getModule("controller");  

    String action = request.getParameter("action");
    String message = "";
    if ("register".equals(action)) {
        String host = request.getParameter("host");
        int port = Integer.parseInt(request.getParameter("port"));
        String installDir = request.getParameter("dir");
        NodeDescriptor node = cluster.registerNode(host, port, installDir);
        if (node.isReachable()) {
    message += "OK : " + host + ":" + port + " registered.";
        } else {
    message += "WARNING : " + host + ":" + port + " registered but unreachable.";
        }
        try {cluster.persistNodeList();} catch(IOException e) {message+="\nWARNING: couldn't persist node list";}
    }
    if ("remove".equals(action)) {
        int idx = Integer.parseInt(request.getParameter("node"));
        NodeDescriptor node = cluster.getNodes().get(idx);
        cluster.unregisterNode(node);
        message += "OK : " + node.getHost() + ":" + node.getPort() + " unregistered.";
        try {cluster.persistNodeList();} catch(IOException e) {message+="\nWARNING: couldn't persist node list";}
    }
    if ("updateall".equals(action)) {
        cluster.updateAllInfoAllNodes();
    }
    if ("update".equals(action)) { //this action updates all states of the node: cluster, monitor,etc.
        int idx = Integer.parseInt(request.getParameter("node"));
        NodeDescriptor node = cluster.getNodes().get(idx); //update node info
        cluster.updateAllInfo(node);
        if (node.isReachable()) {
    message += "OK : " + node.getHost() + ":" + node.getPort() + " updated.";
        } else {
    message += "WARNING : host unreachable";
        }
    }
    if (action != null) {
    	WebModule wm = cluster.getModuleForAction(action);
    	if (wm != null) {
    		String msg = wm.action(action, request);
    		if (msg != null) message += msg;
    	}
    }
%>

<%
    if (message.length()>0) {
%>
<p><%=message%></p>
<%  }
%>

<%  if (cluster.getNodes().isEmpty()) {
%>
        No nodes registered
<%  } else {
%>
        <h2>Current nodes</h2><a href="?action=updateall">update all</a>
        <ul>
<%
        int i = 0;
        for (NodeDescriptor node : cluster.getNodes()) {
%>
            <li> 
            <strong><%= node.getHost() %>:<%= node.getPort() %></strong>:<%= node.getInstallDir() %>
            <%= node.getType() == null ? "" : (" - " + node.getType()) %>
            
<%          for(WebModule wm: cluster.getWebModules()) {
	           String nodeLink = wm.getNodeHTML(node, i);
               if (nodeLink != null) {
                  out.println(" :: " + nodeLink);
               }
            }
%>
            :: <a href="?action=update&node=<%= i %>">update</a>
            - <a href="?action=remove&node=<%= i %>">remove</a>

            </li>
<%          i++;
        }
%>
    </ul>
<%  }
%>

</br>

<%  for(WebModule wm: cluster.getWebModules()) {
        String moduleLink = wm.getModuleHTML();
        if (moduleLink != null) {
            out.println(moduleLink+ "<br/>");
        }
    }
%>

    <br/>
    <form method="post" action="index.jsp">
        <input type="hidden" name="action" value="register"/> 
        <table border="1">
            <tr><th colspan="2">Register node</th></tr>
            <tr>
                <td>Host:</td>
                <td><input type="text" name="host" value=""/></td>
            </tr>
            <tr>
                <td>Port:</td>
                <td><input type="text" name="port" value=""/></td>
            </tr>
            <tr>
                <td>Install dir:</td>
                <td><input type="text" name="dir" value=""/></td>
            </tr>
            <tr>
                <td colspan="2" align="center"><input type="submit" value="Register"/></td>
            </tr>
        </table>
    </form>

<%@include file="include.bottom.jsp" %>
