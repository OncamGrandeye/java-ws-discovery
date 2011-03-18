/*
WsDiscoveryS11Utilities.java

Copyright (C) 2009 Magnus Skjegstad

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
package com.ms.wsdiscovery.standard11;

import com.ms.wsdiscovery.WsDiscoveryFactory;
import com.ms.wsdiscovery.datatypes.WsDiscoveryActionTypes;
import com.ms.wsdiscovery.datatypes.WsDiscoveryNamespaces;
import com.skjegstad.soapoverudp.datatypes.SOAPOverUDPEndpointReferenceType;
import com.skjegstad.soapoverudp.datatypes.SOAPOverUDPGenericAnyType;
import com.ms.wsdiscovery.datatypes.WsDiscoveryScopesType;
import com.ms.wsdiscovery.jaxb.standard11.wsaddressing.AttributedQNameType;
import com.ms.wsdiscovery.jaxb.standard11.wsaddressing.AttributedURIType;
import com.ms.wsdiscovery.jaxb.standard11.wsaddressing.EndpointReferenceType;
import com.ms.wsdiscovery.jaxb.standard11.wsaddressing.MetadataType;
import com.ms.wsdiscovery.jaxb.standard11.wsaddressing.ReferenceParametersType;
import com.ms.wsdiscovery.jaxb.standard11.wsaddressing.RelatesToType;
import com.ms.wsdiscovery.jaxb.standard11.wsdiscovery.ByeType;
import com.ms.wsdiscovery.jaxb.standard11.wsdiscovery.HelloType;
import com.ms.wsdiscovery.jaxb.standard11.wsdiscovery.ProbeMatchType;
import com.ms.wsdiscovery.jaxb.standard11.wsdiscovery.ProbeMatchesType;
import com.ms.wsdiscovery.jaxb.standard11.wsdiscovery.ProbeType;
import com.ms.wsdiscovery.jaxb.standard11.wsdiscovery.ResolveMatchType;
import com.ms.wsdiscovery.jaxb.standard11.wsdiscovery.ResolveMatchesType;
import com.ms.wsdiscovery.jaxb.standard11.wsdiscovery.ResolveType;
import com.ms.wsdiscovery.jaxb.standard11.wsdiscovery.ScopesType;
import com.ms.wsdiscovery.servicedirectory.WsDiscoveryService;
import com.ms.wsdiscovery.servicedirectory.WsDiscoveryServiceDirectory;
import com.ms.wsdiscovery.servicedirectory.exception.WsDiscoveryServiceDirectoryException;
import com.ms.wsdiscovery.servicedirectory.interfaces.IWsDiscoveryServiceDirectory;
import com.ms.wsdiscovery.servicedirectory.matcher.MatchBy;
import com.skjegstad.soapoverudp.datatypes.SOAPOverUDPServiceNameType;
import com.skjegstad.soapoverudp.exceptions.SOAPOverUDPException;
import java.net.URI;
import javax.xml.namespace.QName;

/**
 * Helper methods specific to WS-Discovery draft 2005. These methods depend 
 * on the objects generated by JAXB.
 *
 * @author Magnus Skjegstad
 */
public class WsDiscoveryS11Utilities {
    private static final com.ms.wsdiscovery.jaxb.standard11.wsdiscovery.ObjectFactory wsDiscoveryObjectFactory =
            new com.ms.wsdiscovery.jaxb.standard11.wsdiscovery.ObjectFactory();
    private static final com.ms.wsdiscovery.jaxb.standard11.wsaddressing.ObjectFactory wsAddressingObjectFactory =
            new com.ms.wsdiscovery.jaxb.standard11.wsaddressing.ObjectFactory();
    private static final MatchBy defaultMatcher = WsDiscoveryNamespaces.WS_DISCOVERY_2009_01.getDefaultMatcher();

    /**
     * Creates a WS-Discovery service description based on a JAXB object. The
     * object must an instance of one of:
     * <li>{@link HelloType}</li>
     * <li>{@link ByeType}</li>
     * <li>{@link ProbeMatchType}</li>
     * <li>{@link ResolveMatchesType}</li>
     * <li>{@link ResolveMatchType}</li>
     * When extracting service descriptions from a {@link ProbeMatchesType}, the
     * constructor of {@link WsDiscoveryServiceDirectory} must be used, since
     * {@link ProbeMatchesType} can contain multiple service descriptions.
     * @param jaxbbody JAXB object.
     * @throws WsDiscoveryServiceDirectoryException
     */
    public static WsDiscoveryService createWsDiscoveryService(Object jaxbbody) throws WsDiscoveryServiceDirectoryException {
        WsDiscoveryService service = new WsDiscoveryService();
        if (jaxbbody instanceof HelloType) {
            HelloType m = (HelloType)jaxbbody;
            service.setEndpointReference(createSOAPOverUDPEndpointReferenceType(m.getEndpointReference()));
            service.setPortTypes(m.getTypes());
            service.setScopesType(createWsDiscoveryScopesObject(m.getScopes()));
            service.setXAddrs(m.getXAddrs());
            service.setMetadataVersion(m.getMetadataVersion());
        } else
        if (jaxbbody instanceof ByeType) {
            ByeType m = (ByeType)jaxbbody;
            service.setEndpointReference(createSOAPOverUDPEndpointReferenceType(m.getEndpointReference()));
        } else
        if (jaxbbody instanceof ProbeMatchType) {
            ProbeMatchType m = (ProbeMatchType)jaxbbody;
            service.setEndpointReference(createSOAPOverUDPEndpointReferenceType(m.getEndpointReference()));
            service.setPortTypes(m.getTypes());
            if (m.getScopes() != null)
              service.setScopesType(createWsDiscoveryScopesObject(m.getScopes()));
            service.setXAddrs(m.getXAddrs());
            service.setMetadataVersion(m.getMetadataVersion());
        } else
        if (jaxbbody instanceof ResolveMatchesType) {
            ResolveMatchType m = ((ResolveMatchesType)jaxbbody).getResolveMatch();
            service.setEndpointReference(createSOAPOverUDPEndpointReferenceType(m.getEndpointReference()));
            service.setPortTypes(m.getTypes());
            service.setScopesType(createWsDiscoveryScopesObject(m.getScopes()));
            service.setXAddrs(m.getXAddrs());
            service.setMetadataVersion(m.getMetadataVersion());
        } else
        if (jaxbbody instanceof ResolveMatchType) {
            ResolveMatchType m = (ResolveMatchType)jaxbbody;
            service.setEndpointReference(createSOAPOverUDPEndpointReferenceType(m.getEndpointReference()));
            service.setPortTypes(m.getTypes());
            service.setScopesType(createWsDiscoveryScopesObject(m.getScopes()));
            service.setXAddrs(m.getXAddrs());
            service.setMetadataVersion(m.getMetadataVersion());
        } else
        if (jaxbbody instanceof ProbeMatchesType) {
            throw new WsDiscoveryServiceDirectoryException("Multiple service descriptions found.");
        } else
            throw new WsDiscoveryServiceDirectoryException("Unsupported object type.");
        return service;
    }

    /**
     * Creates a WS-Discovery service description based on a received Hello-packet.
     *
     * @param m Hello-packet.
     * @throws WsDiscoveryServiceDirectoryException
     */
    public static WsDiscoveryService createWsDiscoveryService(HelloType m) throws WsDiscoveryServiceDirectoryException {
        return createWsDiscoveryService((Object)m);
    }

    /**
     * Creates a WS-Discovery service description based on a received Bye-packet.
     *
     * @param m Bye-packet.
     * @throws WsDiscoveryServiceDirectoryException
     */
    public static WsDiscoveryService createWsDiscoveryService(ByeType m) throws WsDiscoveryServiceDirectoryException {
        return createWsDiscoveryService((Object)m);
    }

    /**
     * Creates a WS-Discovery service description based on a received ProbeMatch-packet.
     *
     * @param m ProbeMatch-packet.
     * @throws WsDiscoveryServiceDirectoryException
     */
    public static WsDiscoveryService createWsDiscoveryService(ProbeMatchType m) throws WsDiscoveryServiceDirectoryException {
        return createWsDiscoveryService((Object)m);
    }

    /**
     * Creates a WS-Discovery service description based on ResolveMatches in a ResolveMatch-packet.
     *
     * @param m ResolveMatches data.
     * @throws WsDiscoveryServiceDirectoryException
     */
    public static WsDiscoveryService createWsDiscoveryService(ResolveMatchesType m) throws WsDiscoveryServiceDirectoryException {
        return createWsDiscoveryService((Object)m);
    }

    /**
     * Creates a WS-Discovery service description based on a ResolveMatch-packet.
     * @param m ResolveMatch-packet.
     * @throws WsDiscoveryServiceDirectoryException
     */
    public static WsDiscoveryService createWsDiscoveryService(ResolveMatchType m) throws WsDiscoveryServiceDirectoryException {
        return createWsDiscoveryService((Object)m);
    }

    /**
     * Create a {@link ScopesType} object with values from a service description.
     *
     * @return The scopes of this service represented as a {@link ScopesType}.
     */
    public static ScopesType createScopesObject(WsDiscoveryService service) {
        ScopesType s = new ScopesType();
        synchronized (service) {
            if (service.getScopesMatchBy() != null) {
                s.setMatchBy(service.getScopesMatchBy().toString());
            }
            if (service.getScopes() != null) {
                for (URI u : service.getScopes()) {
                    s.getValue().add(u.toString());
                }
            }
        }
        return s;
    }

    /**
     * Create a {@link WsDiscoveryScopesType} object with values from a {@link ScopesType} object.
     * @param scopes JAXB object
     * @return The scopes from <code>scopes</code> represented as a {@link WsDiscoveryScopesType}.
     */
    public static WsDiscoveryScopesType createWsDiscoveryScopesObject(ScopesType scopes) {
        WsDiscoveryScopesType s = new WsDiscoveryScopesType(
                WsDiscoveryFactory.getMatcher(scopes.getMatchBy(), defaultMatcher),
                scopes.getValue(), scopes.getOtherAttributes());
        return s;
    }


    /**
     * Decide whether a Probe-packet matches a service.
     * See also {@link WsDiscoveryService#isMatchedBy(java.util.List, wsdiscovery.xml.jaxb_generated.ScopesType)}.
     *
     * @param service WS-Discovery service description
     * @param probe Probe-message.
     * @return True if <code>probe</code> matches.
     */
    public static boolean isServiceMatchedBy(WsDiscoveryService service, ProbeType probe) {
        synchronized (service) {
            return service.isMatchedBy(probe.getTypes(), createWsDiscoveryScopesObject(probe.getScopes()), defaultMatcher);
        }
    }

    /**
     * Create JAXB AttributedURI from Java URI.
     * @param uri
     * @return Attributed URI.
     */
    public static AttributedURIType createAttributedURIType(String uri) {
        AttributedURIType a = new AttributedURIType();
        a.setValue(uri);
        return a;
    }

    /**
     * Create JAXB AttributedQName from Java QName.
     * @param name
     * @return Attributed QName.
     */
    public static AttributedQNameType createAttributedQNameType(QName name) {
        AttributedQNameType q = new AttributedQNameType();
        q.setValue(name);
        return q;
    }

    /**
     * Create JAXB RelatesToType from value.
     * @param value
     * @return RelatesToType
     */
    public static RelatesToType createRelatesTo(String value) {
        RelatesToType r = new RelatesToType();
        r.setValue(value);
        return r;
    }

    /**
     * Create JAXB EndpointReference with specified address.
     * @param uri Endpiont address.
     * @return New EndpointReference with specified address.
     */
    public static EndpointReferenceType createEndpointReference(String uri) {
        EndpointReferenceType e = new EndpointReferenceType();
        e.setAddress(createAttributedURIType(uri));
        return e;
    }

    /**
     * Store or update all the entries in a {@link ProbeMatchesType} in the service directory. If a service with
     * the same endpoint reference already exists, the existing service will
     * be updated.
     * @param serviceDirectory Service directory.
     * @param probe Probe matches.
     * @throws WsDiscoveryServiceDirectoryException if store failes.
     */
    public static void storeProbesMatch(IWsDiscoveryServiceDirectory serviceDirectory, ProbeMatchesType probeMatches) throws WsDiscoveryServiceDirectoryException {
        // No locking necessary here
        if ((probeMatches != null) && (probeMatches.getProbeMatch() != null))
            for (ProbeMatchType p : probeMatches.getProbeMatch())
                serviceDirectory.store(createWsDiscoveryService(p));
    }

    /**
     * Store a JAXB object in the service directory. The JAXB object must be
     * recognized by {@link WsDiscoveryS11Utilities#createWsDiscoveryService(java.lang.Object)}.
     * If a service with
     * the same endpoint reference already exists, the existing service will
     * be updated/overwritten.
     *
     * @param serviceDirectory Service directory.
     * @param jaxbobject JAXB object.
     * @throws WsDiscoveryServiceDirectoryException on failure.
     */
    public static void storeJAXBObject(IWsDiscoveryServiceDirectory serviceDirectory, Object jaxbBody) throws WsDiscoveryServiceDirectoryException {
        // No locking necessary here
        serviceDirectory.store(createWsDiscoveryService(jaxbBody));
    }

    /**
     * Remove service based on endpoint address received in a Bye-message.
     * @param serviceDirectory Service directory.
     * @param bye Bye-message with endpoint address to remove.
     */
    public static void removeServiceBye(IWsDiscoveryServiceDirectory serviceDirectory, ByeType bye) {
        if ((bye.getEndpointReference() != null) &&
                (bye.getEndpointReference().getAddress() != null) &&
                (bye.getEndpointReference().getAddress().getValue() != null))
                    serviceDirectory.remove(bye.getEndpointReference().getAddress().getValue());
    }

 /**
     * Create a {@link SOAPOverUDPEndpointReferenceType} object with values from a {@link com.skjegstad.soapoverudp.jaxb.wsaddressing200408.EndpointReferenceType} object.
     * @param endpointReference JAXB object
     * @return The endpoint reference from <code>endpointReference</code> represented as a {@link SOAPOverUDPEndpointReferenceType}.
     */
    public static SOAPOverUDPEndpointReferenceType createSOAPOverUDPEndpointReferenceType(EndpointReferenceType endpointReference) {
        SOAPOverUDPEndpointReferenceType s = new SOAPOverUDPEndpointReferenceType();

        if (endpointReference.getAddress() != null)
            s.setAddress(URI.create(endpointReference.getAddress().getValue()));

        if (endpointReference.getMetadata() != null) {
            SOAPOverUDPGenericAnyType metadata = new SOAPOverUDPGenericAnyType(endpointReference.getMetadata().getAny());
            if (endpointReference.getMetadata().getOtherAttributes() != null)
                metadata.setOtherAttributes(endpointReference.getMetadata().getOtherAttributes());
            s.setMetadata(metadata);
        }

        if (endpointReference.getReferenceParameters() != null)
            s.setReferenceParameters(new SOAPOverUDPGenericAnyType(endpointReference.getReferenceParameters().getAny()));

        if (endpointReference.getOtherAttributes() != null)
            s.setOtherAttributes(endpointReference.getOtherAttributes());

        s.getAny().addAll(endpointReference.getAny());

        return s;
    }

    /**
     * Create a blank WS-Discovery Hello-message.
     * @return Hello-message.
     */
    public static WsDiscoveryS11SOAPMessage<HelloType> createWsdSOAPMessageHello() throws SOAPOverUDPException {
        return new WsDiscoveryS11SOAPMessage<HelloType>(WsDiscoveryActionTypes.HELLO,
                wsDiscoveryObjectFactory.createHello(wsDiscoveryObjectFactory.createHelloType()));
    }


    /**
     * Create a WS-Discovery Hello-message announcing <code>service</code>.
     * @param service Service to announce in Hello-message.
     * @return Hello-message.
     */
    public static WsDiscoveryS11SOAPMessage<HelloType> createWsdSOAPMessageHello(WsDiscoveryService service) throws SOAPOverUDPException {
        WsDiscoveryS11SOAPMessage<HelloType> m = createWsdSOAPMessageHello();
        HelloType h = m.getJAXBBody();

        h.setEndpointReference(createEndpointReferenceTypeObject(service.getEndpointReference()));
        h.setMetadataVersion(service.getMetadataVersion());

        h.setScopes(createScopesObject(service));

        if (service.getPortTypes() != null)
            h.getTypes().addAll(service.getPortTypes());

        if (service.getXAddrs() == null)
            throw new NullPointerException("XAddrs can't be null.");

        h.getXAddrs().addAll(service.getXAddrs());

        return m;
    }

    /**
     * Create a blank WS-Discovery Bye-message.
     * @return Bye-message.
     */
    public static WsDiscoveryS11SOAPMessage<ByeType> createWsdSOAPMessageBye() throws SOAPOverUDPException {
        return new WsDiscoveryS11SOAPMessage<ByeType>(WsDiscoveryActionTypes.BYE,
                wsDiscoveryObjectFactory.createBye(wsDiscoveryObjectFactory.createByeType()));
    }

    /**
     * Create a WS-Discovery Bye-message for <code>service</code>.
     * @param service Service that is about to leave.
     * @return Bye-message.
     */
    public static WsDiscoveryS11SOAPMessage<ByeType> createWsdSOAPMessageBye(WsDiscoveryService service) throws SOAPOverUDPException {
        WsDiscoveryS11SOAPMessage<ByeType> m = createWsdSOAPMessageBye();
        ByeType b = m.getJAXBBody();
        b.setEndpointReference(createEndpointReferenceTypeObject(service.getEndpointReference()));
        return m;
    }

    /**
     * Create blank WS-Discovery Probe-message.
     * @return Probe-message.
     */
    public static WsDiscoveryS11SOAPMessage<ProbeType> createWsdSOAPMessageProbe() throws SOAPOverUDPException {
        return new WsDiscoveryS11SOAPMessage<ProbeType>(WsDiscoveryActionTypes.PROBE,
                wsDiscoveryObjectFactory.createProbe(wsDiscoveryObjectFactory.createProbeType()));
    }

    /**
     * Create blank WS-Discovery ProbeMatches-message.
     * @return ProbeMatches-message.
     */
    public static WsDiscoveryS11SOAPMessage<ProbeMatchesType> createWsdSOAPMessageProbeMatches() throws SOAPOverUDPException {
        return new WsDiscoveryS11SOAPMessage<ProbeMatchesType>(WsDiscoveryActionTypes.PROBEMATCHES,
                wsDiscoveryObjectFactory.createProbeMatches(wsDiscoveryObjectFactory.createProbeMatchesType()));
    }

    /**
     * Create blank WS-Discovery Resolve-message.
     * @return Resolve-message.
     */
    public static WsDiscoveryS11SOAPMessage<ResolveType> createWsdSOAPMessageResolve() throws SOAPOverUDPException {
        return new WsDiscoveryS11SOAPMessage<ResolveType>(WsDiscoveryActionTypes.RESOLVE,
                wsDiscoveryObjectFactory.createResolve(wsDiscoveryObjectFactory.createResolveType()));
    }

    /**
     * Create blank WS-Discovery ResolveMatches-message.
     * @return ResolveMatchse-message.
     */
    public static WsDiscoveryS11SOAPMessage<ResolveMatchesType> createWsdSOAPMessageResolveMatches() throws SOAPOverUDPException {
        return new WsDiscoveryS11SOAPMessage<ResolveMatchesType>(WsDiscoveryActionTypes.RESOLVEMATCHES,
                wsDiscoveryObjectFactory.createResolveMatches(wsDiscoveryObjectFactory.createResolveMatchesType()));
    }

    public static EndpointReferenceType createEndpointReferenceTypeObject(SOAPOverUDPEndpointReferenceType ep) {
                EndpointReferenceType wsaEndpoint = wsAddressingObjectFactory.createEndpointReferenceType();

                // Address
                if (ep.getAddress() != null) {
                    AttributedURIType address = wsAddressingObjectFactory.createAttributedURIType();
                    address.setValue(ep.getAddress().toString());
                    wsaEndpoint.setAddress(address);
                }

                // Meta data
                if (ep.getMetadata() != null) {
                    SOAPOverUDPGenericAnyType metadata = ep.getMetadata();
                    MetadataType mt = wsAddressingObjectFactory.createMetadataType();
                    mt.getAny().addAll(metadata.getAny());
                    wsaEndpoint.setMetadata(mt);
                }

                // Reference parameters
                if (ep.getReferenceParameters() != null) {
                    ReferenceParametersType r = wsAddressingObjectFactory.createReferenceParametersType();
                    r.getAny().addAll(ep.getReferenceParameters().getAny());
                    wsaEndpoint.setReferenceParameters(r);
                }

                // Anything else
                wsaEndpoint.getAny().addAll(ep.getAny());

                return wsaEndpoint;
    }

    public static MatchBy getDefaultMatchBy() {
        return defaultMatcher;
    }
}
