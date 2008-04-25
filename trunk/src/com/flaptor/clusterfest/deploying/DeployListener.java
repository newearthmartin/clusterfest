package com.flaptor.clusterfest.deploying;

import java.util.List;

import com.flaptor.clusterfest.NodeDescriptor;

/**
 * interface for nodes of the deploy module
 *
 * @author Martin Massera
 */
public interface DeployListener {

    /**
     * pinging method for determining if the node is an action receiver
     * @return true
     */
    public boolean ping() throws Exception;

    /**
     * called when action received
     * @param filename the file name
     * @param content the file content
     * @return (is ignored, for compatibility with xmlrpc)
     */
    public boolean deployFile(String filename, byte[] content) throws Exception;
}
