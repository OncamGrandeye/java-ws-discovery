A Java implementation of the WS-Discovery specification draft (http://schemas.xmlsoap.org/ws/2005/04/discovery/).

This is version 0.2 and the third release of this library. Please check http://code.google.com/p/java-ws-discovery/ for a list of changes and for future updates. 

This release is based on revision 55 from the svn repository.

** License **
This program is free software: you can redistribute it and/or modify
it under the terms of the Lesser GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
Lesser GNU General Public License for more details.

You should have received a copy of the Lesser GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

** About the modules **

wsdiscovery-lib            Contains the WS-Discovery core library. The other directories are examples.

wsdiscovery-gui            A simple GUI for publishing and discovering services. This GUI will be replaced by wsdiscovery-gui2 in the next release.

wsdiscovery-gui2		   A second version of the GUI with more functionality. 

wsdiscovery-examples       Some examples of how the WS-discovery implementation can be used to publish and find services.

wsdiscovery-example-ws     How to publish a Web Service description.

** Adding matching algorithms **
Matching algorithms are used in WS-Discovery when probing for new services. The sender of the 
probe specifies which matching algorithm should be used when the receiver is looking for matching
services. If the matching algorithm is unsupported the receiver will simply discard the probe. 

The WS-Discovery specification defaults to the matching algorithm specified in RFC2396, which 
matches the elements in the URI one element at a time (http://a.b.c/Service/ will match 
http://a.b.c/Service/MyService, but http://a.b.c/Serv will not). By creating custom matching algorithms the 
WS-Discovery protocol can be extended to support more advanced service discovery mechanisms.     

Matching algorithms can be added to the implementation by writing new classes that implement the IMatchType interface
and adding them to the com.ms.wsdiscovery.servicedirectory.MatchBy enumerator. 

** Adding transport protocols **
Currently only SOAP-over-UDP and a variant of SOAP-over-UDP using gzip-compression is implemented.  

Other transport protocols can be added by creating a class that implements the ITransportType interface. The new class
must be added to the com.ms.wsdiscovery.transport.TransportType enumerator. To activate the new transport protocol,
change WsDiscoveryConstants.transportType.

com.ms.wsdiscovery.network.transport.soapudp.zlib.SOAPOverUDPzlib is an example of how SOAP-over-UDP can be extended
to support compression. It should be relatively easy to add other compression-methods, like Efx. When compression is
enabled the implementation is not compatible with the WS-Discovery specification draft.

** Requirements **
As of version 0.2, WS-Discovery must be built using Maven. This release has been tested on Maven 2.0.9, but earlier versions
may work as well. The built-in Maven included in Netbeans 6.7 on Windows does not always work. We recommend that Netbeans-users configure their IDE to use an external Maven installation.

The Web Services and the examples interacting with them, need a Java 1.6 environment to compile properly. 

More verbose logging in the WS-Discovery library can be enabled by adjusting com.ms.wsdiscovery.WsDiscoveryConstants.loggerLevel, e.g. by setting it to FINEST.

** Known limitations **
- The matching algorithm for LDAP is not implemented. It can be added by completing the class
com.ms.wsdiscovery.servicedirectory.matcher.MatchScopeLDAP.
- WS-Security signatures are not supported.  

Magnus Skjegstad (magnus@skjegstad.com), Oct 26th 2009