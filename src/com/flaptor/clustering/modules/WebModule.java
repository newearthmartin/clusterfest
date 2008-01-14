package com.flaptor.clustering.modules;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.flaptor.clustering.Node;
import com.flaptor.util.remote.WebServer;

/**
 * interface for modules that have a web face
 * @author martinmassera
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
	String getNodeHTML(Node node, int nodeNum);
	
	/**
	 * some modules register actions for clusterfest home page. If the parameter 
	 * action=?? is passed in the url, and that action is registered by this module
	 * it will be passed in the action method
	 * 
	 * @return a list of action names to be registered for this module 
	 */
	List<String> getActions();
	
	/**
	 * execute an action
	 * @return a message (or null)
	 */
	String action(String action,  HttpServletRequest request);
}
