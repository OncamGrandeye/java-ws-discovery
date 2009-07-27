/*
discovery_using_server.java

Copyright (C) 2008-2009 Magnus Skjegstad

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package wsdiscoveryexamples;

import ws_discovery.WsDiscoveryBuilder;
import ws_discovery.WsDiscoveryServer;
import ws_discovery.network.exception.WsDiscoveryNetworkException;
import ws_discovery.servicedirectory.WsDiscoveryServiceDirectory;

/**
 * Starts a WS-Discovery server, probes for services with a Probe-message 
 * and then displays the result. If some services are missing the XAddrs field
 * (invocation address) a Resolve-message is sent.
 *
 * See the discovery_using_finder-example for an alternative, and perhaps
 * easier way to do this.
 * 
 * @author Magnus Skjegstad
 */
public class discovery_using_server {

    public static void main(String[] args) 
            throws WsDiscoveryNetworkException, InterruptedException {
        
        System.out.println("Starting WS-Discovery server...");
        
        // Create a new server instance
        WsDiscoveryServer server = WsDiscoveryBuilder.createServer();
        
        // Start background threads
        server.start();

        System.out.println("Sending probe...");
        
        // Send Probe-message. 
        server.probe();
        
        // All listening WS-Discovery instances should respond to a blank probe. 
        // The background server will receive the replies and store the 
        // discovered services in a service directory.
        
        System.out.println("Waiting for replies. (2 sec)");
        Thread.sleep(2000);

        // Check if any of the discovered services are missing XAddrs (invocation address).
        // If they are, try to resolve it. 
        {
            // Get a copy of the remote service directory
            WsDiscoveryServiceDirectory result = server.getRemoteServices();
            boolean resolve_sent = false;
            
            for (int i = 0; i < result.size(); i++) 
                // Is XAddrs empty?
                if (result.get(i).getXAddrs().size() == 0) {
                    // Send Resolve-message 
                    System.out.println("Trying to resolve XAddr for service " + result.get(i).getEndpointReferenceAddress());
                    server.resolve(result.get(i));
                    resolve_sent = true;
                }
                        
            if (resolve_sent) {
                System.out.println("Waiting for ResolveMatches. (2 sec)");
                Thread.sleep(2000);
            }
        }
                
        // Get a copy of the remote service directory and display the results.
        {
            System.out.println("** Discovered services: **");
            
            WsDiscoveryServiceDirectory result = server.getRemoteServices();

            for (int i = 0; i < result.size(); i++) {
                // Print service info
                System.out.println(result.get(i).toString());                
                
                System.out.println("---");
            }
        }
    }

}
