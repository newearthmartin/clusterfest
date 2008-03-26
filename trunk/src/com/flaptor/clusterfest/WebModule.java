package com.flaptor.clusterfest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.flaptor.util.Pair;
import com.flaptor.util.Triad;
import com.flaptor.util.remote.WebServer;

/**
 * interface for modules that have a web face
 * @author Martin Massera
 */
public interface WebModule {

	/**
	 * this method is for setting up the module in the web server 
	 * (adding handlers if necessary)
	 * @param server
	 */
	void setup(WebServer server);
	
	/**
	 * @return html (a link) to the module page, or null if there isnt
	 */
	String getModuleHTML();
	
	/**
	 * @param node
	 * @param nodeNum the number of the node in the list (for request parameters)
	 * @return html to be displayed in the node list for this node, or null if there isnt
	 */
	String getNodeHTML(NodeDescriptor node, int nodeNum);
	
	/**
	 * some modules register actions for clusterfest home page. If the parameter 
	 * action=?? is passed in the url, and that action is registered by this module
	 * it will be passed in the action method
	 * 
	 * @return a list of action names to be registered for this module 
	 */
	List<String> getActions();
	
	/**
     * some modules register actions for clusterfest home page, for the nodes selected in the node list. 
     * 
     * @return a list of <action name, action HTML to be displayed> to be registered for this module 
     */
	List<Pair<String,String>> getSelectedNodesActions();

    /**
     * execute an action for the selected nodes
     * @return a message (or null)
     */
    String selectedNodesAction(String action, List<NodeDescriptor> nodes, HttpServletRequest request);
	
	/**
	 * execute an action
	 * @return a message (or null)
	 */
	String action(String action,  HttpServletRequest request);
	
	/**
	 * @return a list of pages that this server attends, these pages will have .do suffix appended:
	 * 
	 * "foo", "bar" --> "foo.do" "bar.do"
	 */
	List<String> getPages();
	
	/**
	 * will be called to process a page, if any of the pages from getPages is called
	 * 
	 * @param request
	 * @param response
	 * @return a template to redirect to (.jsp, .vm, etc) or null if you serve the page yourself
	 */
	String doPage(String page, HttpServletRequest request, HttpServletResponse response);
}
