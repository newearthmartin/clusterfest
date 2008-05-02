package com.flaptor.clusterfest.deploying;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * implementation of the deploy listener
 * 
 * @author marto
 */
public class DeployListenerImplementation implements DeployListener{

    public boolean ping() throws Exception {
        return true;
    }
    
    public boolean deployFile(String path, String filename, byte[] content) throws Exception{
        if (path == null) path = ".";
        OutputStream out = new FileOutputStream(new File(path, filename));
        out.write(content);
        out.flush();
        out.close();
        return true;
    }
}
