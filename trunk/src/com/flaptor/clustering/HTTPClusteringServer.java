/*
Copyright 2008 Flaptor (flaptor.com) 

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

    http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/

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
