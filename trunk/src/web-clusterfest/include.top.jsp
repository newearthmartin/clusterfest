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

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta HTTP-Equiv="Cache-Control" content="no-cache">
	<link rel="stylesheet" type="text/css" href="style.css" />
<head>
<title>ClusterFest - <%=request.getAttribute("pageTitle")%></title>
</head>
<body>
<div id="navbar">
<h1>ClusterFest - <%=request.getAttribute("pageTitle")%></h1>
<a href="index.jsp">ClusterFest home</a>
<%
	Integer nodeId = (Integer)request.getAttribute("node");
	if (nodeId != null) { 
%>		<a href="monitorNode.jsp?node=<%=nodeId%>">node status</a>
<%	}
%>
</div>
<div id="content">
