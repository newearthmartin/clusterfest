package com.flaptor.clusterfest.deploy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    
    public boolean deployFile(String path, String filename, byte[] content) throws IOException{
        if (path == null) path = ".";
        File file = new File(path, filename);
        OutputStream out = new FileOutputStream(file);
        out.write(content);
        out.flush();
        out.close();
        return true;
    }
}
