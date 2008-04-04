package com.flaptor.clusterfest;

/**
 * interface for nodes that can be pinged
 * @author Martin Massera
 *
 */
public interface Pingable {
    /**
     * pinging method for determining if the node implements the interface
     * @return true
     */
    public boolean ping() throws Exception;
}
