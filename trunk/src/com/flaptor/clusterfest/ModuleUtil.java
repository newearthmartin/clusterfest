package com.flaptor.clusterfest;

import java.util.List;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.flaptor.clusterfest.exceptions.NodeException;
import com.flaptor.util.CallableWithId;
import com.flaptor.util.Pair;
import com.flaptor.util.remote.NoSuchRpcMethodException;
import com.flaptor.util.remote.XmlrpcClient;

/**
 * helper methods for modules
 * @author Martin Massera
 *
 */
public class ModuleUtil {
    
    private static final Logger logger = Logger.getLogger(com.flaptor.util.Execute.whoAmI());
    /**
     * determines if the node belongs to the module
     * the node must implement the Pingable interface in the module context
     * 
     * @param node
     * @param contextthrowException
     * @param defaultTrue if the node is unreachable, return true? if false, throws a NodeUnreachableException 
     * @return
     * @throws NodeException if the node is unreachable or throws exception
     */
    public static boolean nodeBelongs(NodeDescriptor node, String context, boolean defaultTrue) throws NodeException {
        try {
            ((Pingable)XmlrpcClient.proxy(context, Pingable.class, node.getXmlrpcClient())).ping();
            return true;
        } catch (NoSuchRpcMethodException e) {
            return false;
        } catch (Throwable t) {
            node.checkAndThrow(t, logger);
            return false; //never called
        }
    }

    /**
     * gets the idx parameter from the request and returns that node
     * @param request
     * @return
     */
    public static NodeDescriptor getNodeFromRequest(HttpServletRequest request) {
        String nodeParam = request.getParameter("idx");
        if (nodeParam != null ) return ClusterManager.getInstance().getNodes().get(Integer.parseInt(nodeParam));
        else return null;
    }

    /**
     * creates a ul list of problems encountered in a multi execution
     * @param problems
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String problemListToHTML(List<Pair<Callable<Void>,Throwable>> problems) {
        String message = "<ul>";
        for (Pair<Callable<Void>,Throwable> problem : problems) {
            NodeDescriptor node = ((CallableWithId<Void, NodeDescriptor>)problem.first()).getId();
            message+="<li><strong>" + (node.getType()!= null ? "@"+node.getType() : "")+ node.getHost() + ":"+node.getPort()+ "</strong>: " + problem.last() + "</li>";
        }
        message += "</ul>";
        return message;
    }
}
