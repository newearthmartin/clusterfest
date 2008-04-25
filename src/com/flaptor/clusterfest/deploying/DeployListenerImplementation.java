package com.flaptor.clusterfest.deploying;

/**
 * implementation of the deploy listener
 * 
 * @author marto
 */
public class DeployListenerImplementation implements DeployListener{

    public boolean ping() throws Exception {
        return true;
    }
    
    public boolean deployFile(String filename, byte[] content) throws Exception{
        throw new Exception("implement this");
    }
}
