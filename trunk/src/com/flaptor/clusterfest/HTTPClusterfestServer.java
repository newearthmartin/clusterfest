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

package com.flaptor.clusterfest;

import java.net.URL;

import com.flaptor.util.remote.WebServer;

/**
 * HTTP server for clusterfest monitor 
 *
 * @author Martin Massera
 */
public class HTTPClusterfestServer extends WebServer {

	public HTTPClusterfestServer(int port){
		super(port);
        URL webappPath = this.getClass().getClassLoader().getResource("web-clusterfest");
        String path = webappPath.getPath();
		addWebAppHandler("/", path);
	}

	/**
	 * starts a webserver exporting the webapp located in web-clusterfest folder (in the classpath)
	 * @param args the port
	 */
	public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("usage: " + HTTPClusterfestServer.class.getSimpleName() + " port");
            System.exit(1);
        }
        HTTPClusterfestServer server = new HTTPClusterfestServer(Integer.parseInt(args[0]));
        server.start();
	}
}
