package com.flaptor.clusterfest.action;

import java.util.List;

import com.flaptor.clusterfest.NodeDescriptor;

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
     */
    public void action(String action, Object[] params) throws Exception;
}
