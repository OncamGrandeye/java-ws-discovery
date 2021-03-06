/*
WsDiscoveryConstants.java

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

package com.ms.wsdiscovery;

import com.ms.wsdiscovery.exception.WsDiscoveryException;
import java.net.InetAddress;
import com.ms.wsdiscovery.xml.WsdXMLBuilder;
import com.ms.wsdiscovery.xml.soap.WsdSOAPMessageBuilder;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import com.ms.wsdiscovery.network.transport.TransportType;
import com.ms.wsdiscovery.servicedirectory.matcher.MatchBy;
import com.ms.wsdiscovery.xml.jaxb_generated.AttributedURI;
import com.ms.wsdiscovery.xml.jaxb_generated.HelloType;
import java.net.NetworkInterface;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;

/**
 * Class containing variables that can be used to change the behaviour of the 
 * WS-Discovery implementation.
 * 
 * @author Magnus Skjegstad
 */
public class WsDiscoveryConstants {
    /**
     * Select the transport protocol to use when sending SOAP-messages. See 
     * {@link TransportType} for valid transport types.
     */
    public final static TransportType transportType = TransportType.SOAP_OVER_UDP;

    /**
     * Builder with helpers used for constructing XML
     */
    public final static WsdXMLBuilder XMLBUILDER = new WsdXMLBuilder();
    
    /**
     * Builder with helpers used for creating SOAP-messages
     */
    public final static WsdSOAPMessageBuilder SOAPBUILDER = new WsdSOAPMessageBuilder();
    
    /**
     * WS-Discovery namespace.
     */
    public final static URI defaultNsDiscovery = 
            URI.create("http://schemas.xmlsoap.org/ws/2005/04/discovery");
    
    /**
     * WS-Addressing namespace.
     */
    public final static URI defaultNsAddressing = 
            URI.create("http://schemas.xmlsoap.org/ws/2004/08/addressing");
    
    /**
     * SOAP protocol. See {@link SOAPConstants} for valid values. Note that 
     * Windows Vista as of 2008-07-22 only appears to respond to SOAP 1.2.
     */
    public final static String defaultSoapProtocol = 
            SOAPConstants.SOAP_1_2_PROTOCOL; 
    
    /**
     * Default string encoding. The WS-Discovery specification requires UTF-8 
     * encoding.
     */
    public static Charset defaultEncoding = null; // Set later, in static{}.
    
    /**
     * Instance name used by JAXB for marshall/unmarshalling.
     */
    public final static String defaultJAXBInstanceName = 
            HelloType.class.getPackage().getName();
    
    /**
     * When set to true all SOAP-messages will start with a valid XML-header.
     */
    public final static Boolean defaultAddXMLHeaderToSOAP = false;

    /**
     * The default recipient used in WS-Addressing.
     */
    public final static AttributedURI defaultTo = 
            XMLBUILDER.createAttributedURI("urn:schemas-xmlsoap-org:ws:2005:04:discovery");

    /**
     * Default match method when receiving or sending Probe-messages. See the 
     * WS-Discovery specification and {@link MatchBy} for details. 
     * <code>RFC2396</code> is the default matcher used in the specification.
     */
    public final static MatchBy defaultMatchBy = MatchBy.RFC2396;
    
    /**
     * The relationship type of the suppression message that is sent to clients 
     * when a proxy server announces itself.
     */
    public final static QName defaultProxyRelatesToRelationship = 
            // TODO, schema defines this to be: new QName(defaultNsDiscovery.toString(), "Suppression");
            // .. however, JAXB doesn't seem to like that and leaves the RelationshipType-attribute empty (null).
            new QName("Suppression");
    
    /**
     * URI used by WS-Addressing when sending messages to anonymous recipients 
     * (wsa:To). In WS-Discovery all multicast messages should be sent to anonymous.
     */
    public final static AttributedURI anonymousTo = 
            XMLBUILDER.createAttributedURI(defaultNsAddressing.toString() + "/role/anonymous");
    
    /**
     * Port used for sending and listening for multicast messages. WS-Discovery 
     * defaults to 3702.
     */
    public static int multicastPort = 3702;

    /**
     * The address used for the proxy service if it is enabled.
     *
     * If it is null, the IP of multicastInterface will be used.
     * If multicastInterface is null as well, the IP will be guessed
     * based on the available network interfaces.
     * 
     */
    public static InetAddress proxyAddress = null; 
    
    /**
     * Address used for sending and listening for multicast messages. 
     * WS-Discovery defaults to 239.255.255.0.
     */
    public static InetAddress multicastAddress; // Set later, in static{}

    /**
     * Interface used for multicast. Set to null to use default based on
     * the address set in multicastAddress. The IP of this interface will also
     * be used as proxy service address if proxyAddress is null.
     */
    public static NetworkInterface multicastInterface = null;
        
    /**
     * Instance ID should be incremented each time the WS-Discovery service is 
     * restarted. See Appendix I - Application Sequencing in the WS-Discovery 
     * specifiction for details.
     */
    public final static long instanceId = (new Date().getTime()) & 0x7FFFFFFFl;
    
    /**
     * Sequence ID must be unique within each instance. See Appendix I - 
     * Application Sequencing in the WS-Discovery specifiction for details.
     */
    public final static UUID sequenceId = UUID.randomUUID(); 
    
    /**
     * Log level.
     */
    public static Level loggerLevel = Level.FINEST;

    /**
     * Handler used for logging debug messages. Default is ConsoleHandler().
     */
    public static Handler loggerHandler = new ConsoleHandler();
    
    /**
     * If proxy server mode is enabled this will be the port type used for the 
     * proxy service. NOTE: The WS-Discovery specification does not define how 
     * a proxy server should announce itself.
     */
    public final static QName proxyPortType = new QName("DiscoveryProxy");
    
    /**
     * Scope used for the proxy service (if enabled). 
     * NOTE: The WS-Discovery specification does not define how a proxy server 
     * should announce itself.
     */
    public final static String proxyScope = "";
                
    static {      
        try {
            multicastAddress = InetAddress.getByName("239.255.255.250");
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
            multicastAddress = null;
            System.exit(-1);
        }
        
        try {
            defaultEncoding = Charset.forName("UTF-8");        
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-2);
        }
    }
}
