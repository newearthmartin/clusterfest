package com.flaptor.clustering;

import com.flaptor.util.Config;
import com.flaptor.util.EmbeddedWebAppHTTPServer;
import com.flaptor.util.PortUtil;
import com.flaptor.util.remote.AServer;
import com.flaptor.util.remote.WebServer;

/**
 * HTTP server for clusterfest monitor 
 */
public class HTTPClusteringServer extends WebServer {

	public HTTPClusteringServer(int port){
		super(port);
        String webappPath = this.getClass().getClassLoader().getResource("web-clustering").getPath();
		addWebAppHandler("/clustering", webappPath);
	}

	public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("usage: " + HTTPClusteringServer.class.getSimpleName() + " port");
            System.exit(1);
        }
        new HTTPClusteringServer(Integer.parseInt(args[0])).start();
	}
}
