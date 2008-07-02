package com.flaptor.clusterfest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.flaptor.clusterfest.WebModule.ActionReturn;
import com.flaptor.util.Config;
import com.flaptor.util.Execute;
import com.flaptor.util.web.MVCServlet;

/**
 * Servlet for the clarin webapp
 */
public class ClusterfestServlet extends MVCServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(Execute.whoAmI());

    private ClusterManager cluster;
    private String appName;
    private String htmlAppName;
    private String additionalCSS;
    private String user;
    private String pass;
    
    @Override
    public void init() throws ServletException {
        super.init();
        cluster = ClusterManager.getInstance();
        Config config = Config.getConfig("clustering.properties");
        appName = config.getString("clustering.web.appName");
        htmlAppName = config.getString("clustering.web.htmlAppName");
        if (config.isDefined("clustering.web.additionalCSS")) {
            additionalCSS = config.getString("clustering.web.additionalCSS");
            if (additionalCSS != null) {
                additionalCSS = additionalCSS.trim();
                if (additionalCSS.length() ==0) additionalCSS = null;
            }
        }
        user = config.getString("clustering.web.login.user");
        pass = config.getString("clustering.web.login.pass");
    }

    @Override
    protected String doRequest(String uri, HttpServletRequest request, HttpServletResponse response) {
        request.setAttribute("cluster",cluster);
        request.setAttribute("appName",appName);
        request.setAttribute("htmlAppName",htmlAppName);
        request.setAttribute("additionalCSS",additionalCSS);
        String idx = request.getParameter("idx");
        if (idx != null) {
            int idxi = Integer.parseInt(idx);
            NodeDescriptor node = ClusterManager.getInstance().getNodes().get(idxi);
            request.setAttribute("idx", idxi);
            request.setAttribute("node", node);
        }
        String template = null;
        if (uri.contains("/index.do")) {
            template = doStartPage(request, response);
        } else if (uri.contains("/showLoginForm.do")) {
            template = "login.vm";
        } else if (uri.contains("/login.do")) {
            template = doLogin(request);
        } else if (uri.contains("/logout.do")) {
            request.getSession().invalidate();
            template = "/index.do";
        } else if (uri.endsWith(".do")) {
            String page = uri.substring(uri.lastIndexOf("/")+1);
            page = page.substring(0, page.lastIndexOf(".do"));
            template = doPage(page, request, response);
        }
        return template;
    }
    
    private String doPage(String page, HttpServletRequest request, HttpServletResponse response) {
        WebModule wm = cluster.getModuleForPage(page);
        if (wm != null) return wm.doPage(page, request, response);
        else return null;
    }
    
    private String doLogin(HttpServletRequest request) {
        String user = request.getParameter("user");
        String pass = request.getParameter("password");
        System.out.println(user + " " + this.user + " " + pass + " " + this.pass);
        if (this.user.equals(user) && this.pass.equals(pass)) {
            request.getSession().setAttribute("user", user);
            return "/index.do";
        } else {
            request.setAttribute("loginFailed", true);
            return "/login.vm";
        }
    }
    
    private String doStartPage(HttpServletRequest request, HttpServletResponse response) {
        String action = request.getParameter("action");
        String message = "";
        if ("register".equals(action)) {
            String host = request.getParameter("host");
            int port = Integer.parseInt(request.getParameter("port"));
            String installDir = request.getParameter("dir");
            NodeDescriptor node = cluster.registerNode(host, port, installDir, null);
            if (node.isReachable()) {
                message += "OK : " + host + ":" + port + " registered.";
            } else {
                message += "WARNING : " + host + ":" + port + " registered but unreachable.";
            }
            try {cluster.persistNodeList();} catch(IOException e) {message+="\nWARNING: couldn't persist node list";}
        } else if ("remove".equals(action)) {
            int idx = Integer.parseInt(request.getParameter("node"));
            NodeDescriptor node = cluster.getNodes().get(idx);
            cluster.unregisterNode(node);
            message += "OK : " + node.getHost() + ":" + node.getPort() + " unregistered.";
            try {cluster.persistNodeList();} catch(IOException e) {message+="\nWARNING: couldn't persist node list";}
        } else if ("updateall".equals(action)) {
            cluster.updateAllInfoAllNodes();
        } else if ("update".equals(action)) { //this action updates all states of the node: cluster, monitor,etc.
            int idx = Integer.parseInt(request.getParameter("node"));
            NodeDescriptor node = cluster.getNodes().get(idx); //update node info
            cluster.updateAllInfo(node);
            if (node.isReachable()) {
                message += "OK : " + node.getHost() + ":" + node.getPort() + " updated.";
            } else {
                message += "WARNING : host unreachable";
            }
        } else if ("selectedNodesAction".equals(action)) {
            String selectedAction = request.getParameter("selectedAction");
            List<NodeDescriptor> selectedNodes = new ArrayList<NodeDescriptor>();
            String [] nodes = request.getParameterValues("node");
            if (nodes != null) {
                for (String idx:nodes){
                    selectedNodes.add(cluster.getNodes().get(Integer.parseInt(idx)));
                }
            }
            if (selectedNodes.size() > 0) {
                WebModule wm = cluster.getModuleForSelectNodeAction(selectedAction);
                if (wm != null) {
                    ActionReturn actionReturn = wm.selectedNodesAction(selectedAction, selectedNodes, request);
                    if (actionReturn.isRedirect()) {
                        return doPage(actionReturn.getRedirectToPage(), request, response);
                    } else {
                        if (null != actionReturn.getMessage()) message += actionReturn.getMessage();
                    }
                }
            }
        } else if (action != null) {
            WebModule wm = cluster.getModuleForAction(action);
            if (wm != null) {
                ActionReturn actionReturn =  wm.action(action, request);
                if (actionReturn.isRedirect()) {
                    return doPage(actionReturn.getRedirectToPage(), request, response);
                } else {
                    if (null != actionReturn.getMessage()) message += actionReturn.getMessage();
                }
            }
        }
        if (message.length() > 0) request.setAttribute("message", message);
        return "index.vm";
    }
}
