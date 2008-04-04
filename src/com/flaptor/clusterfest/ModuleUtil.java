package com.flaptor.clusterfest;

import javax.servlet.http.HttpServletRequest;

import com.flaptor.util.remote.NoSuchRpcMethodException;
import com.flaptor.util.remote.XmlrpcClient;
import com.flaptor.util.remote.XmlrpcSerialization;

/**
 * helper methods for modules
 * @author Martin Massera
 *
 */
public class ModuleUtil {
    
    /**
     * determines if the node belongs to the module
     * the node must implement the Pingable interface in the module context
     * 
     * @param node
     * @param contextthrowException
     * @param defaultTrue if the node is unreachable, return true? if false, throws a NodeUnreachableException 
     * @return
     * @throws NodeUnreachableException if the node is unreachable
     */
    public static boolean nodeBelongs(NodeDescriptor node, String context, boolean defaultTrue) throws NodeUnreachableException {
        try {
            ((Pingable)XmlrpcClient.proxy(context, Pingable.class, node.getXmlrpcClient())).ping();
            return true;
        } catch (NoSuchRpcMethodException e) {
            return false;
        } catch (Exception e) {
            node.setReachable(false);
            if (defaultTrue) return true;
            else throw new NodeUnreachableException(e, node);
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
}
