package com.flaptor.clusterfest.action;


/**
 * interface for nodes that receive actions
 *
 * @author Martin Massera
 */
public interface ActionReceiver {

    /**
     * pinging method for determining if the node is an action receiver
     * @return true
     */
    public boolean ping() throws Exception;

    /**
     * called when action received
     * @param action the action name
     * @param params the action params
     * @return (is ignored, for compatibility with xmlrpc)
     */
    public boolean action(String action, Object[] params) throws Exception;
}
