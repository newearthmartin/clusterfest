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
